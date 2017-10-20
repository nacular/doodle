package com.zinoti.jaz.core

import com.zinoti.jaz.drawing.Canvas
import com.zinoti.jaz.geometry.Point
import com.zinoti.jaz.geometry.Size


interface Renderable {

    val size: Size

    fun render(canvas: Canvas, point: Point)
}
