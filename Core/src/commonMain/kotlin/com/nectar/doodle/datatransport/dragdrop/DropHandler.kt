package com.nectar.doodle.datatransport.dragdrop

import com.nectar.doodle.core.View
import com.nectar.doodle.datatransport.DataBundle
import com.nectar.doodle.datatransport.dragdrop.DragOperation.Action
import com.nectar.doodle.drawing.Renderable
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Point.Companion.Origin

interface DragOperation {
    enum class Action { Copy, Move, Link }

    val bundle        : DataBundle
    val visual        : Renderable? get() = null
    val visualOffset  : Point get() = Origin
    val allowedActions: Set<Action>

    fun completed(action: Action)
    fun canceled (              ) {}
}

class DropEvent(val view: View, val location: Point, val bundle: DataBundle, val action: Action?)

data class DropCompleteEvent(val succeeded: Boolean, val action: Action)

interface DragHandler {
    /**
     * Informs the handler that a drag gesture has ben recognized and gives it the opportunity
     * to initiate a dragdrop operation.
     *
     * @param event the [[MouseEvent]] that initiated the drag
     * @return a [[DragOperation]] to begin dragdrop, or ```null``` to ignore
     */
    fun dragRecognized(event: MouseEvent): DragOperation?
}

interface DropHandler {
    val active: Boolean

    /**
     * Informs handler that the drop operation has ended
     *
     * @param event The event
     */
    fun drop(event: DropEvent): Boolean

    /**
     * Informs handler that the cursor has moved onto a new drop handler
     *
     * @param event The event
     * @return ```true``` if the drop is allowed
     */
    fun dropEnter(event: DropEvent): Boolean

    /**
     * Informs handler that the cursor has moved out of the previous
     * drop handler or that the drop handler has become inactive
     *
     * @param event The event
     */
    fun dropExit(event: DropEvent) {}

    /**
     * Informs handler that the cursor has moved while over the current
     * drop handler
     *
     * @param event The event
     * @return ```true``` if the drop is allowed
     */
    fun dropOver(event: DropEvent): Boolean

    /**
     * Informs handler that the user has selected a new action
     * for this drop operation
     *
     * @param event The event
     * @return ```true``` if the drop is allowed
     */
    fun dropActionChanged(event: DropEvent): Boolean
}