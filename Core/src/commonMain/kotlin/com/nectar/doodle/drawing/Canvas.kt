package com.nectar.doodle.drawing

import com.nectar.doodle.drawing.AffineTransform.Companion.Identity
import com.nectar.doodle.drawing.Color.Companion.black
import com.nectar.doodle.geometry.Circle
import com.nectar.doodle.geometry.Ellipse
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.image.Image
import com.nectar.doodle.text.StyledText
import com.nectar.measured.units.Angle
import com.nectar.measured.units.Measure


sealed class Shadow(val horizontal: Double, val vertical: Double, val blurRadius: Double, val color: Color)
class InnerShadow(horizontal: Double = 0.0, vertical: Double = 0.0, blurRadius: Double = 0.0, color: Color = black): Shadow(horizontal, vertical, blurRadius, color)
class OuterShadow(horizontal: Double = 0.0, vertical: Double = 0.0, blurRadius: Double = 0.0, color: Color = black): Shadow(horizontal, vertical, blurRadius, color)

interface Canvas: Renderer {
    var size: Size

    fun scale(x: Double, y: Double, block: Canvas.() -> Unit) = when {
        x == 1.0 && y == 1.0 -> block()
        else                 -> transform(Identity.scale(x, y), block)
    }

    fun scale(around: Point, x: Double, y: Double, block: Canvas.() -> Unit) = when {
        x == 1.0 && y == 1.0 -> block()
        else                 -> {
            val point = around - (size / 2.0).run { Point(width, height) }

            transform(Identity.translate(point).scale(x, y).translate(-point), block)
        }
    }

    fun rotate(by    : Measure<Angle>,                     block: Canvas.() -> Unit) = transform(Identity.rotate(by), block)
    fun rotate(around: Point,          by: Measure<Angle>, block: Canvas.() -> Unit) {
        val point = around - (size / 2.0).run { Point(width, height) }

        transform(Identity.translate(point).rotate(by).translate(-point), block)
    }

    fun translate(by: Point, block: Canvas.() -> Unit) = when (by) {
        Point.Origin -> block()
        else         -> transform(Identity.translate(by), block)
    }

    fun flipVertically(                block: Canvas.() -> Unit) = scale(1.0, -1.0, block = block)
    fun flipVertically(around: Double, block: Canvas.() -> Unit) = transform(Identity.
            translate(Point(0.0, around)).
            scale(1.0, -1.0).
            translate(Point(0.0, -around)),
            block)

    fun flipHorizontally(                block: Canvas.() -> Unit) = scale(-1.0, 1.0, block = block)
    fun flipHorizontally(around: Double, block: Canvas.() -> Unit) = transform(Identity.
            translate(Point(around, 0.0)).
            scale(-1.0, 1.0).
            translate(Point(-around, 0.0)),
            block)

    fun transform(transform: AffineTransform, block: Canvas.() -> Unit)

    fun rect(rectangle: Rectangle,           brush: Brush        )
    fun rect(rectangle: Rectangle, pen: Pen, brush: Brush? = null)

    fun rect(rectangle: Rectangle, radius: Double,           brush: Brush        )
    fun rect(rectangle: Rectangle, radius: Double, pen: Pen, brush: Brush? = null)

    fun circle(circle: Circle,           brush: Brush        )
    fun circle(circle: Circle, pen: Pen, brush: Brush? = null)

    fun ellipse(ellipse: Ellipse,           brush: Brush        )
    fun ellipse(ellipse: Ellipse, pen: Pen, brush: Brush? = null)

    fun text(text: String, font: Font? = null, at: Point, brush: Brush)

    fun text(text: StyledText, at: Point)

    fun wrapped(
            text       : String,
            font       : Font? = null,
            at         : Point,
            leftMargin : Double,
            rightMargin: Double,
            brush      : Brush)

    fun wrapped(
            text       : StyledText,
            at         : Point,
            leftMargin : Double,
            rightMargin: Double)

    fun image(image: Image, destination: Rectangle = Rectangle(size = image.size), opacity: Float = 1f, radius: Double = 0.0, source: Rectangle = Rectangle(size = image.size))

    fun clip(rectangle: Rectangle, block: Canvas.() -> Unit)

    fun shadow(shadow: Shadow, block: Canvas.() -> Unit)
    fun innerShadow(horizontal: Double = 0.0, vertical: Double = 0.0, blurRadius: Double = 1.0, color: Color = black, block: Canvas.() -> Unit) = shadow(InnerShadow(horizontal, vertical, blurRadius, color), block)
    fun outerShadow(horizontal: Double = 0.0, vertical: Double = 0.0, blurRadius: Double = 1.0, color: Color = black, block: Canvas.() -> Unit) = shadow(OuterShadow(horizontal, vertical, blurRadius, color), block)
}
