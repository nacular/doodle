package io.nacular.doodle.controls.panels

import io.nacular.doodle.controls.theme.SplitPanelBehavior
import io.nacular.doodle.core.View
import io.nacular.doodle.core.behavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.constraints.constrain
import io.nacular.doodle.layout.constraints.fill
import io.nacular.doodle.utils.ChangeObservers
import io.nacular.doodle.utils.ChangeObserversImpl
import io.nacular.doodle.utils.Orientation
import io.nacular.doodle.utils.Orientation.Vertical
import io.nacular.doodle.utils.observable

/**
 * A control that divides a region into two areas, each occupied by a [View]. It also allows the user to change the portion of its viewport
 * dedicated to either view.
 *
 * The panel can hold up to 2 items. It will distribute the visible region between them based on [ratio]. A single item in the panel will
 * get the entire region to itself; meaning [ratio] is effectively ignored in this case.
 *
 * @param orientation of the panel's items, and the divider that might separate them
 * @param ratio of space given to [firstItem], must be within `0 .. 1`.
 */
public class SplitPanel(orientation: Orientation = Vertical, ratio: Float = 0.5f): View() {

    /**
     * Controls the look and behavior of the panel, including the divider it uses.
     */
    public var behavior: SplitPanelBehavior? by behavior(afterChange = { _,new ->
        divider = new?.divider(this)
    })

    /**
     * Item to one side of the divider.
     */
    public var firstItem: View? by observable(null) { old,new ->
        old?.let { children -= it }
        new?.let { children += it }

        updateLayout    ()
        contentsChanged_()
    }

    /**
     * Item to the other side of the divider.
     */
    public var lastItem: View? by observable(null) { old,new ->
        old?.let { children -= it }
        new?.let { children += it }

        updateLayout()
        contentsChanged_()
    }

    @Suppress("PrivatePropertyName")
    private val contentsChanged_ = ChangeObserversImpl(this)

    public val contentsChanged: ChangeObservers<SplitPanel> = contentsChanged_

    @Suppress("PrivatePropertyName")
    private val orientationChanged_ = ChangeObserversImpl(this)

    public val orientationChanged: ChangeObservers<SplitPanel> = orientationChanged_

    public var orientation: Orientation by observable(orientation) { _,_ ->
        updateLayout()
        orientationChanged_()
    }

    /**
     * The fraction of space given to [firstItem], if it is present.
     */
    public var ratio: Float = ratio; set(new) { if (new != field) { field = new; relayout(); changed_() } }

    private var divider: View? by observable(null) { old, new ->
        old?.let { children -= it }

        new?.also {
            children += it
            it.zOrder = 1

            updateLayout()
        }
    }

    @Suppress("PrivatePropertyName")
    private val changed_ = ChangeObserversImpl(this)

    public val changed: ChangeObservers<SplitPanel> = changed_

    public override var insets: Insets
        get(   ) = super.insets
        set(new) { if (new != super.insets) { super.insets = new; relayout() } }

    init {
        require(ratio in 0.0f .. 1.0f) { "ratio must be in 0 .. 1" }
    }

    override fun render(canvas: Canvas) {
        behavior?.render(this, canvas)
    }

    override fun contains(point: Point): Boolean = super.contains(point) && behavior?.contains(this, point) ?: true

    private fun panelSpacing(): Double = when (behavior?.dividerVisible) {
        true -> divider?.let {
            when (orientation) {
                Vertical -> it.width
                else     -> it.height
            }
        } ?: 0.0
        else -> 0.0
    }

    @Suppress("NAME_SHADOWING")
    private fun updateLayout() {
        val first   = firstItem
        val last    = lastItem
        val divider = divider

        val layout = when {
            first != null && last != null -> {
                constrain(first, last) { first, last ->
                    when (orientation) {
                        Vertical -> {
                            first.top    eq insets.top
                            first.left   eq insets.left
                            first.right  eq first.left + (parent.width.readOnly - (panelSpacing() + insets.right)) * ratio
                            first.bottom eq parent.bottom.readOnly - insets.bottom
                            last.top     eq first.top
                            last.left    eq first.right + panelSpacing()
                            last.right   eq parent.right.readOnly - insets.right
                            last.bottom  eq first.bottom
                        }
                        else -> {
                            first.top    eq insets.top
                            first.left   eq insets.left
                            first.right  eq parent.right.readOnly - insets.right
                            first.bottom eq first.top + (parent.height.readOnly - (panelSpacing() + insets.bottom)) * ratio
                            last.top     eq first.bottom + panelSpacing()
                            last.left    eq first.left
                            last.right   eq first.right
                            last.bottom  eq parent.bottom.readOnly - insets.bottom
                        }
                    }
                }
            }

            first != null -> constrain(first) { fill(it) }
            last  != null -> constrain(last ) { fill(it) }
            else -> null
        }

        layout?.let {
            if (divider != null && first != null) {
                it.constrain(divider, first) { divider, first ->
                    when (orientation) {
                        Vertical -> {
                            divider.top     eq first.top
                            divider.bottom  eq first.bottom
                            divider.centerX eq first.right + panelSpacing() / 2
                        }
                        else -> {
                            divider.left    eq first.left
                            divider.right   eq first.right
                            divider.centerY eq first.bottom + panelSpacing() / 2
                        }
                    }
                }
            }
        }

        this.layout = layout
    }
}