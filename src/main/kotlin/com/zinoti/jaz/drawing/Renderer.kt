package com.zinoti.jaz.drawing

import com.zinoti.jaz.geometry.Circle
import com.zinoti.jaz.geometry.Ellipse
import com.zinoti.jaz.geometry.Point
import com.zinoti.jaz.geometry.Polygon
import com.zinoti.jaz.geometry.Rectangle
import com.zinoti.jaz.image.Image

/**
 * Created by Nicholas Eddy on 10/23/17.
 */
interface Renderer {
    fun clear()
    fun flush()

    fun rect(rectangle: Rectangle, pen  : Pen, brush: Brush? = null)
    fun rect(rectangle: Rectangle, brush: Brush                    )

    fun rect(rectangle: Rectangle, radius: Double, pen: Pen, brush: Brush? = null)
    fun rect(rectangle: Rectangle, radius: Double, brush: Brush)

    fun line(point1: Point, point2: Point, pen: Pen)

    fun path(points: List<Point>, pen: Pen)

    fun poly(polygon: Polygon, pen: Pen, brush: Brush? = null)
    fun poly(polygon: Polygon, brush: Brush)

    fun arc(center: Point, radius: Double, sweep: Double, rotation: Double, pen: Pen, brush: Brush? = null)
    fun arc(center: Point, radius: Double, sweep: Double, rotation: Double, brush: Brush)

    fun circle(circle: Circle, pen: Pen, brush: Brush? = null)
    fun circle(circle: Circle, brush: Brush)

    fun ellipse(ellipse: Ellipse, pen: Pen, brush: Brush? = null)
    fun ellipse(ellipse: Ellipse, brush: Brush)

    fun text(text: String, font: Font, at: Point, brush: Brush)

    fun clippedText(
            text    : String,
            font    : Font,
            point   : Point,
            clipRect: Rectangle,
            brush   : Brush)

    fun wrappedText(
            text     : String,
            font     : Font,
            point    : Point,
            minBounds: Double,
            maxBounds: Double,
            brush    : Brush)


    fun image(image: Image, source: Rectangle, destination: Rectangle, opacity: Float = 1f)

    enum class Optimization {
        Speed,
        Quality
    }
}