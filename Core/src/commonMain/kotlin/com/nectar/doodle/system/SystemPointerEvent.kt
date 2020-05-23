package com.nectar.doodle.system

import com.nectar.doodle.geometry.Point

class SystemPointerEvent(
        val type             : Type,
        val location         : Point,
        val buttons          : Set<Button>,
        val clickCount       : Int,
            modifiers        : Set<Modifier>,
        val nativeScrollPanel: Boolean = false): SystemInputEvent(modifiers) {

    constructor(
            type             : Type,
            location         : Point,
            button           : Button,
            clickCount       : Int,
            modifiers        : Set<Modifier>,
            nativeScrollPanel: Boolean = false): this(type, location, setOf(button), clickCount, modifiers, nativeScrollPanel)

    enum class Type { Up, Down, Move, Exit, Drag, Click, Enter }

    enum class Button { Button1, Button2, Button3 }
}