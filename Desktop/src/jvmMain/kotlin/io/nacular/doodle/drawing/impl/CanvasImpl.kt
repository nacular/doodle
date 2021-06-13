package io.nacular.doodle.drawing.impl

import io.nacular.doodle.drawing.AffineTransform
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.ColorPaint
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.ImagePaint
import io.nacular.doodle.drawing.InnerShadow
import io.nacular.doodle.drawing.LinearGradientPaint
import io.nacular.doodle.drawing.OuterShadow
import io.nacular.doodle.drawing.Paint
import io.nacular.doodle.drawing.PatternPaint
import io.nacular.doodle.drawing.RadialGradientPaint
import io.nacular.doodle.drawing.Renderer
import io.nacular.doodle.drawing.Renderer.FillRule.EvenOdd
import io.nacular.doodle.drawing.Shadow
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.drawing.opacity
import io.nacular.doodle.drawing.paint
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
import io.nacular.doodle.skia.skija
import io.nacular.doodle.text.StyledText
import io.nacular.measured.units.Angle
import io.nacular.measured.units.Angle.Companion.radians
import io.nacular.measured.units.Measure
import org.jetbrains.skija.Bitmap
import org.jetbrains.skija.FilterBlurMode
import org.jetbrains.skija.FilterTileMode.REPEAT
import org.jetbrains.skija.FontMgr
import org.jetbrains.skija.ImageFilter
import org.jetbrains.skija.MaskFilter
import org.jetbrains.skija.PathEffect
import org.jetbrains.skija.PathFillMode.*
import org.jetbrains.skija.Shader
import org.jetbrains.skija.paragraph.BaselineMode.ALPHABETIC
import org.jetbrains.skija.paragraph.FontCollection
import org.jetbrains.skija.paragraph.Paragraph
import org.jetbrains.skija.paragraph.ParagraphBuilder
import org.jetbrains.skija.paragraph.ParagraphStyle
import org.jetbrains.skija.paragraph.PlaceholderAlignment.BASELINE
import org.jetbrains.skija.paragraph.PlaceholderStyle
import org.jetbrains.skija.paragraph.TextStyle
import kotlin.Float.Companion.POSITIVE_INFINITY
import kotlin.math.max
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
            is ColorPaint          -> result.color  = color.skija()
            is LinearGradientPaint -> result.shader = Shader.makeLinearGradient(start.skija(), end.skija(), colors.map { it.color.skija() }.toIntArray(), colors.map { it.offset }.toFloatArray())
            is RadialGradientPaint -> result.shader = Shader.makeTwoPointConicalGradient(start.center.skija(), start.radius.toFloat(), end.center.skija(), end.radius.toFloat(), colors.map { it.color.skija() }.toIntArray(), colors.map { it.offset }.toFloatArray())
            is ImagePaint          -> (image as? ImageImpl)?.let { result.shader = it.skiaImage.makeShader(REPEAT, REPEAT) }
            is PatternPaint        -> {
                // FIXME: Reuse bitmaps
                val bitmap = Bitmap().apply {
                    allocN32Pixels(size.width.toInt(), size.height.toInt())
                }
                val bitmapCanvas = org.jetbrains.skija.Canvas(bitmap)
                paint.invoke(CanvasImpl(bitmapCanvas, defaultFont).apply { size = this@skija.size })

                result.shader = bitmap.makeShader(REPEAT, REPEAT)
            }
        }

        result.imageFilter = imageFilter

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

    private val fontCollection = FontCollection().apply {
        setDefaultFontManager(FontMgr.getDefault())
    }

    override var size: Size = Size.Empty

    override fun transform(transform: AffineTransform, block: Canvas.() -> Unit) {
        val oldMatrix = skiaCanvas.localToDeviceAsMatrix33

        skiaCanvas.setMatrix(oldMatrix.makeConcat(transform.skija()))

        block(this)

        skiaCanvas.setMatrix(oldMatrix)
    }

    override fun rect(rectangle: Rectangle, fill: Paint) {
        withShadows({ rectangle.toPath() }) {
            skiaCanvas.drawRect(rectangle.skija(), fill.skija())
        }
    }

    override fun rect(rectangle: Rectangle, stroke: Stroke, fill: Paint?) {
        withShadows({ rectangle.toPath() }) {
            if (fill != null) {
                skiaCanvas.drawRect(rectangle.skija(), fill.skija())
            }
            skiaCanvas.drawRect(rectangle.skija(), stroke.skija())
        }
    }

    override fun rect(rectangle: Rectangle, radius: Double, fill: Paint) {
        withShadows({ rectangle.toPath(radius) }) {
            skiaCanvas.drawRRect(rectangle.rrect(radius.toFloat()), fill.skija())
        }
    }

    override fun rect(rectangle: Rectangle, radius: Double, stroke: Stroke, fill: Paint?) {
        withShadows({ rectangle.toPath(radius) }) {
            if (fill != null) {
                skiaCanvas.drawRRect(rectangle.rrect(radius.toFloat()), fill.skija())
            }
            skiaCanvas.drawRRect(rectangle.rrect(radius.toFloat()), stroke.skija())
        }
    }

    override fun circle(circle: Circle, fill: Paint) {
        withShadows({ circle.toPath() }) {
            skiaCanvas.drawCircle(circle.center.x.toFloat(), circle.center.y.toFloat(), circle.radius.toFloat(), fill.skija())
        }
    }

    override fun circle(circle: Circle, stroke: Stroke, fill: Paint?) {
        withShadows({ circle.toPath() }) {
            if (fill != null) {
                skiaCanvas.drawCircle(circle.center.x.toFloat(), circle.center.y.toFloat(), circle.radius.toFloat(), fill.skija())
            }
            skiaCanvas.drawCircle(circle.center.x.toFloat(), circle.center.y.toFloat(), circle.radius.toFloat(), stroke.skija())
        }
    }

    override fun ellipse(ellipse: Ellipse, fill: Paint) {
        withShadows({ ellipse.toPath() }) {
            skiaCanvas.drawOval(ellipse.boundingRectangle.skija(), fill.skija())
        }
    }

    override fun ellipse(ellipse: Ellipse, stroke: Stroke, fill: Paint?) {
        withShadows({ ellipse.toPath() }) {
            if (fill != null) {
                skiaCanvas.drawOval(ellipse.boundingRectangle.skija(), fill.skija())
            }
            skiaCanvas.drawOval(ellipse.boundingRectangle.skija(), stroke.skija())
        }
    }

    override fun text(text: String, font: Font?, at: Point, fill: Paint) {
        paragraph(text, font, at, fill = fill).paint(skiaCanvas, at.x.toFloat(), at.y.toFloat())
    }

    override fun text(text: StyledText, at: Point) {
        text.paragraph().paint(skiaCanvas, at.x.toFloat(), at.y.toFloat())
    }

    override fun wrapped(text: String, font: Font?, at: Point, leftMargin: Double, rightMargin: Double, fill: Paint) {
        paragraph(text, font, at, leftMargin, rightMargin, fill).paint(skiaCanvas, at.x.toFloat() + leftMargin.toFloat(), at.y.toFloat())
    }

    override fun wrapped(text: StyledText, at: Point, leftMargin: Double, rightMargin: Double) {
        text.paragraph().apply {
            layout(max(minIntrinsicWidth + 1, (rightMargin - leftMargin).toFloat()))
        }.paint(skiaCanvas, at.x.toFloat(), at.y.toFloat())
    }

    override fun image(image: Image, destination: Rectangle, opacity: Float, radius: Double, source: Rectangle) {
        if (image is ImageImpl) {
            withShadows({ destination.toPath() }) {
                skiaCanvas.drawImageRect(image.skiaImage, source.skija(), destination.skija())
            }
        }
    }

    override fun line(start: Point, end: Point, stroke: Stroke) {
        skiaCanvas.drawLine(start.x.toFloat(), start.y.toFloat(), end.x.toFloat(), end.y.toFloat(), stroke.skija())
    }

    override fun path(points: List<Point>, fill: Paint, fillRule: Renderer.FillRule?) {
        skiaCanvas.drawLines(points.map { it.skija() }.toTypedArray(), fill.skija())
    }

    override fun path(path: Path, fill: Paint, fillRule: Renderer.FillRule?) {
        withShadows({ path }) {
            val skijaPath = path.skija()

            if (fillRule != null) {
                skijaPath.fillMode = fillRule.skija()
            }

            skiaCanvas.drawPath(skijaPath, fill.skija())
        }
    }

    override fun path(points: List<Point>, stroke: Stroke) {
        skiaCanvas.drawLines(points.map { it.skija() }.toTypedArray(), stroke.skija())
    }

    override fun path(path: Path, stroke: Stroke) {
        withShadows({ path }) {
            skiaCanvas.drawPath(path.skija(), stroke.skija())
        }
    }

    override fun path(points: List<Point>, stroke: Stroke, fill: Paint, fillRule: Renderer.FillRule?) {
        val skijaPoints = points.map { it.skija() }.toTypedArray()

        skiaCanvas.drawLines(skijaPoints, fill.skija())
        skiaCanvas.drawLines(skijaPoints, stroke.skija())
    }

    override fun path(path: Path, stroke: Stroke, fill: Paint, fillRule: Renderer.FillRule?) {
        withShadows({ path }) {
            val skijaPath = path.skija()

            skiaCanvas.drawPath(skijaPath, fill.skija())
            skiaCanvas.drawPath(skijaPath, stroke.skija())
        }
    }

    override fun poly(polygon: Polygon, fill: Paint) {
        withShadows({ polygon.toPath() }) {
            skiaCanvas.drawPolygon(polygon.points.map { it.skija() }.toTypedArray(), fill.skija())
        }
    }

    override fun poly(polygon: Polygon, stroke: Stroke, fill: Paint?) {
        withShadows({ polygon.toPath() }) {
            val points = polygon.points.map { it.skija() }.toTypedArray()

            if (fill != null) {
                skiaCanvas.drawPolygon(points, fill.skija())
            }
            skiaCanvas.drawPolygon(points, stroke.skija())
        }
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

    private val shadows = mutableListOf<Shadow>()
    private var imageFilter: ImageFilter? = null

    override fun shadow(shadow: Shadow, block: Canvas.() -> Unit) {
        shadows += shadow
//    skiaCanvas.save()
//
//    val previousFilter = imageFilter
//
//    imageFilter = when (val i = imageFilter) {
//        null -> ImageFilter.makeDropShadow(
//                shadow.horizontal.toFloat(),
//                shadow.vertical.toFloat(),
//                shadow.blurRadius.toFloat(),
//                shadow.blurRadius.toFloat(),
//                shadow.color.skija()
//        )
//        else -> ImageFilter.makeDropShadow(
//                shadow.horizontal.toFloat(),
//                shadow.vertical.toFloat(),
//                shadow.blurRadius.toFloat(),
//                shadow.blurRadius.toFloat(),
//                shadow.color.skija(),
//                i,
//                IRect.makeWH(size.width.toInt(), size.height.toInt())
//        )
//    }

        block(this)

//        imageFilter = previousFilter

        shadows -= shadow

        skiaCanvas.restore()
    }

    override fun clear() {
        skiaCanvas.clear(Color.Transparent.skija())
    }

    override fun flush() {
        // no-op
    }

    private val Font?.skia get() = when (this) {
        is FontImpl -> skiaFont
        else        -> defaultFont
    }

    private fun drawOuterShadows(pathProvider: () -> Path) {
        val path: Path by lazy { pathProvider() }

        shadows.filterIsInstance<OuterShadow>().forEach {
            val blur = SkijaPaint().apply {
                color       = it.color.skija()
                maskFilter  = MaskFilter.makeBlur(FilterBlurMode.NORMAL, it.blurRadius.toFloat())
                isAntiAlias = true
            }

            skiaCanvas.save     ()
            skiaCanvas.translate(it.horizontal.toFloat(), it.vertical.toFloat())
            skiaCanvas.drawPath (path.skija(), blur)
            skiaCanvas.restore  ()
        }
    }

    private fun drawInnerShadows(pathProvider: () -> Path) {
        val path: Path by lazy { pathProvider() }

        shadows.filterIsInstance<InnerShadow>().forEach {
            val blur = SkijaPaint().apply {
                color       = it.color.opacity(it.color.opacity * 0.4f).skija()
                maskFilter  = MaskFilter.makeBlur(FilterBlurMode.NORMAL, it.blurRadius.toFloat() / 2)
                isAntiAlias = true
                strokeWidth = it.blurRadius.toFloat()
                setStroke(true)
            }

            skiaCanvas.save     ()
            skiaCanvas.clipPath (path.skija())
            skiaCanvas.translate(it.horizontal.toFloat(), it.vertical.toFloat())
            skiaCanvas.drawPath (path.skija(), blur)
            skiaCanvas.restore  ()
        }
    }

    private fun withShadows(path: () -> Path, block: () -> Unit) {
        drawOuterShadows(path)
        block()
        drawInnerShadows(path)
    }

    private fun paragraph(text: String, font: Font?, at: Point, leftMargin: Double? = null, rightMargin: Double? = null, fill: Paint): Paragraph {
        val style = ParagraphStyle().apply {
            textStyle = TextStyle().apply {
                foreground = fill.skija()
                typeface   = font.skia.typeface
                fontSize   = font.skia.size
                fontStyle  = font.skia.typeface?.fontStyle
            }
        }

        val builder = ParagraphBuilder(style, fontCollection).run {
            if (leftMargin != null) {
                addPlaceholder(PlaceholderStyle((at.x - leftMargin).toFloat(), 0f, BASELINE, ALPHABETIC, 0f))
            }
            addText(text)
        }

        val paragraph = builder.build()

        paragraph.layout(POSITIVE_INFINITY)

        if (leftMargin != null && rightMargin != null) {
            paragraph.layout(max(paragraph.minIntrinsicWidth + 1, (rightMargin - leftMargin).toFloat()))
        }

        return paragraph
    }

    private fun StyledText.paragraph(): Paragraph {
        val builder = ParagraphBuilder(ParagraphStyle(), fontCollection).also { builder ->
            this.forEach { (text, style) ->
                builder.pushStyle(TextStyle().apply {
                    style.background?.skija()?.let { background = it }

                    foreground = style.foreground?.skija() ?: Black.paint.skija()
                    typeface   = style.font.skia.typeface
                    fontSize   = style.font.skia.size
                    fontStyle  = style.font.skia.typeface?.fontStyle
                })
                builder.addText(text)
                builder.popStyle()
            }
        }

        return builder.build().apply { layout(POSITIVE_INFINITY) }
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