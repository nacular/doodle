package com.zinoti.jaz.event

import com.zinoti.jaz.core.Gizmo
import com.zinoti.jaz.geometry.Point


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
