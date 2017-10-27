package com.nectar.doodle.core

import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Size


interface Renderable {

    val size: Size

    fun render(canvas: Canvas, point: Point)
}
