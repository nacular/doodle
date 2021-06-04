package io.nacular.doodle.drawing.impl

import io.nacular.doodle.drawing.AffineTransform
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.ColorPaint
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.ImagePaint
import io.nacular.doodle.drawing.LinearGradientPaint
import io.nacular.doodle.drawing.Paint
import io.nacular.doodle.drawing.PatternPaint
import io.nacular.doodle.drawing.RadialGradientPaint
import io.nacular.doodle.drawing.Renderer
import io.nacular.doodle.drawing.Renderer.FillRule.EvenOdd
import io.nacular.doodle.drawing.Shadow
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.geometry.Circle
import io.nacular.doodle.geometry.Ellipse
import io.nacular.doodle.geometry.Path
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Polygon
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.toPath
import io.nacular.doodle.image.Image
import io.nacular.doodle.image.impl.ImageImpl
import io.nacular.doodle.skia.rrect
import io.nacular.doodle.skia.skia
import io.nacular.doodle.skia.skija
import io.nacular.doodle.text.StyledText
import io.nacular.measured.units.Angle
import io.nacular.measured.units.Angle.Companion.radians
import io.nacular.measured.units.Measure
import org.jetbrains.skija.Bitmap
import org.jetbrains.skija.FilterTileMode.REPEAT
import org.jetbrains.skija.FontMgr
import org.jetbrains.skija.ImageInfo
import org.jetbrains.skija.PathEffect
import org.jetbrains.skija.PathFillMode.*
import org.jetbrains.skija.Shader
import org.jetbrains.skija.TextLine
import org.jetbrains.skija.paragraph.BaselineMode.ALPHABETIC
import org.jetbrains.skija.paragraph.FontCollection
import org.jetbrains.skija.paragraph.ParagraphBuilder
import org.jetbrains.skija.paragraph.ParagraphStyle
import org.jetbrains.skija.paragraph.PlaceholderAlignment.BASELINE
import org.jetbrains.skija.paragraph.PlaceholderStyle
import org.jetbrains.skija.paragraph.TextStyle
import org.jetbrains.skija.Canvas as SkijaCanvas
import org.jetbrains.skija.Font as SkijaFont
import org.jetbrains.skija.Paint as SkijaPaint
import org.jetbrains.skija.Path as SkijaPath


/**
 * Created by Nicholas Eddy on 5/19/21.
 */
internal class CanvasImpl(private val skiaCanvas: SkijaCanvas, private val defaultFont: SkijaFont): Canvas {
    private fun Paint.skija(): SkijaPaint {
        val result = SkijaPaint()

        when (this) {
            is ColorPaint          -> result.color  = color.skia()
            is LinearGradientPaint -> result.shader = Shader.makeLinearGradient(start.skija(), end.skija(), colors.map { it.color.skia() }.toIntArray(), colors.map { it.offset }.toFloatArray())
            is RadialGradientPaint -> result.shader = Shader.makeTwoPointConicalGradient(start.center.skija(), start.radius.toFloat(), end.center.skija(), end.radius.toFloat(), colors.map { it.color.skia() }.toIntArray(), colors.map { it.offset }.toFloatArray())
            is ImagePaint          -> (image as? ImageImpl)?.let { result.shader = it.skiaImage.makeShader(REPEAT, REPEAT) }
            is PatternPaint        -> {
                if (false) {
                    val bitmap = Bitmap().apply { imageInfo = ImageInfo.makeA8(size.width.toInt(), size.height.toInt()) }
                    val bitmapCanvas = org.jetbrains.skija.Canvas(bitmap)
                    paint.invoke(CanvasImpl(bitmapCanvas, defaultFont).apply { size = this@skija.size })

                    result.shader = bitmap.makeShader(REPEAT, REPEAT)
                }
            }
        }

        return result
    }

    private fun Stroke.skija(): SkijaPaint = fill.skija().also {
        it.setStroke(true)
        it.strokeWidth = thickness.toFloat()
        dashes?.let { dashes ->
            it.pathEffect = PathEffect.makeDash(dashes.map { it.toFloat() }.toFloatArray(), dashOffset.toFloat())
        }
    }

    private fun Renderer.FillRule.skija() = when (this) {
        EvenOdd -> EVEN_ODD
        else    -> WINDING
    }

    override var size: Size = Size.Empty

    override fun transform(transform: AffineTransform, block: Canvas.() -> Unit) {
        val oldMatrix = skiaCanvas.localToDeviceAsMatrix33

        skiaCanvas.setMatrix(oldMatrix.makeConcat(transform.skija()))

        block(this)

        skiaCanvas.setMatrix(oldMatrix)
    }

    override fun rect(rectangle: Rectangle, fill: Paint) {
        skiaCanvas.drawRect(rectangle.skija(), fill.skija())
    }

    override fun rect(rectangle: Rectangle, stroke: Stroke, fill: Paint?) {
        if (fill != null) {
            skiaCanvas.drawRect(rectangle.skija(), fill.skija())
        }
        skiaCanvas.drawRect(rectangle.skija(), stroke.skija())
    }

    override fun rect(rectangle: Rectangle, radius: Double, fill: Paint) {
        skiaCanvas.drawRRect(rectangle.rrect(radius.toFloat()), fill.skija())
    }

    override fun rect(rectangle: Rectangle, radius: Double, stroke: Stroke, fill: Paint?) {
        if (fill != null) {
            skiaCanvas.drawRRect(rectangle.rrect(radius.toFloat()), fill.skija())
        }
        skiaCanvas.drawRRect(rectangle.rrect(radius.toFloat()), stroke.skija())
    }

    override fun circle(circle: Circle, fill: Paint) {
        skiaCanvas.drawCircle(circle.center.x.toFloat(), circle.center.y.toFloat(), circle.radius.toFloat(), fill.skija())
    }

    override fun circle(circle: Circle, stroke: Stroke, fill: Paint?) {
        if (fill != null) {
            skiaCanvas.drawCircle(circle.center.x.toFloat(), circle.center.y.toFloat(), circle.radius.toFloat(), fill.skija())
        }
        skiaCanvas.drawCircle(circle.center.x.toFloat(), circle.center.y.toFloat(), circle.radius.toFloat(), stroke.skija())
    }

    override fun ellipse(ellipse: Ellipse, fill: Paint) {
        skiaCanvas.drawOval(ellipse.boundingRectangle.skija(), fill.skija())
    }

    override fun ellipse(ellipse: Ellipse, stroke: Stroke, fill: Paint?) {
        if (fill != null) {
            skiaCanvas.drawOval(ellipse.boundingRectangle.skija(), fill.skija())
        }
        skiaCanvas.drawOval(ellipse.boundingRectangle.skija(), stroke.skija())
    }

    private val Font?.skia get() = when (this) {
        is FontImpl -> skiaFont
        else        -> defaultFont
    }

    override fun text(text: String, font: Font?, at: Point, fill: Paint) {
        // FIXME: Text offset-y not exactly equal to browser
        val yOffset = font.skia.size - (font.skia.metrics._underlinePosition ?: 0f) + (font.skia.metrics._underlineThickness ?: 0f)

        skiaCanvas.drawTextLine(TextLine.make(text, font.skia), at.x.toFloat(), at.y.toFloat() + yOffset, fill.skija())
    }

    override fun text(text: StyledText, at: Point) {
        TODO("Not yet implemented")
    }

    override fun wrapped(text: String, font: Font?, at: Point, leftMargin: Double, rightMargin: Double, fill: Paint) {
        val style = ParagraphStyle().apply {
            textStyle = TextStyle().apply {
                foreground = fill.skija()
                typeface   = font.skia.typeface
                fontSize   = font.skia.size
            }
            this.disableHinting()
        }

        val fontCollection = FontCollection().apply {
            setDefaultFontManager(FontMgr.getDefault())
        }

        val builder = ParagraphBuilder(style, fontCollection).
            addPlaceholder(PlaceholderStyle((at.x - leftMargin).toFloat(), 0f, BASELINE, ALPHABETIC, 0f)).
            addText(text)

        val paragraph = builder.build()

        paragraph.layout((rightMargin - leftMargin).toFloat())

        paragraph.paint(skiaCanvas, leftMargin.toFloat(), at.y.toFloat())
    }

    override fun wrapped(text: StyledText, at: Point, leftMargin: Double, rightMargin: Double) {
        TODO("Not yet implemented")
    }

    override fun image(image: Image, destination: Rectangle, opacity: Float, radius: Double, source: Rectangle) {
        if (image is ImageImpl) {
            skiaCanvas.drawImageRect(image.skiaImage, source.skija(), destination.skija())
        }
    }

    override fun clip(rectangle: Rectangle, radius: Double, block: Canvas.() -> Unit) {
        skiaCanvas.save()
        skiaCanvas.clipRRect(rectangle.rrect(radius.toFloat()))
        block(this)
        skiaCanvas.restore()
    }

    override fun clip(polygon: Polygon, block: Canvas.() -> Unit) {
        clip(polygon.toPath().skija(), block)
    }

    override fun clip(ellipse: Ellipse, block: Canvas.() -> Unit) {
        clip(ellipse.toPath().skija(), block)
    }

    private fun clip(path: SkijaPath, block: Canvas.() -> Unit) {
        skiaCanvas.save()
        skiaCanvas.clipPath(path)
        block(this)
        skiaCanvas.restore()
    }

    override fun shadow(shadow: Shadow, block: Canvas.() -> Unit) {
        skiaCanvas.save()

        block(this)
//        paint.ImageFilter = SKImageFilter.CreateDropShadow(2, 2, 4, 4, shadowColor,
//                SKDropShadowImageFilterShadowMode.DrawShadowAndForeground);

        skiaCanvas.restore()

//        TODO("Not yet implemented")
    }

    override fun clear() {
        skiaCanvas.clear(Color.Transparent.skia())
    }

    override fun flush() {
        // no-op
    }

    override fun line(start: Point, end: Point, stroke: Stroke) {
        skiaCanvas.drawLine(start.x.toFloat(), start.y.toFloat(), end.x.toFloat(), end.y.toFloat(), stroke.skija())
    }

    override fun path(points: List<Point>, fill: Paint, fillRule: Renderer.FillRule?) {
        skiaCanvas.drawLines(points.map { it.skija() }.toTypedArray(), fill.skija())
    }

    override fun path(path: Path, fill: Paint, fillRule: Renderer.FillRule?) {
        val skijaPath = path.skija()

        if (fillRule != null) {
            skijaPath.fillMode = fillRule.skija()
        }

        skiaCanvas.drawPath(skijaPath, fill.skija())
    }

    override fun path(points: List<Point>, stroke: Stroke) {
        skiaCanvas.drawLines(points.map { it.skija() }.toTypedArray(), stroke.skija())
    }

    override fun path(path: Path, stroke: Stroke) {
        skiaCanvas.drawPath(path.skija(), stroke.skija())
    }

    override fun path(points: List<Point>, stroke: Stroke, fill: Paint, fillRule: Renderer.FillRule?) {
        val skijaPoints = points.map { it.skija() }.toTypedArray()

        skiaCanvas.drawLines(skijaPoints, fill.skija())
        skiaCanvas.drawLines(skijaPoints, stroke.skija())
    }

    override fun path(path: Path, stroke: Stroke, fill: Paint, fillRule: Renderer.FillRule?) {
        val skijaPath = path.skija()

        skiaCanvas.drawPath(skijaPath, fill.skija())
        skiaCanvas.drawPath(skijaPath, stroke.skija())
    }

    override fun poly(polygon: Polygon, fill: Paint) {
        skiaCanvas.drawPolygon(polygon.points.map { it.skija() }.toTypedArray(), fill.skija())
    }

    override fun poly(polygon: Polygon, stroke: Stroke, fill: Paint?) {
        val points = polygon.points.map { it.skija() }.toTypedArray()

        if (fill != null) {
            skiaCanvas.drawPolygon(points, fill.skija())
        }
        skiaCanvas.drawPolygon(points, stroke.skija())
    }

    override fun arc(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, fill: Paint) {
        skiaArc(center, radius, sweep, rotation, null, fill, includeCenter = false)
    }

    override fun arc(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, stroke: Stroke, fill: Paint?) {
        skiaArc(center, radius, sweep, rotation, stroke, fill, includeCenter = false)
    }

    override fun wedge(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, fill: Paint) {
        skiaArc(center, radius, sweep, rotation, null, fill, includeCenter = true)
    }

    override fun wedge(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, stroke: Stroke, fill: Paint?) {
        skiaArc(center, radius, sweep, rotation, stroke, fill, includeCenter = true)
    }

    private fun skiaArc(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, stroke: Stroke?, fill: Paint?, includeCenter: Boolean) {
        val boundingRect = Circle(center, radius).boundingRectangle

        if (fill != null) {
            skiaCanvas.drawArc(boundingRect.x.toFloat(),
                    boundingRect.y.toFloat(),
                    boundingRect.right.toFloat(),
                    boundingRect.bottom.toFloat(),
                    (rotation `in` radians).toFloat(),
                    (sweep `in` radians).toFloat(),
                    includeCenter,
                    fill.skija())
        }

        if (stroke != null) {
            skiaCanvas.drawArc(boundingRect.x.toFloat(),
                    boundingRect.y.toFloat(),
                    boundingRect.right.toFloat(),
                    boundingRect.bottom.toFloat(),
                    (rotation `in` radians).toFloat(),
                    (sweep `in` radians).toFloat(),
                    includeCenter,
                    stroke.skija())
        }
    }
}