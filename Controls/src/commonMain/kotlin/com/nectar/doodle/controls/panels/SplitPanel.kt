package com.nectar.doodle.controls.panels

import com.nectar.doodle.controls.theme.SplitPanelBehavior
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.layout.constrain
import com.nectar.doodle.layout.fill
import com.nectar.doodle.utils.ChangeObservers
import com.nectar.doodle.utils.ChangeObserversImpl
import com.nectar.doodle.utils.Orientation
import com.nectar.doodle.utils.Orientation.Vertical


class SplitPanel(orientation: Orientation = Vertical, ratio: Float = 0.5f): View() {

    var behavior: SplitPanelBehavior? = null; set(new) {

            divider?.let { children -= it }

            field?.uninstall(this)

            field = new?.also { behavior ->
                divider = behavior.divider(this)

                divider?.let {
                    if (behavior.dividerVisible) {
                        panelSpacing = it.width
                    }

                    children += it

                    it.zOrder = 1
                }

                if (divider != null) {
                    updateLayout()
                }

                behavior.install(this)
            }
        }

    var firstItem: View? = null; set(new) {
            if (new == field) { return }

            field?.let { children -= it }

            field  = new

            field?.let { children += it }

            fireChanged()
        }

    var lastItem: View? = null; set(new) {
        if (new == field) { return }

        field?.let { children -= it }

        field  = new

        field?.let { children += it }

        fireChanged()
    }

    var orientation = orientation; set(new) { if (new != field) { field = new; fireChanged() } }

    var ratio = ratio; set(new) { if (new != field) { field = new; relayout(); changed_() } }

    private var divider      = null as View?
    private var panelSpacing = 0.0

    @Suppress("PrivatePropertyName")
    private val changed_ = ChangeObserversImpl(this)

    val changed: ChangeObservers<SplitPanel> = changed_

    public override var insets
        get(   ) = super.insets
        set(new) { if (new != super.insets) { super.insets = new; relayout() } }

    init {
        require(ratio in 0.0f .. 1.0f) { "ratio must be in 0 .. 1" }
    }

    override fun render(canvas: Canvas) {
        behavior?.render(this, canvas)
    }
    override fun contains(point: Point) = behavior?.contains(this, point) ?: super.contains(point)

    private fun fireChanged() {
        updateLayout()
        changed_()
    }

    @Suppress("NAME_SHADOWING")
    private fun updateLayout() {
        val first   = firstItem
        val last    = lastItem
        val divider = divider

        val layout = when {
            first != null && last != null -> {
                constrain(first, last) { first, last ->
                    first.top    = first.parent.top    + { insets.top    }
                    first.left   = first.parent.left   + { insets.left   }
                    first.bottom = first.parent.bottom - { insets.bottom }
                    first.right  = first.left + (first.parent.width - { panelSpacing + insets.left + insets.right }) * { ratio }
                    last.top     = first.top
                    last.left    = first.right + { panelSpacing }
                    last.bottom  = first.bottom
                    last.right   = last.parent.right - { insets.right }
                }
            }

            first != null -> constrain(first, fill)
            last  != null -> constrain(last , fill)
            else -> null
        }

        layout?.let {
            if (divider != null && first != null) {
                it.constrain(divider, first) { divider, first ->
                    divider.top     = first.top
                    divider.bottom  = first.bottom
                    divider.centerX = divider.parent.left + { insets.left } + (divider.parent.width - { panelSpacing + insets.left + insets.right }) * { ratio }
                }
            }
        }

        this.layout = layout
    }
}