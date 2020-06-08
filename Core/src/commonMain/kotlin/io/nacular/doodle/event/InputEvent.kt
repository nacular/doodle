package io.nacular.doodle.event

import io.nacular.doodle.core.View
import io.nacular.doodle.system.SystemInputEvent.Modifier


abstract class InputEvent protected constructor(source: View, val modifiers: Set<Modifier>): Event<View>(source) {
    operator fun contains(modifiers: Set<Modifier>) = modifiers.containsAll(modifiers)
    operator fun contains(modifier : Modifier     ) = modifier in modifiers
}
