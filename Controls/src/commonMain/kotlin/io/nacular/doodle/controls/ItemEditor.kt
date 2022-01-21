package io.nacular.doodle.controls

import io.nacular.doodle.core.View

/**
 * Represents an ongoing edit operation of some View. The operation
 * defines any View associated with the edit and the data that gets
 * returned when completed.
 */
public interface EditOperation<T> {
    /**
     * Called to initiate the edit. This allows an optional View
     * to be used within the editing control as a replacement for
     * the existing item being edited. A good example is returning
     * a [TextField][io.nacular.doodle.controls.text.TextField] when editing some text. The text field would be
     * inserted into the editing control in place of the text while
     * editing happens.
     *
     * Some editing does not happen in place, using a separate mechanism
     * entirely (like a pop-up). These are scenarios when `null` might
     * be returned to indicate the editing item should remain in place.
     *
     * @return an optional view to replace the item being edited
     */
    public operator fun invoke(): View?

    /**
     * Called when editing is requested to complete. This can happen if the
     * user triggers a key or mouse event, of if the control being edited
     * needs to transition away from editing and would like to get a result.
     *
     * @return a result if one is ready
     */
    public fun complete(): Result<T>

    /**
     * Called to cancel editing and allow the operation to clean up. Any
     * View returned by [invoke] will be removed from the editing control
     * at this point.
     */
    public fun cancel()
}