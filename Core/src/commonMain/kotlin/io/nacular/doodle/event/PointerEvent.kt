package io.nacular.doodle.event

import io.nacular.doodle.core.View
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.system.SystemInputEvent.Modifier
import io.nacular.doodle.system.SystemPointerEvent.Button
import io.nacular.doodle.system.SystemPointerEvent.Type


class PointerEvent(
        source        : View,
        val target    : View,
        val type      : Type,
        val location  : Point,
        val buttons   : Set<Button>,
        val clickCount: Int,
        modifiers     : Set<Modifier>): InputEvent(source, modifiers) {

    constructor(
            source    : View,
            target    : View,
            type      : Type,
            location  : Point,
            button    : Button,
            clickCount: Int,
            modifiers : Set<Modifier>): this(source, target, type, location, setOf(button), clickCount, modifiers)

    override fun toString() = "${this::class.simpleName} -> ${source::class.simpleName}: $type $location $buttons"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PointerEvent) return false
        if (!super.equals(other)) return false

        if (type       != other.type      ) return false
        if (location   != other.location  ) return false
        if (buttons    != other.buttons   ) return false
        if (clickCount != other.clickCount) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + type.hashCode    ()
        result = 31 * result + location.hashCode()
        result = 31 * result + buttons.hashCode ()
        result = 31 * result + clickCount
        return result
    }
}

fun PointerEvent.with(source: View) = PointerEvent(source, target, type, location, buttons, clickCount, modifiers)
