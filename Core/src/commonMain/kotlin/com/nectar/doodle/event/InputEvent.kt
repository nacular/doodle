package com.nectar.doodle.event

import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.system.SystemInputEvent.Modifier


abstract class InputEvent protected constructor(source: Gizmo, val modifiers: Set<Modifier>): Event<Gizmo>(source) {

    operator fun contains(modifiers: Set<Modifier>) = modifiers.containsAll(modifiers)
    operator fun contains(modifier : Modifier) = modifier in modifiers
}
