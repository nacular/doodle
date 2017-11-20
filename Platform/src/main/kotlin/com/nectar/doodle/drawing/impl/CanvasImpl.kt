package com.nectar.doodle.drawing.impl

import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.dom.add
import com.nectar.doodle.dom.childAt
import com.nectar.doodle.dom.clearBoundStyles
import com.nectar.doodle.dom.clearVisualStyles
import com.nectar.doodle.dom.index
import com.nectar.doodle.dom.numChildren
import com.nectar.doodle.dom.parent
import com.nectar.doodle.dom.remove
import com.nectar.doodle.dom.setBounds
import com.nectar.doodle.dom.setColor
import com.nectar.doodle.dom.setHeight
import com.nectar.doodle.dom.setHeightPercent
import com.nectar.doodle.dom.setLeft
import com.nectar.doodle.dom.setOpacity
import com.nectar.doodle.dom.setTop
import com.nectar.doodle.dom.setTransform
import com.nectar.doodle.dom.setWidth
import com.nectar.doodle.dom.setWidthPercent
import com.nectar.doodle.drawing.AffineTransform
import com.nectar.doodle.drawing.AffineTransform.Companion.Identity
import com.nectar.doodle.drawing.Brush
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Canvas.ImageData
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.ColorBrush
import com.nectar.doodle.drawing.Font
import com.nectar.doodle.drawing.Pen
import com.nectar.doodle.drawing.Renderer
import com.nectar.doodle.drawing.TextFactory
import com.nectar.doodle.geometry.Circle
import com.nectar.doodle.geometry.Ellipse
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Polygon
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.image.Image
import com.nectar.doodle.image.impl.ImageImpl
import com.nectar.doodle.text.StyledText
import com.nectar.doodle.units.Angle
import com.nectar.doodle.units.Measure
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLImageElement
import org.w3c.dom.Node
import kotlin.dom.clear
import kotlin.math.max



private class Shadow(val horizontal: Double = 0.0, val vertical: Double = 0.0, val blurRadius: Double = 0.0, val color: Color = Color.black)

internal class CanvasImpl(
        override val renderRegion: HTMLElement,
        private val htmlFactory: HtmlFactory,
        private val textFactory: TextFactory,
        rendererFactory: VectorRendererFactory): Canvas, Renderer, CanvasContext {

    override var size           = Size.Empty
    private  var frame          = renderRegion
    override var transform      = Identity
    override var optimization   = Renderer.Optimization.Quality
    override var renderPosition = null as Node?

    private val vectorRenderer by lazy { rendererFactory(this) }

    private var shadows = mutableListOf<Shadow>()

    override fun rect(rectangle: Rectangle,           brush: Brush ) = present(null, brush) { getRect(rectangle) }
    override fun rect(rectangle: Rectangle, pen: Pen, brush: Brush?) = vectorRenderer.rect(rectangle, pen, brush)

    override fun rect(rectangle: Rectangle, radius: Double,           brush: Brush ) = present(null, brush) { roundedRect(rectangle, radius) }
    override fun rect(rectangle: Rectangle, radius: Double, pen: Pen, brush: Brush?) = vectorRenderer.rect(rectangle, radius, pen, brush)

    override fun circle(circle: Circle,           brush: Brush ) = present(null, brush) { roundedRect(circle.boundingRectangle, circle.radius) }
    override fun circle(circle: Circle, pen: Pen, brush: Brush?) = vectorRenderer.circle(circle, pen, brush)

    override fun ellipse(ellipse: Ellipse,           brush: Brush ) = present(null, brush) { roundedRect(ellipse.boundingRectangle, ellipse.xRadius, ellipse.yRadius) }
    override fun ellipse(ellipse: Ellipse, pen: Pen, brush: Brush?) = vectorRenderer.ellipse(ellipse, pen, brush)

    // =============== Complex =============== //

    override fun line(point1: Point, point2: Point, pen: Pen) = vectorRenderer.line(point1, point2, pen)

    override fun path(points: List<Point>, pen: Pen) = vectorRenderer.path(points, pen)

    override fun poly(polygon: Polygon,           brush: Brush ) = vectorRenderer.poly(polygon,      brush)
    override fun poly(polygon: Polygon, pen: Pen, brush: Brush?) = vectorRenderer.poly(polygon, pen, brush)

    override fun arc(center: Point, radius: Double, sweep: Double, rotation: Double,           brush: Brush ) = vectorRenderer.arc(center, radius, sweep, rotation,      brush)
    override fun arc(center: Point, radius: Double, sweep: Double, rotation: Double, pen: Pen, brush: Brush?) = vectorRenderer.arc(center, radius, sweep, rotation, pen, brush)

//    override val imageData: ImageData
//        get () {
//            val elements = (0 until region.numChildren).mapTo(mutableListOf()) { region.childAt(it)!! }
//
//            return ImageDataImpl(elements)
//        }

    override fun import(imageData: ImageData, at: Point) {
//        if (imageData is ImageDataImpl) {
//            val elements = (imageData as ImageDataImpl).getElements()
//            val clones   = mutableListOf<HTMLElement>(elements.size)
//
//            for (aElement in elements) {
//                clones.add(aElement.cloneNode())
//            }
//
//            addData(clones, at)
//        }
    }

    override fun text(text: StyledText, at: Point) {
        completeOperation(createStyledTextGlyph(text, at))
    }

    override fun text(text: String, font: Font?, at: Point, brush: Brush) {
        when {
            text.isEmpty() || !brush.visible -> return
            brush is ColorBrush              -> completeOperation(createTextGlyph(brush, text, font, at))
            else                             -> return // TODO IMPLEMENT
        }
    }

    override fun wrapped(text: String, font: Font, point: Point, leftMargin: Double, rightMargin: Double, brush: Brush) {
        when {
            text.isEmpty() || !brush.visible -> return
            brush is ColorBrush              -> completeOperation(createWrappedTextGlyph(brush,
                    text,
                    font,
                    point,
                    leftMargin,
                    rightMargin))
            else                             -> return // TODO IMPLEMENT
        }
    }

    override fun wrapped(text: StyledText, point: Point, leftMargin: Double, rightMargin: Double) {
        completeOperation(createWrappedStyleTextGlyph(
                    text,
                    point,
                    leftMargin,
                    rightMargin))
    }

    override fun image(image: Image, destination: Rectangle, radius: Double, opacity: Float) {
        if (shouldDrawImage(Rectangle(size = image.size), destination, opacity)) {
            completeOperation(createImage(image, destination, radius, opacity))
        }
    }

    private fun subFrame(block: Canvas.() -> Unit, configure: (HTMLElement) -> Unit) {
        // TODO: Not sure if this is causing more element creations than necessary on re-draw

        val clipRect = getRectElement()

        if (clipRect.parentNode == null) {
            frame.add(clipRect)
        }

        configure(clipRect)

        frame          = clipRect
        renderPosition = clipRect.childAt(0)

        apply(block)

        frame          = frame.parent as HTMLElement
        renderPosition = clipRect.nextSibling as HTMLElement?
    }

    override fun image(image: Image, source: Rectangle, destination: Rectangle, opacity: Float) {
        if (shouldDrawImage(source, destination, opacity)) {
            val clipRect          = getRect(destination)
            val oldRenderPosition = renderPosition

            renderPosition = clipRect.childAt(0)

            val xRatio = destination.width  / source.width
            val yRatio = destination.height / source.height

            val imageElement = createImage(image,
                    Rectangle(0 - xRatio * source.x,
                              0 - yRatio * source.y,
                              xRatio * image.size.width,
                              yRatio * image.size.height),
                    0.0,
                    opacity)

            if (renderPosition !== imageElement) {
                clipRect.add(imageElement)
            }

            renderPosition = oldRenderPosition

            completeOperation(clipRect)
        }
    }

    override fun translate(by: Point, block: Canvas.() -> Unit) {
        transform(transform.translate(by), block)
    }

    override fun scale(pin: Point, block: Canvas.() -> Unit) {
        transform(transform.scale(pin), block)
    }

    override fun rotate(angle: Measure<Angle>, block: Canvas.() -> Unit) {
        transform(transform.rotate(angle), block)
    }

    override fun transform(transform: AffineTransform, block: Canvas.() -> Unit) = subFrame(block) {
        val old        = this.transform
        this.transform = this.transform * transform

        it.style.setTransform(this.transform)

        this.transform = old
    }

    override fun rotate(around: Point, angle: Measure<Angle>, block: Canvas.() -> Unit) {
        val point = around - (size / 2).run { Point(width, height) }

        transform(transform.translate(point).rotate(angle).translate(-point), block)
    }

    override fun flipVertically(block: Canvas.() -> Unit) {
        scale(Point(1.0, -1.0), block)
    }

    override fun flipVertically(around: Double, block: Canvas.() -> Unit) {
        transform(transform.translate(Point(0.0, around)).scale(1.0, -1.0).translate(Point(0.0, -around)), block)
    }

    override fun flipHorizontally(block: Canvas.() -> Unit) {
        scale(Point(-1.0, 1.0), block)
    }

    override fun flipHorizontally(around: Double, block: Canvas.() -> Unit) {
        transform(transform.translate(Point(around, 0.0)).scale(-1.0, 1.0).translate(Point(-around, 0.0)), block)
    }

    override fun clear() {
        renderPosition = renderRegion.childAt(0)

        vectorRenderer.clear()
    }

    override fun flush() {
        renderPosition?.let {
            val index = renderRegion.index(it)

            if (index >= 0) {
                while (index < renderRegion.numChildren) {
                    renderRegion.remove(renderRegion.childAt(index)!!)
                }
            }
        }

        vectorRenderer.flush()

        transform = Identity
    }

    override fun clip(rectangle: Rectangle, block: Canvas.() -> Unit) = subFrame(block) {
        it.style.setBounds(rectangle)
    }

    override fun shadow(horizontal: Double, vertical: Double, blurRadius: Double, color: Color, block: Canvas.() -> Unit) {
        val shadow = Shadow(horizontal, vertical, blurRadius, color)

        shadows.add(shadow)

        apply(block)

        shadows.remove(shadow)
    }

//    private fun addData(elements: List<HTMLElement>, at: Point) {
//        elements.forEach { element ->
//            element.style.top  = "${element.top  + at.y}"
//            element.style.left = "${element.left + at.x}"
//
//            if (renderPosition != null) {
//                renderPosition?.let {
//                    val nextSibling = it.nextSibling as HTMLElement?
//
//                    frame.replaceChild(element, it)
//
//                    renderPosition = nextSibling
//                }
//            } else {
//                frame.add(element)
//            }
//        }
//    }

    private fun visible(pen: Pen?, brush: Brush?) = (pen?.visible ?: false) || (brush?.visible ?: false)

    private fun present(pen: Pen?, brush: Brush?, block: () -> HTMLElement?) {
        if (visible(pen, brush)) {
            block()?.let {
                if (brush is ColorBrush) {
                    it.style.backgroundColor = "#${brush.color.hexString}"
                }
                if (pen != null) {
                    it.style.borderWidth = "${pen.thickness}px"
                    it.style.borderStyle = "solid" // TODO: Handle dashes
                    it.style.borderColor = "#${pen.color.hexString}"
                }

                completeOperation(it)
            }
        }
    }

    private fun getRectElement(): HTMLElement = htmlFactory.createOrUse("B", renderPosition).also {
        it.clear()
        it.style.border    = ""
        it.style.transform = ""
        it.style.setWidthPercent (100.0)
        it.style.setHeightPercent(100.0)
    }

    private fun getRect(rectangle: Rectangle): HTMLElement = getRectElement().also {
        it.style.setBounds(rectangle)
    }

    private fun roundedRect(rectangle: Rectangle,                   radius: Double) = getRect(rectangle).also { it.style.borderRadius = "${radius}px" }
    private fun roundedRect(rectangle: Rectangle, xRadius: Double, yRadius: Double) = getRect(rectangle).also { it.style.borderRadius = "${xRadius}px / ${yRadius}px" }

    private fun shouldDrawImage(source: Rectangle, destination: Rectangle, opacity: Float) = opacity > 0 && !(source.empty || destination.empty)

    private fun completeOperation(element: HTMLElement): HTMLElement {
        shadows.forEach {
            val shadow = "${it.horizontal}px ${it.vertical}px ${it.blurRadius}px #${it.color.hexString}"

            when (element.nodeName.toLowerCase()) {
                "pre" -> element.style.textShadow += shadow
                else  -> element.style.boxShadow  += shadow
            }
        }

        if (renderPosition == null) {
            frame.add(element)
        } else {
            if (element !== renderPosition) {
                renderPosition?.parent?.replaceChild(element, renderPosition!!)
            }

            renderPosition = element.nextSibling as HTMLElement?
        }

//        if (isTransformed) {
//            // TODO: Apply transformation if any
//            val elementCenter = element.run { Point(offsetWidth - offsetLeft.toDouble(), offsetHeight - offsetTop.toDouble()) / 2.0 }
//            val canvasCenter  = Point(size.width, size.height) / 2.0
//            val translate     = canvasCenter - elementCenter
//
//            val t = (Identity.translate(translate) * transform).translate(-translate)
//
//            element.style.setTransform(t) //transform)
//        }

        return element
    }

    private fun createTextGlyph(brush: ColorBrush, text: String, font: Font?, at: Point): HTMLElement {
        val element = textFactory.create(text, font, if (renderPosition is HTMLElement) renderPosition as HTMLElement else null)

        return configure(element, brush, at)
    }

    private fun createWrappedTextGlyph(brush: ColorBrush, text: String, font: Font, at: Point, leftMargin: Double, rightMargin: Double): HTMLElement {
        val indent  = max(0.0, at.x - leftMargin)
        val element = textFactory.wrapped(text, font, indent, if (renderPosition is HTMLElement) renderPosition as HTMLElement else null)

        return configure(element, brush, at).also {
            it.style.setWidth(rightMargin - leftMargin)
        }
    }

    private fun createStyledTextGlyph(text: StyledText, at: Point): HTMLElement {
        val element = textFactory.create(text, if (renderPosition is HTMLElement) renderPosition as HTMLElement else null)

        element.style.setTop (at.y)
        element.style.setLeft(at.x)

        return element
    }

    private fun createWrappedStyleTextGlyph(text: StyledText, at: Point, leftMargin: Double, rightMargin: Double): HTMLElement {
        val indent  = max(0.0, at.x - leftMargin)
        val element = textFactory.wrapped(text, indent, if (renderPosition is HTMLElement) renderPosition as HTMLElement else null)

        element.style.setTop (at.y)
        element.style.setLeft(at.x)
        element.style.setWidth(rightMargin - leftMargin)

        return element
    }

    private fun configure(element: HTMLElement, brush: ColorBrush, position: Point): HTMLElement {
        element.style.setTop    (position.y         )
        element.style.setLeft   (position.x         )
        element.style.setColor  (brush.color        )
        element.style.setOpacity(brush.color.opacity)

        return element
    }

    private fun createImage(image: Image, rectangle: Rectangle, radius: Double, opacity: Float): HTMLImageElement {
        val element = pickImageElement((image as ImageImpl).image, renderPosition)

        element.style.setTop    (rectangle.y     )
        element.style.setLeft   (rectangle.x     )
        element.style.setWidth  (rectangle.width )
        element.style.setHeight (rectangle.height)
        element.style.setOpacity(opacity         )

        element.style.borderRadius = "${radius}px"

        return element
    }

    private fun pickImageElement(image: HTMLImageElement, possible: Node?): HTMLImageElement {
        var result = possible

        if (result == null || result !is HTMLImageElement || result.parentNode != null && result.nodeName != image.nodeName) {
            result = image.cloneNode(false)
        } else {
            result.clearBoundStyles ()
            result.clearVisualStyles()
        }

        return result as HTMLImageElement
    }
}
