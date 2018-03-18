package com.nectar.doodle.drawing.impl


import com.nectar.doodle.core.Display
import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.drawing.GraphicsDevice
import com.nectar.doodle.drawing.GraphicsSurface
import com.nectar.doodle.drawing.RenderManager
import com.nectar.doodle.event.DisplayRectEvent
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Rectangle.Companion.Empty
import com.nectar.doodle.scheduler.Scheduler
import com.nectar.doodle.scheduler.Task
import com.nectar.doodle.theme.InternalThemeManager
import com.nectar.doodle.units.seconds
import com.nectar.doodle.utils.ObservableList


class RenderManagerImpl(
        private val display       : Display,
        private val scheduler     : Scheduler,
        private val themeManager  : InternalThemeManager?,
        private val graphicsDevice: GraphicsDevice<*>): RenderManager {

    private val gizmos              = mutableSetOf <Gizmo>()
    private var layingOut           = null as Gizmo?
    private val dirtyGizmos         = mutableSetOf <Gizmo>()
    private val displayTree         = mutableMapOf <Gizmo, DisplayRectNode>()
    private val neverRendered       = mutableSetOf <Gizmo>()
    private val pendingLayout       = mutableSetOf <Gizmo>()
    private val pendingRender       = mutableListOf<Gizmo>()
    private val pendingCleanup      = mutableMapOf <Gizmo, MutableSet<Gizmo>>()
    private val addedInvisible      = mutableSetOf <Gizmo>()
    private val visibilityChanged   = mutableSetOf <Gizmo>()
    private val pendingBoundsChange = mutableSetOf <Gizmo>()
    private var paintTask           = null as Task?
    private val childrenChanged_    = ::childrenChanged   // This is b/c Kotlin doesn't translate inline functions in a way that allows them to be used in maps
    private val boundsChanged_      = ::boundsChanged
    private val visibilityChanged_  = ::visibilityChangedFunc

    init {
        display.children.onChange += childrenChanged_
        display.sizeChanged += { display, _, _ ->

            display.doLayout()

            display.forEach { checkDisplayRectChange(it) }
        }

        display.forEach { recordGizmo(it) }
    }

    override fun render(gizmo: Gizmo) {
        render(gizmo, false)
    }

    override fun renderNow(gizmo: Gizmo) {
        if (gizmo in gizmos && !gizmo.bounds.empty && display.isAncestor(gizmo)) {
            dirtyGizmos += gizmo

            if (gizmo in pendingLayout) {
                performLayout(gizmo)
            }

            val parent = gizmo.parent

            if (parent != null && (parent in neverRendered || parent in dirtyGizmos)) {
                renderNow(parent)
            } else {
                performRender(gizmo)
            }
        }
    }

    override fun displayRect(of: Gizmo): Rectangle? {
        displayTree[of]?.let { return it.clipRect }

        var child = of
        var parent: Gizmo? = of.parent ?: return Empty

        var clipRect = if (of.visible) Rectangle(size = of.size) else Empty

        while (parent != null && !clipRect.empty) {
            clipRect = clipRect.intersect(Rectangle(0 - child.x,
                    0 - child.y,
                    if (parent.visible) parent.width  else 0.0,
                    if (parent.visible) parent.height else 0.0))

            child  = parent
            parent = parent.parent
        }

        return clipRect
    }

    private fun recordGizmo(gizmo: Gizmo) {
        if (gizmo !in gizmos) {
            gizmo.parent?.let {
                if (it !in gizmos) {
                    recordGizmo(it)
                    return
                }
            }

            if (gizmo.parent != null) {
                gizmo.addedToDisplay(this)

                dirtyGizmos         += gizmo
                neverRendered       += gizmo
                pendingBoundsChange += gizmo

                gizmo.boundsChanged      += boundsChanged_
                gizmo.visibilityChanged  += visibilityChanged_
                gizmo.children_.onChange += childrenChanged_
            }

            gizmos += gizmo

            gizmo.children_.forEach { recordGizmo(it) }

            scheduleLayout(gizmo)

            if (gizmo.monitorsDisplayRect) {
                registerDisplayRectMonitoring(gizmo)

                notifyDisplayRectChange(gizmo, Empty, displayRect(gizmo))
            }

            if (displayTree.containsKey(gizmo)) {
                // TODO: IMPLEMENT
            }

            if (gizmo in display) {
                render(gizmo, true)
            }
        }
    }

    private fun schedulePaint() {
//        // TODO: Need to see whether this will be an issue for setups
//        onPaint()
        if (paintTask == null || paintTask?.completed == true) {
            paintTask = scheduler.after(paintDelay) { onPaint() }
        }
    }

    private fun render(gizmo: Gizmo, ignoreEmptyBounds: Boolean) {
        if (prepareRender(gizmo, ignoreEmptyBounds)) {
            schedulePaint()
        }
    }

    private fun prepareRender(gizmo: Gizmo, ignoreEmptyBounds: Boolean): Boolean {
        if ((ignoreEmptyBounds || !gizmo.bounds.empty) && gizmo in gizmos && display.isAncestor(gizmo)) {
            dirtyGizmos += gizmo

            val iterator = pendingRender.iterator()

            while (iterator.hasNext()) {
                val item = iterator.next()

                // Only take reference identity into account
                if (item === gizmo || item.isAncestor_(gizmo)) {
                    return false
                }

                if (gizmo.isAncestor_(item)) {
                    iterator.remove()
                }
            }

            pendingRender += gizmo

            return true
        }

        return false
    }

    private fun onPaint() {
        // TODO: Spread this across multiple frames instead to avoid long running paint
        do {
            pendingLayout.firstOrNull()?.let { performLayout(it) }
        } while (!pendingLayout.isEmpty())

        do {
            pendingRender.firstOrNull()?.let { performRender(it) }
        } while (!pendingRender.isEmpty())

        pendingBoundsChange.forEach {
            if (it !in neverRendered) {
                updateGraphicsSurface(it, graphicsDevice[it])
            }
        }

        paintTask = null
    }

    private fun scheduleLayout(gizmo: Gizmo) {
        // Only take reference identity into account
        if (layingOut !== gizmo) {
            pendingLayout += gizmo
        }
    }

    private fun performLayout(gizmo: Gizmo) {
        if (gizmo.parent in pendingLayout) {
            gizmo.parent?.let { performLayout(it) }
        }

        layingOut = gizmo

        gizmo.doLayout_()

        layingOut = null

        pendingLayout -= gizmo
    }

    private fun performRender(gizmo: Gizmo) {
        pendingRender -= gizmo
        neverRendered -= gizmo

        val visibilityChanged = gizmo in visibilityChanged

        val graphicsSurface = if (gizmo.visible || visibilityChanged) graphicsDevice[gizmo] else null

        graphicsSurface?.let {
            if (gizmo in pendingBoundsChange) {
                updateGraphicsSurface(gizmo, graphicsSurface)

                pendingBoundsChange -= gizmo
            }

            if (visibilityChanged) {
                graphicsSurface.visible = gizmo.visible

                this.visibilityChanged -= gizmo
            }

            if (gizmo.visible && !gizmo.bounds.empty) {
                if (!gizmo.children_.isEmpty()) {
                    val gizmoList = pendingCleanup[gizmo]

                    gizmoList?.forEach {
                        releaseResources(it)

                        graphicsDevice.release(it)
                    }

                    pendingCleanup -= gizmo
                }

                if (gizmo in dirtyGizmos) {
                    dirtyGizmos -= gizmo

                    graphicsSurface.render { canvas ->
                        gizmo.render(canvas)

                        gizmo.children_.reversed().forEach {
                            performRender(it)
                        }
                    }
                }
            }
        }
    }

    private fun updateGraphicsSurface(gizmo: Gizmo, surface: GraphicsSurface) {
        surface.bounds = gizmo.bounds
    }

    private fun releaseResources(gizmo: Gizmo) {
        gizmo.removedFromDisplay_()

        gizmo.children_.forEach {
            releaseResources(it)
        }

        gizmos              -= gizmo
        dirtyGizmos         -= gizmo
        pendingLayout       -= gizmo
        pendingRender       -= gizmo
        pendingBoundsChange -= gizmo

        gizmo.boundsChanged      -= boundsChanged_
        gizmo.visibilityChanged  -= visibilityChanged_
        gizmo.children_.onChange -= childrenChanged_

        unregisterDisplayRectMonitoring(gizmo)
    }

    private fun addToCleanupList(parent: Gizmo, child: Gizmo) {
        pendingCleanup.getOrPut(parent) { mutableSetOf() }.apply { add(child) }
    }

    private fun removeFromCleanupList(parent: Gizmo, child: Gizmo) {
        val gizmos = pendingCleanup[parent]

        if (gizmos != null) {
            gizmos.remove(child)

            if (gizmos.isEmpty()) {
                pendingCleanup.remove(parent)
            }
        }
    }

    private fun childrenChanged(list: ObservableList<Gizmo, Gizmo>, removed: Map<Int, Gizmo>, added: Map<Int, Gizmo>, moved: Map<Int, Pair<Int, Gizmo>>) {
        val parent = list.source

        removed.values.forEach { childRemoved(parent, it) }
        added.values.forEach   { childAdded  (parent, it) }

        if (parent !in pendingRender) {
            moved.forEach {
                val surface = graphicsDevice[it.value.second]

                surface.zIndex = it.key
            }
        }

        if (removed.isEmpty() && added.isEmpty()) {
            return
        }

        if (parent.parent != null && parent.visible && !parent.size.empty) {
            parent.revalidate_()
        } else {
            pendingCleanup[parent]?.forEach {
                releaseResources(it)

                graphicsDevice.release(it)
            }

            parent.doLayout_()
        }
    }

    private fun childAdded(parent: Gizmo, child: Gizmo) {
        removeFromCleanupList(parent, child)

        if (child.visible) {
            handleAddedGizmo(child)
        } else {
            addedInvisible.add(child)
            child.visibilityChanged += visibilityChanged_
        }
    }

    private fun handleAddedGizmo(gizmo: Gizmo) {
        recordGizmo(gizmo)

        themeManager?.update(gizmo)
    }

    private fun childRemoved(parent: Gizmo, child: Gizmo) {
        if (parent.parent != null) {
            addToCleanupList(parent, child)
        } else {
            releaseResources(child)

            graphicsDevice.release(child)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun visibilityChangedFunc(gizmo: Gizmo, old: Boolean, new: Boolean) {
        val parent = gizmo.parent

        if (gizmo in addedInvisible) {
            handleAddedGizmo(gizmo)

            addedInvisible.remove(gizmo)
        }

        if (parent != null && gizmo !in display) {
            scheduleLayout(parent)

            // Gizmos that change bounds while invisible are never scheduled
            // for bounds synch, so catch them here
            if (new) {
                pendingBoundsChange += gizmo
            }

            visibilityChanged += gizmo

            render(parent)
        } else if (gizmo in display) {
            if (new) {
                visibilityChanged   += gizmo
                pendingBoundsChange += gizmo // See above

                render(gizmo)
            } else {
                graphicsDevice[gizmo].visible = false
            }
        }

        if (gizmo in displayTree) {
            checkDisplayRectChange(gizmo)
        }
    }

    private fun boundsChanged(gizmo: Gizmo, old: Rectangle, new: Rectangle) {
        val parent = gizmo.parent

        // Early exit if this event was triggered by an item as it is being removed from the container tree.
        //
        // Same for invisible items.
        if (parent == null || !gizmo.visible) {
            return
        }

        var reRender = false

        pendingBoundsChange += gizmo

        if (old.size != new.size) {
            reRender = true
            scheduleLayout(gizmo)
        }

//        if (parent in display) {
//            parent.doLayout_()
//        } else {
            scheduleLayout(parent)

            schedulePaint()
//        }

        if (reRender) {
            render(gizmo, true)
        } else {
            schedulePaint()
        }

        if (displayTree.containsKey(gizmo)) {
            checkDisplayRectChange(gizmo)
        }
    }

//    private inner class InternalPropertyListener : PropertyListener {
//            } else if (aProperty === Gizmo.DISPLAYRECT_HANDLING_REQUIRED) {
//                val aGizmo = aPropertyEvent.getSource() as Gizmo
//
//                if (aGizmo.getDisplayRectHandlingEnabled()) {
//                    registerDisplayRectMonitoring(aGizmo)
//                } else {
//                    unregisterDisplayRectMonitoring(aGizmo)
//                }
//            } else if (aProperty === Gizmo.IDEAL_SIZE || aProperty === Gizmo.MINIMUM_SIZE) {
//                val aParent = (aPropertyEvent.getSource() as Gizmo).parent
//
//                if (aParent.layout != null) {
//                    val aNeedsLayout = if (aProperty === Gizmo.IDEAL_SIZE)
//                        aParent.layout!!.usesChildIdealSize()
//                    else
//                        aParent.layout!!.usesChildMinimumSize()
//
//                    if (aNeedsLayout) {
//                        if (display.contains(aParent)) {
//                            aParent.doLayout()
//                        } else {
//                            scheduleLayout(aParent)
//
//                            schedulePaint()
//                        }
//                    }
//                }
//            }
//        }
//    }

    private fun registerDisplayRectMonitoring(gizmo: Gizmo) {
        if (!displayTree.containsKey(gizmo)) {
            val node = DisplayRectNode(gizmo)

            node.clipRect = Rectangle(size = gizmo.size)

            displayTree[gizmo] = node

            gizmo.parent?.let {
                registerDisplayRectMonitoring(it)

                val parentNode = displayTree[it]

                updateClipRect(node, parentNode)

                parentNode!! += node
            }
        }
    }

    private fun unregisterDisplayRectMonitoring(gizmo: Gizmo) {
        displayTree[gizmo]?.let {
            if (it.numChildren == 0) {
                displayTree -= gizmo

                it.parent?.minusAssign(it)

                gizmo.parent?.let { unregisterDisplayRectMonitoring(it) }
            }
        }
    }

    private fun checkDisplayRectChange(gizmo: Gizmo) {
        displayTree[gizmo]?.let { node ->
            val oldDisplayRect = node.clipRect

            updateClipRect(node, displayTree[gizmo.parent])

            if (oldDisplayRect != node.clipRect) {
                if (gizmo.monitorsDisplayRect) {
                    notifyDisplayRectChange(gizmo, oldDisplayRect, node.clipRect)
                }

                for (i in 0 until node.numChildren) {
                    checkDisplayRectChange(node[i].gizmo)
                }
            }
        }
    }

    private fun notifyDisplayRectChange(gizmo: Gizmo, old: Rectangle?, new: Rectangle?) {
        if (old != new) {
            gizmo.handleDisplayRectEvent(DisplayRectEvent(gizmo, old, new))
        }
    }

    private fun updateClipRect(node: DisplayRectNode, parent: DisplayRectNode?) {
        val gizmo = node.gizmo

        val gizmoRect = if (gizmo.visible) Rectangle(size = gizmo.size) else Empty

        if (parent == null) {
            node.clipRect = gizmoRect
        } else {
            val parentClip = parent.clipRect
            val parentBounds = Rectangle(parentClip!!.x - gizmo.x,
                    parentClip.y - gizmo.y,
                    parentClip.width,
                    parentClip.height)

            node.clipRect = gizmoRect.intersect(parentBounds)
        }
    }

    private class DisplayRectNode(val gizmo: Gizmo) {
        var parent: DisplayRectNode? = null
        var clipRect: Rectangle? = null
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

    companion object {
        // TODO: This may need to be browser specific
        private val paintDelay = 0.seconds
    }
}
