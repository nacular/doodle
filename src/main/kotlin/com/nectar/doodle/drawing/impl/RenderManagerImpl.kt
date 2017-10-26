package com.nectar.doodle.drawing.impl


import com.nectar.doodle.core.Display
import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.drawing.GraphicsDevice
import com.nectar.doodle.drawing.GraphicsSurface
import com.nectar.doodle.drawing.RenderManager
import com.nectar.doodle.event.DisplayRectEvent
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.scheduler.Scheduler
import com.nectar.doodle.scheduler.Task
import com.nectar.doodle.ui.UIManager
import com.nectar.doodle.units.seconds
import com.nectar.doodle.utils.ObservableList


class RenderManagerImpl(
        private val display       : Display,
        private val uiManager     : UIManager,
        private val scheduler     : Scheduler,
        private val graphicsDevice: GraphicsDevice<*>): RenderManager {

    private val gizmos              = mutableSetOf <Gizmo>()
    private var layingOut           = null as Gizmo?
    private val dirtyGizmos         = mutableSetOf <Gizmo>()
    private val displayTree         = mutableMapOf <Gizmo, DisplayRectNode>()
    private val neverRendered       = mutableSetOf <Gizmo>()
    private val pendingLayout       = mutableSetOf <Gizmo>()
    private val pendingRender       = mutableListOf<Gizmo>()
    private val pendingCleanup      = mutableMapOf <Gizmo, Set<Gizmo>>()
    private val addedInvisible      = mutableSetOf <Gizmo>()
    private val visibilityChanged   = mutableSetOf <Gizmo>()
    private val pendingBoundsChange = mutableSetOf <Gizmo>()

    private var paintTask           = null as Task?
//    private val mPropertyListener   = InternalPropertyListener()
//    private val mContainerListener  = InternalContainerListener()

    init {
        display.children.onChange += this::childrenChanged
        display.sizeChange        += { gizmo, _, _ ->

            gizmo.doLayout_()

            if (displayTree.containsKey(gizmo)) {
                checkDisplayRectChange(gizmo)
            }
        }

//        mDisplay.addContainerListener(mContainerListener)

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

    override fun displayRect(of: Gizmo): Rectangle? = displayRect(of, of.size)

    private fun displayRect(gizmo: Gizmo, size: Size): Rectangle? {
        displayTree[gizmo]?.let { return it.clipRect }

        var child = gizmo
        var parent: Gizmo? = gizmo.parent ?: return Rectangle.Empty

        var clipRect = if (gizmo.visible) Rectangle(size = Size(size.width, size.width)) else Rectangle.Empty

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

    private fun schedulePaint() {
        if (paintTask == null) {
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
        do {
            val iterator = pendingLayout.iterator()

            while (iterator.hasNext()) {
                val item = iterator.next()
                iterator.remove()
                performLayout(item)
            }

            layingOut = null
        } while (!pendingLayout.isEmpty())

        val copy = pendingRender.toTypedArray()

        copy.forEach { performRender(it) }

        pendingBoundsChange.forEach {
            if (it !in neverRendered) {
                updateGraphicsSurface(it, graphicsDevice[it])
            }
        }

        paintTask = null
    }

    private fun performLayout(gizmo: Gizmo) {
        layingOut = gizmo

        gizmo.doLayout_()
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
                var wasRendered = false

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

                    graphicsSurface.beginRender()

                    gizmo.render(graphicsSurface.canvas)

                    wasRendered = true
                }

                gizmo.childrenByZIndex_.reversed().forEach {
                    performRender(it)
                }

                if (wasRendered) {
                    graphicsSurface.endRender()
                }
            }
        }
    }

    private fun recordGizmo(gizmo: Gizmo) {
        if (gizmo !in gizmos) {
            gizmo.addedToDisplay(this, uiManager)

            gizmos              += gizmo
            dirtyGizmos         += gizmo
            neverRendered += gizmo
            pendingBoundsChange += gizmo

            gizmo.boundsChange += this::boundsChanged

//            gizmo.addPropertyListener(mPropertyListener)
//            gizmo.addContainerListener(mContainerListener)

            gizmo.children_.forEach { recordGizmo(it) }

            scheduleLayout(gizmo)

            if (gizmo.monitorsDisplayRect) {
                registerDisplayRectMonitoring(gizmo)

                notifyDisplayRectChange(gizmo, Rectangle.Empty, displayRect(gizmo))
            }

            if (displayTree.containsKey(gizmo)) {
                // TODO: IMPLEMENT
            }

            if (gizmo in display) {
                render(gizmo, true)
            }
        }
    }

    private fun updateGraphicsSurface(gizmo: Gizmo, surface: GraphicsSurface) {
        surface.bounds = gizmo.bounds
    }

    private fun releaseResources(gizmo: Gizmo) {
        gizmo.removedFromDisplay()

        gizmo.children_.forEach {
            releaseResources(it)
        }

//        gizmo.removeContainerListener(mContainerListener)

        gizmos              -= gizmo
        dirtyGizmos         -= gizmo
        pendingLayout       -= gizmo
        pendingRender       -= gizmo
        pendingBoundsChange -= gizmo

        gizmo.boundsChange -= this::boundsChanged

//        gizmo.removePropertyListener(mPropertyListener)

        unregisterDisplayRectMonitoring(gizmo)
    }

    private fun addToCleanupList(parent: Gizmo, child: Gizmo) {
        var gizmos = pendingCleanup[parent]

        if (gizmos == null) {
            gizmos = mutableSetOf()

            pendingCleanup.put(parent, gizmos)
        }

        gizmos += child

//        releaseResources( aChild );
    }

    private fun removeFromCleanupList(parent: Gizmo, child: Gizmo) {
        var gizmos = pendingCleanup[parent]

        if (gizmos != null) {
            gizmos -= child

            if (gizmos.isEmpty()) {
                pendingCleanup.remove(parent)
            }
        }
    }

    private fun registerDisplayRectMonitoring(gizmo: Gizmo) {
        if (!displayTree.containsKey(gizmo)) {
            val node = DisplayRectNode(gizmo)

            node.clipRect = Rectangle(size = gizmo.size)

            displayTree.put(gizmo, node)

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

        val gizmoRect = if (gizmo.visible) Rectangle(size = gizmo.size) else Rectangle.Empty

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

    private fun scheduleLayout(gizmo: Gizmo) {
        // Only take reference identity into account
        if (layingOut != gizmo) {
            pendingLayout += gizmo
        }
    }

    private fun handleAddedGizmo(gizmo: Gizmo) {
        recordGizmo(gizmo)

        uiManager.revalidateUI(gizmo)
    }

    private fun childrenChanged(list: ObservableList<Gizmo, Gizmo>, removed: List<Int>, added: Map<Int, Gizmo>) {
        val parent = list.source

        removed.map { parent.children_[it] }.forEach { childRemoved(parent, it) }

        added.values.forEach { childAdded(parent, it) }

        if (parent.parent != null) {
            parent.revalidate_()
        } else {
            parent.doLayout_()
        }
    }

    private fun childAdded(parent: Gizmo, child: Gizmo) {
        removeFromCleanupList(parent, child)

        if (child.visible) {
            handleAddedGizmo(child)
        } else {
//                child.addPropertyListener(mPropertyListener)

            addedInvisible.add(child)
        }
    }

    private fun childRemoved(parent: Gizmo, child: Gizmo) {
        if (parent.parent != null) {
            addToCleanupList(parent, child)
        } else {
            releaseResources(child)

            graphicsDevice.release(child)
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

        if (parent in display) {
            parent.doLayout_()
        } else {
            scheduleLayout(parent)

            schedulePaint()
        }

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
//        fun propertyChanged(aPropertyEvent: PropertyEvent) {
//            val aProperty = aPropertyEvent.getProperty()
//
//            if (aProperty === Gizmo.VISIBLE) {
//                val aGizmo = aPropertyEvent.getSource() as Gizmo
//                val aParent = aGizmo.parent
//
//                if (mAddedInvisible.contains(aGizmo)) {
//                    aGizmo.removePropertyListener(mPropertyListener)
//
//                    handleAddedGizmo(aGizmo)
//
//                    mAddedInvisible.remove(aGizmo)
//                }
//
//                if (aParent != null && !display.contains(aGizmo)) {
//                    scheduleLayout(aParent)
//
//                    // Gizmos that change bounds while invisible are never scheduled
//                    // for bounds synch, so catch them here
//                    if (aGizmo.visible) {
//                        pendingBoundsChange.add(aGizmo)
//                    }
//
//                    visibilityChanged.add(aGizmo)
//
//                    render(aParent!!)
//                } else if (display.contains(aGizmo)) {
//                    if (aGizmo.visible) {
//                        visibilityChanged.add(aGizmo)
//                        pendingBoundsChange.add(aGizmo) // See above
//
//                        render(aGizmo)
//                    } else {
//                        val aScreenContext = graphicsDevice.get(aGizmo)
//
//                        if (aScreenContext != null) {
//                            aScreenContext!!.setVisible(false)
//                        }
//                    }
//                }
//
//                if (displayTree.containsKey(aGizmo)) {
//                    checkDisplayRectChange(aGizmo)
//                }
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
//
//    private inner class InternalContainerListener : ContainerListener {
//        fun itemsAdded(aContainerEvent: ContainerEvent) {
//            val aChanges = aContainerEvent.getChanges()
//            val aContainer = aContainerEvent.getSource()
//
//            for (aChange in aChanges) {
//                val aGizmo = aChange.getFirst()
//
//                removeFromCleanupList(aContainer, aGizmo)
//
//                if (aGizmo.visible) {
//                    handleAddedGizmo(aGizmo)
//                } else {
//                    aGizmo.addPropertyListener(mPropertyListener)
//
//                    mAddedInvisible.add(aGizmo)
//                }
//            }
//
//            if (aContainer.parent != null) {
//                aContainer.revalidate()
//            } else {
//                aContainer.doLayout()
//            }
//        }
//
//        fun itemsRemoved(aContainerEvent: ContainerEvent) {
//            val aChanges = aContainerEvent.getChanges()
//            val aContainer = aContainerEvent.getSource()
//
//            if (aContainer.parent != null) {
//                for (aChange in aChanges) {
//                    addToCleanupList(aContainer, aChange.getFirst())
//                }
//
//                aContainer.revalidate()
//            } else {
//                for (aChange in aChanges) {
//                    releaseResources(aChange.getFirst())
//
//                    graphicsDevice.release(aChange.getFirst())
//                }
//            }
//        }
//
//        fun itemsZIndexChanged(aContainerEvent: ContainerEvent) {
//            val aChanges = aContainerEvent.getChanges()
//
//            for (aChange in aChanges) {
//                val aSurface = graphicsDevice.get(aChange.getFirst())
//
//                aSurface.setZIndex(aChange.getSecond())
//            }
//        }
//    }

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
