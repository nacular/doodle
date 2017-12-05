package com.nectar.doodle.drawing

import com.nectar.doodle.geometry.Circle
import com.nectar.doodle.geometry.Ellipse
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.image.Image
import com.nectar.doodle.text.StyledText
import com.nectar.doodle.units.Angle
import com.nectar.doodle.units.Measure


interface Canvas: Renderer {

    var size        : Size
    var transform   : AffineTransform
    var optimization: Renderer.Optimization

    fun import(imageData: ImageData, at: Point)

    fun scale    (by       : Point,                        block: Canvas.() -> Unit)
    fun scale    (around   : Point, by : Point,            block: Canvas.() -> Unit)
    fun rotate   (angle    : Measure<Angle>,               block: Canvas.() -> Unit)
    fun rotate   (around   : Point, angle: Measure<Angle>, block: Canvas.() -> Unit)
    fun translate(by       : Point,                        block: Canvas.() -> Unit)
    fun transform(transform: AffineTransform,              block: Canvas.() -> Unit)

    fun flipVertically(                block: Canvas.() -> Unit)
    fun flipVertically(around: Double, block: Canvas.() -> Unit)

    fun flipHorizontally(                block: Canvas.() -> Unit)
    fun flipHorizontally(around: Double, block: Canvas.() -> Unit)

    fun rect(rectangle: Rectangle,           brush: Brush        )
    fun rect(rectangle: Rectangle, pen: Pen, brush: Brush? = null)

    fun rect(rectangle: Rectangle, radius: Double,           brush: Brush)
    fun rect(rectangle: Rectangle, radius: Double, pen: Pen, brush: Brush? = null)

    fun circle(circle: Circle,           brush: Brush        )
    fun circle(circle: Circle, pen: Pen, brush: Brush? = null)

    fun ellipse(ellipse: Ellipse,           brush: Brush        )
    fun ellipse(ellipse: Ellipse, pen: Pen, brush: Brush? = null)

    fun text(text: String, font: Font? = null, at: Point, brush: Brush)

    fun text(text: StyledText, at: Point)

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

    fun image(image: Image, destination: Rectangle, radius: Double = 0.0, opacity: Float = 1f)

    fun clip(rectangle: Rectangle, block: Canvas.() -> Unit)

    fun shadow(horizontal: Double = 0.0, vertical: Double = 0.0, blurRadius: Double = 0.0, color: Color = Color.black, block: Canvas.() -> Unit)

    interface ImageData
}
