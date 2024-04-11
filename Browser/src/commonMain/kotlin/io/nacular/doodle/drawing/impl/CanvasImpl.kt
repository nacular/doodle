package io.nacular.doodle.drawing.impl

import io.nacular.doodle.core.Camera
import io.nacular.doodle.dom.HTMLElement
import io.nacular.doodle.dom.HTMLImageElement
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.Node
import io.nacular.doodle.dom.Text
import io.nacular.doodle.dom.Visible
import io.nacular.doodle.dom.add
import io.nacular.doodle.dom.childAt
import io.nacular.doodle.dom.clear
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
import io.nacular.doodle.dom.setPerspectiveTransform
import io.nacular.doodle.dom.setSize
import io.nacular.doodle.dom.setTextAlignment
import io.nacular.doodle.dom.setTop
import io.nacular.doodle.dom.setTransform
import io.nacular.doodle.dom.top
import io.nacular.doodle.dom.translate
import io.nacular.doodle.dom.willChange
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
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.geometry.Circle
import io.nacular.doodle.geometry.Ellipse
import io.nacular.doodle.geometry.Path
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Point.Companion.Origin
import io.nacular.doodle.geometry.Polygon
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.Size.Companion.Empty
import io.nacular.doodle.geometry.toPath
import io.nacular.doodle.image.Image
import io.nacular.doodle.image.impl.ImageImpl
import io.nacular.doodle.text.StyledText
import io.nacular.doodle.text.TextSpacing
import io.nacular.doodle.utils.TextAlignment
import io.nacular.doodle.utils.splitMatches
import io.nacular.measured.units.Angle
import io.nacular.measured.units.Measure
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

internal open class CanvasImpl(
    private val renderParent   : HTMLElement,
    private val htmlFactory    : HtmlFactory,
    private val textFactory    : TextFactory,
    private val textMetrics    : TextMetrics,
    private val useShadowHack  : Boolean,
    rendererFactory: VectorRendererFactory): NativeCanvas {

    private inner class Context: CanvasContext {
        override var size
            get(   ) = this@CanvasImpl.size
            set(new) { this@CanvasImpl.size = new }

        override val shadows        get() = this@CanvasImpl.shadows
        override val renderRegion   get() = this@CanvasImpl.renderRegion
        override var renderPosition get() = this@CanvasImpl.renderPosition
            set(new) { this@CanvasImpl.renderPosition = new }

        override val isRawData get() = this@CanvasImpl.isRawData

        override fun markDirty() { vectorRenderDirty = true }
    }

    override var size             = Empty
    private val shadows           = mutableListOf<Shadow>()
    private var renderRegion      = renderParent
    private var renderPosition    = null as Node?
    private var isRawData         = false
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

    override fun text(text: StyledText, at: Point, textSpacing: TextSpacing) {
        when {
            isSimple(text) -> { updateRenderPosition(); completeOperation(createStyledTextGlyph(text, at, textSpacing)) }
            else           -> vectorRenderer.text(text, at, textSpacing)
        }
    }

    override fun text(text: String, font: Font?, at: Point, fill: Paint, textSpacing: TextSpacing) {
        when {
            text.isEmpty() || !fill.visible -> return
            isSimpleText(fill)              -> { updateRenderPosition(); completeOperation(createTextGlyph(fill, text, font, at, textSpacing)) }
            else                            -> vectorRenderer.text(text, font, at, fill, textSpacing)
        }
    }

    override fun wrapped(text: String, at: Point, width: Double, fill: Paint, font: Font?, indent: Double, alignment: TextAlignment, lineSpacing: Float, textSpacing: TextSpacing) {
        when {
            text.isEmpty() || !fill.visible -> return
            isSimpleText(fill)              -> {
                updateRenderPosition()
                completeOperation(
                    createWrappedTextGlyph(
                        fill        = fill,
                        text        = text,
                        font        = font,
                        at          = at,
                        width       = width,
                        indent      = indent,
                        alignment   = alignment,
                        lineSpacing = lineSpacing,
                        textSpacing = textSpacing
                    )
                )
            }
            else                            -> vectorRenderer.wrapped(text, at, width, fill, font, indent, alignment, lineSpacing, textSpacing)
        }
    }

    override fun wrapped(text: StyledText, at: Point, width: Double, indent: Double, alignment: TextAlignment, lineSpacing: Float, textSpacing: TextSpacing) {
        when {
            isSimple(text) -> {
                updateRenderPosition()
                completeOperation(createWrappedStyleTextGlyph(text, at, width, indent, alignment, lineSpacing, textSpacing))
            }
            else           -> vectorRenderer.wrapped(text, at, width, indent, alignment, lineSpacing, textSpacing)
        }
    }

    override fun image(image: Image, destination: Rectangle, opacity: Float, radius: Double, source: Rectangle) {
        if (image is ImageImpl && opacity > 0 && !(source.empty || destination.empty)) {
            updateRenderPosition()

            if (source.size == image.size && source.position == Origin) {
                completeOperation(createImage(image, destination, radius, opacity))
            } else {
                getRect(destination, clear = false)?.let { clipRect ->
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
                        clipRect.clear()
                        clipRect.add(imageElement)
                    }

                    renderPosition = oldRenderPosition

                    clipRect.style.setBorderRadius(radius)
                    completeOperation(clipRect)
                }
            }
        }
    }

    override fun transform(transform: AffineTransform, block: Canvas.() -> Unit) = when {
        transform.isIdentity -> block()
        else                 -> subFrame(block) {
            val point = -Point(size.width / 2, size.height / 2)
            it.style.setTransform(((Identity translate point) * transform) translate -point)
            it.style.setOverflow(Visible())
        }
    }

    override fun transform(transform: AffineTransform, camera: Camera, block: Canvas.() -> Unit) = when {
        transform.isIdentity -> block()
        else                 -> subFrame(block) {
            val point = -Point(size.width / 2, size.height / 2)
            val modifiedTransform = ((Identity translate point) * transform) translate -point
            it.style.setPerspectiveTransform((camera.projection(point) * modifiedTransform).matrix)
            it.style.setOverflow(Visible())
        }
    }

    override fun clear() {
        renderPosition = renderParent.firstChild

        vectorRenderDirty = false
        vectorRenderer.clear()
        isRawData = false // HACK: must be reset after vectorRenderer clear
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

    override fun clip(polygon: Polygon, block: Canvas.() -> Unit) = clip(polygon.toPath(), block)

    override fun clip(ellipse: Ellipse, block: Canvas.() -> Unit) = clip(ellipse.toPath(), block)

    override fun clip(path: Path, block: Canvas.() -> Unit) = subFrame(block) {
        it.style.setClipPath(path)
    }

    override fun shadow(shadow: Shadow, block: Canvas.() -> Unit) {
        shadows += shadow

        if (shadow is InnerShadow) ++innerShadowCount

        apply(block)

        if (shadow is InnerShadow) --innerShadowCount

        shadows -= shadow
    }

    override fun addData(elements: List<HTMLElement>, at: Point) = elements.forEach { element ->
        isRawData = true // HACK: to communicate to vectorRenderer that it should not dig into the current element
        updateRenderPosition()

        if (at.y != 0.0 ) element.style.setTop (element.top  + at.y)
        if (at.x != 0.0 ) element.style.setLeft(element.left + at.x)

        when (val position = renderPosition) {
            null -> renderRegion.add(element)
            else -> {
                val nextSibling = position.nextSibling

                if (element !== position) {
                    renderRegion.replaceChild(element, position)
                }

                renderPosition = nextSibling
            }
        }
    }.also {
        vectorRenderer.flush()
    }

    private fun isSimple(fill: Paint): Boolean = when {
        !fill.visible                               -> true
        fill is ColorPaint && innerShadowCount == 0 -> true
        else                                        -> false
    }


    @OptIn(ExperimentalContracts::class)
    private fun isSimpleText(fill: Paint): Boolean {
        contract {
            returns(true) implies (fill is ColorPaint)
        }

        return fill is ColorPaint && innerShadowCount <= 0
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
        if (isVisibleColorPaint(fill)) {
            updateRenderPosition()

            block()?.let {
                it.style.setBackgroundColor(fill.color)

                completeOperation(it)
            }
        }
    }

    /**
     * Must only be called on Paints that have been considered simple via isSimple
     */
    @OptIn(ExperimentalContracts::class)
    private fun isVisibleColorPaint(fill: Paint?): Boolean {
        contract {
            // This works b/c isSimple is used before all calls to present, and only Paints that are
            // !visible || ColorPaint pass that test. So a visible Paint at this point must be
            // a ColorPaint
            returns(true) implies (fill is ColorPaint)
        }

        return fill?.visible == true
    }

    private fun getRectElement(clear: Boolean = true): HTMLElement = htmlFactory.createOrUse("B", renderPosition as? HTMLElement).also {
        if (clear) {
            it.clear()
        }

        it.style.filter = ""
        it.style.setOverflow     (null)
        it.style.setClipPath     (null)
        it.style.setTransform    (    )
        it.style.setBorderRadius (null)
        it.style.setTextAlignment(null)
    }

    private fun getRect(rectangle: Rectangle, clear: Boolean = true): HTMLElement? = rectangle.takeIf { !it.empty }?.let {
        getRectElement(clear).also {
            // This is done b/c there's an issue w/ handling half-pixels in Chrome: https://movier.me/blog/2017/realize-half-pixel-border-in-chrome/

            var width     = rectangle.width
            var height    = rectangle.height
            var transform = Identity.translate(rectangle.position)

            if (rectangle.height < 1) {
                height    *= 2
                transform  = transform.scale(y = 0.5)
            }

            if (rectangle.width < 1) {
                width     *= 2
                transform  = transform.scale(x = 0.5)
            }

            it.style.setSize        (Size(width, height))
            it.style.setTransform   (transform          )
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
                is Text -> element.style.textShadow += shadow // FIXME: inset not supported, so using SVG for now
                else    -> {
                    element.style.filter += "drop-shadow($shadow)"
                    if (useShadowHack) {
                        element.style.willChange = "filter" // FIXME: This is a hack to avoid issues on Safari
                        if (shadows.size > 1) {
                            element.style.setOverflow(Visible())
                        }
                    }
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

    private fun createTextGlyph(fill: ColorPaint, text: String, font: Font?, at: Point, textSpacing: TextSpacing) = configure(textFactory.create(text, font, textSpacing, if (renderPosition is HTMLElement) renderPosition as HTMLElement else null), fill, at)

    private fun String.firstWord    (): String     = splitMatches("""\s""".toRegex()).matches.firstOrNull()?.match ?: ""
    private fun StyledText.firstWord(): StyledText = this.subString(0 .. text.firstWord().length)

    private fun createWrappedTextGlyph(fill: ColorPaint, text: String, font: Font?, at: Point, width: Double, indent: Double, alignment: TextAlignment, lineSpacing: Float, textSpacing: TextSpacing): HTMLElement {
        val firstWordWidth = if (indent > 0.0) textMetrics.width(text.firstWord(), font, textSpacing) else 0.0

        val element = textFactory.wrapped(
            text,
            font,
            width       = width,
            indent      = if (width > firstWordWidth + indent) indent else 0.0,
            possible    = if (renderPosition is HTMLElement) renderPosition as HTMLElement else null,
            alignment   = alignment,
            lineSpacing = lineSpacing,
            textSpacing = textSpacing
        )

        return configure(element, fill, at)
    }

    private fun createStyledTextGlyph(text: StyledText, at: Point, textSpacing: TextSpacing) = textFactory.create(text, textSpacing, if (renderPosition is HTMLElement) renderPosition as HTMLElement else null).apply {
        style.translate(at)
    }

    private fun createWrappedStyleTextGlyph(text: StyledText, at: Point, width: Double, indent: Double, alignment: TextAlignment, lineSpacing: Float, textSpacing: TextSpacing): HTMLElement {
        val firstWordWidth = if (indent > 0.0) textMetrics.width(text.firstWord(), textSpacing) else 0.0

        val element = textFactory.wrapped(
            text        = text,
            width       = width,
            indent      = if (width > firstWordWidth + indent) indent else 0.0,
            possible    = if (renderPosition is HTMLElement) renderPosition as HTMLElement else null,
            alignment   = alignment,
            lineSpacing = lineSpacing,
            textSpacing = textSpacing,
        )

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
        } else {
            result.src              = image.src
            result.style.filter     = ""
            result.style.willChange = ""
            result.style.setOverflow(null)
        }

        return result as HTMLImageElement
    }
}