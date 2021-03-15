package io.nacular.doodle.event

import io.nacular.doodle.core.Internal
import io.nacular.doodle.core.View
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.system.SystemInputEvent.Modifier
import io.nacular.doodle.system.SystemPointerEvent
import io.nacular.doodle.system.SystemPointerEvent.Button
import io.nacular.doodle.system.SystemPointerEvent.Type

/**
 * Represents an input device (Mouse, Pen, Touch, etc.) that is actively interacting
 * with a View.
 *
 * @constructor
 * @param id of the Pointer that uniquely identifies it throughout its interaction
 * @param target the Pointer is interacting with
 * @param state the Pointer is in
 * @param location of the Pointer within its [target]
 *
 * @property id of the Pointer that uniquely identifies it throughout its interaction
 * @property target the Pointer is interacting with
 * @property state the Pointer is in
 * @property location of the Pointer within its [target]
 */
public class Pointer internal constructor(internal val id: Int, public val target: View, public val state: Type, public val location: Point)

/**
 * Event triggered when a pointing device (Mouse, Pen, Touch, etc.) interacts with a View.
 *
 * @constructor
 * @param source receiving the notification
 * @param target where the Pointer interaction occurred
 * @param buttons that are pressed (applicable for Mouse)
 * @param targetPointers active Pointers that started within the [target]
 * @param changedPointers active Pointers that changed since the last event
 * @param allActivePointers that are currently active (even those not directed at the [target])
 * @param modifiers that are pressed
 *
 * @property target where the Pointer interaction occurred
 * @property buttons that are pressed (applicable for Mouse)
 * @property targetPointers active Pointers that started within the [target]
 * @property changedPointers active Pointers that changed since the last event
 */
public class PointerEvent internal constructor(
                   source           : View,
        public val target           : View,
        public val buttons          : Set<Button>,
        public val clickCount       : Int,
        public val targetPointers   : List<Pointer>,
        public val changedPointers  : Set<Pointer>,
                   allActivePointers: () -> List<Pointer>,
                   modifiers        : Set<Modifier>): InputEvent(source, modifiers) {

    /** Pointers that are currently active (even those not directed at the [target]) */
    public val allActivePointers: List<Pointer> by lazy { allActivePointers() }

    /** Type of the first item in [changedPointers] */
    public val type: Type  get() = changedPointers.first().state

    /** Location of the first item in [changedPointers] */
    public val location: Point get() = changedPointers.first().location

    public companion object {
        @Internal
        public operator fun invoke(target: View, event: SystemPointerEvent): PointerEvent {
            val pointers = listOf(Pointer(event.id, target, event.type, target.fromAbsolute(event.location)))

            return PointerEvent(target,
                                target,
                                event.buttons,
                                event.clickCount,
                                targetPointers    = pointers,
                                changedPointers   = pointers.toSet(),
                                allActivePointers = { pointers },
                                modifiers         = event.modifiers)
        }
    }
}

internal fun PointerEvent.with(source: View) = PointerEvent(source, target, buttons, clickCount, targetPointers, changedPointers, { allActivePointers }, modifiers)
