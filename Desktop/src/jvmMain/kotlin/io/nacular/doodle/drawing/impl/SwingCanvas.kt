package io.nacular.doodle.drawing.impl

import io.nacular.doodle.core.Camera
import io.nacular.doodle.drawing.AffineTransform
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.ColorPaint
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.LinearGradientPaint
import io.nacular.doodle.drawing.Paint
import io.nacular.doodle.drawing.RadialGradientPaint
import io.nacular.doodle.drawing.Renderer
import io.nacular.doodle.drawing.Shadow
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.drawing.height
import io.nacular.doodle.geometry.Circle
import io.nacular.doodle.geometry.Ellipse
import io.nacular.doodle.geometry.Path
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Polygon
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.image.Image
import io.nacular.doodle.image.impl.ImageImpl
import io.nacular.doodle.skia.skia
import io.nacular.doodle.text.StyledText
import io.nacular.doodle.text.TextSpacing
import io.nacular.doodle.theme.native.toAwt
import io.nacular.doodle.utils.TextAlignment
import io.nacular.measured.units.Angle
import io.nacular.measured.units.Measure
import org.jetbrains.skia.PathVerb
import java.awt.BasicStroke
import java.awt.BasicStroke.CAP_BUTT
import java.awt.BasicStroke.CAP_ROUND
import java.awt.BasicStroke.CAP_SQUARE
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.Shape
import java.awt.font.FontRenderContext
import java.awt.font.GlyphVector
import java.awt.font.LineBreakMeasurer
import java.awt.font.TextAttribute
import java.awt.geom.CubicCurve2D
import java.awt.geom.Ellipse2D
import java.awt.geom.Path2D
import java.awt.geom.Point2D
import java.awt.image.BufferedImage
import java.awt.image.BufferedImageOp
import java.awt.image.ImageObserver
import java.awt.image.ImageObserver.ALLBITS
import java.awt.image.RenderedImage
import java.awt.image.renderable.RenderableImage
import java.text.AttributedCharacterIterator
import java.text.AttributedString
import org.jetbrains.skia.Font as SkiaFont
import org.jetbrains.skia.Image as SkiaImage
import java.awt.Image as AwtImage
import java.awt.geom.AffineTransform as AWTTransform


internal class SwingCanvas(private val graphics: Graphics2D, private val defaultFont: SkiaFont, private val initialSize: Size): Canvas {

    private class ShadowedCanvas(private val delegate: Graphics2D, private val shadow: Shadow): DelegatingGraphics2D(delegate) {

        private class MonoChromeGraphics(delegate: Graphics2D, color: io.nacular.doodle.drawing.Color): DelegatingGraphics2D(delegate) {
            init {
                super.setColor(color.toAwt())
            }

            override fun setColor     (c    : Color?         ) { /* no-op */ }
            override fun setPaint     (paint: java.awt.Paint?) { /* no-op */ }
            override fun setBackground(color: Color?         ) { /* no-op */ }
        }

        private fun <T> shadowed(block: Graphics2D.() -> T): T {
            delegate.translate(shadow.horizontal, shadow.vertical)

            val color = delegate.color

            // FIXME: Support blurring
            block(MonoChromeGraphics(delegate, shadow.color)) // draw shadow

            delegate.translate(-shadow.horizontal, -shadow.vertical)
            delegate.color = color

            return block(delegate) // draw normal
        }

        override fun drawLine     (x1: Int, y1: Int, x2: Int, y2: Int                                     ) = shadowed { drawLine(x1, y1, x2, y2) }
        override fun fillRect     (x: Int, y: Int, width: Int, height: Int                                ) = shadowed { fillRect(x, y, width, height) }
        override fun clearRect    (x: Int, y: Int, width: Int, height: Int                                ) = shadowed { clearRect(x, y, width, height) }
        override fun drawRoundRect(x: Int, y: Int, width: Int, height: Int, arcWidth: Int, arcHeight: Int ) = shadowed { drawRoundRect(x, y, width, height, arcWidth, arcHeight) }
        override fun fillRoundRect(x: Int, y: Int, width: Int, height: Int, arcWidth: Int, arcHeight: Int ) = shadowed { fillRoundRect(x, y, width, height, arcWidth, arcHeight) }
        override fun drawOval     (x: Int, y: Int, width: Int, height: Int                                ) = shadowed { drawOval(x, y, width, height) }
        override fun fillOval     (x: Int, y: Int, width: Int, height: Int                                ) = shadowed { fillOval(x, y, width, height) }
        override fun drawArc      (x: Int, y: Int, width: Int, height: Int, startAngle: Int, arcAngle: Int) = shadowed { drawArc(x, y, width, height, startAngle, arcAngle) }
        override fun fillArc      (x: Int, y: Int, width: Int, height: Int, startAngle: Int, arcAngle: Int) = shadowed { fillArc(x, y, width, height, startAngle, arcAngle) }

        override fun drawPolyline (xPoints: IntArray?, yPoints: IntArray?, nPoints: Int) = shadowed { drawPolygon(xPoints, yPoints, nPoints) }
        override fun drawPolygon  (xPoints: IntArray?, yPoints: IntArray?, nPoints: Int) = shadowed { drawPolygon(xPoints, yPoints, nPoints) }
        override fun fillPolygon  (xPoints: IntArray?, yPoints: IntArray?, nPoints: Int) = shadowed { fillPolygon(xPoints, yPoints, nPoints) }

        override fun drawString(str: String,  x: Int,   y: Int                            ) = shadowed { drawString(str,      x, y) }
        override fun drawString(str: String?, x: Float, y: Float                          ) = shadowed { drawString(str,      x, y) }
        override fun drawString(iterator: AttributedCharacterIterator?, x: Int,   y: Int  ) = shadowed { drawString(iterator, x, y) }
        override fun drawString(iterator: AttributedCharacterIterator?, x: Float, y: Float) = shadowed { drawString(iterator, x, y) }

        override fun drawImage(img: AwtImage?, xform: java.awt.geom.AffineTransform?, obs: ImageObserver?                                                               ) = shadowed { drawImage(img, xform, obs) }
        override fun drawImage(img: BufferedImage?, op: BufferedImageOp?, x: Int, y: Int                                                                                ) = shadowed { drawImage(img, op, x, y) }
        override fun drawImage(img: AwtImage?, x: Int, y: Int, observer: ImageObserver?                                                                                 ) = shadowed { drawImage(img, x, y, observer) }
        override fun drawImage(img: AwtImage?, x: Int, y: Int, width: Int, height: Int, observer: ImageObserver?                                                        ) = shadowed { drawImage(img, x, y, width, height, observer) }
        override fun drawImage(img: AwtImage?, x: Int, y: Int, bgcolor: Color?, observer: ImageObserver?                                                                ) = shadowed { drawImage(img, x, y, bgcolor, observer) }
        override fun drawImage(img: AwtImage?, x: Int, y: Int, width: Int, height: Int, bgcolor: Color?, observer: ImageObserver?                                       ) = shadowed { drawImage(img, x, y, width, height, bgcolor, observer) }
        override fun drawImage(img: AwtImage?, dx1: Int, dy1: Int, dx2: Int, dy2: Int, sx1: Int, sy1: Int, sx2: Int, sy2: Int, observer: ImageObserver?                 ) = shadowed { drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer) }
        override fun drawImage(img: AwtImage?, dx1: Int, dy1: Int, dx2: Int, dy2: Int, sx1: Int, sy1: Int, sx2: Int, sy2: Int, bgcolor: Color?, observer: ImageObserver?) = shadowed { drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer) }

        override fun draw(s: Shape?) = shadowed { draw(s) }

        override fun drawRenderedImage(img: RenderedImage?, xform: java.awt.geom.AffineTransform?) = shadowed { drawRenderedImage(img, xform) }

        override fun drawRenderableImage(img: RenderableImage?, xform: java.awt.geom.AffineTransform?) = shadowed { drawRenderableImage(img, xform) }

        override fun drawGlyphVector(g: GlyphVector?, x: Float, y: Float) = shadowed { drawGlyphVector(g, x, y) }

        override fun fill(s: Shape?) = shadowed { fill(s) }
    }

    init {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    override fun transform(transform: AffineTransform, block: Canvas.() -> Unit) {
        val oldTransform = graphics.transform.clone() as AWTTransform
        val newTransform = oldTransform.clone() as AWTTransform

        with(transform) {
            // for some reason graphics.transform.concatenate doesn't change the graphics.transform
            newTransform.concatenate(AWTTransform(
                scaleX,     shearY,
                shearX,     scaleY,
                translateX, translateY
            ))

            graphics.transform = newTransform
        }

        block()

        graphics.transform = oldTransform
    }

    override fun transform(transform: AffineTransform, camera: Camera, block: Canvas.() -> Unit) {
        // TODO: No perspective transform support in Swing
        transform(transform, block)
    }

    override fun clip(rectangle: Rectangle, radius: Double, block: Canvas.() -> Unit) {
        clip(java.awt.Rectangle(
            rectangle.position.run { java.awt.Point(x.toInt(), y.toInt()) },
            rectangle.size.run { Dimension(width.toInt(), height.toInt()) }
        ), block)
    }

    override fun clip(polygon: Polygon, block: Canvas.() -> Unit) {
        clip(java.awt.Polygon(
            polygon.points.map { it.x.toInt() }.toIntArray(),
            polygon.points.map { it.y.toInt() }.toIntArray(),
            polygon.points.size
        ), block)
    }

    override fun clip(ellipse: Ellipse, block: Canvas.() -> Unit) {
        clip(Ellipse2D.Double(
            ellipse.center.x,
            ellipse.center.y,
            ellipse.xRadius,
            ellipse.yRadius
        ), block)
    }

    override fun clip(path: Path, block: Canvas.() -> Unit) {
        clip(path.toShape(), block)
    }

    override fun shadow(shadow: Shadow, block: Canvas.() -> Unit) {
        block(SwingCanvas(ShadowedCanvas(graphics, shadow), defaultFont, initialSize))
    }

    override var size: Size get() = initialSize; set(value) {}

    override fun rect(rectangle: Rectangle,                                 fill: Paint ) = rect_(rectangle,                 fill = fill)
    override fun rect(rectangle: Rectangle,                 stroke: Stroke, fill: Paint?) = rect_(rectangle, stroke,                fill)
    override fun rect(rectangle: Rectangle, radius: Double,                 fill: Paint ) = rect_(rectangle, radius,         fill = fill)
    override fun rect(rectangle: Rectangle, radius: Double, stroke: Stroke, fill: Paint?) = rect_(rectangle, radius, stroke,        fill)

    override fun circle(circle: Circle,                 fill: Paint ) = ellipse(circle,         fill)
    override fun circle(circle: Circle, stroke: Stroke, fill: Paint?) = ellipse(circle, stroke, fill)

    override fun ellipse(ellipse: Ellipse,                 fill: Paint ) = ellipse_(ellipse,         fill = fill)
    override fun ellipse(ellipse: Ellipse, stroke: Stroke, fill: Paint?) = ellipse_(ellipse, stroke,        fill)

    override fun text(text: String, font: Font?, at: Point, fill: Paint, textSpacing: TextSpacing) {
        text(StyledText(text, font, foreground = fill), at, textSpacing)
    }

    override fun text(text: String, font: Font?, at: Point, stroke: Stroke, fill: Paint?, textSpacing: TextSpacing) {
        text(StyledText(text, font, foreground = fill), at, textSpacing)
    }

    override fun text(text: StyledText, at: Point, textSpacing: TextSpacing) {
        graphics.drawString(text.attributedString().iterator, at.x.toInt(), at.y.toInt())
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
        drawWrappedText(graphics, StyledText(text, font), Rectangle(at.x, at.y, width, height))
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
        drawWrappedText(graphics, StyledText(text, font), Rectangle(at.x, at.y, width, height))
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
        TODO("Not yet implemented")
    }

    override fun image(image: Image, destination: Rectangle, opacity: Float, radius: Double, source: Rectangle) {
        graphics.drawImage(
            (image as ImageImpl).skiaImage.awt,
            destination.x.toInt     (),
            destination.y.toInt     (),
            destination.width.toInt (),
            destination.height.toInt(),
            source.x.toInt          (),
            source.y.toInt          (),
            source.width.toInt      (),
            source.height.toInt     ()
        ) { _,flags,_,_,_,_ ->
            flags == ALLBITS
        }
    }

    override fun clear() {
        graphics.clearRect(0, 0, initialSize.width.toInt(), initialSize.height.toInt())
    }

    override fun flush() {
        // NO-OP
    }

    override fun line(start: Point, end: Point, stroke: Stroke) = graphics.with(stroke) {
        drawLine(start.x.toInt(), start.y.toInt(), end.x.toInt(), end.y.toInt())
    }

    override fun path(points: List<Point>, fill: Paint, fillRule: Renderer.FillRule?) = path_(points, fill = fill)

    override fun path(path: Path, fill: Paint, fillRule: Renderer.FillRule?) = graphics.with(fill) {
        fill(path.toShape())
    }

    override fun path(points: List<Point>, stroke: Stroke) = path_(points, stroke)

    override fun path(path: Path, stroke: Stroke) = graphics.with(stroke) {
        draw(path.toShape())
    }

    override fun path(points: List<Point>, stroke: Stroke, fill: Paint, fillRule: Renderer.FillRule?) = path_(points, stroke, fill)

    override fun path(path: Path, stroke: Stroke, fill: Paint, fillRule: Renderer.FillRule?) {
        graphics.with(fill) {
            fill(path.toShape())
        }
        graphics.with(stroke) {
            draw(path.toShape())
        }
    }

    override fun poly(polygon: Polygon,                 fill: Paint ) = path_(polygon.points,         fill = fill)
    override fun poly(polygon: Polygon, stroke: Stroke, fill: Paint?) = path_(polygon.points, stroke,        fill)

    override fun arc(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, fill: Paint) {
        TODO("Not yet implemented")
    }

    override fun arc(
        center  : Point,
        radius  : Double,
        sweep   : Measure<Angle>,
        rotation: Measure<Angle>,
        stroke  : Stroke,
        fill    : Paint?
    ) {
        TODO("Not yet implemented")
    }

    override fun wedge(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, fill: Paint) {
        TODO("Not yet implemented")
    }

    override fun wedge(
        center  : Point,
        radius  : Double,
        sweep   : Measure<Angle>,
        rotation: Measure<Angle>,
        stroke  : Stroke,
        fill    : Paint?
    ) {
        TODO("Not yet implemented")
    }

    private fun clip(shape: Shape, block: Canvas.() -> Unit) {
        val oldClip = graphics.clip

        graphics.clip = shape

        block()

        graphics.clip = oldClip
    }

    private fun drawWrappedText(g: Graphics2D, text: StyledText, bounds: Rectangle) {
        val context  = FontRenderContext(null, true, false)
        val measurer = LineBreakMeasurer(text.attributedString().iterator, context)

        measurer.nextLayout(bounds.width.toFloat())
    }

    private fun StyledText.attributedString(textSpacing: TextSpacing = TextSpacing()) = AttributedString(text).apply {
        var start = 0

        forEach {
            addAttribute(
                TextAttribute.FONT,
                it.second.font.toAwt(defaultFont).deriveFont(mapOf(TextAttribute.TRACKING to textSpacing.letterSpacing)),
                start,
                start + it.first.length
            )

            start += it.first.length
        }
    }

    private val SkiaImage.awt: AwtImage get() = BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB).apply {
        val graphics = createGraphics()
        graphics.drawImage(this@awt.awt, 0, 0, null)
        graphics.dispose()
    }

    private fun Path.toShape() = Path2D.Float().apply {
        skia().forEach {
            when (it?.verb) {
                PathVerb.MOVE  -> moveTo (it.p0!!.x, it.p0!!.y)
                PathVerb.LINE  -> lineTo (it.p0!!.x, it.p0!!.y)
                PathVerb.QUAD  -> quadTo (it.p0!!.x, it.p0!!.y, it.p1!!.x, it.p1!!.y)
                PathVerb.CONIC -> curveTo(it.p0!!.x, it.p0!!.y, it.p1!!.x, it.p1!!.y, it.p2!!.x, it.p2!!.y)
                PathVerb.CUBIC -> append(CubicCurve2D.Float(it.p0!!.x, it.p0!!.y, it.p1!!.x, it.p1!!.y, it.p2!!.x, it.p2!!.y, it.p3!!.x, it.p3!!.y), true)
                else           -> closePath()
            }
        }
    }

    private fun Paint.awt() = when (this) {
        is ColorPaint           -> Color(this.color.run { (red.toUInt() shl 16) + (green.toUInt() shl 8) + blue.toUInt() }.toInt())
        is LinearGradientPaint -> java.awt.LinearGradientPaint(
            start.run  { Point2D.Double(x, y) },
            end.run    { Point2D.Double(x, y) },
            colors.map { it.offset        }.toFloatArray(),
            colors.map { it.color.toAwt() }.toTypedArray()
        )
        is RadialGradientPaint -> java.awt.RadialGradientPaint(
            start.center.run { Point2D.Double(x, y) },
            start.radius.toFloat(),
            colors.map { it.offset        }.toFloatArray(),
            colors.map { it.color.toAwt() }.toTypedArray()
        )
        else -> Color.BLACK
    }

    private fun Stroke.awt() = BasicStroke(
        thickness.toFloat(), //  lineWidth
        when (lineCap) {
            Stroke.LineCap.Butt  -> CAP_BUTT
            Stroke.LineCap.Round -> CAP_ROUND
            else                 -> CAP_SQUARE
        },
        when (lineJoint) {
            Stroke.LineJoint.Round -> BasicStroke.JOIN_ROUND
            Stroke.LineJoint.Bevel -> BasicStroke.JOIN_BEVEL
            else                   ->  BasicStroke.JOIN_MITER
        },
        10f,
        dashes?.map { it.toFloat() }?.toFloatArray(),
        0f
    )

    private fun rect_(rectangle: Rectangle, stroke: Stroke? = null, fill: Paint? = null) {
        graphics.with(stroke) {
            drawRect(
                rectangle.x.toInt     (),
                rectangle.y.toInt     (),
                rectangle.width.toInt (),
                rectangle.height.toInt()
            )
        }
        graphics.with(fill) {
            fillRect(
                rectangle.x.toInt     (),
                rectangle.y.toInt     (),
                rectangle.width.toInt (),
                rectangle.height.toInt()
            )
        }
    }

    private fun rect_(rectangle: Rectangle, radius: Double, stroke: Stroke? = null, fill: Paint? = null) {
        graphics.with(fill) {
            fillRoundRect(
                rectangle.x.toInt     (),
                rectangle.y.toInt     (),
                rectangle.width.toInt (),
                rectangle.height.toInt(),
                radius.toInt(),
                radius.toInt()
            )
        }
        graphics.with(stroke) {
            drawRoundRect(
                rectangle.x.toInt     (),
                rectangle.y.toInt     (),
                rectangle.width.toInt (),
                rectangle.height.toInt(),
                radius.toInt(),
                radius.toInt()
            )
        }
    }

    private fun ellipse_(el: Ellipse, stroke: Stroke? = null, fill: Paint? = null) {
        graphics.with(fill) {
            fillOval(el.center.x.toInt(), el.center.y.toInt(), el.xRadius.toInt(), el.yRadius.toInt())
        }
        graphics.with(stroke) {
            drawOval(el.center.x.toInt(), el.center.y.toInt(), el.xRadius.toInt(), el.yRadius.toInt())
        }
    }

    private fun path_(points: List<Point>, stroke: Stroke? = null, fill: Paint? = null) {
        graphics.with(fill) {
            fillPolygon(
                points.map { it.x.toInt() }.toIntArray(),
                points.map { it.y.toInt() }.toIntArray(),
                points.size
            )
        }
        graphics.with(stroke) {
            drawPolyline(
                points.map { it.x.toInt() }.toIntArray(),
                points.map { it.y.toInt() }.toIntArray(),
                points.size
            )
        }
    }

    private fun Graphics2D.with(fill: Paint?, block: Graphics2D.() -> Unit) {
        if (fill != null) {
            val old = paint
            paint = fill.awt()
            block(this)
            paint = old
        }
    }

    private fun Graphics2D.with(stroke: Stroke?, block: Graphics2D.() -> Unit) {
        if (stroke != null) {
            val old = this.stroke
            this.stroke = stroke.awt()
            block(this)
            this.stroke = old
        }
    }
}