package com.nectar.doodle.controls.panels

import com.nectar.doodle.controls.ChangeObservers
import com.nectar.doodle.controls.ChangeObserversImpl
import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.layout.Constraints
import com.nectar.doodle.layout.Insets
import com.nectar.doodle.layout.constrain
import com.nectar.doodle.utils.Orientation
import com.nectar.doodle.utils.Orientation.Horizontal


class SplitPanel(orientation: Orientation? = Horizontal, ratio: Float = 0.5f): Gizmo() {

    var firstItem: Gizmo? = null
        set(new) {
            if (new == field) {
                return
            }

            field?.let { children -= it }

            field  = new

            field?.let { children.add(0, it) }

            fireChanged()
        }

    var lastItem: Gizmo? = null
        set(new) {
            if (new == field) {
                return
            }

            field?.let { children -= it }

            field  = new

            field?.let { children.add(1, it) }

            fireChanged()
        }

    var orientation = orientation
        set(new) { if (new != field) fireChanged().also { field = new } }

    var ratio = ratio
        set(new) { if (new != field) fireChanged().also { field = new } }

    @Suppress("PrivatePropertyName")
    private val onChanged_ = ChangeObserversImpl<SplitPanel>()

    val onChanged: ChangeObservers<SplitPanel> = onChanged_

    var panelSpacing = 0.0
        set(new) { if (new != field) fireChanged().also { field = new } }

    public override var insets
        get() = super.insets
        set(new) { if (new != super.insets) fireChanged().also { super.insets = new } }

    init {
        this.ratio = (if (ratio >= 0) ratio else 0.5f)
    }

    private fun fireChanged() {
        updateLayout()
        onChanged_.set.forEach { it(this) }
    }

    private fun updateLayout() {
        val first = firstItem
        val last  = lastItem

        val fill: (Constraints, Insets) -> Unit = { gizmo, insets ->
            gizmo.top    = gizmo.parent.top    + { insets.top    }
            gizmo.left   = gizmo.parent.left   + { insets.left   }
            gizmo.bottom = gizmo.parent.bottom + { insets.bottom }
            gizmo.right  = gizmo.parent.right  + { insets.right  }
        }

        layout = when {
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
    }
}