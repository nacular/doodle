package com.nectar.doodle.event

import com.nectar.doodle.core.View
import com.nectar.doodle.system.SystemInputEvent.Modifier


abstract class InputEvent protected constructor(source: View, val modifiers: Set<Modifier>): Event<View>(source) {

    operator fun contains(modifiers: Set<Modifier>) = modifiers.containsAll(modifiers)
    operator fun contains(modifier : Modifier     ) = modifier in modifiers
}
