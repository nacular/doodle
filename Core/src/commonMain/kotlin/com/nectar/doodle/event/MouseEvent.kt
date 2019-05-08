package com.nectar.doodle.event

import com.nectar.doodle.core.View
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.system.SystemInputEvent.Modifier
import com.nectar.doodle.system.SystemMouseEvent.Button
import com.nectar.doodle.system.SystemMouseEvent.Type


class MouseEvent(
        source        : View,
        val type      : Type,
        val location  : Point,
        val buttons   : Set<Button>,
        val clickCount: Int,
        modifiers     : Set<Modifier>): InputEvent(source, modifiers) {

    constructor(
            source    : View,
            type      : Type,
            location  : Point,
            button    : Button,
            clickCount: Int,
            modifiers : Set<Modifier>): this(source, type, location, setOf(button), clickCount, modifiers)

    override fun toString() = "${this::class.simpleName} -> ${source::class.simpleName}: $type $location $buttons"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MouseEvent) return false
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
