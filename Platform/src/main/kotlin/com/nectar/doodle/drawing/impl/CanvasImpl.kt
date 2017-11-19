package com.nectar.doodle.drawing.impl

import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.dom.add
import com.nectar.doodle.dom.childAt
import com.nectar.doodle.dom.clearBoundStyles
import com.nectar.doodle.dom.clearVisualStyles
import com.nectar.doodle.dom.index
import com.nectar.doodle.dom.left
import com.nectar.doodle.dom.numChildren
import com.nectar.doodle.dom.parent
import com.nectar.doodle.dom.remove
import com.nectar.doodle.dom.setColor
import com.nectar.doodle.dom.setHeight
import com.nectar.doodle.dom.setHeightPercent
import com.nectar.doodle.dom.setLeft
import com.nectar.doodle.dom.setOpacity
import com.nectar.doodle.dom.setTop
import com.nectar.doodle.dom.setTransform
import com.nectar.doodle.dom.setWidth
import com.nectar.doodle.dom.setWidthPercent
import com.nectar.doodle.dom.top
import com.nectar.doodle.drawing.AffineTransform.Companion.Identity
import com.nectar.doodle.drawing.Brush
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Canvas.ImageData
import com.nectar.doodle.drawing.ColorBrush
import com.nectar.doodle.drawing.Font
import com.nectar.doodle.drawing.Pen
import com.nectar.doodle.drawing.Renderer
import com.nectar.doodle.drawing.Shadow
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


internal class CanvasImpl(
        override val renderRegion: HTMLElement,
        private val htmlFactory: HtmlFactory,
        private val textFactory: TextFactory,
        rendererFactory: VectorRendererFactory): Canvas, Renderer, CanvasContext {

    override var size           = Size.Empty
    override var transform      = Identity
    override var optimization   = Renderer.Optimization.Quality
    override var renderPosition = null as Node?

    private val vectorRenderer by lazy { rendererFactory(this) }
    private val isTransformed get() = !transform.isIdentity

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
        when {
            true /*!isTransformed*/ -> completeOperation(createStyledTextGlyph(text, at))
            else           -> vectorRenderer.text(text, at)
        }

    }

    override fun text(text: String, font: Font?, at: Point, brush: Brush) {
        when {
            text.isEmpty() || !brush.visible      -> return
            true /*!isTransformed*/ && brush is ColorBrush -> completeOperation(createTextGlyph(brush, text, font, at))
            else                                  -> vectorRenderer.text(text, font, at, brush)
        }
    }

    override fun clipped(text: String, font: Font, point: Point, clipRect: Rectangle, brush: Brush) {
//        if (text.isEmpty() || clipRect.empty || !brush.visible) {
//            return
//        }
//
//        if (!isTransformed && brush is SolidBrush && !isComplexFont(font)) {
//            val aGlyph = createClipRect(clipRect)
//
//            val aOldRenderPosition = renderPosition
//
//            renderPosition = aGlyph.getData().getChildAt(0)
//
//            val aTextGlyph = createTextGlyph(brush, font, text, Point.create(point.x - clipRect.x, point.y - clipRect.y))
//
//            val aData = aGlyph.getData()
//
//            if (renderPosition !== aTextGlyph.getData()) {
//                aData.add(aTextGlyph.getData())
//            }
//
//            renderPosition = aOldRenderPosition
//
//            completeOperation(aData)
//        } else {
//            vectorRenderer.clipped(text, font, point, clipRect, brush)
//        }
    }

    override fun wrapped(text: String, font: Font, point: Point, leftMargin: Double, rightMargin: Double, brush: Brush) {
        when {
            text.isEmpty() || !brush.visible      -> return
            true /*!isTransformed*/ && brush is ColorBrush -> completeOperation(createWrappedTextGlyph(brush,
                    text,
                    font,
                    point,
                    leftMargin,
                    rightMargin))
            else                                  -> return // TODO IMPLEMENT
        }
    }

    override fun wrapped(text: StyledText, point: Point, leftMargin: Double, rightMargin: Double) {
        when {
            true /*!isTransformed*/ -> completeOperation(createWrappedStyleTextGlyph(
                    text,
                    point,
                    leftMargin,
                    rightMargin))
            else           -> return // TODO IMPLEMENT
        }
    }

    override fun image(image: Image, destination: Rectangle, radius: Double, opacity: Float) {
        val rect = Rectangle(size = image.size)

        if (shouldDrawImage(image, rect, destination, opacity)) {
            if (canTransformFilledRect) {
                completeOperation(createImage(image, transform(destination), radius, opacity))
            } else {
                vectorRenderer.image(image, rect, destination, opacity)
            }
        }
    }

    override fun image(image: Image, source: Rectangle, destination: Rectangle, opacity: Float) {
        if (shouldDrawImage(image, source, destination, opacity)) {
            if (canTransformFilledRect) {
                val transformedRect   = transform(destination)
                val clipRect          = getRect(transformedRect)
                val oldRenderPosition = renderPosition

                renderPosition = clipRect.childAt(0)

                val xRatio = transformedRect.width  / source.width
                val yRatio = transformedRect.height / source.height

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
            } else {
                vectorRenderer.image(image, source, destination, opacity)
            }
        }
    }

    override fun translate(by: Point) {
        transform = transform.translate(by)
    }

    override fun scale(pin: Point) {
        transform = transform.scale(pin)
    }

    override fun rotate(angle: Measure<Angle>) {
        transform = transform.rotate(angle)
    }

    override fun rotate(around: Point, angle: Measure<Angle>) {
        translate(around)
        rotate(angle)
        translate(-around)
    }

    override fun flipVertically() {
        scale(Point(1.0, -1.0))
    }

    override fun flipVertically(around: Double) {
        translate(Point(0.0, around))
        flipVertically()
        translate(Point(0.0, -around))
    }

    override fun flipHorizontally() {
        scale(Point(-1.0, 1.0))
    }

    override fun flipHorizontally(around: Double) {
        translate(Point(around, 0.0))
        flipHorizontally()
        translate(Point(-around, 0.0))
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

    override fun shadowed(shadow: Shadow, block: Canvas.() -> Unit) {
        shadows.add(shadow)

        apply(block)

        shadows.remove(shadow)
    }

    private fun addData(elements: List<HTMLElement>, at: Point) {
        elements.forEach { element ->
            element.style.top  = "${element.top  + at.y}"
            element.style.left = "${element.left + at.x}"

            if (renderPosition != null) {
                renderPosition?.let {
                    val nextSibling = it.nextSibling as HTMLElement?

                    renderRegion.replaceChild(element, it)

                    renderPosition = nextSibling
                }
            } else {
                renderRegion.add(element)
            }
        }
    }

    private val canTransformFilledRect = true /*get() =
        transform.scaleX >= 0   &&
        transform.scaleY >= 0   &&
        transform.shearX == 0.0 &&
        transform.shearY == 0.0*/

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
        it.style.setTop   (rectangle.y     )
        it.style.setLeft  (rectangle.x     )
        it.style.setWidth (rectangle.width )
        it.style.setHeight(rectangle.height)
    }

    private fun roundedRect(rectangle: Rectangle,                   radius: Double) = getRect(rectangle).also { it.style.borderRadius = "${radius}px" }
    private fun roundedRect(rectangle: Rectangle, xRadius: Double, yRadius: Double) = getRect(rectangle).also { it.style.borderRadius = "${xRadius}px / ${yRadius}px" }

    private fun transform(rectangle: Rectangle): Rectangle {
        if (isTransformed) {
            val points = transform.transform(rectangle.position, Point(rectangle.x + rectangle.width, rectangle.y + rectangle.height))

            val x1 = minOf(points[0].x, points[1].x)
            val x2 = maxOf(points[0].x, points[1].x)
            val y1 = minOf(points[0].y, points[1].y)
            val y2 = maxOf(points[0].y, points[1].y)

            return Rectangle(x1, y1, x2 - x1, y2 - y1)
        }

        return rectangle
    }

    private fun shouldDrawImage(image: Image, source: Rectangle, destination: Rectangle, opacity: Float) = opacity > 0 && !(source.empty || destination.empty)

    private fun completeOperation(element: HTMLElement): HTMLElement {
        shadows.forEach {
            val shadow = "${it.horizontal}px ${it.vertical}px ${it.blurRadius}px #${it.color.hexString}"

            when (element.nodeName.toLowerCase()) {
                "pre" -> element.style.textShadow += shadow
                else  -> element.style.boxShadow  += shadow
            }
        }

        if (renderPosition == null) {
            renderRegion.add(element)
        } else {
            if (element !== renderPosition) {
                renderPosition?.parent?.replaceChild(element, renderPosition!!)
            }

            renderPosition = element.nextSibling as HTMLElement?
        }

        if (isTransformed) {
            // TODO: Apply transformation if any
            element.style.setTransform(transform)
        }

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
