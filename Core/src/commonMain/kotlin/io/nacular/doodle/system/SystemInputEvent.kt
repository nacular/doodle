package io.nacular.doodle.system

import io.nacular.doodle.event.Event

public abstract class SystemInputEvent internal constructor(public val modifiers: Set<Modifier>): Event<Any?>(null) {

    public operator fun contains(modifiers: Set<Modifier>): Boolean = this.modifiers.containsAll(modifiers)
    public operator fun contains(modifier : Modifier     ): Boolean = modifier in modifiers

    public enum class Modifier {
        Alt, Ctrl, Shift, Meta
    }
}