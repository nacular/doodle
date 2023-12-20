package io.nacular.doodle.drawing.impl

import io.nacular.doodle.dom.HTMLElement
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Point

/**
 * Created by Nicholas Eddy on 3/25/20.
 */
internal interface NativeCanvas: Canvas {
    fun addData(elements: List<HTMLElement>, at: Point = Point.Origin)
}