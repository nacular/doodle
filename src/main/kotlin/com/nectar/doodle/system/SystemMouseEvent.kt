package com.nectar.doodle.system

import com.nectar.doodle.geometry.Point

class SystemMouseEvent(
        val type      : Type,
        val location  : Point,
        val buttons   : Set<Button>,
        val clickCount: Int,
        modifiers     : Set<Modifier>): SystemInputEvent(modifiers) {

    constructor(
            type      : Type,
            location  : Point,
            button    : Button,
            clickCount: Int,
            modifiers : Set<Modifier>): this(type, location, setOf(button), clickCount, modifiers)

    enum class Type { Up, Down, Move, Exit, Drag, Click, Enter }

    enum class Button { Button1, Button2, Button3 }
}