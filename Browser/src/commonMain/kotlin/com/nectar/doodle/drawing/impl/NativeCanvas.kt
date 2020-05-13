package com.nectar.doodle.drawing.impl

import com.nectar.doodle.HTMLElement
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.geometry.Point

/**
 * Created by Nicholas Eddy on 3/25/20.
 */
internal interface NativeCanvas: Canvas {
    fun addData(elements: List<HTMLElement>, at: Point = Point.Origin)
}