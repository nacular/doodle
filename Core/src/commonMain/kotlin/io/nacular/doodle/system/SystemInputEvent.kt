package io.nacular.doodle.system

import io.nacular.doodle.event.Event

abstract class SystemInputEvent protected constructor(val modifiers: Set<Modifier>): Event<Any?>(null) {

    operator fun contains(modifiers: Set<Modifier>) = this.modifiers.containsAll(modifiers)
    operator fun contains(modifier : Modifier     ) = modifier in modifiers

    enum class Modifier {
        Alt, Ctrl, Shift, Meta
    }
}