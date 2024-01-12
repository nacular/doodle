package io.nacular.doodle.drawing.impl

import io.nacular.doodle.accessibility.AccessibilityManager
import io.nacular.doodle.core.Camera
import io.nacular.doodle.core.Internal
import io.nacular.doodle.core.InternalDisplay
import io.nacular.doodle.core.PositionableContainer
import io.nacular.doodle.core.View
import io.nacular.doodle.core.View.SizePreferences
import io.nacular.doodle.core.height
import io.nacular.doodle.core.width
import io.nacular.doodle.drawing.AffineTransform
import io.nacular.doodle.drawing.GraphicsDevice
import io.nacular.doodle.drawing.GraphicsSurface
import io.nacular.doodle.drawing.RenderManager
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Rectangle.Companion.Empty
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.scheduler.AnimationScheduler
import io.nacular.doodle.scheduler.Task
import io.nacular.doodle.theme.InternalThemeManager
import io.nacular.doodle.utils.MutableTreeSet
import io.nacular.doodle.utils.diff.Delete
import io.nacular.doodle.utils.diff.Differences
import io.nacular.doodle.utils.diff.Insert
import io.nacular.doodle.utils.fastMutableMapOf
import io.nacular.doodle.utils.fastMutableSetOf
import io.nacular.doodle.utils.firstOrNull
import io.nacular.doodle.utils.ifTrue
import kotlin.math.abs

/** @suppress */
@Internal
@Suppress("NestedLambdaShadowedImplicitParameter")
public open class RenderManagerImpl(
        private val display             : InternalDisplay,
        private val scheduler           : AnimationScheduler,
        private val themeManager        : InternalThemeManager?,
        private val accessibilityManager: AccessibilityManager?,
        private val graphicsDevice      : GraphicsDevice<*>): RenderManager() {

    private var painting  = false
    private var layingOut = null as View?
    private var paintTask = null as Task?

    protected object AncestorComparator: Comparator<View> {
        override fun compare(a: View, b: View): Int = when {
            a == b          ->  0
            b ancestorOf_ a ->  1
            else            -> -1
        }
    }

    private val generationComparator: (View, View) -> Int = { a: View, b: View ->
        when (val genDifference = a.generationNumber - b.generationNumber) {
            0    -> {
                when (a) {
                    b    -> genDifference
                    else -> -1
                }
            }
            else -> genDifference
        }
    }

    protected open val views              : MutableSet<View>                   = fastMutableSetOf()
    protected open val dirtyViews         : MutableSet<View>                   = fastMutableSetOf()
    protected open val displayTree        : MutableMap<View?, DisplayRectNode> = fastMutableMapOf()
    protected open val neverRendered      : MutableSet<View>                   = fastMutableSetOf()
    protected open val pendingLayout      : MutableSet<View>                   = MutableTreeSet(generationComparator)
    protected open val pendingRender      : MutableSet<View>                   = LinkedHashSet()
    protected open val pendingCleanup     : MutableMap<View, MutableSet<View>> = fastMutableMapOf()
    protected open val addedInvisible     : MutableSet<View>                   = fastMutableSetOf()
    protected open val visibilityChanged  : MutableSet<View>                   = fastMutableSetOf()
    protected open val pendingBoundsChange: MutableSet<View>                   = fastMutableSetOf()
    protected open val popups             : MutableSet<View>                   = fastMutableSetOf()
    protected open val livingViews        : MutableSet<View>                   = fastMutableSetOf()

    init {
        display.childrenChanged += { _, diffs ->
            var needsLayout = false

            diffs.computeMoves().forEach {
                when (it) {
                    is Insert -> {
                        it.items.forEach { item ->
                            if (it.origin(of = item) == null) {
                                childAdded(null, item)
                                needsLayout = true
                            }
                        }
                    }
                    is Delete -> {
                        it.items.forEach { item ->
                            when (val destination = it.destination(of = item)) {
                                null -> {
                                    childRemoved(null, item)
                                    needsLayout = true
                                }
                                else -> {
                                   graphicsDevice[item].index = destination
                               }
                            }
                        }
                    }
                    else -> {}
                }
            }

            if (needsLayout) {
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
        if (view in views && !view.bounds.empty && view.displayed) {
            dirtyViews += view

            if (view in pendingLayout) {
                performLayout(view)
            }

            val parent = view.parent

            if (parent != null && (parent in neverRendered || parent in dirtyViews)) {
                renderNow(parent)
            }

            if (performRender(view).rendered) {
                pendingRender -= view
            }
        }
    }

    override fun layout(view: View) {
        scheduleLayout(view)
    }

    override fun layoutNow(view: View) {
        if (layingOut !== view && view.children_.isNotEmpty() && view in views && !view.bounds.empty && view.displayed) {
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

    private val View.highestAncestor: View get() {
        var result = this

        while (result.parent != null) {
            result = result.parent!!
        }

        return result
    }

    private fun record(view: View) {
        if (view !in views) {
            view.parent?.let {
                if (it !in views) {
                    record(it)
                    return
                }
            }

            views       += view
            livingViews += view

            val highestAncestor = view.highestAncestor

            if (highestAncestor in display || highestAncestor in popups) {
                view.display?.let { it -= view } // Remove from other display if any

                view.addedToDisplay(display, this, accessibilityManager)

                dirtyViews          += view
                neverRendered       += view
                pendingRender       += view
                pendingBoundsChange += view

                if (!recursivelyVisible(view)) {
                    addedInvisible += view
                }
            }

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

            if (view in display || view in popups) {
                render(view, true)
            }
        }
    }

    private fun schedulePaint() {
        if (!painting && (paintTask == null || paintTask?.completed == true)) {
            paintTask = scheduler.onNextFrame { onPaint() }
        }
    }

    private fun render(view: View, ignoreEmptyBounds: Boolean) {
        if (prepareRender(view, ignoreEmptyBounds)) {
            schedulePaint()
        }
    }

    private fun prepareRender(view: View, ignoreEmptyBounds: Boolean) = ((ignoreEmptyBounds || !view.bounds.empty) && view in views && view.displayed).ifTrue {
        dirtyViews    += view
        pendingRender += view
    }

    private fun onPaint() {
        painting = true

        val newRenders = mutableListOf<View>()

        // This loop is here because the process of laying out, rendering etc can generate new objects that need to be
        // handled. Therefore, the loop runs until these items are exhausted.
        // TODO: Should this loop be limited to avoid infinite spinning? That way we could defer things that happen beyond a point to run on the next animation frame
        while (true) {
            do {
                pendingLayout.firstOrNull()?.let {
                    performLayout(it)
                }
            } while (pendingLayout.isNotEmpty())

            do {
                pendingRender.firstOrNull()?.let { item ->
                    val renderResult = performRender(item)

                    pendingRender.remove(item)

                    if (!renderResult.rendered && renderResult.renderable && !item.bounds.empty && item in dirtyViews) {
                        newRenders += item
                    }
                }
            } while (pendingRender.isNotEmpty())

            pendingRender.addAll(newRenders)

            var numIterations = 0
            val maxIterations = pendingBoundsChange.size

            do {
                pendingBoundsChange.firstOrNull()?.let { item ->
                    if (item !in neverRendered ) {
                        updateGraphicsSurface(item, graphicsDevice[item])

                        pendingBoundsChange.remove(item)
                    }
                }
            } while (pendingBoundsChange.isNotEmpty() && ++numIterations < maxIterations)

            if (pendingLayout.isEmpty() && newRenders.none { it !in neverRendered } && pendingBoundsChange.isEmpty()) {
                break
            }

            newRenders.clear()
        }

        painting  = false
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
        val renderable         = (recursivelyVisible || visibilityChanged) && view.displayed
        val graphicsSurface    = if (renderable) graphicsDevice[view] else null

        graphicsSurface?.let {
            if (recursivelyVisible && !view.size.empty && view in neverRendered) {
                graphicsSurface.transform = view.transform
                graphicsSurface.camera    = view.camera
                graphicsSurface.opacity   = view.opacity
                graphicsSurface.index     = when (val parent = view.parent) {
                    null -> display.indexOf(view)
                    else -> parent.children_.indexOf(view)
                }
                if (view !in popups) {
                    graphicsSurface.zOrder = view.zOrder
                }
            }

            if (view in pendingBoundsChange) {
                updateGraphicsSurface(view, graphicsSurface)

                pendingBoundsChange -= view
            }

            if (visibilityChanged) {
                graphicsSurface.visible = view.visible

                this.visibilityChanged -= view
            }

            if (recursivelyVisible && !view.size.empty) {
                val viewList = pendingCleanup[view]

                viewList?.forEach {
                    releaseResources(null, it)
                }

                pendingCleanup -= view

                if (view in dirtyViews) {
                    dirtyViews    -= view
                    neverRendered -= view

                    graphicsSurface.apply {
                        clipCanvasToBounds = view.clipCanvasToBounds_
                        childrenClipPath   = view.childrenClipPath_?.path
                        mirrored           = view.needsMirrorTransform

                        render { canvas ->
                            view.render(canvas)
                        }
                    }

                    rendered = true
                    view.rendered = rendered
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
        // Check if this view was "revived" since being marked
        // for cleanup. Clear cleanup state for this parent
        // if so.
        if (view in livingViews) {
            parent?.let {
                pendingCleanup[parent]?.remove(view)
            }

            return
        }

        if (view in views) {
            view.removedFromDisplay_()

            view.rendered = false

            view.children_.forEach {
                releaseResources(it.parent, it)
            }

            pendingCleanup[view]?.forEach {
                releaseResources(view, it)
            }

            views               -= view
            dirtyViews          -= view
            livingViews         -= view
            pendingLayout       -= view
            pendingBoundsChange -= view

            parent?.let {
                pendingCleanup[parent]?.remove(view)
            }

            unregisterDisplayRectMonitoring(view)

            graphicsDevice.release(view)
        }
    }

    protected open fun addToCleanupList(parent: View, child: View) {
        pendingCleanup.getOrPut(parent) { fastMutableSetOf() }.apply { add(child) }
    }

    private fun removeFromCleanupList(parent: View?, child: View) {
        if (child !in views) {
            return
        }

        val iterator = pendingCleanup.iterator()

        while (iterator.hasNext()) {
            val it    = iterator.next()
            val views = it.value

            if (views.remove(child)) {
                val oldParent = it.key

                if (oldParent != parent) {
                    // The child is being moved to a different parent, so we force clean-up
                    releaseResources(it.key, child)
                }

                if (views.isEmpty()) {
                    iterator.remove()
                }

                return
            }
        }
    }

    override fun childrenChanged(view: View, differences: Differences<View>) {
        differences.computeMoves().forEach {
            when (it) {
                is Insert -> {
                    it.items.forEach { item ->
                        if (it.origin(of = item) == null) {
                            childAdded(view, item)
                        }
                    }
                }
                is Delete -> {
                    it.items.forEach { item ->
                        when (val newIndex = it.destination(of = item)) {
                            null -> childRemoved(view, item)
                            else -> if (item !in pendingRender) {
                                graphicsDevice[item].index = newIndex
                            }
                        }
                    }
                }
                else -> {}
            }
        }

        when {
            view.visible && !view.size.empty -> view.revalidate_()
            else                             -> {
                pendingCleanup[view]?.forEach {
                    releaseResources(view, it)
                }

                view.doLayout_()
            }
        }
    }

    private fun childAdded(parent: View?, child: View) {
        when {
            parent == null                               -> child.parent?.children_?.remove(child) // View is moved to the Display after it is already withing another View
            child in display.children || child in popups -> display -= child                       // View is moved to a new parent after it is already within the Display
        }

        removeFromCleanupList(parent, child)

        when {
            child.visible -> record(child)
            else          -> {
                addedInvisible          += child
                child.visibilityChanged += ::visibilityChanged
            }
        }
    }

    private fun removeFromLiving(view: View) {
        livingViews -= view

        view.children_.forEach { removeFromLiving(it) }
    }

    private fun childRemoved(parent: View?, child: View) {
        // TODO: Consider whether to optimize this; maybe update view.displayed here and check in releaseResources
        //  to see if it has been displayed again before cleaning up. Downside is that Views would be notified
        //  that they are not in the display up to 1 frame before they are removed.
        removeFromLiving(child)

        when {
            parent != null -> addToCleanupList(parent, child)
            else           -> releaseResources(parent, child)
        }
    }

    override fun visibilityChanged(view: View, old: Boolean, new: Boolean) {
        val parent            = view.parent
        val wasAddedInvisible = view in addedInvisible

        if (wasAddedInvisible) {
            record(view)

            addedInvisible -= view
            view.visibilityChanged -= ::visibilityChanged
        }

        if (!(parent == null || view in display || view in popups)) {
            pendingLayout += parent

            // Views that change bounds while invisible are never scheduled
            // for bounds sync, so catch them here
            if (new) {
                pendingBoundsChange += view
            }

            visibilityChanged += view
            pendingRender     += view

            render(parent)
        } else if (view in display || view in popups) {
            if (new) {
                visibilityChanged   += view
                pendingBoundsChange += view // See above

                if (!wasAddedInvisible) {
                    render(view)
                }
            } else {
                graphicsDevice[view].visible = false
                pendingBoundsChange -= view
            }
        }

        if (view in displayTree) {
            checkDisplayRectChange(view)
        }
    }

    override fun opacityChanged(view: View, old: Float, new: Float) {
        graphicsDevice[view].opacity = new
    }

    override fun zOrderChanged(view: View, old: Int, new: Int) {
        if (view !in popups) {
            graphicsDevice[view].zOrder = new
        }
    }

    override fun displayRectHandlingChanged(view: View, old: Boolean, new: Boolean) {
        when (new) {
            true -> registerDisplayRectMonitoring  (view)
            else -> unregisterDisplayRectMonitoring(view)
        }
    }

    override fun sizePreferencesChanged(view: View, old: SizePreferences, new: SizePreferences) {
        val parent = view.parent

        // Early exit if this event was triggered by an item as it is being removed from the container tree.
        //
        // Same for invisible items.
        if ((parent == null && view !in display && view !in popups) || !view.visible) {
            return
        }

        when (parent) {
            null -> if (display.layout?.requiresLayout(view, displayPositionableContainer, old, new) == true) display.relayout()
            else -> if (parent.layout_?.requiresLayout(view, parent.positionableWrapper,   old, new) == true) {
                pendingLayout += parent
                schedulePaint()
            }
        }
    }

    private val displayPositionableContainer = object: PositionableContainer {
        override val size        get() = display.size
        override val width       get() = display.width
        override val height      get() = display.height
        override var idealSize   get() = null as Size?; set(_) {}
        override var minimumSize get() = Size.Empty;    set(_) {}

        override val insets      get() = display.insets
        override val children    get() = display.children
    }

    override fun boundsChanged(view: View, old: Rectangle, new: Rectangle) {
        val parent = view.parent

        // Early exit if this event was triggered by an item as it is being removed from the container tree.
        //
        // Same for invisible items.
        if ((parent == null && view !in display && view !in popups) || !view.visible) {
            return
        }

        var reRender = false

        pendingBoundsChange += view

        if (!old.size.fastEquals(new.size)) {
            reRender = true

            if (view.children_.isNotEmpty() && view.layout_?.requiresLayout(view.positionableWrapper, old.size, new.size) == true) {
                when {
                    layingOut !== view                       -> pendingLayout += view

                    // view is in the middle of a layout, so re-do it to allow bounds
                    // changes to take effect
                    old.size sufficientlyDifferentTo new.size -> view.doLayout_()
                    else                                      -> return
                }
            }
        }

        when (parent) {
            null -> if (display.layout?.requiresLayout(view, displayPositionableContainer, old, new) == true) display.relayout()
//            parent.layout_ == null && old.size == new.size -> updateGraphicsSurface(view, graphicsDevice[view]) // There are cases when an item's position might be constrained by logic outside a layout
            else -> if (parent.layout_?.requiresLayout(view, parent.positionableWrapper, old, new) == true) pendingLayout += parent
        }

        when {
            reRender -> render(view, true)
            else     -> schedulePaint()
        }
    }

    private infix fun Size.sufficientlyDifferentTo(other: Size): Boolean {
        val epsilon = 1e-8

        return abs(width - other.width) > epsilon || abs(height - other.height) > epsilon
    }

    override fun transformChanged(view: View, old: AffineTransform, new: AffineTransform) {
        graphicsDevice[view].transform = new
    }

    override fun cameraChanged(view: View, old: Camera, new: Camera) {
        graphicsDevice[view].camera = new
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

            updateClipRect(node, view.parent?.let { displayTree[it] })

            if (!oldDisplayRect.fastEqual(node.clipRect)) {
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
        if (!old.fastEqual(new)) {
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

    protected class DisplayRectNode(public val view: View) {
        public var parent     : DisplayRectNode? = null
        public var clipRect   : Rectangle        = Empty
        public val numChildren: Int get()        = children.size

        private val children = mutableListOf<DisplayRectNode>()

        public operator fun plusAssign(child: DisplayRectNode) {
            child.parent = this
            children += child
        }

        public operator fun minusAssign(child: DisplayRectNode) {
            child.parent = null
            children -= child
        }

        public operator fun get(index: Int): DisplayRectNode = children[index]
    }

    override fun popupShown(view: View) {
        popups += view
        childAdded(null, view)
    }

    override fun popupHidden(view: View) {
        if (view.displayed) {
            childRemoved(null, view)
        }
        popups -= view
    }
}
