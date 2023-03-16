package io.nacular.doodle.examples

import io.nacular.doodle.core.View
import io.nacular.doodle.event.Interaction

fun Interaction.inParent(child: View) = when (val p = child.parent) {
    null -> child.toAbsolute(location)
    else -> p.toLocal(location, child)
}