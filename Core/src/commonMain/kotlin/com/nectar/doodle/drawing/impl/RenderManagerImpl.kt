package com.nectar.doodle.drawing.impl


import com.nectar.doodle.core.Display
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.AffineTransform
import com.nectar.doodle.drawing.GraphicsDevice
import com.nectar.doodle.drawing.GraphicsSurface
import com.nectar.doodle.drawing.RenderManager
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Rectangle.Companion.Empty
import com.nectar.doodle.scheduler.AnimationScheduler
import com.nectar.doodle.scheduler.Task
import com.nectar.doodle.theme.InternalThemeManager
import com.nectar.doodle.time.Timer
import com.nectar.doodle.utils.MutableTreeSet
import com.nectar.doodle.utils.ifTrue
import com.nectar.measured.units.Measure
import com.nectar.measured.units.Time
import com.nectar.measured.units.milliseconds
import com.nectar.measured.units.times

private object AncestorComparator: Comparator<View> {
    override fun compare(a: View, b: View) = when {
        a == b          ->  0
        b ancestorOf_ a ->  1
        else            -> -1
    }
}

private val frameDuration = 1000 * milliseconds / 60

@Suppress("PrivatePropertyName", "NestedLambdaShadowedImplicitParameter")
class RenderManagerImpl(
        private val timer         : Timer,
        private val display       : Display,
        private val scheduler     : AnimationScheduler,
        private val themeManager  : InternalThemeManager?,
        private val graphicsDevice: GraphicsDevice<*>): RenderManager {

    private val views                       = mutableSetOf <View>()
    private var layingOut                   = null as View?
    private val dirtyViews                  = mutableSetOf <View>()
    private val displayTree                 = mutableMapOf <View?, DisplayRectNode>()
    private val neverRendered               = mutableSetOf <View>()
    private val pendingLayout               = MutableTreeSet(AncestorComparator)
    private val pendingRender               = LinkedHashSet<View>()
    private val pendingCleanup              = mutableMapOf <View, MutableSet<View>>()
    private val addedInvisible              = mutableSetOf <View>()
    private val visibilityChanged           = mutableSetOf <View>()
    private val pendingBoundsChange         = mutableSetOf <View>()
    private var paintTask                   = null as Task?
    private val boundsChanged_              = ::boundsChanged
    private val zOrderChanged_              = ::zOrderChanged
    private val childrenChanged_            = ::childrenChanged   // This is b/c Kotlin doesn't translate inline functions in a way that allows them to be used in maps
    private val transformChanged_           = ::transformChanged
    private val visibilityChanged_          = ::visibilityChangedFunc
    private val displayRectHandlingChanged_ = ::displayRectHandlingChanged

    init {
        display.children.changed += { _,removed,added,moved ->

            removed.values.forEach { childRemoved(null, it) }
            added.values.forEach   { childAdded  (null, it) }

            moved.forEach {
                val surface = graphicsDevice[it.value.second]

                surface.index = it.key
            }

            if (removed.isNotEmpty() || added.isNotEmpty()) {
                display.doLayout()
            }
        }

        display.children.forEach {
            childAdded(null, it)
        }

        display.sizeChanged += { display, _, _ ->
            display.doLayout()

            display.forEach { checkDisplayRectChange(it) }
        }

        display.forEach { record(it) }
    }

    override fun render(view: View) {
        render(view, false)
    }

    override fun renderNow(view: View) {
        if (view in views && !view.bounds.empty && display ancestorOf view) {
            dirtyViews += view

            if (view in pendingLayout) {
                performLayout(view)
            }

            val parent = view.parent

            if (parent != null && (parent in neverRendered || parent in dirtyViews)) {
                renderNow(parent)
            } else {
                performRender(view).ifTrue {
                    pendingRender -= view
                }
            }
        }
    }

    override fun displayRect(of: View): Rectangle {
        displayTree[of]?.let { return it.clipRect }

        var child = of
        var parent: View? = of.parent ?: return Empty

        var clipRect = if (of.visible) Rectangle(size = of.size) else Empty

        while (parent != null && !clipRect.empty) {
            clipRect = clipRect intersect Rectangle(-child.x,
                                                    -child.y,
                                                    if (parent.visible) parent.width  else 0.0,
                                                    if (parent.visible) parent.height else 0.0)

            child  = parent
            parent = parent.parent
        }

        return clipRect
    }

    private fun record(view: View) {
        if (view !in views) {
            view.parent?.let {
                if (it !in views) {
                    record(it)
                    return
                }
            }

            if (display ancestorOf view) {
                view.addedToDisplay(this)

                dirtyViews          += view
                neverRendered       += view
                pendingRender       += view
                pendingBoundsChange += view

                if (!recursivelyVisible(view)) { //.visible) {
                    addedInvisible += view
                }

                view.boundsChanged              += boundsChanged_
                view.zOrderChanged              += zOrderChanged_
                view.transformChanged           += transformChanged_
                view.visibilityChanged          += visibilityChanged_
                view.childrenChanged_           += childrenChanged_
                view.displayRectHandlingChanged += displayRectHandlingChanged_
            }

            views += view

            themeManager?.update(view)

            view.children_.forEach { childAdded(view, it) }

            scheduleLayout(view)

            if (view.monitorsDisplayRect) {
                registerDisplayRectMonitoring(view)

                notifyDisplayRectChange(view, Empty, displayRect(view))
            }

            if (displayTree.containsKey(view)) {
                // TODO: IMPLEMENT
            }

            if (view in display) {
                render(view, true)
            }
        }
    }

    private fun schedulePaint() {
        if (paintTask == null || paintTask?.completed == true) {
            paintTask = scheduler.onNextFrame { onPaint() }
        }
    }

    private fun render(view: View, ignoreEmptyBounds: Boolean) {
        if (prepareRender(view, ignoreEmptyBounds)) {
            schedulePaint()
        }
    }

    private fun prepareRender(view: View, ignoreEmptyBounds: Boolean) = ((ignoreEmptyBounds || !view.bounds.empty) && view in views && display ancestorOf view).ifTrue {
        dirtyViews    += view
        pendingRender += view
    }

    // TODO: Can this be used w/o creating copies?
//    private fun onPaint() {
//        strand (
//            pendingLayout.map {{ performLayout(it) }} +
//            pendingRender.map {{ performRender(it) }} +
//            pendingBoundsChange.filterNot { it in neverRendered }.map {{ updateGraphicsSurface(it, graphicsDevice[it]) }}
//        )
//    }

    private fun checkFrameTime(start: Measure<Time>) = (timer.now - start).let {
        (it >= frameDuration).ifTrue { schedulePaint() }
    }

    private fun onPaint() {
        val start = timer.now

        do {
            pendingLayout.firstOrNull()?.let {
                performLayout(it)

                if (checkFrameTime(start)) { return }
            }
        } while (!pendingLayout.isEmpty())

        pendingRender.iterator().let {
            while(it.hasNext()) {
                if (performRender(it.next())) {
                    it.remove()
                }

                if (checkFrameTime(start)) { return }
            }
        }

        pendingBoundsChange.forEach {
            if (it !in neverRendered) {
                updateGraphicsSurface(it, graphicsDevice[it])

                if (checkFrameTime(start)) { return }
            }
        }

        paintTask = null
    }

    private fun scheduleLayout(view: View) {
        // Only take reference identity into account
        if (layingOut !== view) {
            pendingLayout += view
        }
    }

    private fun performLayout(view: View) {
        layingOut = view

        view.doLayout_()

        layingOut = null

        pendingLayout -= view
    }

    private fun recursivelyVisible(view: View): Boolean {
        var current = view as View?

        while (current != null) {
            if (!current.visible) {
                return false
            }

            current = current.parent
        }

        return true
    }

    private fun performRender(view: View): Boolean {
        var rendered          = false
        val visibilityChanged = view in visibilityChanged

        val recursivelyVisible = recursivelyVisible(view)

        val graphicsSurface = if ((recursivelyVisible || visibilityChanged) && display ancestorOf view) graphicsDevice[view] else null

        graphicsSurface?.let {
            if (view in pendingBoundsChange) {
                updateGraphicsSurface(view, graphicsSurface)

                pendingBoundsChange -= view
            }

            if (visibilityChanged) {
                graphicsSurface.visible = view.visible

                this.visibilityChanged -= view
            }

            if (recursivelyVisible && !view.bounds.empty) {
                val viewList = pendingCleanup[view]

                viewList?.forEach {
                    releaseResources(it)

                    graphicsDevice.release(it)
                }

                pendingCleanup -= view

                if (view in dirtyViews) {
                    dirtyViews    -= view
                    neverRendered -= view

                    graphicsSurface.render { canvas ->
                        view.render(canvas)
                    }

                    rendered = true
                }
            }
        }

        return rendered
    }

    private fun updateGraphicsSurface(view: View, surface: GraphicsSurface) {
        surface.bounds = view.bounds

        if (view in displayTree) {
            checkDisplayRectChange(view)
        }
    }

    private fun releaseResources(view: View) {
        view.removedFromDisplay_()

        view.children_.forEach {
            releaseResources(it)
        }

        views               -= view
        dirtyViews          -= view
        pendingLayout       -= view
        pendingBoundsChange -= view

        view.boundsChanged              -= boundsChanged_
        view.zOrderChanged              -= zOrderChanged_
        view.transformChanged           -= transformChanged_
        view.visibilityChanged          -= visibilityChanged_
        view.childrenChanged_           -= childrenChanged_
        view.displayRectHandlingChanged -= displayRectHandlingChanged_

        unregisterDisplayRectMonitoring(view)
    }

    private fun addToCleanupList(parent: View, child: View) {
        pendingCleanup.getOrPut(parent) { mutableSetOf() }.apply { add(child) }
    }

    private fun removeFromCleanupList(parent: View?, child: View) {
        if (child !in views) {
            return
        }

        pendingCleanup.forEach {
            val views = it.value

            if (views.remove(child)) {
                val oldParent = it.key

                if (oldParent != parent) {
                    // The child is being moved to a different parent, so we force clean-up
                    releaseResources(child)
                    graphicsDevice.release(child)
                }

                if (views.isEmpty()) {
                    pendingCleanup.remove(parent)
                }

                return
            }
        }
    }

    private fun childrenChanged(parent: View, removed: Map<Int, View>, added: Map<Int, View>, moved: Map<Int, Pair<Int, View>>) {
        removed.values.forEach { childRemoved(parent, it) }
        added.values.forEach   { childAdded  (parent, it) }

        if (parent !in pendingRender) {
            moved.forEach {
                val surface = graphicsDevice[it.value.second]

                surface.index = it.key
            }
        }

//        if (removed.isEmpty() && added.isEmpty()) {
//            return
//        }

        if (parent.visible && !parent.size.empty) {
            parent.revalidate_()
        } else {
            pendingCleanup[parent]?.forEach {
                releaseResources(it)

                graphicsDevice.release(it)
            }

            parent.doLayout_()
        }
    }

    private fun childAdded(parent: View?, child: View) {
        removeFromCleanupList(parent, child)

        if (child.visible) {
            record(child)
        } else {
            addedInvisible          += child
            child.visibilityChanged += visibilityChanged_
        }
    }

    private fun childRemoved(parent: View?, child: View) {
        if (parent != null) {
            addToCleanupList(parent, child)
        } else {
            releaseResources(child)

            graphicsDevice.release(child)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun visibilityChangedFunc(view: View, old: Boolean, new: Boolean) {
        val parent = view.parent

        if (view in addedInvisible) {
            record(view)

            addedInvisible -= view
        }

        if (!(parent == null || view in display)) {
            scheduleLayout(parent)

            // Views that change bounds while invisible are never scheduled
            // for bounds sync, so catch them here
            if (new) {
                pendingBoundsChange += view
            }

            visibilityChanged += view
            pendingRender     += view

            render(parent)
        } else if (view in display) {
            if (new) {
                visibilityChanged   += view
                pendingBoundsChange += view // See above

                render(view)
            } else {
                graphicsDevice[view].visible = false
            }
        }

        if (view in displayTree) {
            checkDisplayRectChange(view)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun zOrderChanged(view: View, old: Int, new: Int) {
        graphicsDevice[view].zOrder = new
    }

    @Suppress("UNUSED_PARAMETER")
    private fun displayRectHandlingChanged(view: View, old: Boolean, new: Boolean) {
        when (new) {
            true -> registerDisplayRectMonitoring  (view)
            else -> unregisterDisplayRectMonitoring(view)
        }
    }

    private fun boundsChanged(view: View, old: Rectangle, new: Rectangle) {
        val parent = view.parent

        // Early exit if this event was triggered by an item as it is being removed from the container tree.
        //
        // Same for invisible items.
        if ((parent == null && view !in display) || !view.visible) {
            return
        }

        var reRender = false

        pendingBoundsChange += view

        if (old.size != new.size) {
            reRender = true
            scheduleLayout(view)
        }

        when (parent) {
            null -> display.doLayout()
//            parent.layout_ == null && old.size == new.size -> updateGraphicsSurface(view, graphicsDevice[view]) // There are cases when an item's position might be constrained by logic outside a layout
            else -> scheduleLayout(parent)
        }

        if (reRender) {
            render(view, true)
        } else {
            schedulePaint()
        }

//        if (displayTree.containsKey(view)) {
//            checkDisplayRectChange(view)
//        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun transformChanged(view: View, old: AffineTransform, new: AffineTransform) {
        graphicsDevice[view].transform = new
    }

    private fun registerDisplayRectMonitoring(view: View) {
        if (!displayTree.containsKey(view)) {
            val node = DisplayRectNode(view)

            node.clipRect = Rectangle(size = view.size)

            displayTree[view] = node

            view.parent?.let {
                registerDisplayRectMonitoring(it)

                displayTree[it]?.let {
                    updateClipRect(node, it)

                    it += node
                }
            }
        }
    }

    private fun unregisterDisplayRectMonitoring(view: View) {
        displayTree[view]?.let {
            if (it.numChildren == 0) {
                displayTree -= view

                it.parent?.minusAssign(it)

                view.parent?.let { unregisterDisplayRectMonitoring(it) }
            }
        }
    }

    private fun checkDisplayRectChange(view: View) {
        displayTree[view]?.let { node ->
            val oldDisplayRect = node.clipRect

            updateClipRect(node, displayTree[view.parent])

            if (oldDisplayRect != node.clipRect) {
                if (view.monitorsDisplayRect) {
                    notifyDisplayRectChange(view, oldDisplayRect, node.clipRect)
                }

                for (i in 0 until node.numChildren) {
                    checkDisplayRectChange(node[i].view)
                }
            }
        }
    }

    private fun notifyDisplayRectChange(view: View, old: Rectangle, new: Rectangle) {
        if (old != new) {
            view.handleDisplayRectEvent_(old, new)
        }
    }

    private fun updateClipRect(node: DisplayRectNode, parent: DisplayRectNode?) {
        val view = node.view

        val viewRect = if (view.visible) Rectangle(size = view.size) else Empty

        val parentBounds = when (parent) {
            null -> Rectangle(-view.x, -view.y, display.size.width, display.size.height)
            else -> parent.clipRect.let { Rectangle(it.x - view.x, it.y - view.y, it.width, it.height) }
        }

        // TODO: Change to convex-polygon?
        node.clipRect = parentBounds.let { viewRect intersect it }
    }

    private class DisplayRectNode(val view: View) {
        var parent            = null as DisplayRectNode?
        var clipRect          = Empty
        val numChildren get() = children.size

        private val children = mutableListOf<DisplayRectNode>()

        operator fun plusAssign(child: DisplayRectNode) {
            child.parent = this
            children += child
        }

        operator fun minusAssign(child: DisplayRectNode) {
            child.parent = null
            children -= child
        }

        operator fun get(index: Int) = children[index]
    }
}
