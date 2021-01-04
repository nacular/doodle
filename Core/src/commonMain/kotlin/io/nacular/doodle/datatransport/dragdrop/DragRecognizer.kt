package io.nacular.doodle.datatransport.dragdrop

import io.nacular.doodle.core.View
import io.nacular.doodle.datatransport.DataBundle
import io.nacular.doodle.datatransport.dragdrop.DragOperation.Action.Copy
import io.nacular.doodle.datatransport.dragdrop.DragOperation.Action.Link
import io.nacular.doodle.datatransport.dragdrop.DragOperation.Action.Move
import io.nacular.doodle.drawing.Renderable
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.geometry.Point


/**
 * Represents the characteristics of data that is transferred from a source to a target during a drag-drop
 * sequence.
 */
interface DragOperation {
    /**
     * The set of possible drop modes for the data in [bundle]
     *
     * @property Copy An independent copy is given to the target
     * @property Move The data is moved from source to target
     * @property Link The data is linked so the source and target share it
     */
    enum class Action { Copy, Move, Link }

    /** The data to be dropped */
    val bundle: DataBundle

    /** The bundle's visual representation */
    val visual: Renderable? get() = null

    /** The offset (relative to the pointer) where the [visual] should be rendered */
    val visualOffset: Point get() = Point.Origin

    /** The set of allowed drop actions */
    val allowedActions: Set<Action>

    /**
     * Indicates that the operation is started.  This will be called once the user begins
     * dragging the pointer.
     */
    fun started() {}

    /**
     * Called when the drag operation was successfully completed.  Implementations must
     * properly handle the [Action] to ensure that data dropped on the target is correctly
     * copied, moved, or linked.
     *
     * @param action The [Action] that was used in dropping the bundle onto a target
     */
    fun completed(action: Action)

    /**
     * Called when the drag operation was not completed for some reason (i.e. the user canceled,
     * or the [Action] requested was not supported by this operation or the target where the drop
     * was attempted.
     */
    fun canceled() {}
}

/**
 * Defines how drag recognition works for [View]s.  Adding drag support to a [View] requires registering
 * a DragHandler that will determine when to initiate a [DragOperation] in response to a [PointerEvent].
 */
interface DragRecognizer {
    /**
     * Informs the recognizer that a drag gesture has ben recognized and gives it the opportunity
     * to initiate a [DragOperation].
     *
     * @param event the [PointerEvent] that initiated the drag
     * @return a [DragOperation] to begin drag-drop, or ```null``` to ignore
     */
    fun dragRecognized(event: PointerEvent): DragOperation?
}

/**
 * Helper to create a simple [DragRecognizer] that delegates to the given [handler].
 *
 * @param handler that is delegated to
 */
fun dragRecognized(handler: (event: PointerEvent) -> DragOperation?) = object: DragRecognizer {
    override fun dragRecognized(event: PointerEvent) = handler(event)
}