package com.nectar.doodle.drawing.impl

import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.dom.add
import com.nectar.doodle.dom.setFontFamily
import com.nectar.doodle.dom.setFontSize
import com.nectar.doodle.dom.setFontWeight
import com.nectar.doodle.dom.setSize
import com.nectar.doodle.drawing.AffineTransform
import com.nectar.doodle.drawing.AffineTransform.Companion.Identity
import com.nectar.doodle.drawing.Brush
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.ColorBrush
import com.nectar.doodle.drawing.Font
import com.nectar.doodle.drawing.Pen
import com.nectar.doodle.drawing.Renderer
import com.nectar.doodle.drawing.Renderer.Optimization.Quality
import com.nectar.doodle.drawing.Shadow
import com.nectar.doodle.geometry.Circle
import com.nectar.doodle.geometry.ConvexPolygon
import com.nectar.doodle.geometry.Ellipse
import com.nectar.doodle.geometry.Path
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.geometry.Size.Companion.Empty
import com.nectar.doodle.image.Image
import com.nectar.doodle.image.impl.ImageImpl
import com.nectar.doodle.text.StyledText
import com.nectar.measured.units.Angle
import com.nectar.measured.units.Measure
import com.nectar.measured.units.radians
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.CanvasTextBaseline
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.TOP
import kotlin.browser.window
import kotlin.math.PI

/**
 * Created by Nicholas Eddy on 6/22/19.
 */
class ImageCanvas(renderParent: HTMLElement, htmlFactory: HtmlFactory, private val handleRetina: Boolean = true): Canvas {

    private interface CSSFontSerializer {
        operator fun invoke(font: Font?): String
    }

    private class CSSFontSerializerImpl(htmlFactory: HtmlFactory): CSSFontSerializer {
        private val element = htmlFactory.create<HTMLElement>()

        override fun invoke(font: Font?): String = when {
            font != null -> element.run {
                style.setFontSize  (font.size  )
                style.setFontFamily(font.family.toLowerCase())
                style.setFontWeight(font.weight)

                style.run { "$fontStyle $fontVariant $fontWeight $fontSize $fontFamily" }

//                style.font
            }
            else -> "13px monospace" // FIXME: centralize w/ FontDetector
        }
    }

    val image: Image get() = object: Image {
        override val size   = this@ImageCanvas.size
        override val source = this@ImageCanvas.delegate.canvas.toDataURL("image/png", 1.0)
    }

    override var size = Empty
        set(new) {
            field = new

            // FIXME: Inject Window object
            val scale = if (handleRetina) window.devicePixelRatio else 1.0 // Address issues on Retina displays

            delegate.canvas.apply {
                style.setSize(new)

                width  = (new.width  * scale).toInt()
                height = (new.height * scale).toInt()
            }

            delegate.scale(scale, scale)

            currentTransform = Identity.scale(scale, scale)
        }

    override var optimization = Quality

    private val delegate: CanvasRenderingContext2D = htmlFactory.create<HTMLCanvasElement>("canvas").getContext("2d") as CanvasRenderingContext2D

    private var currentTransform = Identity

    private val fontSerializer = CSSFontSerializerImpl(htmlFactory)

    init {
        renderParent.add(delegate.canvas)
    }

    override fun clear() = delegate.clearRect(0.0, 0.0, size.width, size.height)

    override fun flush() {}

    override fun scale(x: Double, y: Double, block: Canvas.() -> Unit) = scale((size / 2.0).run { Point(width, height) }, x, y, block)

    override fun scale(around: Point, x: Double, y: Double, block: Canvas.() -> Unit) = when {
        x == 1.0 && y == 1.0 -> block()
        else                 -> transform(Identity.translate(around).scale(x, y).translate(-around), block)
    }

    override fun rotate(by    : Measure<Angle>,                     block: Canvas.() -> Unit) = rotate((size / 2.0).run { Point(width, height) }, by, block)
    override fun rotate(around: Point,          by: Measure<Angle>, block: Canvas.() -> Unit) {
        transform(Identity.translate(around).rotate(by).translate(-around), block)
    }

    override fun transform(transform: AffineTransform, block: Canvas.() -> Unit) = when (transform.isIdentity) {
        true -> block()
        else -> {
            val old = currentTransform

            transform.apply { delegate.transform(scaleX, shearY, shearX, scaleY, translateX, translateY) }

            block(this)

            old.apply { delegate.setTransform(scaleX, shearY, shearX, scaleY, translateX, translateY) }

            Unit
        }
    }

    private fun visible(pen: Pen?, brush: Brush?) = (pen?.visible ?: false) || (brush?.visible ?: false)

    private fun configureFill(brush: Brush): Boolean {
        if (!brush.visible) {
            return false
        }

        when (brush) {
            is ColorBrush          -> delegate.fillColor = brush.color
//            is CanvasBrush         -> it.style.background = vectorBackgroundFactory(brush)
//            is LinearGradientBrush -> it.style.background = "linear-gradient(${90 * degrees - brush.rotation `in` degrees}deg, ${brush.colors.joinToString(",") { "${it.color.run {"rgba($red,$green,$blue,$opacity)"}} ${it.offset * 100}%" }})"
            else                   -> return false
        }

        return true
    }

    private fun present(pen: Pen? = null, brush: Brush?, block: () -> Unit) {
        if (visible(pen, brush)) {
            delegate.beginPath()
            block()

            if (brush != null && configureFill(brush)) {
                delegate.fill()
            }

            if (pen != null) {
                delegate.lineWidth   = pen.thickness
                delegate.strokeColor = pen.color

                pen.dashes?.let { delegate.setLineDash(it.map { it.toDouble() }.toTypedArray()) }

                delegate.stroke()
            }
            delegate.closePath()
        }
    }

    override fun rect(rectangle: Rectangle,                           brush: Brush ) = present(brush = brush) { rectangle.apply { delegate.rect(x, y, width, height) } }
    override fun rect(rectangle: Rectangle,                 pen: Pen, brush: Brush?) = present(pen,    brush) { rectangle.apply { delegate.rect(x, y, width, height) } }
    override fun rect(rectangle: Rectangle, radius: Double,           brush: Brush ) = present(brush = brush) { rectangle.apply { delegate.roundedRect(rectangle, radius) } }
    override fun rect(rectangle: Rectangle, radius: Double, pen: Pen, brush: Brush?) = present(pen,    brush) { rectangle.apply { delegate.roundedRect(rectangle, radius) } }

    private fun path(block: CanvasRenderingContext2D.() -> Unit) {
        delegate.beginPath()

        block(delegate)

        delegate.closePath()
    }

    override fun circle(circle: Circle,           brush: Brush ) = present(brush = brush) { path { circle.apply { arc(center.x, center.y, radius, 0.0, 2 * PI, false) } } }
    override fun circle(circle: Circle, pen: Pen, brush: Brush?) = present(pen,    brush) { path { circle.apply { arc(center.x, center.y, radius, 0.0, 2 * PI, false) } } }

    override fun arc(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>,           brush: Brush ) = present(brush = brush) { path { arc(center.x, center.y, radius, rotation `in` radians, sweep `in` radians, false) } }
    override fun arc(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, pen: Pen, brush: Brush?) = present(pen,    brush) { path { arc(center.x, center.y, radius, rotation `in` radians, sweep `in` radians, false) } }

    override fun wedge(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, brush: Brush) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun wedge(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, pen: Pen, brush: Brush?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun ellipse(ellipse: Ellipse,           brush: Brush ) = present(brush = brush) { path { ellipse.apply { ellipse(center.x, center.y, xRadius, yRadius, 0.0, 0.0, 2 * PI, false) } } }
    override fun ellipse(ellipse: Ellipse, pen: Pen, brush: Brush?) = present(pen,    brush) { path { ellipse.apply { ellipse(center.x, center.y, xRadius, yRadius, 0.0, 0.0, 2 * PI, false) } } }

    override fun text(text: String, font: Font?, at: Point, brush: Brush) {
        if (text.isEmpty()) {
            return
        }

        configureFill(brush)

        delegate.font = fontSerializer(font)

        delegate.textBaseline = CanvasTextBaseline.TOP

        delegate.fillText(text, at.x, at.y)
    }

    override fun text(text: StyledText, at: Point) {
        var offset = at

        text.forEach {  (text, style) ->
            val metrics = delegate.measureText(text)

            style.background?.let {
                rect(Rectangle(position = offset, size = Size(metrics.width, metrics.actualBoundingBoxDescent)), ColorBrush(it))
            }

            text(text, style.font, at = offset, brush = ColorBrush(style.foreground ?: Color.black))

            offset += Point(metrics.width, 0.0)
        }
    }

    override fun wrapped(text: String, font: Font, point: Point, leftMargin: Double, rightMargin: Double, brush: Brush) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun wrapped(text: StyledText, point: Point, leftMargin: Double, rightMargin: Double) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun drawImage(image: ImageImpl, source: Rectangle, destination: Rectangle, opacity: Float) {
        delegate.globalAlpha = opacity.toDouble()

        delegate.drawImage(image.image,
                sx = source.x,      sy = source.y,      sw = source.width,      sh = source.height,
                dx = destination.x, dy = destination.y, dw = destination.width, dh = destination.height)

        delegate.globalAlpha = 1.0
    }

    override fun image(image: Image, source: Rectangle, destination: Rectangle, opacity: Float) {
        if (image is ImageImpl && opacity > 0 && !source.empty && !destination.empty) {
            drawImage(image, source, destination, opacity)
        }
    }

    override fun image(image: Image, destination: Rectangle, radius: Double, opacity: Float) {
        if (image is ImageImpl && opacity > 0 && !destination.empty) {
            if (radius > 0) {
                delegate.roundedRect(destination, radius)

                delegate.clip()

                drawImage(image, Rectangle(size = image.size), destination, opacity)

                delegate.resetClip()
            } else {
                drawImage(image, Rectangle(size = image.size), destination, opacity)
            }
        }
    }

    override fun clip(rectangle: Rectangle, block: Canvas.() -> Unit) {
        rectangle.apply { delegate.rect(x, y, width, height) }
        delegate.clip()

        block(this)

        delegate.resetClip()
    }

    override fun shadow(shadow: Shadow, block: Canvas.() -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun line(point1: Point, point2: Point, pen: Pen) = present(pen, brush = null) {
        path {
            moveTo(point1.x, point1.y)
            lineTo(point2.x, point2.y)
        }
    }

    override fun path(points: List<Point>, pen: Pen) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun path(path: Path, brush: Brush, fillRule: Renderer.FillRule?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun path(path: Path, pen: Pen, brush: Brush?, fillRule: Renderer.FillRule?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun poly(polygon: ConvexPolygon, brush: Brush) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun poly(polygon: ConvexPolygon, pen: Pen, brush: Brush?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

private fun CanvasRenderingContext2D.roundedRect(rectangle: Rectangle, radius: Double) {
    rectangle.apply {
        val r = when {
            width  < 2 * radius -> width  / 2
            height < 2 * radius -> height / 2
            else                -> radius
        }

        beginPath()
        moveTo(x + radius, y)
        arcTo(x + width, y,          x + width, y + height, r)
        arcTo(x + width, y + height, x,         y + height, r)
        arcTo(x,         y + height, x,         y,          r)
        arcTo(x,         y,          x + width, y,          r)
        closePath()
    }
}

private var CanvasRenderingContext2D.strokeColor: Color
    get() = Color.black
    set(new) {
        this.strokeStyle = "#${new.hexString}"
    }

private var CanvasRenderingContext2D.fillColor: Color
    get() = Color.black
    set(new) {
        this.fillStyle = "#${new.hexString}"
    }