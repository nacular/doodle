package io.nacular.doodle.drawing.impl

import io.nacular.doodle.core.Camera
import io.nacular.doodle.drawing.AffineTransform
import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.Transparent
import io.nacular.doodle.drawing.ColorPaint
import io.nacular.doodle.drawing.CommonCanvas
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.FrostedGlassPaint
import io.nacular.doodle.drawing.ImagePaint
import io.nacular.doodle.drawing.InnerShadow
import io.nacular.doodle.drawing.LinearGradientPaint
import io.nacular.doodle.drawing.OuterShadow
import io.nacular.doodle.drawing.Paint
import io.nacular.doodle.drawing.PatternCanvas
import io.nacular.doodle.drawing.PatternPaint
import io.nacular.doodle.drawing.RadialGradientPaint
import io.nacular.doodle.drawing.Renderer
import io.nacular.doodle.drawing.Renderer.FillRule.EvenOdd
import io.nacular.doodle.drawing.Shadow
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.drawing.Stroke.LineCap
import io.nacular.doodle.drawing.Stroke.LineJoint
import io.nacular.doodle.drawing.SweepGradientPaint
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
import io.nacular.doodle.image.height
import io.nacular.doodle.image.impl.ImageImpl
import io.nacular.doodle.image.impl.SvgImage
import io.nacular.doodle.image.width
import io.nacular.doodle.skia.rrect
import io.nacular.doodle.skia.skia
import io.nacular.doodle.skia.skia33
import io.nacular.doodle.skia.skia44
import io.nacular.doodle.skia.textStyle
import io.nacular.doodle.text.StyledText
import io.nacular.doodle.text.TextDecoration.Line.Over
import io.nacular.doodle.text.TextDecoration.Line.Through
import io.nacular.doodle.text.TextDecoration.Line.Under
import io.nacular.doodle.text.TextDecoration.Style
import io.nacular.doodle.text.TextSpacing
import io.nacular.doodle.theme.native.textStyle
import io.nacular.doodle.utils.TextAlignment
import io.nacular.doodle.utils.TextAlignment.Center
import io.nacular.doodle.utils.TextAlignment.End
import io.nacular.doodle.utils.TextAlignment.Justify
import io.nacular.doodle.utils.TextAlignment.Start
import io.nacular.doodle.utils.isOdd
import io.nacular.measured.units.Angle
import io.nacular.measured.units.Angle.Companion.radians
import io.nacular.measured.units.Measure
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.BlendMode
import org.jetbrains.skia.BlendMode.DST_IN
import org.jetbrains.skia.BlendMode.SRC_OUT
import org.jetbrains.skia.Canvas.SaveLayerFlags
import org.jetbrains.skia.Canvas.SaveLayerFlagsSet.InitWithPrevious
import org.jetbrains.skia.Canvas.SaveLayerRec
import org.jetbrains.skia.ColorFilter
import org.jetbrains.skia.FilterTileMode.CLAMP
import org.jetbrains.skia.FilterTileMode.DECAL
import org.jetbrains.skia.FilterTileMode.REPEAT
import org.jetbrains.skia.GradientStyle
import org.jetbrains.skia.ImageFilter
import org.jetbrains.skia.Matrix33
import org.jetbrains.skia.Matrix44
import org.jetbrains.skia.PaintStrokeCap.BUTT
import org.jetbrains.skia.PaintStrokeCap.ROUND
import org.jetbrains.skia.PaintStrokeCap.SQUARE
import org.jetbrains.skia.PaintStrokeJoin
import org.jetbrains.skia.PathEffect
import org.jetbrains.skia.PathFillMode.EVEN_ODD
import org.jetbrains.skia.PathFillMode.WINDING
import org.jetbrains.skia.RRect
import org.jetbrains.skia.Rect
import org.jetbrains.skia.SamplingMode.Companion.MITCHELL
import org.jetbrains.skia.Shader
import org.jetbrains.skia.paragraph.Alignment
import org.jetbrains.skia.paragraph.BaselineMode.IDEOGRAPHIC
import org.jetbrains.skia.paragraph.DecorationLineStyle.DASHED
import org.jetbrains.skia.paragraph.DecorationLineStyle.DOTTED
import org.jetbrains.skia.paragraph.DecorationLineStyle.DOUBLE
import org.jetbrains.skia.paragraph.DecorationLineStyle.SOLID
import org.jetbrains.skia.paragraph.DecorationLineStyle.WAVY
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
import org.jetbrains.skia.Font as SkiaFont
import org.jetbrains.skia.Paint as SkiaPaint
import org.jetbrains.skia.Path as SkiaPath

internal class PatternCanvasWrapper(private val canvas: Canvas): PatternCanvas, CommonCanvas by canvas {
    override fun transform(transform: AffineTransform, block: PatternCanvas.() -> Unit) = canvas.transform(transform) { block(this@PatternCanvasWrapper) }

    override fun clip(rectangle: Rectangle, radius: Double, block: PatternCanvas.() -> Unit)  = canvas.clip(rectangle, radius) { block(this@PatternCanvasWrapper) }

    override fun clip(polygon: Polygon, block: PatternCanvas.() -> Unit) = canvas.clip(polygon) { block(this@PatternCanvasWrapper) }

    override fun clip(ellipse: Ellipse, block: PatternCanvas.() -> Unit) = canvas.clip(ellipse) { block(this@PatternCanvasWrapper) }

    override fun clip(path: Path, block: PatternCanvas.() -> Unit) = canvas.clip(path) { block(this@PatternCanvasWrapper) }

    override fun shadow(shadow: Shadow, block: PatternCanvas.() -> Unit) = canvas.shadow(shadow) { block(this@PatternCanvasWrapper) }
}

internal class CanvasImpl(
    internal var skiaCanvas    : SkiaCanvas,
    private  val defaultFont   : SkiaFont,
    private  val fontCollection: FontCollection
): Canvas {
    override var size: Size = Size.Empty

    var canvasClip: Rect? = null

    private val shadows = mutableListOf<Shadow>()

    override fun transform(transform: AffineTransform, block: Canvas.() -> Unit) {
        val oldMatrix = skiaCanvas.localToDevice

        when {
            transform.is3d -> skiaCanvas.concat(transform.skia44())
            else           -> skiaCanvas.concat(transform.skia33())
        }

        block(this)

        skiaCanvas.resetMatrix().concat(oldMatrix)
    }

    override fun transform(transform: AffineTransform, camera: Camera, block: Canvas.() -> Unit) {
        val oldMatrix = skiaCanvas.localToDevice

        val matrix = (camera.projection * transform).matrix

        skiaCanvas.concat(Matrix44(
            matrix[0,0].toFloat(), matrix[0,1].toFloat(), matrix[0,2].toFloat(), matrix[0,3].toFloat(),
            matrix[1,0].toFloat(), matrix[1,1].toFloat(), matrix[1,2].toFloat(), matrix[1,3].toFloat(),
            matrix[2,0].toFloat(), matrix[2,1].toFloat(), matrix[2,2].toFloat(), matrix[2,3].toFloat(),
            matrix[3,0].toFloat(), matrix[3,1].toFloat(), matrix[3,2].toFloat(), matrix[3,3].toFloat()
        ))

        block(this)

        skiaCanvas.resetMatrix().concat(oldMatrix)
    }

    override fun rect(rectangle: Rectangle, fill: Paint) {
        draw(fill, null) {
            skiaCanvas.drawRect(rectangle.skia(), it)
        }
    }

    override fun rect(rectangle: Rectangle, stroke: Stroke, fill: Paint?) {
        draw(fill, stroke) {
            skiaCanvas.drawRect(rectangle.skia(), it)
        }
    }

    override fun rect(rectangle: Rectangle, radius: Double, fill: Paint) {
        draw(fill, null) {
            skiaCanvas.drawRRect(rectangle.rrect(radius.toFloat()), it)
        }
    }

    override fun rect(rectangle: Rectangle, radius: Double, stroke: Stroke, fill: Paint?) {
        draw(fill, stroke) {
            skiaCanvas.drawRRect(rectangle.rrect(radius.toFloat()), it)
        }
    }

    override fun circle(circle: Circle, fill: Paint) {
        draw(fill, null) {
            with(circle) {
                skiaCanvas.drawCircle(center.x.toFloat(), center.y.toFloat(), radius.toFloat(), it)
            }
        }
    }

    override fun circle(circle: Circle, stroke: Stroke, fill: Paint?) {
        draw(fill, stroke) {
            with(circle) {
                skiaCanvas.drawCircle(center.x.toFloat(), center.y.toFloat(), radius.toFloat(), it)
            }
        }
    }

    override fun ellipse(ellipse: Ellipse, fill: Paint) {
        draw(fill, null) {
            skiaCanvas.drawOval(ellipse.boundingRectangle.skia(), it)
        }
    }

    override fun ellipse(ellipse: Ellipse, stroke: Stroke, fill: Paint?) {
        draw(fill, stroke) {
            skiaCanvas.drawOval(ellipse.boundingRectangle.skia(), it)
        }
    }

    override fun text(text: String, font: Font?, at: Point, fill: Paint, textSpacing: TextSpacing) {
        draw(fill, null) {
            paragraph(
                text,
                font,
                paint       = it,
                alignment   = Start,
                lineHeight  = 1f,
                textSpacing = textSpacing
            ).paint(skiaCanvas, at.x.toFloat(), at.y.toFloat())
        }
    }

    override fun text(
        text       : String,
        font       : Font?,
        at         : Point,
        stroke     : Stroke,
        fill       : Paint?,
        textSpacing: TextSpacing
    ) = text(StyledText(text, font, fill, stroke = stroke), at, textSpacing)

    override fun text(text: StyledText, at: Point, textSpacing: TextSpacing) {
        val shadowParagraph by lazy { text.fill(textSpacing = textSpacing) }

        draw({
            shadowParagraph.updateForegroundPaint(0, shadowParagraph.getText().length, it).paint(skiaCanvas, at.x.toFloat(), at.y.toFloat())
        }) {
            drawWithBlurredBackground(
                text,
                at,
                { text, paint -> text.paragraph(textSpacing = textSpacing, paint = paint) },
                fill         = { text.fill     (textSpacing = textSpacing               ) },
                stroke       = { text.stroke   (textSpacing = textSpacing               ) },
            )
        }
    }

    override fun wrapped(
        text       : String,
        at         : Point,
        width      : Double,
        fill       : Paint,
        font       : Font?,
        indent     : Double,
        alignment  : TextAlignment,
        lineSpacing: Float,
        textSpacing: TextSpacing
    ) {
        draw(fill, null) {
            paragraph(text, font, indent, width, it, alignment, lineSpacing, textSpacing).paint(
                skiaCanvas,
                at.x.toFloat(),
                at.y.toFloat()
            )
        }
    }

    override fun wrapped(
        text       : String,
        at         : Point,
        width      : Double,
        stroke     : Stroke,
        fill       : Paint?,
        font       : Font?,
        indent     : Double,
        alignment  : TextAlignment,
        lineSpacing: Float,
        textSpacing: TextSpacing
    ) {
        draw(fill, stroke) {
            paragraph(text, font, indent, width, it, alignment, lineSpacing, textSpacing).paint(
                skiaCanvas,
                at.x.toFloat(),
                at.y.toFloat()
            )
        }
    }

    override fun wrapped(
        text       : StyledText,
        at         : Point,
        width      : Double,
        indent     : Double,
        alignment  : TextAlignment,
        lineSpacing: Float,
        textSpacing: TextSpacing
    ) {
        val shadowParagraph by lazy {
            text.fill(
                width       = width,
                indent      = indent,
                alignment   = alignment,
                lineHeight  = lineSpacing,
                textSpacing = textSpacing,
            )
        }

        draw({
            shadowParagraph.updateForegroundPaint(0, shadowParagraph.getText().length, it).paint(skiaCanvas, at.x.toFloat(), at.y.toFloat())
        }) {
            drawWithBlurredBackground(
                text,
                at,
                { text, paint -> text.paragraph(alignment, lineSpacing, textSpacing, indent, width, paint) },
                fill         = { text.fill     (alignment, lineSpacing, textSpacing, indent, width       ) },
                stroke       = { text.stroke   (alignment, lineSpacing, textSpacing, indent, width       ) }
            )
        }
    }

    override fun image(image: Image, destination: Rectangle, opacity: Float, radius: Double, source: Rectangle) {
        if (image is ImageImpl) {
            val rect = when {
                radius > 0.0 -> destination.rrect(radius.toFloat())
                else         -> destination.skia()
            }

            draw({
                when (rect) {
                    is RRect -> skiaCanvas.drawRRect(rect, it)
                    else     -> skiaCanvas.drawRect (rect, it)
                }
            }) {
                skiaCanvas.save()
                when (rect) {
                    is RRect -> skiaCanvas.clipRRect(rect)
                    else     -> skiaCanvas.clipRect (rect)
                }
                skiaCanvas.drawImageRect(image.skiaImage, source.skia(), destination.skia(), (Black opacity opacity).paint.skia())
                skiaCanvas.restore()
            }
        } else if (image is SvgImage) {
            // TODO support shadows here as well
            image.render(skiaCanvas, source.skia(), destination.skia(), radius.toFloat())
        }
    }

    override fun line(start: Point, end: Point, stroke: Stroke) {
        draw(null, stroke) {
            skiaCanvas.drawLine(start.x.toFloat(), start.y.toFloat(), end.x.toFloat(), end.y.toFloat(), it)
        }
    }

    override fun path(points: List<Point>, fill: Paint, fillRule: Renderer.FillRule?) {
        if (points.isNotEmpty()) {
            draw(fill, null) {
                skiaCanvas.drawPath(points.toPath().skia(), it)
            }
        }
    }

    override fun path(path: Path, fill: Paint, fillRule: Renderer.FillRule?) {
        val skiaPath = path.skia()

        if (fillRule != null) {
            skiaPath.fillMode = fillRule.skia()
        }

        draw(fill, null) {
            skiaCanvas.drawPath(skiaPath, it)
        }
    }

    override fun path(points: List<Point>, stroke: Stroke) {
        if (points.isNotEmpty()) {
            path(points.toPath(), stroke)
        }
    }

    override fun path(path: Path, stroke: Stroke) {
        val skiaPath = path.skia()

        draw(null, stroke) {
            skiaCanvas.drawPath(skiaPath, it)
        }
    }

    override fun path(points: List<Point>, stroke: Stroke, fill: Paint, fillRule: Renderer.FillRule?) {
        if (points.isNotEmpty()) {
            path(points.toPath(), stroke, fill, fillRule)
        }
    }

    override fun path(path: Path, stroke: Stroke, fill: Paint, fillRule: Renderer.FillRule?) {
        val skiaPath = path.skia()

        if (fillRule != null) {
            skiaPath.fillMode = fillRule.skia()
        }

        draw(fill, stroke) {
            skiaCanvas.drawPath(skiaPath, it)
        }
    }

    override fun poly(polygon: Polygon, fill: Paint) {
        val path by lazy { polygon.toPath() }

        this.draw(fill, null) {
            skiaCanvas.drawPath(path.skia(), it)
        }
    }

    override fun poly(polygon: Polygon, stroke: Stroke, fill: Paint?) {
        val path by lazy { polygon.toPath() }

        this.draw(fill, stroke) {
            skiaCanvas.drawPath(path.skia(), it)
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

    override fun clip(path: Path, block: Canvas.() -> Unit) {
        clip(path.skia(), block)
    }

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

    private val Font?.newTextStyle get() = when (this) {
        is FontImpl -> this.textStyle()
        else        -> defaultFont.textStyle()
    }

    private fun Paint.skia(): SkiaPaint {
        val result = SkiaPaint()

        when (this) {
            is ColorPaint          -> result.color  = color.skia()
            is LinearGradientPaint -> result.shader = Shader.makeLinearGradient(start.skia(), end.skia(), colors.map { it.color.skia() }.toIntArray(), colors.map { it.offset }.toFloatArray())
            is RadialGradientPaint -> result.shader = Shader.makeTwoPointConicalGradient(start.center.skia(), start.radius.toFloat(), end.center.skia(), end.radius.toFloat(), colors.map { it.color.skia() }.toIntArray(), colors.map { it.offset }.toFloatArray())
            is ImagePaint          -> (image as? ImageImpl)?.let {
                result.color  = (Black opacity opacity).skia()
                result.shader = it.skiaImage.makeShader(REPEAT, REPEAT, Matrix33.Companion.makeScale((size.width / image.width).toFloat(), (size.height / image.height).toFloat()))
            }
            is PatternPaint        -> {
                // FIXME: Reuse bitmaps?
                val bitmap = Bitmap().apply {
                    allocN32Pixels(size.width.toInt(), size.height.toInt())
                }

                val bitmapCanvas = SkiaCanvas(bitmap)

                paint(PatternCanvasWrapper(CanvasImpl(bitmapCanvas, defaultFont, fontCollection).apply { size = this@skia.size }))

                val matrix = (Identity.translate(bounds.x, bounds.y) * transform).skia33()

                result.shader = bitmap.makeShader(REPEAT, REPEAT, MITCHELL, matrix)
            }
            is SweepGradientPaint  -> {
                result.shader = Shader.makeSweepGradient(
                    center     = center.skia(),
                    startAngle = 0f,
                    endAngle   = 360f,
                    colors     = colors.map { it.color.skia() }.toIntArray(),
                    positions  = colors.map { it.offset }.toFloatArray(),
                    style      = GradientStyle(DECAL, isPremul = true, Identity.rotate(around = center, rotation).skia33())
                )
            }
            is FrostedGlassPaint   -> result.color = (color ?: Transparent).skia() // Background blurring happens elsewhere, so we just need to treat this like ColorPaint now
        }

        return result
    }

    private fun Stroke.skia(): SkiaPaint = fill.skia().also { it.setStroke(this) }

    private fun SkiaPaint.setStroke(stroke: Stroke) {
        setStroke(true)
        strokeWidth = stroke.thickness.toFloat()
        stroke.dashes?.let { dashes ->
            val fixedDashes = when {
                dashes.size.isOdd -> { doubleArrayOf(*dashes, dashes.last()) }
                else              -> dashes
            }

            pathEffect = PathEffect.makeDash(fixedDashes.map { it.toFloat() }.toFloatArray(), stroke.dashOffset.toFloat())
        }

        strokeCap = when (stroke.lineCap) {
            LineCap.Square -> SQUARE
            LineCap.Round  -> ROUND
            else           -> BUTT
        }

        strokeJoin = when (stroke.lineJoint) {
            LineJoint.Round -> PaintStrokeJoin.ROUND
            LineJoint.Bevel -> PaintStrokeJoin.BEVEL
            else            -> PaintStrokeJoin.MITER
        }
    }

    private fun Renderer.FillRule.skia() = when (this) {
        EvenOdd -> EVEN_ODD
        else    -> WINDING
    }

    private fun clip(path: SkiaPath, block: Canvas.() -> Unit) {
        skiaCanvas.save()
        skiaCanvas.clipPath(path, antiAlias = true)
        block(this)
        skiaCanvas.restore()
    }

    private fun draw(fill: Paint?, stroke: Stroke?, block: (SkiaPaint) -> Unit) {
        drawOuterShadows(fill, stroke, block)
        drawBackground(fill, stroke, block)

        fill?.let   { block(it.skia()) }
        stroke?.let { block(it.skia()) }

        drawInnerShadows(fill, stroke, block)
    }

    private fun draw(shadowed: (SkiaPaint) -> Unit, block: () -> Unit) {
        drawOuterShadows(fill = Black.paint, stroke = null, shadowed)
        block()
        drawInnerShadows(fill = Black.paint, stroke = null, shadowed)
    }

    private fun drawOuterShadows(fill: Paint?, stroke: Stroke?, operation: (SkiaPaint) -> Unit) = drawShadows(
        fill,
        stroke,
        shadows.filterIsInstance<OuterShadow>(),
        operation
    ) { shadow, old ->
        ImageFilter.makeDropShadowOnly(
            dx     = shadow.horizontal.toFloat(),
            dy     = shadow.vertical.toFloat(),
            sigmaX = shadow.blurRadius.toFloat(),
            sigmaY = shadow.blurRadius.toFloat(),
            color  = shadow.color.skia(),
            input  = old
        )
    }

    private fun drawInnerShadows(fill: Paint?, stroke: Stroke?, operation: (SkiaPaint) -> Unit) = drawShadows(
        fill,
        stroke,
        shadows.filterIsInstance<InnerShadow>(),
        operation
    ) { shadow, old ->
        innerShadow(
            color   = shadow.color,
            offsetX = shadow.horizontal,
            offsetY = shadow.vertical,
            blur    = shadow.blurRadius,
            filter  = old
        )
    }

    private fun drawShadow(shadow: Shadow, paint: SkiaPaint, operation: (SkiaPaint) -> Unit, imageFilter: (Shadow, ImageFilter?) -> ImageFilter) {
        val oldFilter = paint.imageFilter

        paint.imageFilter = imageFilter(shadow, oldFilter)

        operation(paint)

        paint.imageFilter = oldFilter
    }

    private fun drawShadows(
        fill       : Paint?,
        stroke     : Stroke?,
        shadows    : List<Shadow>,
        operation  : (SkiaPaint) -> Unit,
        imageFilter: (Shadow, ImageFilter?) -> ImageFilter
    ) {
        shadows.forEach { shadow ->
            fill?.let   { drawShadow(shadow, it.skia(), operation, imageFilter) }
            stroke?.let { drawShadow(shadow, it.skia(), operation, imageFilter) }
        }
    }

    private fun drawWithBlurredBackground(paint: SkiaPaint, blurRadius: Float, block: (SkiaPaint) -> Unit) {
        canvasClip?.let {
            skiaCanvas.clipRect(it)
        }

        // Backdrop blur layer
        skiaCanvas.saveLayer(SaveLayerRec(
            backdrop       = ImageFilter.makeBlur(blurRadius, blurRadius, CLAMP),
            saveLayerFlags = SaveLayerFlags(InitWithPrevious),
        ))

        // Shape mask layer
        skiaCanvas.saveLayer(null, SkiaPaint().apply { blendMode = DST_IN })

        block(paint)

        skiaCanvas.restore()
        skiaCanvas.restore()
    }

    private fun drawWithBlurredBackground(
        text  : StyledText,
        at    : Point,
        block : (StyledText, TextStyle.(io.nacular.doodle.text.Style) -> Unit) -> Paragraph,
        fill  : () -> Paragraph,
        stroke: () -> Paragraph,
    ) {
        text.forEach { (_,style) ->
            (style.foreground as? FrostedGlassPaint)?.let { paint ->
                drawWithBlurredBackground(SkiaPaint(), paint.blurRadius.toFloat()) {
                    block(text) {
                        foreground = if (it.foreground == paint) Black.paint.skia() else Transparent.paint.skia()
                    }.paint(skiaCanvas, at.x.toFloat(), at.y.toFloat())
                }
            }
        }

        fill().paint(skiaCanvas, at.x.toFloat(), at.y.toFloat())

        text.forEach { (_,style) ->
            (style.stroke?.fill as? FrostedGlassPaint)?.let { paint ->
                drawWithBlurredBackground(SkiaPaint(), paint.blurRadius.toFloat()) {
                    block(text) {
                        foreground = Stroke(
                            fill       = if (it.stroke?.fill == paint) Black.paint else Transparent.paint,
                            thickness  = it.stroke?.thickness ?: 0.0,
                            dashes     = it.stroke?.dashes,
                            dashOffset = it.stroke?.dashOffset ?: 0.0,
                            lineJoint  = it.stroke?.lineJoint,
                            lineCap    = it.stroke?.lineCap,
                        ).skia()
                    }.paint(skiaCanvas, at.x.toFloat(), at.y.toFloat())
                }
            }
        }

        stroke().paint(skiaCanvas, at.x.toFloat(), at.y.toFloat())
    }

    private fun drawBackground(fill: Paint?, stroke: Stroke?, block: (SkiaPaint) -> Unit) {
        (fill         as? FrostedGlassPaint)?.let { drawWithBlurredBackground(SkiaPaint(),                             it.blurRadius.toFloat(), block) }
        (stroke?.fill as? FrostedGlassPaint)?.let { drawWithBlurredBackground(SkiaPaint().apply { setStroke(stroke) }, it.blurRadius.toFloat(), block) }
    }

    private fun paragraph(
        text       : String,
        font       : Font?,
        indent     : Double? = null,
        width      : Double? = null,
        paint      : SkiaPaint?,
        alignment  : TextAlignment,
        lineHeight : Float = 1f,
        textSpacing: TextSpacing
    ): Paragraph {
        val builder = ParagraphBuilder(ParagraphStyle().apply {
            this.alignment = alignment.skia
        }, fontCollection).run {
            if (indent != null && indent != 0.0) {
                addPlaceholder(PlaceholderStyle(indent.toFloat(), 0f, BASELINE, IDEOGRAPHIC, 0f))
            }

            pushStyle(font.newTextStyle.apply {
                foreground = paint

                if (lineHeight                >= 0f ) height        = lineHeight
                if (textSpacing.wordSpacing   >= 0.0) wordSpacing   = textSpacing.wordSpacing.toFloat()
                if (textSpacing.letterSpacing >= 0.0) letterSpacing = textSpacing.letterSpacing.toFloat()
            })
            addText(text)
        }

        val paragraph = builder.build()

        paragraph.layout(POSITIVE_INFINITY)

        if (width != null) {
            paragraph.layout(max(paragraph.minIntrinsicWidth + 1, width.toFloat()))
        }

        return paragraph
    }

    private val TextAlignment.skia: Alignment get() = when (this) {
        Start   -> Alignment.LEFT
        Center  -> Alignment.CENTER
        End     -> Alignment.RIGHT
        Justify -> Alignment.JUSTIFY
    }

    /**
     * Adapted from: https://github.com/flutter/flutter/issues/18636#issuecomment-1066475971
     */
    private fun innerShadow(color: Color, offsetX: Double, offsetY: Double, blur: Double, filter: ImageFilter?): ImageFilter {
        val f1 = ImageFilter.makeColorFilter(ColorFilter.makeBlend(color.skia(), SRC_OUT), input = filter, crop = null)
        val f2 = ImageFilter.makeOffset(offsetX.toFloat(), offsetY.toFloat(), f1, crop = null)
        val f3 = ImageFilter.makeBlur(blur.toFloat(), blur.toFloat(), DECAL, f2)

        return ImageFilter.makeBlend(BlendMode.SRC_IN, bg = null, f3, crop = null)
    }

    private fun StyledText.fill(
        alignment  : TextAlignment = Start,
        lineHeight : Float         = 1f,
        textSpacing: TextSpacing,
        indent     : Double        = 0.0,
        width      : Double?       = null,
    ) = paragraph(alignment, lineHeight, textSpacing, indent, width) {
        foreground = (it.foreground ?: Black.paint).skia()

        it.background?.skia()?.let { background = it }
        it.decoration?.run {
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
                2f
            )
        }
    }

    private fun StyledText.stroke(
        alignment  : TextAlignment = Start,
        lineHeight : Float         = 1f,
        textSpacing: TextSpacing,
        indent     : Double        = 0.0,
        width      : Double?       = null,
    ) = paragraph(alignment, lineHeight, textSpacing, indent, width) {
        foreground = it.stroke?.skia() ?: Transparent.paint.skia()
    }

    private fun StyledText.paragraph(
        alignment  : TextAlignment = Start,
        lineHeight : Float         = 1f,
        textSpacing: TextSpacing,
        indent     : Double        = 0.0,
        width      : Double?       = null,
        paint      : TextStyle.(io.nacular.doodle.text.Style) -> Unit,
    ): Paragraph {
        val builder = ParagraphBuilder(ParagraphStyle().apply {
            this.alignment = alignment.skia
        }, fontCollection).also { builder ->

            if (indent != 0.0) {
                builder.addPlaceholder(PlaceholderStyle(indent.toFloat(), 0f, BASELINE, IDEOGRAPHIC, 0f))
            }

            this.forEach { (text, style) ->
                builder.pushStyle(style.font.newTextStyle.apply {
                    paint(style)

                    if (lineHeight                >= 0f ) { height = lineHeight }
                    if (textSpacing.wordSpacing   >= 0.0) { this.wordSpacing   = textSpacing.wordSpacing.toFloat()   }
                    if (textSpacing.letterSpacing >= 0.0) { this.letterSpacing = textSpacing.letterSpacing.toFloat() }
                })
                builder.addText(text)
                builder.popStyle()
            }
        }

        return builder.build().apply {
            layout(POSITIVE_INFINITY)

            width?.let {
                layout(max(minIntrinsicWidth + 1, it.toFloat()))
            }
        }
    }

    private fun List<Point>.toPath() = path(from = this[0]).run {
        subList(1, size).forEach { lineTo(it) }
        finish()
    }

    private fun skiaArc(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, stroke: Stroke?, fill: Paint?, includeCenter: Boolean) {
        val boundingRect = Circle(center, radius).boundingRectangle

        draw(fill, stroke) {
            skiaCanvas.drawArc(
                boundingRect.x.toFloat(),
                boundingRect.y.toFloat(),
                boundingRect.right.toFloat(),
                boundingRect.bottom.toFloat(),
                (rotation `in` radians).toFloat(),
                (sweep `in` radians).toFloat(),
                includeCenter,
                it
            )
        }
    }
}