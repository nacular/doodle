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
    var optimization: Optimization
    fun clear()
    fun flush()

    fun rect(rectangle: Rectangle, pen  : Pen, brush: Brush? = null)
    fun rect(rectangle: Rectangle, brush: Brush                    )

    fun rect(rectangle: Rectangle, radius: Double, pen: Pen, brush: Brush? = null)
    fun rect(rectangle: Rectangle, radius: Double, brush: Brush)

    fun line(point1: Point, point2: Point, pen: Pen)

    fun path(points: List<Point>, pen: Pen)

    fun poly(aPolygon: Polygon, pen: Pen, brush: Brush? = null)
    fun poly(aPolygon: Polygon, brush: Brush)

    fun arc(aCenter: Point, radius: Double, sweep: Double, rotation: Double, pen: Pen, brush: Brush? = null)
    fun arc(aCenter: Point, radius: Double, sweep: Double, rotation: Double, brush: Brush)

    fun circle(aCircle: Circle, pen: Pen, brush: Brush? = null)
    fun circle(aCircle: Circle, brush: Brush)

    fun ellipse(ellipse: Ellipse, pen: Pen, brush: Brush? = null)
    fun ellipse(ellipse: Ellipse, brush: Brush)

    fun drawString(aString: String, aFont: Font, aPoint: Point, aBrush: Brush)

    fun drawClippedString(aString: String, aFont: Font, aPoint: Point, aClipRect: Rectangle, aBrush: Brush)

    fun drawImage(aImage: Image, aSource: Rectangle, aDestination: Rectangle, aOpacity: Float)

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

    fun image(image: Image,                    destination: Rectangle, opacity: Float = 1f)
    fun image(image: Image, source: Rectangle, destination: Rectangle, opacity: Float = 1f)


    enum class Optimization {
        Speed,
        Quality
    }
}