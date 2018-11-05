package com.nectar.doodle.event

import com.nectar.doodle.core.View

/**
 * Represents an event triggered by a focus traversal to/from a [View].
 * [View]s will receive symmetric events when focus moves between them;
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
 * @param companion The View from/to which focus moved (may be null).
 */
constructor(source: View, val type: Type, val companion: View?): Event<View>(source) {
    enum class Type {
        Gained, Lost
    }
}