package com.nectar.doodle.controls.panels

import com.nectar.doodle.controls.theme.SplitPanelUI
import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.layout.Constraints
import com.nectar.doodle.layout.Insets
import com.nectar.doodle.layout.constrain
import com.nectar.doodle.utils.ChangeObservers
import com.nectar.doodle.utils.ChangeObserversImpl
import com.nectar.doodle.utils.Orientation
import com.nectar.doodle.utils.Orientation.Vertical


class SplitPanel(orientation: Orientation = Vertical, ratio: Float = 0.5f): Gizmo() {

    var renderer: SplitPanelUI? = null
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
        set(new) { if (new != field) { field = new; doLayout(); changed_() } }

    @Suppress("PrivatePropertyName")
    private val changed_ = ChangeObserversImpl(this)

    val changed: ChangeObservers<SplitPanel> = changed_

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
        changed_()
    }

    @Suppress("NAME_SHADOWING")
    private fun updateLayout() {
        val first   = firstItem
        val last    = lastItem
        val divider = divider
        val resizer = resizer

        // TODO: Handle Orientation
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