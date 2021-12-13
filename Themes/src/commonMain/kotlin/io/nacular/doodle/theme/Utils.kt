package io.nacular.doodle.theme

import io.nacular.doodle.drawing.Paint

/**
 * Used to transform a [Paint], typically helpful to change the look of an item that
 * uses the paint for fills or strokes.
 */
public typealias PaintMapper = (Paint) -> Paint
