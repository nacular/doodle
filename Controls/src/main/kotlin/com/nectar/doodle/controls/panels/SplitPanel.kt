package com.nectar.doodle.controls.panels

import com.nectar.doodle.controls.ChangeObservers
import com.nectar.doodle.controls.ChangeObserversImpl
import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.event.MouseListener
import com.nectar.doodle.event.MouseMotionListener
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.layout.Constraints
import com.nectar.doodle.layout.Insets
import com.nectar.doodle.layout.constrain
import com.nectar.doodle.system.Cursor.Companion.ColResize
import com.nectar.doodle.system.Cursor.Companion.RowResize
import com.nectar.doodle.theme.Renderer
import com.nectar.doodle.utils.Orientation
import com.nectar.doodle.utils.Orientation.Horizontal
import com.nectar.doodle.utils.Orientation.Vertical
import kotlin.math.max
import kotlin.math.min


interface SplitPanelRenderer: Renderer<SplitPanel> {
    fun divider(panel: SplitPanel): Gizmo?
    fun resizer(panel: SplitPanel): Gizmo?
}

abstract class AbstractSplitPanelRenderer(
        private val spacing: Double = 7.0,
        private val divider: Gizmo,
        private val resizer: Gizmo? = null): SplitPanelRenderer, MouseListener, MouseMotionListener {

    private var splitPanel      = null as SplitPanel?
    private var orientation     = Vertical
    private var pressedLocation = 0.0

    init {
        divider.mouseChanged       += this
        divider.mouseMotionChanged += this

        resizer?.visible            = false
    }

    override fun divider(panel: SplitPanel): Gizmo? = divider
    override fun resizer(panel: SplitPanel): Gizmo? = resizer

    override fun install(gizmo: SplitPanel) {
        splitPanel  = gizmo.also { it.panelSpacing = spacing }
        orientation = gizmo.orientation

        when (gizmo.orientation) {
            Vertical   -> divider.cursor = ColResize
            Horizontal -> divider.cursor = RowResize
        }
    }

    override fun uninstall(gizmo: SplitPanel) {
        splitPanel = null
    }

    override fun mousePressed(event: MouseEvent) {
        resizer?.bounds  = divider.bounds
        resizer?.visible = true

        pressedLocation = when (orientation) {
            Vertical   -> event.location.x
            Horizontal -> event.location.y
        }
    }

    override fun mouseReleased(event: MouseEvent) {
        splitPanel?.let { splitPanel ->
            resizer?.let { resizer ->
                var minPosition = 0.0
                var position = 0.0
                var maxPosition = 0.0

                when (orientation) {
                    Vertical   -> {
                        minPosition = splitPanel.insets.left
                        position = resizer.x
                        maxPosition = splitPanel.width - splitPanel.panelSpacing - splitPanel.insets.run { right }
                    }

                    Horizontal -> {
                        minPosition = splitPanel.insets.bottom
                        position = resizer.y
                        maxPosition = splitPanel.height - splitPanel.panelSpacing - splitPanel.insets.run { bottom }
                    }
                }

                splitPanel.ratio = ((position - minPosition) / (maxPosition - minPosition)).toFloat()

                resizer.visible = false
            }
        }
    }

    override fun mouseDragged(mouseEvent: MouseEvent) {
        splitPanel?.let { splitPanel ->

            var minPosition = 0.0
            var maxPosition = 0.0
            var position    = 0.0

            when (orientation) {
                Vertical -> {
                    minPosition = splitPanel.insets.left
                    position    = divider.x + mouseEvent.location.x - pressedLocation
                    maxPosition = splitPanel.run { width - panelSpacing - insets.run { right } }
                }

                Horizontal -> {

                    minPosition = splitPanel.insets.top
                    position    = divider.y + mouseEvent.location.y - pressedLocation
                    maxPosition = splitPanel.run { height - panelSpacing - insets.run { bottom } }
                }
            }

            val newPosition = min(maxPosition, max(minPosition, position))

            if (resizer != null) {
                when (orientation) {
                    Vertical   -> resizer.x = newPosition
                    Horizontal -> resizer.y = newPosition
                }
            } else {
                splitPanel.ratio = ((newPosition - minPosition) / (maxPosition - minPosition)).toFloat()
            }
        }
    }
}

class SplitPanel(orientation: Orientation = Vertical, ratio: Float = 0.5f): Gizmo() {

    var renderer: SplitPanelRenderer? = null
        set(new) {

            divider?.let { children -= it }
            resizer?.let { children -= it }

            field = new?.also {
                divider = it.divider(this)
                resizer = it.resizer(this)

                divider?.let {
                    it.width = panelSpacing

                    children += it

                    setZIndex(it, 0)
                }

                resizer?.let {
                    it.width = panelSpacing

                    children += it

                    setZIndex(it, 0)
                }

                if (divider != null || resizer != null) {
                    updateLayout()
                }
            }
        }

    var firstItem: Gizmo? = null
        set(new) {
            if (new == field) { return }

            field?.let { children -= it }

            field  = new

            field?.let { children += it }

            fireChanged()
        }

    private var divider: Gizmo? = null
    private var resizer: Gizmo? = null

    var lastItem: Gizmo? = null
        set(new) {
            if (new == field) { return }

            field?.let { children -= it }

            field  = new

            field?.let { children += it }

            fireChanged()
        }

    var orientation = orientation
        set(new) { if (new != field) { field = new; fireChanged() } }

    var ratio = ratio
        set(new) { if (new != field) { field = new; doLayout(); onChanged_.set.forEach { it(this) } } }

    @Suppress("PrivatePropertyName")
    private val onChanged_ = ChangeObserversImpl<SplitPanel>()

    val onChanged: ChangeObservers<SplitPanel> = onChanged_

    var panelSpacing = 0.0
        set(new) { if (new != field) { field = new; doLayout() } }

    public override var insets
        get(   ) = super.insets
        set(new) { if (new != super.insets) { super.insets = new; doLayout() } }

    init {
        require(ratio in 0.0 .. 1.0) { "ratio must be in 0 .. 1" }
    }

    override fun render(canvas: Canvas) {
        renderer?.render(this, canvas)
    }

    override fun contains(point: Point) = renderer?.contains(this, point) ?: super.contains(point)

    private fun fireChanged() {
        updateLayout()
        onChanged_.set.forEach { it(this) }
    }

    private fun updateLayout() {
        val first   = firstItem
        val last    = lastItem
        val divider = divider
        val resizer = resizer

        val fill: (Constraints, Insets) -> Unit = { gizmo, insets ->
            gizmo.top    = gizmo.parent.top    + { insets.top    }
            gizmo.left   = gizmo.parent.left   + { insets.left   }
            gizmo.bottom = gizmo.parent.bottom + { insets.bottom }
            gizmo.right  = gizmo.parent.right  + { insets.right  }
        }

        val layout = when {
            first != null && last != null -> {
                constrain(first, last) { first, last ->
                    first.top    = first.parent.top    + { insets.top    }
                    first.left   = first.parent.left   + { insets.left   }
                    first.bottom = first.parent.bottom - { insets.bottom }
//                    first.width  = (first.parent.width - panelSpacing - insets.left - insets.right) * ratio
                    first.right  = first.left + (first.parent.width - { panelSpacing + insets.left + insets.right }) * { ratio }
                    last.top     = first.top
                    last.left    = first.right + { panelSpacing }
                    last.bottom  = first.bottom
                    last.right   = last.parent.right - { insets.right }
                }
            }

            first != null -> constrain(first) { fill(it, insets) }
            last  != null -> constrain(last ) { fill(it, insets) }
            else -> null
        }

        layout?.let {
            // TODO: Handle Orientation
            if (divider != null && first != null) {
                it.constrain(divider, first) { divider, first ->
                    divider.top    = first.top
                    divider.left   = first.right
                    divider.bottom = first.bottom
                }
            }

            if (resizer != null && first != null) {
                it.constrain(resizer, first) { resizer, first ->
                    resizer.top    = first.top
                    resizer.bottom = first.bottom
                }
            }
        }

        this.layout = layout
    }
}