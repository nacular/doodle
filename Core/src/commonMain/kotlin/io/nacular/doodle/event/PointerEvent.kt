package io.nacular.doodle.event

import io.nacular.doodle.core.Internal
import io.nacular.doodle.core.View
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.system.SystemInputEvent.Modifier
import io.nacular.doodle.system.SystemPointerEvent
import io.nacular.doodle.system.SystemPointerEvent.Button
import io.nacular.doodle.system.SystemPointerEvent.Type


public class Pointer internal constructor(internal val id: Int, public val target: View?, public val state: Type, public val location: Point)

public class PointerEvent internal constructor(
                   source           : View,
        public val target           : View,
        public val buttons          : Set<Button>,
        public val clickCount       : Int,
        public val targetPointers   : List<Pointer>,
        public val changedPointers  : Set<Pointer>,
                   allActivePointers: () -> List<Pointer>,
                   modifiers        : Set<Modifier>): InputEvent(source, modifiers) {

    public val allActivePointers: List<Pointer> by lazy { allActivePointers() }

    public val type    : Type  get() = targetPointers.first().state
    public val location: Point get() = targetPointers.first().location

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
