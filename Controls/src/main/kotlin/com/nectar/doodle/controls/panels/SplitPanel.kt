package com.nectar.doodle.controls.panels

import com.nectar.doodle.controls.ChangeObservers
import com.nectar.doodle.controls.ChangeObserversImpl
import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.layout.Constraints
import com.nectar.doodle.layout.Insets
import com.nectar.doodle.layout.constrain
import com.nectar.doodle.theme.Renderer
import com.nectar.doodle.utils.Orientation
import com.nectar.doodle.utils.Orientation.Horizontal


interface SplitPanelRenderer: Renderer<SplitPanel> {
    fun divider(panel: SplitPanel): Gizmo?
}

class SplitPanel(orientation: Orientation? = Horizontal, ratio: Float = 0.5f): Gizmo() {

    var renderer: SplitPanelRenderer? = null
        set(new) {

            divider?.let { children -= it }

            field = new?.also {
                divider = it.divider(this)

                divider?.let {
                    children += it

                    setZIndex(it, 0)

                    updateLayout()
                }
            }
        }

    var firstItem: Gizmo? = null
        set(new) {
            if (new == field) {
                return
            }

            field?.let { children -= it }

            field  = new

            field?.let { children += it }

            fireChanged()
        }

    private var divider: Gizmo? = null

    var lastItem: Gizmo? = null
        set(new) {
            if (new == field) {
                return
            }

            field?.let { children -= it }

            field  = new

            field?.let { children += it }

            fireChanged()
        }

    var orientation = orientation
        set(new) { if (new != field) { field = new; fireChanged() } }

    var ratio = ratio
        set(new) { if (new != field) { field = new; doLayout() } }

    @Suppress("PrivatePropertyName")
    private val onChanged_ = ChangeObserversImpl<SplitPanel>()

    val onChanged: ChangeObservers<SplitPanel> = onChanged_

    var panelSpacing = 0.0
        set(new) { if (new != field) { field = new; doLayout() } }

    public override var insets
        get(   ) = super.insets
        set(new) { if (new != super.insets) { super.insets = new; doLayout() } }

    init {
        this.ratio = (if (ratio >= 0) ratio else 0.5f)
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

        if (divider != null && layout != null) {
            layout.constrain(divider) { divider ->
                divider.top    = divider.parent.top
                divider.left   = divider.parent.left + { insets.left } + (divider.parent.width - { panelSpacing + insets.left + insets.right }) * { ratio }
                divider.bottom = divider.parent.bottom
            }
        }

        this.layout = layout
    }
}