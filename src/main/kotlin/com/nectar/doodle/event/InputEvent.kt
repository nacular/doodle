package com.nectar.doodle.event

import com.nectar.doodle.core.Gizmo


abstract class InputEvent protected constructor(source: Gizmo, private val modifiers: Set<Modifier>) : Event<Gizmo>(source) {

    operator fun contains(modifiers: Set<Modifier>) = this.modifiers.containsAll(modifiers)
    operator fun contains(modifier : Modifier     ) = modifier in modifiers

    enum class Modifier {
        Alt, Ctrl, Shift
    }
}
