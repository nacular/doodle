package com.nectar.doodle.drawing

import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Polygon
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.image.Image
import com.nectar.doodle.text.StyledText

/**
 * Created by Nicholas Eddy on 10/23/17.
 */
interface Renderer {
    fun clear()
    fun flush()

    fun line(point1: Point, point2: Point, pen: Pen)

    fun path(points: List<Point>, pen: Pen)

    fun poly(polygon: Polygon, pen: Pen, brush: Brush? = null)
    fun poly(polygon: Polygon, brush: Brush)

    fun arc(center: Point, radius: Double, sweep: Double, rotation: Double, pen: Pen, brush: Brush? = null)
    fun arc(center: Point, radius: Double, sweep: Double, rotation: Double, brush: Brush)

    fun text(text: String, font: Font? = null, at: Point, brush: Brush)

    fun text(text: StyledText, at: Point)

    fun clipped(
            text    : String,
            font    : Font,
            point   : Point,
            clipRect: Rectangle,
            brush   : Brush)

    fun wrapped(
            text       : String,
            font       : Font,
            point      : Point,
            leftMargin : Double,
            rightMargin: Double,
            brush      : Brush)

    fun wrapped(
            text       : StyledText,
            point      : Point,
            leftMargin : Double,
            rightMargin: Double)

    fun image(image: Image, source: Rectangle, destination: Rectangle, opacity: Float = 1f)

    enum class Optimization {
        Speed,
        Quality
    }
}