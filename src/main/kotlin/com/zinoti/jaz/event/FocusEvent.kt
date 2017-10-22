package com.zinoti.jaz.event

import com.zinoti.jaz.core.Gizmo

/**
 * Represents an event triggered by a focus traversal to/from a Gizmo.
 * Gizmos will receive symmetric events when focus moves between them;
 * A will receive a focus lost event with B as a companion, then B will
 * receive a focus gained event with A as the companion.  Only one event
 * is generated if focus moves in/out of system scope.  In this case the
 * companion will be null.
 */

class FocusEvent
/**
 * Constructs a FocusEvent.
 *
 * @param source    The source of the event
 * @param type      The type of focus event: gained/lost.
 * @param companion The Gizmo from/to which focus moved (may be null).
 */
constructor(source: Gizmo, val type: Type, val companion: Gizmo?): Event<Gizmo>(source) {
    enum class Type {
        Gained, Lost
    }
}