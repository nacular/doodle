package io.nacular.doodle.controls

import io.nacular.doodle.core.Internal
import io.nacular.doodle.core.InternalDisplay
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.RenderManager
import io.nacular.doodle.geometry.Point.Companion.Origin
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.constraints.Bounds
import io.nacular.doodle.layout.constraints.ConstraintDslContext
import io.nacular.doodle.layout.constraints.constrain
import io.nacular.doodle.utils.RelativePositionMonitor
import kotlin.math.abs

/**
 * Provides a robust way to display temporary top-level [View]s that can be anchored to others. This
 * makes it easy to show things like drop-downs, modals, or tool tips.
 */
public interface PopupManager {
    /**
     * Shows [view] as though it is a pop-up, that means it is placed atop all other views in the app.
     *
     * ```
     * show(view) {
     *     it.center eq parent.center
     *
     *     // more constraints to avoid popup going outside parent if desired
     * }
     * ```
     * @param view to be shown
     * @param constraints used to position [view]
     * @return the same [view] provided
     */
    public fun show(view: View, constraints: ConstraintDslContext.(Bounds) -> Unit): View

    /**
     * Shows [view] as though it is a pop-up, that means it is placed atop all other views in the app.
     * The given constraints include details about the bounds of [relativeTo], which allows [view] to be positioned
     * relative to it.
     *
     * ```
     * show(view, other) { popup, anchor ->
     *     popup.top     eq anchor.bottom + 2
     *     popup.centerX eq anchor.center.x
     *
     *     // more constraints to avoid popup going outside parent if desired
     * }
     * ```
     *
     * @param view to be shown
     * @param relativeTo is the View the popup will be positioned relative to
     * @param constraints used to position [view]
     * @return the same [view] provided
     */
    public fun show(view: View, relativeTo: View, constraints: ConstraintDslContext.(Bounds, Rectangle) -> Unit): View

    /**
     * Trigger re-layout of [view] if it is currently [active].
     *
     * @param view to act on
     */
    public fun relayout(view: View)

    /**
     * Hides [view] if it was previously displayed using [show]
     */
    public fun hide(view: View)

    /**
     * Indicates whether [view] is currently being shown via [show]. Calling [hide] on a View
     * will mean this method returns `false` for it.
     *
     * @param view to test
     * @return `true` IFF the view is currently being displayed by a call to [show]
     */
    public fun active(view: View): Boolean
}

/** @suppress */
@Internal
public class PopupManagerImpl(
    private val display      : InternalDisplay,
    private val renderManager: RenderManager,
    private val boundsMonitor: RelativePositionMonitor
): PopupManager {
    private abstract inner class Popup(val view: View) {
        private var layingOut   = false
        private var needsLayout = false

        fun relayout() {
            if (!layingOut) {
                layingOut = true
                doLayout()
                layingOut = false
                if (needsLayout) {
                    needsLayout = false
                    relayout()
                }
            } else {
                needsLayout = true
            }
        }

        abstract fun doLayout()

        fun show() {
            display.showPopup(view)
        }

        open fun discard() {
            display.hidePopup(view)
        }
    }

    private inner class UnboundedPopup(view: View, val constraints: ConstraintDslContext.(Bounds) -> Unit): Popup(view) {
        private val viewList = listOf(view.positionable)

        override fun doLayout() {
            viewList.constrain(constraints) { _,_ ->
                Rectangle(size = display.size)
            }
        }
    }

    private inner class BoundedPopup(view: View, val relativeTo: View, val constraints: ConstraintDslContext.(Bounds, Rectangle) -> Unit): Popup(view) {
        private val viewList       = sequenceOf(view.positionable)
        lateinit var relativeBounds: Rectangle
        private val constraint: ConstraintDslContext.(Bounds) -> Unit = {
            constraints(it, relativeBounds)
        }

        private val layout = constrain(view, constraint)

        private val monitor = { _: View, _: Rectangle, _: Rectangle ->
            calculateRelativeBounds()
            relayout()
        }

        override fun doLayout() {
            if (!::relativeBounds.isInitialized) {
                calculateRelativeBounds()
            }

            layout.layout(viewList, Size.Empty, display.size, display.size)
        }

        init {
            boundsMonitor[relativeTo] += monitor
        }

        override fun discard() {
            super.discard()

            boundsMonitor[relativeTo] -= monitor
            layout.unconstrain(view, constraint)
        }

        private fun calculateRelativeBounds() {
            relativeBounds = Rectangle(display.fromAbsolute(relativeTo.toAbsolute(Origin)), relativeTo.size)
        }
    }

    private val popups = LinkedHashMap<View, Popup>()

    init {
        display.sizeChanged += { _,_,_ ->
            popups.values.forEach { it.relayout() }
        }
    }

    override fun show(view: View, constraints: ConstraintDslContext.(Bounds) -> Unit): View = view.also {
        showInternal(view) { UnboundedPopup(it, constraints) }
    }

    override fun show(
        view       : View,
        relativeTo : View,
        constraints: ConstraintDslContext.(popUp: Bounds, anchor: Rectangle) -> Unit
    ): View = view.also {
        showInternal(view) { BoundedPopup(it, relativeTo, constraints) }
    }

    override fun relayout(view: View) {
        popups[view]?.relayout()
    }

    override fun hide(view: View) {
        popups.remove(view)?.let {
            it.discard()
            view.parentChange  -= parentChanged
            view.boundsChanged -= boundsChanged
            view.displayChange -= displayChanged
            renderManager.popupHidden(view)
        }
    }

    override fun active(view: View): Boolean = popups.containsKey(view)

    private val parentChanged = { view: View, _: View?, new: View? ->
        if (new != null) {
            hide(view)
        }
    }

    private val displayChanged = { view: View, _: Boolean, displayed: Boolean ->
        if (!displayed) {
            hide(view)
        }
    }

    private val boundsChanged = { view: View, old: Rectangle, new: Rectangle ->
        if (old.size sufficientlyDifferentTo new.size) {
            popups[view]?.relayout().let {}
        }
    }

    private infix fun Size.sufficientlyDifferentTo(other: Size): Boolean {
        val epsilon = 1e-8

        return abs(width - other.width) > epsilon || abs(height - other.height) > epsilon
    }

    private fun showInternal(view: View, block: (View) -> Popup) {
        hide(view)

        val popUp = block(view)

        popups[view] = popUp // make sure registered as active before adding to display
        popUp.show()

        renderManager.popupShown(view)

        view.parentChange  += parentChanged
        view.boundsChanged += boundsChanged
        view.displayChange += displayChanged

        popUp.relayout()
    }
}