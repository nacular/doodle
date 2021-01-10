package io.nacular.doodle.event

import io.nacular.doodle.core.View
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.system.SystemInputEvent.Modifier
import io.nacular.doodle.system.SystemPointerEvent
import io.nacular.doodle.system.SystemPointerEvent.Button
import io.nacular.doodle.system.SystemPointerEvent.Type


open class PointerEvent internal constructor(
        source        : View,
        val target    : View,
        val type      : Type,
        val location  : Point,
        val buttons   : Set<Button>,
        val clickCount: Int,
        modifiers     : Set<Modifier>): InputEvent(source, modifiers) {

    internal constructor(
            source    : View,
            target    : View,
            type      : Type,
            location  : Point,
            button    : Button,
            clickCount: Int,
            modifiers : Set<Modifier>): this(source, target, type, location, setOf(button), clickCount, modifiers)

    companion object {
        operator fun invoke(target: View, systemPointerEvent: SystemPointerEvent) = PointerEvent(
            target,
            target,
            systemPointerEvent.type,
            target.fromAbsolute(systemPointerEvent.location),
            systemPointerEvent.buttons,
            systemPointerEvent.clickCount,
            systemPointerEvent.modifiers)
    }
}

internal fun PointerEvent.with(source: View) = PointerEvent(source, target, type, location, buttons, clickCount, modifiers)
