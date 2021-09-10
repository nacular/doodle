package io.nacular.doodle.drawing.impl

import io.nacular.doodle.drawing.AffineTransform
import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
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
import io.nacular.doodle.geometry.path
import io.nacular.doodle.geometry.toPath
import io.nacular.doodle.image.Image
import io.nacular.doodle.image.impl.ImageImpl
import io.nacular.doodle.image.impl.SvgImage
import io.nacular.doodle.skia.rrect
import io.nacular.doodle.skia.skia
import io.nacular.doodle.text.StyledText
import io.nacular.doodle.text.TextDecoration.Line.*
import io.nacular.doodle.text.TextDecoration.Style
import io.nacular.doodle.theme.native.textStyle
import io.nacular.doodle.utils.isOdd
import io.nacular.measured.units.Angle
import io.nacular.measured.units.Angle.Companion.radians
import io.nacular.measured.units.Measure
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.FilterBlurMode.NORMAL
import org.jetbrains.skia.FilterTileMode.REPEAT
import org.jetbrains.skia.ImageFilter
import org.jetbrains.skia.MaskFilter
import org.jetbrains.skia.PathEffect
import org.jetbrains.skia.PathFillMode.*
import org.jetbrains.skia.SamplingMode.Companion.MITCHELL
import org.jetbrains.skia.Shader
import org.jetbrains.skia.paragraph.BaselineMode.IDEOGRAPHIC
import org.jetbrains.skia.paragraph.DecorationLineStyle.*
import org.jetbrains.skia.paragraph.DecorationStyle
import org.jetbrains.skia.paragraph.FontCollection
import org.jetbrains.skia.paragraph.Paragraph
import org.jetbrains.skia.paragraph.ParagraphBuilder
import org.jetbrains.skia.paragraph.ParagraphStyle
import org.jetbrains.skia.paragraph.PlaceholderAlignment.BASELINE
import org.jetbrains.skia.paragraph.PlaceholderStyle
import org.jetbrains.skia.paragraph.TextStyle
import kotlin.Float.Companion.POSITIVE_INFINITY
import kotlin.math.max
import org.jetbrains.skia.Canvas as SkiaCanvas
import org.jetbrains.skia.Font   as SkiaFont
import org.jetbrains.skia.Paint  as SkiaPaint
import org.jetbrains.skia.Path   as SkiaPath


/**
 * Created by Nicholas Eddy on 5/19/21.
 */
internal class CanvasImpl(
        internal val skiaCanvas: SkiaCanvas,
        private val defaultFont: SkiaFont,
        private val fontCollection: FontCollection
): Canvas {
    private fun Paint.skia(): SkiaPaint {
        val result = SkiaPaint()

        when (this) {
            is ColorPaint          -> result.color  = color.skia()
            is LinearGradientPaint -> result.shader = Shader.makeLinearGradient(start.skia(), end.skia(), colors.map { it.color.skia() }.toIntArray(), colors.map { it.offset }.toFloatArray())
            is RadialGradientPaint -> result.shader = Shader.makeTwoPointConicalGradient(start.center.skia(), start.radius.toFloat(), end.center.skia(), end.radius.toFloat(), colors.map { it.color.skia() }.toIntArray(), colors.map { it.offset }.toFloatArray())
            is ImagePaint          -> (image as? ImageImpl)?.let { result.shader = it.skiaImage.makeShader(REPEAT, REPEAT) }
            is PatternPaint        -> {
                // FIXME: Reuse bitmaps?
                val bitmap = Bitmap().apply {
                    allocN32Pixels(size.width.toInt(), size.height.toInt())
                }

                val bitmapCanvas = SkiaCanvas(bitmap)

                paint(CanvasImpl(bitmapCanvas, defaultFont, fontCollection).apply { size = this@skia.size })

                result.shader = bitmap.makeShader(REPEAT, REPEAT, MITCHELL, (Identity.translate(bounds.x, bounds.y) * transform).skia())
            }
        }

        result.imageFilter = imageFilter

        return result
    }

    private fun Stroke.skia(): SkiaPaint = fill.skia().also {
        it.setStroke(true)
        it.strokeWidth = thickness.toFloat()
        dashes?.let { dashes ->
            val fixedDashes = when {
                dashes.size.isOdd -> { doubleArrayOf(*dashes, dashes.last()) }
                else              -> dashes
            }

            it.pathEffect = PathEffect.makeDash(fixedDashes.map { it.toFloat() }.toFloatArray(), dashOffset.toFloat())
        }
    }

    private fun Renderer.FillRule.skia() = when (this) {
        EvenOdd -> EVEN_ODD
        else    -> WINDING
    }

    override var size: Size = Size.Empty

    override fun transform(transform: AffineTransform, block: Canvas.() -> Unit) {
        val oldMatrix = skiaCanvas.localToDeviceAsMatrix33

        skiaCanvas.setMatrix(oldMatrix.makeConcat(transform.skia()))

        block(this)

        skiaCanvas.setMatrix(oldMatrix)
    }

    override fun rect(rectangle: Rectangle, fill: Paint) {
        withShadows({ rectangle.toPath() }) {
            skiaCanvas.drawRect(rectangle.skia(), fill.skia())
        }
    }

    override fun rect(rectangle: Rectangle, stroke: Stroke, fill: Paint?) {
        withShadows({ rectangle.toPath() }) {
            if (fill != null) {
                skiaCanvas.drawRect(rectangle.skia(), fill.skia())
            }
            skiaCanvas.drawRect(rectangle.skia(), stroke.skia())
        }
    }

    override fun rect(rectangle: Rectangle, radius: Double, fill: Paint) {
        withShadows({ rectangle.toPath(radius) }) {
            skiaCanvas.drawRRect(rectangle.rrect(radius.toFloat()), fill.skia())
        }
    }

    override fun rect(rectangle: Rectangle, radius: Double, stroke: Stroke, fill: Paint?) {
        withShadows({ rectangle.toPath(radius) }) {
            if (fill != null) {
                skiaCanvas.drawRRect(rectangle.rrect(radius.toFloat()), fill.skia())
            }
            skiaCanvas.drawRRect(rectangle.rrect(radius.toFloat()), stroke.skia())
        }
    }

    override fun circle(circle: Circle, fill: Paint) {
        withShadows({ circle.toPath() }) {
            skiaCanvas.drawCircle(circle.center.x.toFloat(), circle.center.y.toFloat(), circle.radius.toFloat(), fill.skia())
        }
    }

    override fun circle(circle: Circle, stroke: Stroke, fill: Paint?) {
        withShadows({ circle.toPath() }) {
            if (fill != null) {
                skiaCanvas.drawCircle(circle.center.x.toFloat(), circle.center.y.toFloat(), circle.radius.toFloat(), fill.skia())
            }
            skiaCanvas.drawCircle(circle.center.x.toFloat(), circle.center.y.toFloat(), circle.radius.toFloat(), stroke.skia())
        }
    }

    override fun ellipse(ellipse: Ellipse, fill: Paint) {
        withShadows({ ellipse.toPath() }) {
            skiaCanvas.drawOval(ellipse.boundingRectangle.skia(), fill.skia())
        }
    }

    override fun ellipse(ellipse: Ellipse, stroke: Stroke, fill: Paint?) {
        withShadows({ ellipse.toPath() }) {
            if (fill != null) {
                skiaCanvas.drawOval(ellipse.boundingRectangle.skia(), fill.skia())
            }
            skiaCanvas.drawOval(ellipse.boundingRectangle.skia(), stroke.skia())
        }
    }

    override fun text(text: String, font: Font?, at: Point, fill: Paint) {
        paragraph(text, font, at, fill = fill).apply {
            drawOuterShadows {
                updateForegroundPaint(0, text.length - 1, it)
                paint(skiaCanvas, at.x.toFloat(), at.y.toFloat())
                updateForegroundPaint(0, text.length - 1, fill.skia())
            }

            paint(skiaCanvas, at.x.toFloat(), at.y.toFloat())
        }
    }

    override fun text(text: StyledText, at: Point) {
        drawOuterShadows {
            text.paragraph(paint = it).paint(skiaCanvas, at.x.toFloat(), at.y.toFloat())
        }

        text.paragraph().paint(skiaCanvas, at.x.toFloat(), at.y.toFloat())
    }

    override fun wrapped(text: String, font: Font?, at: Point, leftMargin: Double, rightMargin: Double, fill: Paint) {
        paragraph(text, font, at, leftMargin, rightMargin, fill).apply {
            drawOuterShadows {
                updateForegroundPaint(0, text.length - 1, it)
                paint(skiaCanvas, at.x.toFloat(), at.y.toFloat())
                updateForegroundPaint(0, text.length - 1, fill.skia())
            }

            paint(skiaCanvas, at.x.toFloat() + leftMargin.toFloat(), at.y.toFloat())
        }
    }

    override fun wrapped(text: StyledText, at: Point, leftMargin: Double, rightMargin: Double) {
        drawOuterShadows {
            text.paragraph(paint = it).apply {
                layout(max(minIntrinsicWidth + 1, (rightMargin - leftMargin).toFloat()))
                paint(skiaCanvas, at.x.toFloat(), at.y.toFloat())
            }
        }

        text.paragraph().apply {
            layout(max(minIntrinsicWidth + 1, (rightMargin - leftMargin).toFloat()))
            paint(skiaCanvas, at.x.toFloat(), at.y.toFloat())
        }
    }

    override fun image(image: Image, destination: Rectangle, opacity: Float, radius: Double, source: Rectangle) {
        if (image is ImageImpl) {
            withShadows({ destination.toPath() }) {
                skiaCanvas.drawImageRect(image.skiaImage, source.skia(), destination.skia())
            }
        } else if (image is SvgImage) {
            image.render(skiaCanvas, source.skia(), destination.skia())
        }
    }

    override fun line(start: Point, end: Point, stroke: Stroke) {
        skiaCanvas.drawLine(start.x.toFloat(), start.y.toFloat(), end.x.toFloat(), end.y.toFloat(), stroke.skia())
    }

    override fun path(points: List<Point>, fill: Paint, fillRule: Renderer.FillRule?) {
        if (points.isNotEmpty()) {
            skiaCanvas.drawPath(points.toPath().skia(), fill.skia())
        }
    }

    override fun path(path: Path, fill: Paint, fillRule: Renderer.FillRule?) {
        withShadows({ path }) {
            val skiaPath = path.skia()

            if (fillRule != null) {
                skiaPath.fillMode = fillRule.skia()
            }

            skiaCanvas.drawPath(skiaPath, fill.skia())
        }
    }

    override fun path(points: List<Point>, stroke: Stroke) {
        if (points.isNotEmpty()) {
            skiaCanvas.drawPath(points.toPath().skia(), stroke.skia())
        }
    }

    override fun path(path: Path, stroke: Stroke) {
        withShadows({ path }) {
            skiaCanvas.drawPath(path.skia(), stroke.skia())
        }
    }

    override fun path(points: List<Point>, stroke: Stroke, fill: Paint, fillRule: Renderer.FillRule?) {
        if (points.isNotEmpty()) {
            path(points.toPath(), fill, fillRule)
        }
    }

    override fun path(path: Path, stroke: Stroke, fill: Paint, fillRule: Renderer.FillRule?) {
        withShadows({ path }) {
            val skiaPath = path.skia()

            if (fillRule != null) {
                skiaPath.fillMode = fillRule.skia()
            }

            skiaCanvas.drawPath(skiaPath, fill.skia ())
            skiaCanvas.drawPath(skiaPath, stroke.skia())
        }
    }

    override fun poly(polygon: Polygon, fill: Paint) {
        val path by lazy { polygon.toPath() }

        withShadows({ path }) {
            skiaCanvas.drawPath(path.skia(), fill.skia())
        }
    }

    override fun poly(polygon: Polygon, stroke: Stroke, fill: Paint?) {
        val path by lazy { polygon.toPath() }

        withShadows({ path }) {
            if (fill != null) {
                skiaCanvas.drawPath(path.skia(), fill.skia())
            }
            skiaCanvas.drawPath(path.skia(), stroke.skia())
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
        clip(polygon.toPath().skia(), block)
    }

    override fun clip(ellipse: Ellipse, block: Canvas.() -> Unit) {
        clip(ellipse.toPath().skia(), block)
    }

    private fun clip(path: SkiaPath, block: Canvas.() -> Unit) {
        skiaCanvas.save()
        skiaCanvas.clipPath(path)
        block(this)
        skiaCanvas.restore()
    }

    private val shadows = mutableListOf<Shadow>()
    private var imageFilter: ImageFilter? = null

    override fun shadow(shadow: Shadow, block: Canvas.() -> Unit) {
        shadows += shadow
        skiaCanvas.save()
        block(this)
        shadows -= shadow
        skiaCanvas.restore()
    }

    override fun clear() {
        skiaCanvas.clear(Color.Transparent.skia())
    }

    override fun flush() {
        // no-op
    }

    private val Font?.textStyle get() = when (this) {
        is FontImpl -> textStyle
        else        -> defaultFont.textStyle()
    }

    private fun drawOuterShadows(operation: SkiaCanvas.(SkiaPaint) -> Unit) {
        shadows.filterIsInstance<OuterShadow>().forEach {
            val blur = SkiaPaint().apply {
                color       = it.color.skia()
                isAntiAlias = true
                if (it.blurRadius > 0f) {
                    maskFilter = MaskFilter.makeBlur(NORMAL, it.blurRadius.toFloat())
                }
            }

            skiaCanvas.save     ()
            skiaCanvas.translate(it.horizontal.toFloat(), it.vertical.toFloat())
            operation(skiaCanvas, blur)
            skiaCanvas.restore  ()
        }
    }

    private fun drawInnerShadows(pathProvider: () -> Path) {
        val path: Path by lazy { pathProvider() }

        shadows.filterIsInstance<InnerShadow>().forEach {
            val blur = SkiaPaint().apply {
                color       = it.color.opacity(it.color.opacity * 0.4f).skia()
                isAntiAlias = true
                strokeWidth = it.blurRadius.toFloat()
                setStroke(true)
                if (it.blurRadius > 0f) {
                    maskFilter = MaskFilter.makeBlur(NORMAL, it.blurRadius.toFloat() / 2)
                }
            }

            skiaCanvas.save     ()
            skiaCanvas.clipPath (path.skia())
            skiaCanvas.translate(it.horizontal.toFloat(), it.vertical.toFloat())
            skiaCanvas.drawPath (path.skia(), blur)
            skiaCanvas.restore  ()
        }
    }

    private fun withShadows(path: () -> Path, block: () -> Unit) {
        drawOuterShadows { drawPath(path().skia(), it) }
        block()
        drawInnerShadows(path)
    }

    private fun paragraph(text: String, font: Font?, at: Point, leftMargin: Double? = null, rightMargin: Double? = null, fill: Paint) = paragraph(text, font, at, leftMargin, rightMargin, fill.skia())

    private fun paragraph(text: String, font: Font?, at: Point, leftMargin: Double? = null, rightMargin: Double? = null, paint: SkiaPaint): Paragraph {
        val style = ParagraphStyle().apply {
            textStyle = font.textStyle.apply { foreground = paint }
        }

        val builder = ParagraphBuilder(style, fontCollection).run {
            if (leftMargin != null) {
                addPlaceholder(PlaceholderStyle((at.x - leftMargin).toFloat(), 0f, BASELINE, IDEOGRAPHIC, 0f))
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

    private fun StyledText.paragraph(paint: SkiaPaint? = null): Paragraph {
        val builder = ParagraphBuilder(ParagraphStyle(), fontCollection).also { builder ->
            this.forEach { (text, style) ->
                builder.pushStyle(style.font.textStyle.apply {
                    foreground = paint ?: style.foreground?.skia() ?: Black.paint.skia()

                    style.background?.skia()?.let { background = it }
                    style.decoration?.run {
                        decorationStyle = DecorationStyle(
                            Under   in lines,
                            Over    in lines,
                            Through in lines,
                            false,
                            color?.skia() ?: foreground?.color ?: Black.skia(),
                            when (this.style) {
                                Style.Solid  -> SOLID
                                Style.Double -> DOUBLE
                                Style.Dotted -> DOTTED
                                Style.Dashed -> DASHED
                                Style.Wavy   -> WAVY
                            },
                            2f)
                    }
                })
                builder.addText(text)
                builder.popStyle()
            }
        }

        return builder.build().apply { layout(POSITIVE_INFINITY) }
    }

    private fun List<Point>.toPath() = path(from = this[0]).run {
        subList(1, size).forEach { lineTo(it) }
        finish()
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
                    fill.skia())
        }

        if (stroke != null) {
            skiaCanvas.drawArc(boundingRect.x.toFloat(),
                    boundingRect.y.toFloat(),
                    boundingRect.right.toFloat(),
                    boundingRect.bottom.toFloat(),
                    (rotation `in` radians).toFloat(),
                    (sweep `in` radians).toFloat(),
                    includeCenter,
                    stroke.skia())
        }
    }
}