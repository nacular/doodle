package com.nectar.doodle.event

import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.geometry.Point


class MouseEvent(
        source        : Gizmo,
        val type      : Type,
        val location  : Point,
        val buttons   : Set<Button>,
        val clickCount: Int,
        modifiers     : Set<Modifier>) : InputEvent(source, modifiers) {

    constructor(
            source    : Gizmo,
            type      : Type,
            location  : Point,
            button    : Button,
            clickCount: Int,
            modifiers : Set<Modifier>): this(source, type, location, setOf(button), clickCount, modifiers)

    enum class Type { Up, Down, Move, Exit, Drag, Click, Enter }

    enum class Button { Button1, Button2, Button3 }
}
