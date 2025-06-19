package io.nacular.doodle.drawing.impl

import io.nacular.doodle.dom.Event
import io.nacular.doodle.dom.MouseEvent

internal fun isKeyboardClick(event: Event) = event is MouseEvent && event.detail <= 0

internal val lineBreakRegex = "(\r\n|\r|\n)".toRegex()