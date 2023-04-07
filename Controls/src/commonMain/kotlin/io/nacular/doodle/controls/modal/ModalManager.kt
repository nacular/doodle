package io.nacular.doodle.controls.modal

import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Paint
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.event.PointerMotionListener
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.layout.constraints.Bounds
import io.nacular.doodle.layout.constraints.ConstraintDslContext
import io.nacular.doodle.utils.Pool

/**
 * Provides a way to create Modals which are specialized pop-ups that block until some value can be produced.
 * Modals are great for getting user input and preventing interaction with the app until the user has responded.
 */
public interface ModalManager {
    /**
     * Configures a modal that is shown via[invoke].
     *
     * @property background determines how an overlay on items below the modal is filled
     * @property pointerOutsideModalChanged allows listening to pointer events outside the modal
     * @property pointerMotionOutsideModalChanged allows listening to pointer-motion events outside the modal
     */
    public interface ModalContext<T> {
        /**
         * Paint used to fill the background behind the modal View.
         */
        public var background : Paint

        /** Notified whenever Pointer events occur outside the modal View */
        public val pointerOutsideModalChanged: Pool<PointerListener>

        /** Notified whenever Pointer motion events occur outside the modal View */
        public val pointerMotionOutsideModalChanged: Pool<PointerMotionListener>

        /**
         * Causes the modal's layout to be recalculated. This is useful when
         * trying to animate a modal's position.
         */
        public fun reLayout()

        /**
         * Called when the modal should dismiss with the given [result].
         *
         * @param result that will be returned to the modal creator via [invoke]
         */
        public fun completed(result: T)
    }

    public sealed class ModalType(internal val view: View)

    /**
     * A Modal that is positioned relative to the Display only.
     *
     * ```
     * Modal(view) {
     *     it.bottom  eq parent.bottom - 10
     *     it.centerX eq button.center.x
     * }
     * ```
     *
     * @param view to display as a Modal
     * @param layout controls the modal's positioning within its parent
     */
    public class Modal(
        view: View,
        internal val layout: ConstraintDslContext.(Bounds) -> Unit = { it.center eq parent.center }
    ): ModalType(view)

    /**
     * A Modal that is positioned relative to another View and the Display.
     *
     * ```
     * RelativeModal(view, relativeTo = button) { modal, button ->
     *     modal.top     eq bottom.bottom + 10
     *     modal.centerX eq button.center.x
     * }
     * ```
     *
     * @param view to display as a Modal
     * @param relativeTo another View (represented in the layout as the second parameter
     * @param layout controls the modal's positioning within its parent
     */
    public class RelativeModal(
        view: View,
        internal val relativeTo: View,
        internal val layout: ConstraintDslContext.(Bounds, Rectangle) -> Unit = { modal, _ -> modal.center eq parent.center }
    ): ModalType(view)

    /**
     * Creates a modal that blocks until it has input and generates a result [T].
     *
     * @param contents to display within the modal, and a way to indicate when a result is ready
     * @return result [T] from completed
     */
    public suspend operator fun <T> invoke(contents: ModalContext<T>.() -> ModalType): T
}