package io.nacular.doodle.drawing.impl

import io.nacular.doodle.HTMLElement
import io.nacular.doodle.HTMLImageElement
import io.nacular.doodle.Node
import io.nacular.doodle.Text
import io.nacular.doodle.clear
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.Overflow.Visible
import io.nacular.doodle.dom.add
import io.nacular.doodle.dom.childAt
import io.nacular.doodle.dom.index
import io.nacular.doodle.dom.left
import io.nacular.doodle.dom.numChildren
import io.nacular.doodle.dom.parent
import io.nacular.doodle.dom.remove
import io.nacular.doodle.dom.rgbaString
import io.nacular.doodle.dom.setBackgroundColor
import io.nacular.doodle.dom.setBorderRadius
import io.nacular.doodle.dom.setBounds
import io.nacular.doodle.dom.setClipPath
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
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.ColorPaint
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.InnerShadow
import io.nacular.doodle.drawing.OuterShadow
import io.nacular.doodle.drawing.Paint
import io.nacular.doodle.drawing.Renderer.FillRule
import io.nacular.doodle.drawing.Shadow
import io.nacular.doodle.drawing.Stroke
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

        override fun markDirty() { vectorRenderDirty = true }
    }

    override var size             = Empty
    private val shadows           = mutableListOf<Shadow>()
    private var renderRegion      = renderParent
    private var renderPosition    = null as Node?
    private val vectorRenderer    by lazy { rendererFactory(Context()) }

    private var innerShadowCount  = 0
    private var vectorRenderDirty = false

    override fun rect(rectangle: Rectangle,                 fill: Paint ) = if (isSimple(fill)) present(fill = fill) { getRect(rectangle) } else vectorRenderer.rect(rectangle, fill)
    override fun rect(rectangle: Rectangle, stroke: Stroke, fill: Paint?) = vectorRenderer.rect(rectangle, stroke, fill)

    override fun rect(rectangle: Rectangle, radius: Double,                 fill: Paint ) = if (isSimple(fill)) present(fill = fill) { roundedRect(rectangle, radius) } else vectorRenderer.rect(rectangle, radius, fill)
    override fun rect(rectangle: Rectangle, radius: Double, stroke: Stroke, fill: Paint?) = vectorRenderer.rect(rectangle, radius, stroke, fill)

    override fun circle(circle: Circle,                 fill: Paint ) = if (isSimple(fill)) present(fill = fill) { roundedRect(circle.boundingRectangle, circle.radius) } else vectorRenderer.circle(circle, fill)
    override fun circle(circle: Circle, stroke: Stroke, fill: Paint?) = vectorRenderer.circle(circle, stroke, fill)

    override fun ellipse(ellipse: Ellipse,                 fill: Paint ) = if (isSimple(fill)) present(fill = fill) { roundedRect(ellipse.boundingRectangle, ellipse.xRadius, ellipse.yRadius) } else vectorRenderer.ellipse(ellipse, fill)
    override fun ellipse(ellipse: Ellipse, stroke: Stroke, fill: Paint?) = vectorRenderer.ellipse(ellipse, stroke, fill)

    // =============== Complex =============== //

    override fun line(start: Point, end: Point, stroke: Stroke) = vectorRenderer.line(start, end, stroke)

    override fun path(points: List<Point>, stroke: Stroke                                  ) = vectorRenderer.path(points, stroke                )
    override fun path(points: List<Point>,                 fill: Paint, fillRule: FillRule?) = vectorRenderer.path(points,         fill, fillRule)
    override fun path(points: List<Point>, stroke: Stroke, fill: Paint, fillRule: FillRule?) = vectorRenderer.path(points, stroke, fill, fillRule)

    override fun path(path: Path, stroke: Stroke                                  ) = vectorRenderer.path(path, stroke                )
    override fun path(path: Path,                 fill: Paint, fillRule: FillRule?) = vectorRenderer.path(path,         fill, fillRule)
    override fun path(path: Path, stroke: Stroke, fill: Paint, fillRule: FillRule?) = vectorRenderer.path(path, stroke, fill, fillRule)

    override fun poly(polygon: Polygon,                 fill: Paint ) = vectorRenderer.poly(polygon,         fill)
    override fun poly(polygon: Polygon, stroke: Stroke, fill: Paint?) = vectorRenderer.poly(polygon, stroke, fill)

    override fun arc(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>,                 fill: Paint ) = vectorRenderer.arc(center, radius, sweep, rotation,         fill)
    override fun arc(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, stroke: Stroke, fill: Paint?) = vectorRenderer.arc(center, radius, sweep, rotation, stroke, fill)

    override fun wedge(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>,                 fill: Paint ) = vectorRenderer.wedge(center, radius, sweep, rotation,         fill)
    override fun wedge(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, stroke: Stroke, fill: Paint?) = vectorRenderer.wedge(center, radius, sweep, rotation, stroke, fill)

    override fun text(text: StyledText, at: Point) {
        when {
            isSimple(text) -> { updateRenderPosition(); completeOperation(createStyledTextGlyph(text, at)) }
            else           -> vectorRenderer.text(text, at)
        }
    }

    override fun text(text: String, font: Font?, at: Point, fill: Paint) {
        when {
            text.isEmpty() || !fill.visible -> return
            fill is ColorPaint              -> { updateRenderPosition(); completeOperation(createTextGlyph(fill, text, font, at)) }
            else                            -> vectorRenderer.text(text, font, at, fill)
        }
    }

    override fun wrapped(text: String, font: Font?, at: Point, leftMargin: Double, rightMargin: Double, fill: Paint) {
        when {
            text.isEmpty() || !fill.visible -> return
            fill is ColorPaint              -> {
                updateRenderPosition()
                completeOperation(createWrappedTextGlyph(fill,
                                                         text,
                                                         font,
                                                         at,
                                                         leftMargin,
                                                         rightMargin))
            }
            else                            -> vectorRenderer.wrapped(text, font, at, leftMargin, rightMargin, fill)
        }
    }

    override fun wrapped(text: StyledText, at: Point, leftMargin: Double, rightMargin: Double) {
        when {
            isSimple(text) -> {
                updateRenderPosition()
                completeOperation(createWrappedStyleTextGlyph(
                    text,
                    at,
                    leftMargin,
                    rightMargin))
            }
            else           -> vectorRenderer.wrapped(text, at, leftMargin, rightMargin)
        }
    }

    override fun image(image: Image, destination: Rectangle, opacity: Float, radius: Double, source: Rectangle) {
        if (image is ImageImpl && opacity > 0 && !(source.empty || destination.empty)) {
            updateRenderPosition()

            if (source.size == image.size && source.position == Origin) {
                completeOperation(createImage(image, destination, radius, opacity))
            } else {
                getRect(destination)?.let { clipRect ->
                    val oldRenderPosition = renderPosition

                    renderPosition = clipRect.firstChild

                    val xRatio = destination.width  / source.width
                    val yRatio = destination.height / source.height

                    val imageElement = createImage(
                            image,
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

                    clipRect.style.setBorderRadius(radius)
                    completeOperation(clipRect)
                }
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

        vectorRenderDirty = false
        vectorRenderer.clear()
    }

    override fun flush() {
        clearFromRenderPosition()

        vectorRenderer.flush()
    }

    private fun clearFromRenderPosition() {
        updateRenderPosition()

        renderPosition?.let { child -> child.parentNode?.let { it to it.index(child) } }?.let { (parent, index) ->
            if (index >= 0) {
                while (index < parent.numChildren) {
                    parent.remove(parent.childAt(index)!!)
                }
            }
        }
    }

    override fun clip(rectangle: Rectangle, radius: Double, block: Canvas.() -> Unit) = when (radius) {
        0.0  -> clip(rectangle, block)
        else -> subFrame({ translate(-rectangle.position, block) }) {
            it.style.setBounds      (rectangle)
            it.style.setBorderRadius(radius   )
        }
    }

    override fun clip(polygon: Polygon, block: Canvas.() -> Unit) = subFrame(block) {
        it.style.setClipPath(polygon)
    }

    override fun clip(ellipse: Ellipse, block: Canvas.() -> Unit) = subFrame(block) {
        it.style.setClipPath(ellipse)
    }

    override fun shadow(shadow: Shadow, block: Canvas.() -> Unit) {
        shadows += shadow

        if (shadow is InnerShadow) ++innerShadowCount

        apply(block)

        if (shadow is InnerShadow) --innerShadowCount

        shadows -= shadow
    }

    override fun addData(elements: List<HTMLElement>, at: Point) = elements.forEach { element ->
        updateRenderPosition()

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
        vectorRenderer.flush()
    }

    protected open fun isSimple(fill: Paint) = when {
        !fill.visible                               -> true
        fill is ColorPaint && innerShadowCount == 0 -> true
        else                                        -> false
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
        updateRenderPosition()

        // TODO: Not sure if this is causing more element creations than necessary on re-draw

        val clipRect = getRectElement(clear = false)

        if (clipRect.parent == null) {
            renderPosition?.let {
                it.parent?.replaceChild(clipRect, it)
            } ?: renderRegion.add(clipRect)
        }

        clipRect.style.setSize        (size)
        clipRect.style.setBorderRadius(null)
        clipRect.style.background = ""

        configure(clipRect)

        renderRegion   = clipRect
        renderPosition = clipRect.firstChild

        apply(block)

        // clear potentially unused elements from the clipRect
        clearFromRenderPosition()

        renderRegion   = renderRegion.parent as HTMLElement
        renderPosition = clipRect.nextSibling
    }

    private fun present(fill: Paint?, block: () -> HTMLElement?) {
        if (fill?.visible == true) {
            updateRenderPosition()

            block()?.let {
                when (fill) {
                    is ColorPaint -> it.style.setBackgroundColor(fill.color)
                }

                completeOperation(it)
            }
        }
    }

    private fun getRectElement(clear: Boolean = true): HTMLElement = htmlFactory.createOrUse("B", renderPosition).also {
        if (clear) {
            it.clear()
        }

        it.style.setTransform()
        it.style.filter = ""
    }

    private fun getRect(rectangle: Rectangle): HTMLElement? = rectangle.takeIf { !it.empty }?.let {
        getRectElement().also {
            // This is done b/c there's an issue w/ handling half-pixels in Chrome: https://movier.me/blog/2017/realize-half-pixel-border-in-chrome/

            var transform = Identity.translate(rectangle.position)
            var width = rectangle.width
            var height = rectangle.height

            if (rectangle.height < 1) {
                height *= 2
                transform = transform.scale(y = 0.5)
            }

            if (rectangle.width < 1) {
                width *= 2
                transform = transform.scale(x = 0.5)
            }

            it.style.setSize(Size(width, height))
            it.style.setTransform(transform)
        }
    }

    private fun roundedRect(rectangle: Rectangle,                   radius: Double) = getRect(rectangle)?.also { it.style.setBorderRadius(radius          ) }
    private fun roundedRect(rectangle: Rectangle, xRadius: Double, yRadius: Double) = getRect(rectangle)?.also { it.style.setBorderRadius(xRadius, yRadius) }

    private fun updateRenderPosition() {
        if (vectorRenderDirty) {
            renderPosition = renderPosition?.nextSibling

            vectorRenderDirty = false
        }
    }

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
                else    -> {
                    element.style.filter += "drop-shadow($shadow)"
                    element.style.setOverflow(Visible())
                }
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

    private fun createTextGlyph(fill: ColorPaint, text: String, font: Font?, at: Point) = configure(textFactory.create(text, font, if (renderPosition is HTMLElement) renderPosition as HTMLElement else null), fill, at)

    private fun createWrappedTextGlyph(fill: ColorPaint, text: String, font: Font?, at: Point, leftMargin: Double, rightMargin: Double): HTMLElement {
        val indent  = max(0.0, at.x - leftMargin)
        val element = textFactory.wrapped(
                text,
                font,
                width    = rightMargin - leftMargin,
                indent   = indent,
                possible = if (renderPosition is HTMLElement) renderPosition as HTMLElement else null)

        return configure(element, fill, at)
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

    private fun configure(element: HTMLElement, fill: ColorPaint, position: Point): HTMLElement = element.also {
        it.style.apply {
            translate (position          )
            setColor  (fill.color        )
            setOpacity(fill.color.opacity)
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
            result.src = image.src
            result.style.filter = ""
        }

        return result as HTMLImageElement
    }
}