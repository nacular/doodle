package io.nacular.doodle.drawing.impl

import io.nacular.doodle.accessibility.AccessibilityManager
import io.nacular.doodle.core.Internal
import io.nacular.doodle.core.InternalDisplay
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.AffineTransform
import io.nacular.doodle.drawing.GraphicsDevice
import io.nacular.doodle.drawing.GraphicsSurface
import io.nacular.doodle.drawing.RenderManager
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Rectangle.Companion.Empty
import io.nacular.doodle.scheduler.AnimationScheduler
import io.nacular.doodle.scheduler.Task
import io.nacular.doodle.theme.InternalThemeManager
import io.nacular.doodle.utils.MutableTreeSet
import io.nacular.doodle.utils.ifTrue

private object AncestorComparator: Comparator<View> {
    override fun compare(a: View, b: View) = when {
        a == b          ->  0
        b ancestorOf_ a ->  1
        else            -> -1
    }
}

@Internal
@Suppress("PrivatePropertyName", "NestedLambdaShadowedImplicitParameter")
public class RenderManagerImpl(
        private val display             : InternalDisplay,
        private val scheduler           : AnimationScheduler,
        private val themeManager        : InternalThemeManager?,
        private val accessibilityManager: AccessibilityManager?,
        private val graphicsDevice      : GraphicsDevice<*>): RenderManager {

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
        display.childrenChanged += { _,removed,added,moved ->
            removed.values.forEach { childRemoved(null, it) }
            added.values.forEach   { childAdded  (null, it) }

            moved.forEach {
                val surface = graphicsDevice[it.value.second]

                surface.index = it.key
            }

            if (removed.isNotEmpty() || added.isNotEmpty()) {
                display.relayout()
            }
        }

        display.forEach {
            childAdded(null, it)
        }

        display.sizeChanged += { _,_,_ ->
            display.relayout()

            display.forEach { checkDisplayRectChange(it) }
        }

        display.contentDirectionChanged += {
            display.forEach { checkContentDirectionChange(it) }
        }

        display.mirroringChanged += {
            display.forEach { it.updateNeedsMirror() }
        }
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
                performRender(view).rendered.ifTrue {
                    pendingRender -= view
                }
            }
        }
    }

    override fun layout(view: View) {
        scheduleLayout(view)
    }

    override fun layoutNow(view: View) {
        if (layingOut !== view && view.children_.isNotEmpty() && view in views && !view.bounds.empty && display ancestorOf view) {
            pendingLayout += view
            performLayout(view)
        }
    }

    override fun displayRect(of: View): Rectangle {
        displayTree[of]?.let { return it.clipRect }

        var child = of
        var parent: View? = of.parent ?: return Empty

        var clipRect = if (of.visible) Rectangle(size = of.size) else Empty

        while (parent != null) {
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
                view.addedToDisplay(display, this, accessibilityManager)

                dirtyViews          += view
                neverRendered       += view
                pendingRender       += view
                pendingBoundsChange += view

                if (!recursivelyVisible(view)) {
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

            if (view.children_.isNotEmpty()) {
                pendingLayout += view
            }

            if (view.monitorsDisplayRect) {
                registerDisplayRectMonitoring(view)

                notifyDisplayRectChange(view, Empty, displayRect(view))
            }

            if (displayTree.containsKey(view)) {
                // TODO: IMPLEMENT
            }

            checkContentDirectionChange(view)

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

    private fun onPaint() {
        val newRenders = mutableListOf<View>()

        // This loop is here because the process of laying out, rendering etc can generate new objects that need to be
        // handled. Therefore, the loop runs until these items are exhausted.
        // TODO: Should this loop be limited to avoid infinite spinning?  That way we could defer things that happen beyond a point to run on the next animation frame
        while (true) {
            do {
                pendingLayout.firstOrNull()?.let {
                    performLayout(it)
                }
            } while (pendingLayout.isNotEmpty())

            pendingRender.iterator().let {
                while (it.hasNext()) {
                    val item = it.next()
                    val renderResult = performRender(item)

                    if (renderResult.rendered || item !in views) {
                        it.remove()
                    } else if (renderResult.renderable && !item.bounds.empty && item in dirtyViews) {
                        newRenders += item
                    }
                }
            }

            pendingBoundsChange.iterator().let {
                while (it.hasNext()) {
                    val item = it.next()

                    if (item !in neverRendered ) {
                        updateGraphicsSurface(item, graphicsDevice[item])

                        it.remove()
                    }
                }
            }

            if (pendingLayout.isEmpty() && newRenders.none { it !in neverRendered } && pendingBoundsChange.isEmpty()) {
                break
            }

            newRenders.clear()
        }

        paintTask = null
    }

    private fun scheduleLayout(view: View) {
        // Only take reference identity into account
        if (layingOut !== view) {
            pendingLayout += view
            schedulePaint()
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

    private class RenderResult(val rendered: Boolean, val renderable: Boolean)

    private fun performRender(view: View): RenderResult {
        var rendered           = false
        val visibilityChanged  = view in visibilityChanged
        val recursivelyVisible = recursivelyVisible(view)

        val renderable      = (recursivelyVisible || visibilityChanged) && display ancestorOf view
        val graphicsSurface = if (renderable) graphicsDevice[view] else null

        graphicsSurface?.let {
            if (view in neverRendered) {
                graphicsSurface.transform = view.transform
            }

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
                    releaseResources(null, it)

                    graphicsDevice.release(it)
                }

                pendingCleanup -= view

                if (view in dirtyViews) {
                    dirtyViews    -= view
                    neverRendered -= view

                    graphicsSurface.apply {
                        clipCanvasToBounds = view.clipCanvasToBounds_
                        childdrenClipPoly  = view.childrenClipPoly_
                        mirrored           = view.needsMirrorTransform

                        render { canvas ->
                            view.render(canvas)
                        }
                    }

                    rendered = true
                }
            }
        }

        return RenderResult(rendered = rendered, renderable = renderable)
    }

    private fun updateGraphicsSurface(view: View, surface: GraphicsSurface) {
        surface.bounds = view.bounds

        if (view in displayTree) {
            checkDisplayRectChange(view)
        }
    }

    private fun releaseResources(parent: View?, view: View) {
        view.removedFromDisplay_()

        view.children_.forEach {
            releaseResources(it.parent, it)
        }

        views               -= view
        dirtyViews          -= view
        pendingLayout       -= view
        pendingBoundsChange -= view

        pendingCleanup[parent]?.remove(view)

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
                    releaseResources(it.key, child)
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

        if (parent.visible && !parent.size.empty) {
            parent.revalidate_()
        } else {
            pendingCleanup[parent]?.forEach {
                releaseResources(parent, it)

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
            releaseResources(parent, child)

            graphicsDevice.release(child)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun visibilityChangedFunc(view: View, old: Boolean, new: Boolean) {
        val parent            = view.parent
        val wasAddedInvisible = view in addedInvisible

        if (wasAddedInvisible) {
            record(view)

            addedInvisible -= view
        }

        if (!(parent == null || view in display)) {
            pendingLayout += parent

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

                if (!wasAddedInvisible) {
                    render(view)
                }
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
            if (view.children_.isNotEmpty() && view.layout_ != null) {
                pendingLayout += view
            }
        }

        when (parent) {
            null -> display.relayout()
//            parent.layout_ == null && old.size == new.size -> updateGraphicsSurface(view, graphicsDevice[view]) // There are cases when an item's position might be constrained by logic outside a layout
            else -> if (parent.layout_ != null) pendingLayout += parent
        }

        if (reRender) {
            render(view, true)
        } else {
            schedulePaint()
        }
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

    private fun checkContentDirectionChange(view: View) {
        if (view.localContentDirection == null) {
            view.contentDirectionChanged_()
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
