package io.nacular.doodle.event

import io.nacular.doodle.core.View
import io.nacular.doodle.system.SystemInputEvent.Modifier


public abstract class InputEvent internal constructor(source: View, public val modifiers: Set<Modifier>): Event<View>(source) {
    public operator fun contains(modifiers: Set<Modifier>): Boolean = modifiers.containsAll(modifiers)
    public operator fun contains(modifier : Modifier     ): Boolean = modifier in modifiers
}
