package io.nacular.doodle.drawing.impl

import io.nacular.doodle.HTMLElement
import io.nacular.doodle.HTMLImageElement
import io.nacular.doodle.Node
import io.nacular.doodle.Text
import io.nacular.doodle.clear
import io.nacular.doodle.clipPath
import io.nacular.doodle.dom.BorderStyle.Solid
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.Overflow.Visible
import io.nacular.doodle.dom.add
import io.nacular.doodle.dom.childAt
import io.nacular.doodle.dom.clearBoundStyles
import io.nacular.doodle.dom.clearVisualStyles
import io.nacular.doodle.dom.index
import io.nacular.doodle.dom.left
import io.nacular.doodle.dom.numChildren
import io.nacular.doodle.dom.parent
import io.nacular.doodle.dom.remove
import io.nacular.doodle.dom.rgbaString
import io.nacular.doodle.dom.setBackgroundColor
import io.nacular.doodle.dom.setBorderColor
import io.nacular.doodle.dom.setBorderRadius
import io.nacular.doodle.dom.setBorderStyle
import io.nacular.doodle.dom.setBorderWidth
import io.nacular.doodle.dom.setBounds
import io.nacular.doodle.dom.setColor
import io.nacular.doodle.dom.setLeft
import io.nacular.doodle.dom.setOpacity
import io.nacular.doodle.dom.setOverflow
import io.nacular.doodle.dom.setSize
import io.nacular.doodle.dom.setTop
import io.nacular.doodle.dom.setTransform
import io.nacular.doodle.dom.top
import io.nacular.doodle.dom.translate
import io.nacular.doodle.drawing.AffineTransform
import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.drawing.Brush
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.ColorBrush
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.InnerShadow
import io.nacular.doodle.drawing.OuterShadow
import io.nacular.doodle.drawing.Pen
import io.nacular.doodle.drawing.Renderer.FillRule
import io.nacular.doodle.drawing.Shadow
import io.nacular.doodle.drawing.TextFactory
import io.nacular.doodle.geometry.Circle
import io.nacular.doodle.geometry.Ellipse
import io.nacular.doodle.geometry.Path
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Point.Companion.Origin
import io.nacular.doodle.geometry.Polygon
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.Size.Companion.Empty
import io.nacular.doodle.image.Image
import io.nacular.doodle.image.impl.ImageImpl
import io.nacular.doodle.text.StyledText
import io.nacular.measured.units.Angle
import io.nacular.measured.units.Measure
import kotlin.math.max


internal open class CanvasImpl(
        private val renderParent   : HTMLElement,
        private val htmlFactory    : HtmlFactory,
        private val textFactory    : TextFactory,
                    rendererFactory: VectorRendererFactory): NativeCanvas {

    private inner class Context: CanvasContext {
        override var size
            get(   ) = this@CanvasImpl.size
            set(new) { this@CanvasImpl.size = new }

        override val shadows        get() = this@CanvasImpl.shadows
        override val renderRegion   get() = this@CanvasImpl.renderRegion
        override var renderPosition get() = this@CanvasImpl.renderPosition
            set(new) { this@CanvasImpl.renderPosition = new }
    }

    override var size            = Empty
    private val shadows          = mutableListOf<Shadow>()
    private var renderRegion     = renderParent
    private var renderPosition   = null as Node?
    private val vectorRenderer   by lazy { rendererFactory(Context()) }
    private var innerShadowCount = 0

    override fun rect(rectangle: Rectangle,           brush: Brush ) = if (isSimple(brush)) present(brush = brush) { getRect(rectangle) } else vectorRenderer.rect(rectangle, brush)
    override fun rect(rectangle: Rectangle, pen: Pen, brush: Brush?) = vectorRenderer.rect(rectangle, pen, brush)

    override fun rect(rectangle: Rectangle, radius: Double,           brush: Brush ) = if (isSimple(brush)) present(brush = brush) { roundedRect(rectangle, radius) } else vectorRenderer.rect(rectangle, radius, brush)
    override fun rect(rectangle: Rectangle, radius: Double, pen: Pen, brush: Brush?) = vectorRenderer.rect(rectangle, radius, pen, brush)

    override fun circle(circle: Circle,           brush: Brush ) = if (isSimple(brush)) present(brush = brush) { roundedRect(circle.boundingRectangle, circle.radius) } else vectorRenderer.circle(circle, brush)
    override fun circle(circle: Circle, pen: Pen, brush: Brush?) = vectorRenderer.circle(circle, pen, brush)

    override fun ellipse(ellipse: Ellipse,           brush: Brush ) = if (isSimple(brush)) present(brush = brush) { roundedRect(ellipse.boundingRectangle, ellipse.xRadius, ellipse.yRadius) } else vectorRenderer.ellipse(ellipse, brush)
    override fun ellipse(ellipse: Ellipse, pen: Pen, brush: Brush?) = vectorRenderer.ellipse(ellipse, pen, brush)

    // =============== Complex =============== //

    override fun line(start: Point, end: Point, pen: Pen) = vectorRenderer.line(start, end, pen)

    override fun path(points: List<Point>, pen: Pen                                   ) = vectorRenderer.path(points, pen                 )
    override fun path(points: List<Point>,           brush: Brush, fillRule: FillRule?) = vectorRenderer.path(points,      brush, fillRule)
    override fun path(points: List<Point>, pen: Pen, brush: Brush, fillRule: FillRule?) = vectorRenderer.path(points, pen, brush, fillRule)

    override fun path(path: Path, pen: Pen                                   ) = vectorRenderer.path(path, pen                 )
    override fun path(path: Path,           brush: Brush, fillRule: FillRule?) = vectorRenderer.path(path,      brush, fillRule)
    override fun path(path: Path, pen: Pen, brush: Brush, fillRule: FillRule?) = vectorRenderer.path(path, pen, brush, fillRule)

    override fun poly(polygon: Polygon,           brush: Brush ) = vectorRenderer.poly(polygon,      brush)
    override fun poly(polygon: Polygon, pen: Pen, brush: Brush?) = vectorRenderer.poly(polygon, pen, brush)

    override fun arc(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>,           brush: Brush ) = vectorRenderer.arc(center, radius, sweep, rotation,      brush)
    override fun arc(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, pen: Pen, brush: Brush?) = vectorRenderer.arc(center, radius, sweep, rotation, pen, brush)

    override fun wedge(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>,           brush: Brush ) = vectorRenderer.wedge(center, radius, sweep, rotation,      brush)
    override fun wedge(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, pen: Pen, brush: Brush?) = vectorRenderer.wedge(center, radius, sweep, rotation, pen, brush)

//    override val imageData: ImageData
//        get () {
//            val elements = (0 until region.numChildren).mapTo(mutableListOf()) { region.childAt(it)!! }
//
//            return ImageDataImpl(elements)
//        }

//    override fun import(imageData: ImageData, at: Point) {
//        if (imageData is ImageDataImpl) {
//            val elements = imageData.elements
//            val clones   = elements.mapTo(ArrayList(elements.size)) { it.cloneNode(deep = true) as HTMLElement }
//
//            addData(clones, at)
//        }
//    }

    override fun text(text: StyledText, at: Point) {
        when {
            isSimple(text) -> completeOperation(createStyledTextGlyph(text, at))
            else           -> vectorRenderer.text(text, at)
        }
    }

    override fun text(text: String, font: Font?, at: Point, brush: Brush) {
        when {
            text.isEmpty() || !brush.visible  -> return
            brush is ColorBrush               -> completeOperation(createTextGlyph(brush, text, font, at))
            else                              -> vectorRenderer.text(text, font, at, brush)
        }
    }

    override fun wrapped(text: String, font: Font?, at: Point, leftMargin: Double, rightMargin: Double, brush: Brush) {
        when {
            text.isEmpty() || !brush.visible -> return
            brush is ColorBrush              -> completeOperation(createWrappedTextGlyph(brush,
                                                                  text,
                                                                  font,
                                                                  at,
                                                                  leftMargin,
                                                                  rightMargin))
            else                             -> vectorRenderer.text(text, font, at, brush)
        }
    }

    override fun wrapped(text: StyledText, at: Point, leftMargin: Double, rightMargin: Double) {
        when {
            isSimple(text) -> completeOperation(createWrappedStyleTextGlyph(
                    text,
                    at,
                    leftMargin,
                    rightMargin))
            else           -> vectorRenderer.wrapped(text, at, leftMargin, rightMargin)
        }
    }

    override fun image(image: Image, destination: Rectangle, opacity: Float, radius: Double, source: Rectangle) {
        if (image is ImageImpl && opacity > 0 && !(source.empty || destination.empty)) {
            if (source.size == image.size && source.position == Origin) {
                completeOperation(createImage(image, destination, radius, opacity))
            } else {

                val clipRect = getRect(destination)
                val oldRenderPosition = renderPosition

                renderPosition = clipRect.childAt(0)

                val xRatio = destination.width / source.width
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
    }

    override fun transform(transform: AffineTransform, block: Canvas.() -> Unit) = when (transform.isIdentity) {
        true -> block()
        else -> subFrame(block) {
            val point = -Point(size.width / 2, size.height / 2)
            it.style.setTransform(((Identity translate point) * transform) translate -point)
            it.style.setOverflow(Visible())
        }
    }

    override fun clear() {
        renderPosition = renderParent.firstChild

        vectorRenderer.clear()
    }

    override fun flush() {
        renderPosition?.let {
            val index = renderParent.index(it)

            if (index >= 0) {
                while (index < renderParent.numChildren) {
                    renderParent.remove(renderParent.childAt(index)!!)
                }
            }
        }

        vectorRenderer.flush()
    }

    override fun clip(rectangle: Rectangle, radius: Double, block: Canvas.() -> Unit) = subFrame({ translate(-rectangle.position, block) }) {
        it.style.setBounds      (rectangle)
        it.style.setBorderRadius(radius   )
    }

    override fun clip(polygon: Polygon, block: Canvas.() -> Unit) = subFrame(block) {
        it.style.clipPath = "polygon(${polygon.points.joinToString { point -> "${point.x / size.width * 100}% ${point.y / size.height * 100}%" }})"
    }

    override fun shadow(shadow: Shadow, block: Canvas.() -> Unit) {
        shadows += shadow

        if (shadow is InnerShadow) ++innerShadowCount

        vectorRenderer.add(shadow)

        apply(block)

        vectorRenderer.remove(shadow)

        if (shadow is InnerShadow) --innerShadowCount

        shadows -= shadow
    }

    override fun addData(elements: List<HTMLElement>, at: Point) = elements.forEach { element ->

        if (at.y != 0.0 ) element.style.setTop (element.top  + at.y)
        if (at.x != 0.0 ) element.style.setLeft(element.left + at.x)

        if (renderPosition != null) {
            renderPosition?.let {
                val nextSibling = it.nextSibling

                if (element !== it) {
                    renderRegion.replaceChild(element, it)
                }

                renderPosition = nextSibling
            }
        } else {
            renderRegion.add(element)
        }
    }.also {
        vectorRenderer.clear() // FIXME: THIS IS A HACK.  Should communicate this better to the VectorRenderer
    }

    protected open fun isSimple(brush: Brush) = when {
        !brush.visible                               -> true
        brush is ColorBrush && innerShadowCount == 0 -> true
        else                                         -> false
    }

    private fun isSimple(text: StyledText): Boolean {
        text.forEach { (_, style) ->
            val simpleForeground = style.foreground?.let { isSimple(it) } ?: true
            val simpleBackground = style.background?.let { isSimple(it) } ?: true

            if (!(simpleForeground && simpleBackground)) {
                return false
            }
        }

        return true
    }

    private fun subFrame(block: Canvas.() -> Unit, configure: (HTMLElement) -> Unit) {
        // TODO: Not sure if this is causing more element creations than necessary on re-draw

        val clipRect = getRectElement()

        if (clipRect.parent == null) {
            renderPosition?.let {
                it.parent?.replaceChild(clipRect, it)
            } ?: renderRegion.add(clipRect)
        }

        clipRect.style.setSize(size)

        configure(clipRect)

        renderRegion   = clipRect
        renderPosition = clipRect.firstChild

        apply(block)

        renderRegion   = renderRegion.parent as HTMLElement
        renderPosition = clipRect.nextSibling
    }

    private fun visible(pen: Pen?, brush: Brush?) = (pen?.visible ?: false) || (brush?.visible ?: false)

    private fun present(pen: Pen? = null, brush: Brush?, block: () -> HTMLElement?) {
        if (visible(pen, brush)) {
            block()?.let {
                when (brush) {
                    is ColorBrush -> it.style.setBackgroundColor(brush.color)
                }
                if (pen != null) {
                    it.style.setBorderWidth(pen.thickness)
                    it.style.setBorderStyle(Solid()      )
                    it.style.setBorderColor(pen.color    )
                }

                completeOperation(it)
            }
        }
    }

    private fun getRectElement(): HTMLElement = htmlFactory.createOrUse("B", renderPosition).also {
        it.clear()
        it.style.setTransform()
        it.style.filter = ""
    }

    private fun getRect(rectangle: Rectangle): HTMLElement = getRectElement().also {
        /*
         * This is done b/c there's an issue w/ handling half-pixels in Chrome: https://movier.me/blog/2017/realize-half-pixel-border-in-chrome/
         */

        var transform = Identity.translate(rectangle.position)
        var width     = rectangle.width
        var height    = rectangle.height

        if (rectangle.height < 1) {
            height    *= 2
            transform  = transform.scale(y = 0.5)
        }

        if (rectangle.width < 1) {
            width     *= 2
            transform  = transform.scale(x = 0.5)
        }

        it.style.setSize     (Size(width, height))
        it.style.setTransform(transform          )
    }

    private fun roundedRect(rectangle: Rectangle,                   radius: Double) = getRect(rectangle).also { it.style.setBorderRadius(radius          ) }
    private fun roundedRect(rectangle: Rectangle, xRadius: Double, yRadius: Double) = getRect(rectangle).also { it.style.setBorderRadius(xRadius, yRadius) }

    private fun completeOperation(element: HTMLElement): HTMLElement {
        shadows.forEach {
            // FIXME: Need to move this to Style and avoid raw px
            // FIXME: Move text inner shadow to vector renderer
            val shadow = "${when(it) {
                is InnerShadow -> "inset "
                is OuterShadow -> ""
            }}${it.horizontal}px ${it.vertical}px ${it.blurRadius - if (it is InnerShadow) 1 else 0}px ${it.color.rgbaString}"

            when (element.firstChild) {
                is Text -> element.style.textShadow += shadow
                else    -> element.style.filter     += "drop-shadow($shadow)"
            }
        }

        if (renderPosition == null) {
            renderRegion.add(element)
        } else {
            if (element !== renderPosition) {
                renderPosition?.parent?.replaceChild(element, renderPosition!!)
            }

            renderPosition = element.nextSibling
        }

        return element
    }

    private fun createTextGlyph(brush: ColorBrush, text: String, font: Font?, at: Point) = configure(textFactory.create(text, font, if (renderPosition is HTMLElement) renderPosition as HTMLElement else null), brush, at)

    private fun createWrappedTextGlyph(brush: ColorBrush, text: String, font: Font?, at: Point, leftMargin: Double, rightMargin: Double): HTMLElement {
        val indent  = max(0.0, at.x - leftMargin)
        val element = textFactory.wrapped(
                text,
                font,
                width    = rightMargin - leftMargin,
                indent   = indent,
                possible = if (renderPosition is HTMLElement) renderPosition as HTMLElement else null)

        return configure(element, brush, at)
    }

    private fun createStyledTextGlyph(text: StyledText, at: Point) = textFactory.create(text, if (renderPosition is HTMLElement) renderPosition as HTMLElement else null).apply {
        style.translate(at)
    }

    private fun createWrappedStyleTextGlyph(text: StyledText, at: Point, leftMargin: Double, rightMargin: Double): HTMLElement {
        val indent  = max(0.0, at.x - leftMargin)
        val element = textFactory.wrapped(
                text     = text,
                width    = rightMargin - leftMargin,
                indent   = indent,
                possible = if (renderPosition is HTMLElement) renderPosition as HTMLElement else null)

        element.style.translate(at)

        return element
    }

    private fun configure(element: HTMLElement, brush: ColorBrush, position: Point): HTMLElement = element.also {
        it.style.apply {
            translate (position           )
            setColor  (brush.color        )
            setOpacity(brush.color.opacity)
        }
    }

    private fun createImage(image: ImageImpl, rectangle: Rectangle, radius: Double, opacity: Float): HTMLImageElement = pickImageElement(image.image, renderPosition).also {
        it.style.apply {
            translate      (rectangle.position)
            setSize        (rectangle.size    )
            setOpacity     (opacity           )
            setBorderRadius(radius            )
        }
    }

    private fun pickImageElement(image: HTMLImageElement, possible: Node?): HTMLImageElement {
        var result = possible

        if (result == null || result !is HTMLImageElement || result.parent != null && result.nodeName != image.nodeName) {
            result = image.cloneNode(false)
            (result as? HTMLImageElement)?.ondragstart = { false } // TODO: This is a work-around for Firefox not honoring the draggable (= false) property for images
        } else {
            result.clearBoundStyles ()
            result.clearVisualStyles()
            result.src = image.src
        }

        return result as HTMLImageElement
    }
}

//class ImageDataImpl(val elements: List<HTMLElement>): ImageData