package io.nacular.doodle.drawing.impl

import io.nacular.doodle.dom.BoundingBoxOptions
import io.nacular.doodle.dom.HTMLElement
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.Node
import io.nacular.doodle.dom.SVGCircleElement
import io.nacular.doodle.dom.SVGElement
import io.nacular.doodle.dom.SVGEllipseElement
import io.nacular.doodle.dom.SVGGraphicsElement
import io.nacular.doodle.dom.SVGLinearGradientElement
import io.nacular.doodle.dom.SVGPathElement
import io.nacular.doodle.dom.SVGPatternElement
import io.nacular.doodle.dom.SVGPolygonElement
import io.nacular.doodle.dom.SVGRadialGradientElement
import io.nacular.doodle.dom.SVGRectElement
import io.nacular.doodle.dom.SVGTSpanElement
import io.nacular.doodle.dom.SvgFactory
import io.nacular.doodle.dom.add
import io.nacular.doodle.dom.addIfNotPresent
import io.nacular.doodle.dom.childAt
import io.nacular.doodle.dom.clear
import io.nacular.doodle.dom.clipPath
import io.nacular.doodle.dom.defaultFontSize
import io.nacular.doodle.dom.get
import io.nacular.doodle.dom.getBBox_
import io.nacular.doodle.dom.parent
import io.nacular.doodle.dom.remove
import io.nacular.doodle.dom.removeTransform
import io.nacular.doodle.dom.rgbaString
import io.nacular.doodle.dom.setBorderRadius
import io.nacular.doodle.dom.setBounds
import io.nacular.doodle.dom.setCircle
import io.nacular.doodle.dom.setClipPath
import io.nacular.doodle.dom.setDefaultFill
import io.nacular.doodle.dom.setEllipse
import io.nacular.doodle.dom.setEnd
import io.nacular.doodle.dom.setFill
import io.nacular.doodle.dom.setFillPattern
import io.nacular.doodle.dom.setFillRule
import io.nacular.doodle.dom.setFloodColor
import io.nacular.doodle.dom.setFont
import io.nacular.doodle.dom.setGradientUnits
import io.nacular.doodle.dom.setHeight
import io.nacular.doodle.dom.setHeightPercent
import io.nacular.doodle.dom.setId
import io.nacular.doodle.dom.setOpacity
import io.nacular.doodle.dom.setPathData
import io.nacular.doodle.dom.setPatternTransform
import io.nacular.doodle.dom.setPoints
import io.nacular.doodle.dom.setPosition
import io.nacular.doodle.dom.setRX
import io.nacular.doodle.dom.setRY
import io.nacular.doodle.dom.setRadius
import io.nacular.doodle.dom.setStart
import io.nacular.doodle.dom.setStopColor
import io.nacular.doodle.dom.setStopOffset
import io.nacular.doodle.dom.setStroke
import io.nacular.doodle.dom.setStrokeColor
import io.nacular.doodle.dom.setStrokePattern
import io.nacular.doodle.dom.setTextDecoration
import io.nacular.doodle.dom.setTextSpacing
import io.nacular.doodle.dom.setTransform
import io.nacular.doodle.dom.setWidth
import io.nacular.doodle.dom.setWidthPercent
import io.nacular.doodle.dom.setX
import io.nacular.doodle.dom.setX1
import io.nacular.doodle.dom.setX2
import io.nacular.doodle.dom.setY
import io.nacular.doodle.dom.setY1
import io.nacular.doodle.dom.setY2
import io.nacular.doodle.drawing.AffineTransform
import io.nacular.doodle.drawing.Color.Companion.White
import io.nacular.doodle.drawing.ColorPaint
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.GradientPaint
import io.nacular.doodle.drawing.ImagePaint
import io.nacular.doodle.drawing.InnerShadow
import io.nacular.doodle.drawing.LinearGradientPaint
import io.nacular.doodle.drawing.OuterShadow
import io.nacular.doodle.drawing.Paint
import io.nacular.doodle.drawing.PatternPaint
import io.nacular.doodle.drawing.RadialGradientPaint
import io.nacular.doodle.drawing.Renderer.FillRule
import io.nacular.doodle.drawing.Shadow
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.drawing.SweepGradientPaint
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.geometry.Circle
import io.nacular.doodle.geometry.Ellipse
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Point.Companion.Origin
import io.nacular.doodle.geometry.Polygon
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.toPath
import io.nacular.doodle.image.Image
import io.nacular.doodle.image.impl.ImageImpl
import io.nacular.doodle.text.Style
import io.nacular.doodle.text.StyledText
import io.nacular.doodle.text.TextSpacing
import io.nacular.doodle.utils.IdGenerator
import io.nacular.doodle.utils.TextAlignment
import io.nacular.doodle.utils.TextAlignment.Center
import io.nacular.doodle.utils.TextAlignment.End
import io.nacular.doodle.utils.TextAlignment.Justify
import io.nacular.doodle.utils.TextAlignment.Start
import io.nacular.doodle.utils.splitMatches
import io.nacular.measured.units.Angle
import io.nacular.measured.units.Angle.Companion.cos
import io.nacular.measured.units.Angle.Companion.degrees
import io.nacular.measured.units.Angle.Companion.sin
import io.nacular.measured.units.Measure
import io.nacular.measured.units.times
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.min
import io.nacular.doodle.dom.SVGTextElement as SVGTextElement1

internal open class VectorRendererSvg(
    protected var context       : CanvasContext,
    private   val svgFactory    : SvgFactory,
    private   val htmlFactory   : HtmlFactory,
    private   val aligner       : TextVerticalAligner,
    private   val textMetrics   : TextMetrics,
    private   val idGenerator   : IdGenerator,
                  rootSvgElement: SVGElement? = null
): VectorRenderer {

    private inner class PatternCanvas(
        context       : CanvasContext,
        svgFactory    : SvgFactory,
        htmlFactory   : HtmlFactory,
        aligner       : TextVerticalAligner,
        idGenerator   : IdGenerator,
        patternElement: SVGElement
    ): VectorRendererSvg(context, svgFactory, htmlFactory, aligner, textMetrics, idGenerator, patternElement), io.nacular.doodle.drawing.PatternCanvas {
        private val contextWrapper = ContextWrapper(context)

        init {
            this.context = contextWrapper
        }

        override var size get() = context.size; set(@Suppress("UNUSED_PARAMETER") value) {}

        override fun transform(transform: AffineTransform, block: io.nacular.doodle.drawing.PatternCanvas.() -> Unit) = when {
            transform.isIdentity -> block(this)
            else                 -> {
                pushGroup()

                svgElement?.setTransform(transform)

                block(this)

                popGroup()
            }
        }

        override fun image(image: Image, destination: Rectangle, opacity: Float, radius: Double, source: Rectangle) {
            if (image is ImageImpl && opacity > 0 && !(source.empty || destination.empty)) {
                updateRootSvg()

                if (source.size == image.size && source.position == Origin) {
                    completeOperation(createImage(image, destination, radius, opacity))
                } else {
                    val xRatio = destination.width  / source.width
                    val yRatio = destination.height / source.height

                    val imageElement = createImage(image,
                        Rectangle(0 - xRatio * source.x,
                            0 - yRatio * source.y,
                            xRatio * image.size.width,
                            yRatio * image.size.height),
                        0.0,
                        opacity)

                    createClip(destination, radius).let {
                        completeOperation(it)
                        imageElement.style.clipPath = "url(#${it.id})"
                    }

                    completeOperation(imageElement)
                }
            }
        }

        override fun clip(rectangle: Rectangle, radius: Double, block: io.nacular.doodle.drawing.PatternCanvas.() -> Unit) {
            pushClip(rectangle.toPath(radius))
            block   (this                    )
            popClip (                        )
        }

        override fun clip(polygon: Polygon, block: io.nacular.doodle.drawing.PatternCanvas.() -> Unit) {
            pushClip(polygon.toPath())
            block   (this            )
            popClip (                )
        }

        override fun clip(ellipse: Ellipse, block: io.nacular.doodle.drawing.PatternCanvas.() -> Unit) {
            pushClip(ellipse.toPath())
            block   (this            )
            popClip (                )
        }

        override fun clip(path: io.nacular.doodle.geometry.Path, block: io.nacular.doodle.drawing.PatternCanvas.() -> Unit) {
            pushClip(path)
            block   (this)
            popClip (    )
        }

        override fun shadow(shadow: Shadow, block: io.nacular.doodle.drawing.PatternCanvas.() -> Unit) {
            contextWrapper.shadows += shadow
            block(this)
            contextWrapper.shadows -= shadow
        }

        private fun createClip(rectangle: Rectangle, radius: Double = 0.0) = createOrUse<SVGElement>("clipPath").apply {
            if (id.isBlank()) { setId(nextId()) }

            addIfNotPresent(makeRect(rectangle, radius) ,0)
        }
    }

    private class ContextWrapper(delegate: CanvasContext): CanvasContext by delegate {
        override val shadows: MutableList<Shadow> = mutableListOf()
    }

    protected var svgElement    : SVGElement? = null
    private   var rootSvgElement: SVGElement? = null

    private val region get() = context.renderRegion

    private var renderPosition: Node? = null

    private val previousElementCache = mutableMapOf<Any, SVGElement>()
    private val elementCache         = mutableMapOf<Any, SVGElement>()

    init {
        rootSvgElement?.let {
            svgElement          = it
            this.rootSvgElement = svgElement
            renderPosition      = svgElement?.firstChild
        }
    }

    override fun line(start: Point, end: Point, stroke: Stroke) = drawPath(stroke, null, null, start, end)

    override fun path(points: List<Point>,                 fill: Paint, fillRule: FillRule?) = drawPath(null,   fill, fillRule, *points.toTypedArray())
    override fun path(points: List<Point>, stroke: Stroke                                  ) = drawPath(stroke, null, null,     *points.toTypedArray())
    override fun path(points: List<Point>, stroke: Stroke, fill: Paint, fillRule: FillRule?) = drawPath(stroke, fill, fillRule, *points.toTypedArray())

    override fun path(path: io.nacular.doodle.geometry.Path,                 fill: Paint, fillRule: FillRule?) = drawPath(path.data, null,   fill, fillRule)
    override fun path(path: io.nacular.doodle.geometry.Path, stroke: Stroke                                  ) = drawPath(path.data, stroke, null, null    )
    override fun path(path: io.nacular.doodle.geometry.Path, stroke: Stroke, fill: Paint, fillRule: FillRule?) = drawPath(path.data, stroke, fill, fillRule)

    override fun rect(rectangle: Rectangle,                 fill: Paint ) = drawRect(rectangle, null,   fill)
    override fun rect(rectangle: Rectangle, stroke: Stroke, fill: Paint?) = drawRect(rectangle, stroke, fill)

    override fun poly(polygon: Polygon,                 fill: Paint ) = drawPoly(polygon, null,   fill)
    override fun poly(polygon: Polygon, stroke: Stroke, fill: Paint?) = drawPoly(polygon, stroke, fill)

    override fun rect(rectangle: Rectangle, radius: Double,                 fill: Paint ) = drawRect(rectangle, radius, null,   fill)
    override fun rect(rectangle: Rectangle, radius: Double, stroke: Stroke, fill: Paint?) = drawRect(rectangle, radius, stroke, fill)

    override fun arc(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>,                 fill: Paint ) = drawArc(center, radius, sweep, rotation, null,   fill)
    override fun arc(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, stroke: Stroke, fill: Paint?) = drawArc(center, radius, sweep, rotation, stroke, fill)

    override fun wedge(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>,                 fill: Paint ) = drawWedge(center, radius, sweep, rotation, null,   fill)
    override fun wedge(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, stroke: Stroke, fill: Paint?) = drawWedge(center, radius, sweep, rotation, stroke, fill)

    override fun circle(circle: Circle,                 fill: Paint ) = drawCircle(circle, null,   fill)
    override fun circle(circle: Circle, stroke: Stroke, fill: Paint?) = drawCircle(circle, stroke, fill)

    override fun ellipse(ellipse: Ellipse,                 fill: Paint ) = drawEllipse(ellipse, null,   fill)
    override fun ellipse(ellipse: Ellipse, stroke: Stroke, fill: Paint?) = drawEllipse(ellipse, stroke, fill)

    override fun text(text: String, font: Font?, at: Point, fill: Paint, textSpacing: TextSpacing) = text_(
        text,
        font,
        at,
        stroke = null,
        fill,
        textSpacing
    )

    override fun text(text: String, font: Font?, at: Point, stroke: Stroke, fill: Paint?, textSpacing: TextSpacing) = text_(
        text,
        font,
        at,
        stroke,
        fill,
        textSpacing
    )

    private fun text_(text: String, font: Font?, at: Point, stroke: Stroke?, fill: Paint?, textSpacing: TextSpacing) {
        var textElement: SVGTextElement1? = null

        present(stroke = stroke, fill = fill) {
            when {
                text.isNotBlank() -> makeText(text, font, at, stroke, fill, textSpacing).also { textElement = it }
                else              -> null
            }
        }

        textElement?.let { adjustTextAfterDisplay(it, aligner.verticalOffset(text, font)) }
    }

    override fun text(text: StyledText, at: Point, textSpacing: TextSpacing) {
        textInternal(text, at, textSpacing, aligner.verticalOffset(text.text, text.maxFont))
    }

    private fun textInternal(text: StyledText, at: Point, textSpacing: TextSpacing, yOffset: Double) {
        when {
            text.count > 0 -> {
                syncShadows  ()
                updateRootSvg() // Done here since present normally does this

                val texts = makeStyledText(text, at, textSpacing)

                texts.text.let {
                    completeOperation(it)
                    adjustTextAfterDisplay(it, yOffset)
                }

                texts.backgrounds.forEach { (element, paint) ->
                    val bbox = element.getBBox_(BoundingBoxOptions())

                    makeRect(Rectangle(bbox.x, bbox.y, bbox.width, bbox.height)).also {
                        fillElement(it, paint)
                        texts.text.parent?.insertBefore(it, texts.text)
                    }
                }
            }
        }
    }

    private val StyledText.maxFont    : Font? get() = filter { it.first.isNotBlank() }.mapNotNull  { it.second.font }.maxByOrNull { it.size }
    private val StyledText.maxFontSize: Int   get() = maxFont?.size ?: defaultFontSize

    private fun adjustTextAfterDisplay(textElement: SVGTextElement1, yOffset: Double) {
        // shift text down since no other way to get baseline alignment to work
        textElement.setY(
            ceil((textElement.getAttribute("y")?.toDouble() ?: 0.0) + yOffset)
        )
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
        syncShadows()

        wrappedText(StyledText(text, font, foreground = fill), at, indent, width, alignment, lineSpacing, textSpacing)
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
        syncShadows()

        wrappedText(StyledText(text, font, foreground = fill, stroke = stroke), at, indent, width, alignment, lineSpacing, textSpacing)
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
        syncShadows()

        wrappedText(text, at, indent, width, alignment, lineSpacing, textSpacing)
    }

    private var shadows = mutableListOf<Shadow>()

    private fun syncShadows() {
        while (shadows.size < context.shadows.size) {
            add(context.shadows[shadows.size])
        }

        while (shadows.size > context.shadows.size) {
            shadows.lastOrNull()?.let { remove(it) }
        }
    }

    protected fun add(shadow: Shadow) {
        shadows.plusAssign(shadow)

        pushSvg()

        when (shadow) {
            is InnerShadow -> innerShadow(shadow)
            is OuterShadow -> outerShadow(shadow)
        }.let {
            completeOperation(it)
            svgElement?.style?.filter = "url(#${it.id})"
        }
    }

    protected fun remove(shadow: Shadow) {
        shadows.minusAssign(shadow)

        popClip()
    }

    override fun clear() {
        val renderPosition = context.renderPosition

        this.renderPosition = null
        rootSvgElement      = null
        svgElement          = null
        shadows.clear()

        // HACK: to avoid interpreting the contents of an 'opaque' element
        if (renderPosition != null && !context.isRawData) {
            findSvgDepthFirst(context.renderRegion)?.let {
                rootSvgElement      = it
                svgElement          = it
                this.renderPosition = it.firstChild
            }
        }

        previousElementCache.clear()
        previousElementCache += elementCache
        elementCache.clear()
    }

    private var internalFlush = false

    private fun internalFlush() {
        internalFlush = true
        flush()
        internalFlush = false
    }

    override fun flush() {
        if (!internalFlush) {
            finalizeShadows()
        }

        var element = renderPosition

        while (element != null) {
            val next = element.nextSibling

            element.parent?.remove(element)

            element = next
        }

        renderPosition = null
    }

    protected fun nextId() = idGenerator.nextId()

    protected fun makeRect(rectangle: Rectangle, radius: Double = 0.0): SVGRectElement = createOrUse<SVGRectElement>("rect").apply {
        setBounds(rectangle)

        setRadius(radius)
        setFill  (null  )
        setStroke(null  )
    }

    protected fun pushClip(path: io.nacular.doodle.geometry.Path) {
        pushSvg {
            style.setClipPath(path)

            renderPosition = renderPosition?.nextSibling
        }
    }

    protected fun pushSvg(block: SVGElement.() -> Unit = {}) {
        val svg = createOrUse<SVGElement>("svg").apply {
            renderPosition = this.firstChild

            block(this)
        }

        if (svgElement == null || svg.parentNode != svgElement) {
            updateRootSvg()

            completeOperation(svg)
        }

        svgElement = svg
    }

    protected fun popClip() {
        // Clear any remaining items that were previously rendered within the sub-region that won't be rendered anymore
        internalFlush()

        renderPosition = svgElement?.nextSibling

        svgElement?.parentNode?.let {
            svgElement = it as SVGElement
        }
    }

    protected fun pushGroup() {
        createOrUse<SVGElement>("g").apply {
            renderPosition = this.firstChild
        }.also {
            completeOperation(it)
            svgElement = it
        }
    }

    protected fun popGroup() {
        popClip()
        //renderPosition = renderPosition?.parent?.nextSibling
    }

    protected fun updateRootSvg() {
        if (rootSvgElement == null ||
            (context.renderPosition !== rootSvgElement /*&&
//                    (rootSvgLastChild() ||
                     context.renderPosition !== rootSvgElement?.nextSibling*/)) {
            // Initialize new SVG root if
            // 1) not initialized
            // 2) it is not longer the active element
            svgElement     = createOrUse("svg", context.renderPosition)
            rootSvgElement = svgElement
            renderPosition = svgElement?.firstChild
        }
    }

    protected open fun completeOperation(element: SVGElement) {
        if (context.renderPosition == null && svgElement?.parent == null) {
            region.add(svgElement!!)
            context.renderPosition = rootSvgElement
        } else if (context.renderPosition !== rootSvgElement) {
            context.renderPosition?.parent?.replaceChild(rootSvgElement!!, context.renderPosition!!)
            context.renderPosition = rootSvgElement
        }

        if (renderPosition == null) {
            svgElement?.add(element)
        } else {
            if (renderPosition !== element) {
                renderPosition?.parent?.replaceChild(element, renderPosition!!)
            }

            renderPosition = element.nextSibling
        }

        context.markDirty()
    }

    protected fun <T: SVGElement> createOrUse(tag: String, possible: Node? = renderPosition): T {
        val element: Node? = possible

        return when {
            element == null || element.nodeName != tag -> svgFactory(tag)
            element is SVGElement                      -> {
                if (tag !in containerElements) { element.clear() }
                element.style.filter         = ""
                element.style.textDecoration = ""
                element.removeTransform()
                @Suppress("UNCHECKED_CAST")
                element as T
            }
            else -> throw Exception("Error") // FIXME: handle better
        }
    }

    private fun makeText(text: String, font: Font?, at: Point, stroke: Stroke?, fill: Paint?, textSpacing: TextSpacing) = createOrUse<SVGTextElement1>("text").apply {
        if (textContent != text) {
            textContent = text
        }

        setPosition(at)

        this.style.whiteSpace = "pre"
        this.style.setTextSpacing(textSpacing)

        font?.let {
            style.setFont(it)
        }

        when (fill) {
            null -> setDefaultFill(    )
            else -> setFill       (null)
        }

        setStroke(stroke)
    }

    private class StyledTextInfo(val text: SVGTextElement1, val backgrounds: Map<SVGTSpanElement, Paint>)

    private fun makeStyledText(text: StyledText, at: Point, textSpacing: TextSpacing): StyledTextInfo {
        val backgrounds = mutableMapOf<SVGTSpanElement, Paint>()

        val textElement = createOrUse<SVGTextElement1>("text").apply {
            setPosition(at)

            text.forEach { (text, style) ->
                add(makeTextSegment(text, style, textSpacing).apply {
                    style.background?.let { backgrounds[this] = it }
                })
            }
        }

        return StyledTextInfo(textElement, backgrounds)
    }

    private fun makeTextSegment(text: String, style: Style, textSpacing: TextSpacing) = createOrUse<SVGTSpanElement>("tspan").apply {
        if (textContent != text) {
            textContent = text
        }

        this.style.whiteSpace = "pre"
        this.style.setTextSpacing(textSpacing)
        this.style.setTextDecoration(style.decoration)

        style.font?.let {
            this.style.setFont(it)
        }

        style.foreground?.let {
            fillElement(this, it, true)
        } ?: setDefaultFill()

        style.stroke?.let {
            strokeElement(this, it)
        } ?: setStroke(null)
    }

    private data class LineInfo(val text: StyledText, val position: Point, val wordSpacing: Double)

    private fun wrappedText(
        text       : StyledText,
        at         : Point,
        indent     : Double,
        width      : Double,
        alignment  : TextAlignment,
        lineSpacing: Float,
        textSpacing: TextSpacing
    ): Point {
        val lines              = mutableListOf<LineInfo>()
        val (words, remaining) = text.text.splitMatches("""\s""".toRegex()).run { matches to remaining }
        var line               = StyledText("")
        var lineTest           : StyledText
        var currentPoint       = at + Point(x = indent)
        var endX               = currentPoint.x
        var currentLineWidth   = 0.0
        var oldLineWidth       = 0.0
        var numWords           = 0

        val calcStartX = { isLast: Boolean ->
            var wordSpacing = 0.0

            when (alignment) {
                Start   -> currentPoint.x
                Center  -> currentPoint.x + (width - currentLineWidth) / 2
                End     -> at.x + width - currentLineWidth
                Justify -> currentPoint.x.also {
                    if (!isLast && numWords > 1) {
                        wordSpacing = (width - (currentPoint.x - at.x) - oldLineWidth) / (numWords - 1)
                    }
                }
            } to wordSpacing
        }

        var offsetY     = 0.0
        var maxFontSize = 0

        val handleWord = { delimiter: StyledText, word: StyledText ->
            lineTest      = line.copy() + delimiter.copy() + word.copy()
            val lineWidth = textMetrics.width(lineTest, textSpacing)

            endX = currentPoint.x + lineWidth

            if (endX > at.x + width) {
                // ignore whitespace beyond the line break
                if (word.isNotBlank()) {
                    val (startX, wordSpacing) = calcStartX(false)

                    lines += LineInfo(line, Point(startX, currentPoint.y), wordSpacing)
                    line   = word.copy()

                    if (numWords > 0) {
                        maxFontSize = line.maxFontSize
                        offsetY += lineSpacing * maxFontSize
                    }

                    currentPoint = Point(at.x, at.y + offsetY)
                    endX = startX + currentLineWidth
                    numWords = 1
                }
            } else {
                ++numWords
                line              = lineTest
                currentLineWidth  = lineWidth

                val newMaxFontSize = word.maxFontSize

                // account for case where font grows as line progresses
                if (maxFontSize in 1..<newMaxFontSize) {
                    val delta = Point(y = lineSpacing * (newMaxFontSize - maxFontSize))

                    offsetY      += delta.y
                    currentPoint += delta
                }
            }

            oldLineWidth = lineWidth
        }

        var startCharIndex    = 0
        var previousDelimiter = StyledText("")

        words.forEach { chunk ->
            val word        = text.subString(startCharIndex until startCharIndex + chunk.match.length)
            startCharIndex += chunk.match.length

            handleWord(previousDelimiter, word)

            previousDelimiter = text.subString(startCharIndex until startCharIndex + chunk.delimiter.length)
            startCharIndex += chunk.delimiter.length
        }

        handleWord(previousDelimiter, text.subString(startCharIndex until startCharIndex + remaining.length))

        if (line.isNotBlank()) {
            val (startX, wordSpacing) = calcStartX(true)
            val lineWidth = textMetrics.width(line, textSpacing)
            endX          = startX + lineWidth

            lines += LineInfo(line, Point(startX, currentPoint.y), wordSpacing)
        }

        val verticalOffset = lines.firstOrNull { it.text.isNotBlank() }?.text?.let { l ->
            aligner.verticalOffset(l.text, l.maxFont, lineSpacing)
        } ?: 0.0

        lines.filter { it.text.isNotBlank() }.forEach { (text, at, wordSpacing) ->
            textInternal(
                text,
                at,
                TextSpacing(
                    letterSpacing = textSpacing.letterSpacing,
                    wordSpacing   = wordSpacing + textSpacing.wordSpacing
                ),
                verticalOffset
            )
        }

        return Point(endX, currentPoint.y)
    }

    private fun drawPath(stroke: Stroke?, fill: Paint? = null, fillRule: FillRule? = null, vararg points: Point) = present(stroke, fill) {
        when {
            points.isNotEmpty() -> makePath(*points).also { it.setFillRule(fillRule) }
            else                -> null
        }
    }

    private fun drawPath(data: String, stroke: Stroke?, fill: Paint?, fillRule: FillRule?) = present(stroke, fill ) {
        makePath(data).also { it.setFillRule(fillRule) }
    }

    private fun present(stroke: Stroke?, fill: Paint?, block: () -> SVGGraphicsElement?) {
        syncShadows()

        if (visible(stroke, fill)) {
            // Update SVG Element to enable re-use if the top-level cursor has moved to a new place
            updateRootSvg()

            block()?.let {
                // make sure element is in dom first since some fills add new elements to the dom
                // this means we get better re-use of nodes since the order in the dom is the element creation order
                completeOperation(it)

                if (fill != null) {
                    fillElement(it, fill, stroke == null || !stroke.visible)
                }
                if (stroke != null) {
                    outlineElement(it, stroke, fill == null || !fill.visible)
                }
            }
        }
    }

    private fun drawRect(rectangle: Rectangle, stroke: Stroke?, fill: Paint?) = present(stroke, fill) {
        when {
            !rectangle.empty -> makeClosedPath(
                    Point(rectangle.x,                   rectangle.y                   ),
                    Point(rectangle.x + rectangle.width, rectangle.y                   ),
                    Point(rectangle.x + rectangle.width, rectangle.y + rectangle.height),
                    Point(rectangle.x,                   rectangle.y + rectangle.height))
            else -> null
        }
    }

    private fun drawRect(rectangle: Rectangle, radius: Double, stroke: Stroke?, fill: Paint?) = present(stroke, fill) {
        when {
            !rectangle.empty -> makeRoundedRect(rectangle, radius)
            else             -> null
        }
    }

    private fun visible(stroke: Stroke?, fill: Paint?) = (stroke?.visible ?: false) || (fill?.visible ?: false)

    private fun drawPoly(polygon: Polygon, stroke: Stroke?, fill: Paint?) = present(stroke, fill) {
        when {
            !polygon.empty -> makeClosedPath(*polygon.points.toTypedArray())
            else           -> null
        }
    }

    private fun drawArc(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, stroke: Stroke?, fill: Paint?) = present(stroke, fill) {
        when {
            radius <= 0 || sweep == 0 * degrees -> null
            sweep < 360 * degrees               -> makeArc(center, radius, sweep, rotation)
            else                                -> makeCircle(Circle(center, radius))
        }
    }

    private fun drawWedge(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, stroke: Stroke?, fill: Paint?) = present(stroke, fill) {
        when {
            radius <= 0 || sweep == 0 * degrees -> null
            sweep < 360 * degrees               -> makeWedge(center, radius, sweep, rotation)
            else                                -> makeCircle(Circle(center, radius))
        }
    }

    private fun drawCircle(circle: Circle, stroke: Stroke?, fill: Paint?) = present(stroke, fill) {
        when {
            !circle.empty -> makeCircle(circle)
            else          -> null
        }
    }

    private fun drawEllipse(ellipse: Ellipse, stroke: Stroke?, fill: Paint?) = present(stroke, fill) {
        when {
            !ellipse.empty -> makeEllipse(ellipse)
            else           -> null
        }
    }

    private fun makeRoundedRect(rectangle: Rectangle, radius: Double): SVGRectElement = makeRect(rectangle).apply {
        setRX(radius)
        setRY(radius)

        setFill  (null)
        setStroke(null)
    }

    private fun makeCircle(circle: Circle): SVGCircleElement = createOrUse<SVGCircleElement>("circle").apply {
        setCircle(circle)

        setFill  (null)
        setStroke(null)
    }

    private fun makeEllipse(ellipse: Ellipse): SVGEllipseElement = createOrUse<SVGEllipseElement>("ellipse").apply {
        setEllipse(ellipse)

        setFill  (null)
        setStroke(null)
    }

    private fun makeArc  (center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>) = withPath("${makeArcPathData(center, radius, sweep, rotation)}Z")
    private fun makeWedge(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>) = withPath("${makeArcPathData(center, radius, sweep, rotation)} L${center.x},${center.y}Z")

    private fun withPath(path: String): SVGPathElement = createOrUse<SVGPathElement>("path").apply {
        setPathData(path)
        setFill    (null)
        setStroke  (null)
    }

    private fun makeArcPathData(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>): String {
        val startX = center.x + radius * cos(rotation)
        val startY = center.y - radius * sin(rotation)
        val endX   = center.x + radius * cos(sweep + rotation)
        val endY   = center.y - radius * sin(sweep + rotation)

        val largeArc = if (sweep > 180 * degrees) "1" else "0"

        return "M$startX,$startY A$radius,$radius ${rotation `in` degrees } $largeArc,0 $endX,${endY}"
    }

    private fun makePath(vararg points: Point): SVGPathElement {
        val path = SVGPath()

        path.addPath(*points)
        path.end()

        return makePath(path)
    }

    private fun makeClosedPath(vararg points: Point) = createOrUse<SVGPolygonElement>("polygon").apply {
        setPoints(*points)
    }

    private fun makePath(path: Path) = makePath(path.data)

    private fun makePath(pathData: String) = createOrUse<SVGPathElement>("path").apply {
        setPathData(pathData)
    }

    private fun outlineElement(element: SVGGraphicsElement, stroke: Stroke, clearFill: Boolean = true) {
        if (!stroke.visible) {
            return
        }

        if (clearFill) {
            element.setFill(null)
        }

        strokeElement(element, stroke)
    }

    private fun fillElement(element: SVGGraphicsElement, fill: Paint, clearOutline: Boolean = true) {
        when (fill) {
            is ColorPaint          -> SolidFillHandler.fill         (this, element, fill)
            is PatternPaint        -> canvasFillHandler.fill        (this, element, fill)
            is LinearGradientPaint -> linearGradientFillHandler.fill(this, element, fill)
            is RadialGradientPaint -> radialGradientFillHandler.fill(this, element, fill)
            is ImagePaint          -> imageFillHandler.fill         (this, element, fill)
            is SweepGradientPaint  -> sweepGradientFillHandler.fill (this, element, fill)
        }

        if (clearOutline) {
            element.setStroke(null)
        }
    }

    private fun strokeElement(element: SVGGraphicsElement, stroke: Stroke) {
        when (val fill = stroke.fill) {
            is ColorPaint          -> SolidFillHandler.stroke         (this, element, fill, stroke)
            is PatternPaint        -> canvasFillHandler.stroke        (this, element, fill, stroke)
            is LinearGradientPaint -> linearGradientFillHandler.stroke(this, element, fill, stroke)
            is RadialGradientPaint -> radialGradientFillHandler.stroke(this, element, fill, stroke)
            is ImagePaint          -> imageFillHandler.stroke         (this, element, fill, stroke)
            is SweepGradientPaint  -> sweepGradientFillHandler.stroke (this, element, fill, stroke)
        }

        element.setStroke(stroke)
    }

    private fun textBackground(fill: ColorPaint) = createOrUse<SVGElement>("filter").apply {
        if (id.isBlank()) { setId(nextId()) }

        setBounds(Rectangle(size = Size(1)))

        var index = 0
        val `in`  = "in"

        val oldRenderPosition = renderPosition

        renderPosition = firstChild

        addIfNotPresent(createOrUse<SVGElement>("feFlood").apply {
            setFloodColor(fill.color)
            setAttribute("flood-opacity", "${fill.color.opacity}")
        }, index++)

        renderPosition = renderPosition?.nextSibling

        addIfNotPresent(createOrUse<SVGElement>("feComposite").apply {
            setAttribute(`in`,       "SourceGraphic")
            setAttribute("operator", "over"         )
        }, index)

        renderPosition = renderPosition?.nextSibling

        internalFlush()
        renderPosition = if (parentNode != null) this else oldRenderPosition

        completeOperation(this)
    }

    private val shadowFinalizers = mutableListOf<() -> Unit>()

    internal fun finalizeShadows() {
        shadowFinalizers.forEach { it() }
        shadowFinalizers.clear()
    }

    private fun outerShadow(shadow: OuterShadow) = createOrUse<SVGElement>("filter").apply {
        if (id.isBlank()) { setId(nextId()) }

        setAttribute("filterUnits", "userSpaceOnUse")

        // this needs to happen after svgElement has a chance to expand in size
        // so, it is done during flush()
        shadowFinalizers += {
            with(shadow) {
                val factor    = 2.6
                val svgBounds = svgElement?.getBBox_(BoundingBoxOptions())?.run { Rectangle(x, y, width, height) } ?: Rectangle(this@VectorRendererSvg.context.size)

                setX     (svgBounds.x - blurRadius * factor + min(0.0, horizontal)    )
                setY     (svgBounds.y - blurRadius * factor + min(0.0, vertical  )    )
                setWidth (svgBounds.width  + 2 * blurRadius * factor + abs(horizontal))
                setHeight(svgBounds.height + 2 * blurRadius * factor + abs(vertical  ))
            }
        }

        val oldRenderPosition = renderPosition

        renderPosition = firstChild
        var index      = 0

        addIfNotPresent(createOrUse<SVGElement>("feOffset").apply {
            setAttribute("dx", "${shadow.horizontal}")
            setAttribute("dy", "${shadow.vertical  }")
        }, index++)

        renderPosition = renderPosition?.nextSibling

        addIfNotPresent(createOrUse<SVGElement>("feGaussianBlur").apply {
            setAttribute("stdDeviation", "${shadow.blurRadius - 1}")
        }, index++)

        renderPosition = renderPosition?.nextSibling

        with(shadow.color) {
            addIfNotPresent(createOrUse<SVGElement>("feColorMatrix").apply {
                setAttribute("type",   "matrix")
                setAttribute("values", "0 0 0 0 ${red.toDouble() / 255}, 0 0 0 0 ${green.toDouble() / 255}, 0 0 0 0 ${blue.toDouble() / 255}, 0 0 0 $opacity 0")
                setAttribute("result", "shadow")
            }, index++)
        }

        renderPosition = renderPosition?.nextSibling

        addIfNotPresent(createOrUse<SVGElement>("feMerge").apply {
            var innerIndex = 0

            renderPosition = firstChild

            addIfNotPresent(createOrUse<SVGElement>("feMergeNode").apply {
                setAttribute("in", "shadow")
            }, innerIndex++)

            renderPosition = renderPosition?.nextSibling

            addIfNotPresent(createOrUse<SVGElement>("feMergeNode").apply {
                setAttribute("in", "SourceGraphic")
            }, innerIndex)
        }, index++)

        renderPosition = renderPosition?.nextSibling

        internalFlush()
        renderPosition = if (parentNode != null) this else oldRenderPosition
    }

    private fun innerShadow(shadow: InnerShadow) = createOrUse<SVGElement>("filter").apply {
        if (id.isBlank()) { setId(nextId()) }

        val oldRenderPosition = renderPosition

        renderPosition = firstChild

        // TODO: Make first-class methods for these attributes
        var index   = 0
        val `in`    = "in"
        val in2     = "in2"
        val result  = "result"
        val inverse = "inverse"

        // Shadow Offset
        addIfNotPresent(createOrUse<SVGElement>("feOffset").apply {
            setAttribute("dx", "${shadow.horizontal}")
            setAttribute("dy", "${shadow.vertical  }")
        }, index++)

        renderPosition = renderPosition?.nextSibling

        // Shadow Blur
        addIfNotPresent(createOrUse<SVGElement>("feGaussianBlur").apply {
            setAttribute("stdDeviation", "${shadow.blurRadius}")
            setAttribute(result,         "offset-blur"         )
        }, index++)

        renderPosition = renderPosition?.nextSibling

        // Invert the drop shadow to create an inner shadow
        addIfNotPresent(createOrUse<SVGElement>("feComposite").apply {
            setAttribute("operator", "out"          )
            setAttribute(`in`,       "SourceGraphic")
            setAttribute(in2,        "offset-blur"  )
            setAttribute(result,     inverse        )
        }, index++)

        renderPosition = renderPosition?.nextSibling

        addIfNotPresent(createOrUse<SVGElement>("feFlood").apply {
            setFloodColor(shadow.color)
            setAttribute("flood-opacity", "${shadow.color.opacity}")
            setAttribute(result,          "color"                  )
        }, index++)

        renderPosition = renderPosition?.nextSibling

        // Clip color inside shadow
        addIfNotPresent(createOrUse<SVGElement>("feComposite").apply {
            setAttribute("operator", `in`     )
            setAttribute(`in`,       "color"  )
            setAttribute(in2,        inverse  )
            setAttribute("result",   "shadow" )
        }, index++)

        renderPosition = renderPosition?.nextSibling

        // Put shadow over original object
        addIfNotPresent(createOrUse<SVGElement>("feComposite").apply {
            setAttribute("operator", "over"         )
            setAttribute(`in`,       "shadow"       )
            setAttribute(in2,        "SourceGraphic")
        }, index)

        renderPosition = renderPosition?.nextSibling

        internalFlush()
        renderPosition = if (parentNode != null) this else oldRenderPosition
    }

    private fun findSvgDepthFirst(parent: Node): SVGElement? {
        if (parent is SVGElement) return parent

        var svg = null as SVGElement?

        (0 until parent.childNodes.length).mapNotNull { parent.childNodes[it] }.forEach {
            svg = findSvgDepthFirst(it)

            if (svg != null) {
                return svg
            }
        }

        return svg
    }

    private class SVGPath: Path("M", "L", "Z")

    private interface FillHandler<B: Paint> {
        fun fill              (renderer: VectorRendererSvg, element: SVGGraphicsElement, paint: B                )
        fun stroke            (renderer: VectorRendererSvg, element: SVGGraphicsElement, paint: B, stroke: Stroke)
        fun textBackgroundFill(renderer: VectorRendererSvg, element: SVGGraphicsElement, paint: B                )
    }

    private object SolidFillHandler: FillHandler<ColorPaint> {
        override fun fill(renderer: VectorRendererSvg, element: SVGGraphicsElement, paint: ColorPaint) {
            element.setFill(paint.color)
        }

        override fun stroke(renderer: VectorRendererSvg, element: SVGGraphicsElement, paint: ColorPaint, stroke: Stroke) {
            element.setStrokeColor(paint.color)
        }

        override fun textBackgroundFill(
            renderer: VectorRendererSvg,
            element : SVGGraphicsElement,
            paint   : ColorPaint
        ) { element.style.filter = "url(#${renderer.textBackground(paint).id})" }
    }

    // TODO: Explore using a filter for this instead: https://www.smashingmagazine.com/2015/05/why-the-svg-filter-is-awesome/#image-fill
    private val canvasFillHandler: FillHandler<PatternPaint> by lazy {
        object: FillHandler<PatternPaint> {
            private fun makeFill(renderer: VectorRendererSvg, paint: PatternPaint): String {
                // FIXME: Re-use elements when possible
                val pattern = createOrUse<SVGPatternElement>("pattern").apply {
                    if (id.isBlank()) { setId(nextId()) }

                    setAttribute("patternUnits", "userSpaceOnUse")

                    if (!paint.transform.isIdentity) {
                        setPatternTransform(paint.transform)
                    }

                    setBounds(paint.bounds)
                    clear    (            )
                }

                renderer.completeOperation(pattern)

                val canvas = PatternCanvas(object: CanvasContext {
                    override var size get()            = paint.bounds.size; set(@Suppress("UNUSED_PARAMETER") value) {}
                    override val renderRegion          = pattern
                    override var renderPosition: Node? = pattern
                    override val shadows get()         = context.shadows
                    override fun markDirty()           = context.markDirty()
                    override val isRawData get()       = context.isRawData
                }, svgFactory, htmlFactory, aligner, idGenerator, pattern)

                paint.paint(canvas)

                shadowFinalizers += {
                    canvas.finalizeShadows()
                }

                return pattern.id
            }

            override fun fill(renderer: VectorRendererSvg, element: SVGGraphicsElement, paint: PatternPaint) {
                element.setFillPattern(makeFill(renderer, paint), paint.opacity)
            }

            override fun stroke(renderer: VectorRendererSvg, element: SVGGraphicsElement, paint: PatternPaint, stroke: Stroke) {
                element.setStrokePattern(makeFill(renderer, paint), paint.opacity)
            }

            override fun textBackgroundFill(
                renderer: VectorRendererSvg,
                element : SVGGraphicsElement,
                paint   : PatternPaint
            ) { element.style.filter = "url(#${makeFill(renderer, paint)})" }
        }
    }

    private val imageFillHandler: FillHandler<ImagePaint> by lazy {
        object: FillHandler<ImagePaint> {
            private fun makeFill(renderer: VectorRendererSvg, paint: ImagePaint): String {
                // FIXME: Re-use elements when possible
                val pattern = createOrUse<SVGPatternElement>("pattern").apply {
                    if (id.isBlank()) { setId(nextId()) }

                    setAttribute("patternUnits", "userSpaceOnUse")

                    val destination = Rectangle(paint.size)

                    setBounds(destination)
                    clear    (           )

                    add(createImage(paint.image, destination, opacity = paint.opacity, radius = 0.0))
                }

                renderer.completeOperation(pattern)

                return pattern.id
            }

            override fun fill(renderer: VectorRendererSvg, element: SVGGraphicsElement, paint: ImagePaint) {
                element.setFillPattern(makeFill(renderer, paint))
            }

            override fun stroke(renderer: VectorRendererSvg, element: SVGGraphicsElement, paint: ImagePaint, stroke: Stroke) {
                element.setStrokePattern(makeFill(renderer, paint))
            }

            override fun textBackgroundFill(
                renderer: VectorRendererSvg,
                element: SVGGraphicsElement,
                paint: ImagePaint
            ) { element.style.filter = "url(#${makeFill(renderer, paint)})" }
        }
    }

    private val linearGradientFillHandler by lazy {
        object: FillHandler<LinearGradientPaint> {
            private fun makeFill(renderer: VectorRendererSvg, paint: LinearGradientPaint) = getSharedElement(paint) {
                val gradient = createOrUse<SVGLinearGradientElement>("linearGradient").apply {
                    if (id.isBlank()) { setId(nextId()) }

                    setGradientUnits("userSpaceOnUse")
                    setX1           (paint.start.x   )
                    setY1           (paint.start.y   )
                    setX2           (paint.end.x     )
                    setY2           (paint.end.y     )

                    updateStops(paint.colors)
                }

                renderer.completeOperation(gradient)

                gradient
            }.id

            override fun fill(renderer: VectorRendererSvg, element: SVGGraphicsElement, paint: LinearGradientPaint) {
                element.setFillPattern(makeFill(renderer, paint))
            }

            override fun stroke(renderer: VectorRendererSvg, element: SVGGraphicsElement, paint: LinearGradientPaint, stroke: Stroke) {
                element.setStrokePattern(makeFill(renderer, paint))
            }

            override fun textBackgroundFill(
                renderer: VectorRendererSvg,
                element: SVGGraphicsElement,
                paint: LinearGradientPaint
            ) { element.style.filter = "url(#${makeFill(renderer, paint)})" }
        }
    }

    private val radialGradientFillHandler by lazy {
        object: FillHandler<RadialGradientPaint> {
            private fun makeFill(renderer: VectorRendererSvg, paint: RadialGradientPaint) = getSharedElement(paint) {
                val gradient = createOrUse<SVGRadialGradientElement>("radialGradient").apply {
                    if (id.isBlank()) { setId(nextId()) }

                    setGradientUnits("userSpaceOnUse")
                    setStart        (paint.start     )
                    setEnd          (paint.end       )
                    updateStops     (paint.colors    )
                }

                renderer.completeOperation(gradient)

                gradient
            }.id

            override fun fill(renderer: VectorRendererSvg, element: SVGGraphicsElement, paint: RadialGradientPaint) {
                element.setFillPattern(makeFill(renderer, paint))
            }

            override fun stroke(renderer: VectorRendererSvg, element: SVGGraphicsElement, paint: RadialGradientPaint, stroke: Stroke) {
                element.setStrokePattern(makeFill(renderer, paint))
            }

            override fun textBackgroundFill(
                renderer: VectorRendererSvg,
                element: SVGGraphicsElement,
                paint: RadialGradientPaint
            ) { element.style.filter = "url(#${makeFill(renderer, paint)})" }
        }
    }

    private val sweepGradientFillHandler by lazy {
        object: FillHandler<SweepGradientPaint> {
            private fun makeForeign(mask: SVGElement, paint: SweepGradientPaint) = getSharedElement(paint) {
                val gradient = createOrUse<SVGElement>("foreignObject").apply {
                    setAttribute("mask", "url(#${mask.id})")

                    appendChild(htmlFactory.create<HTMLElement>().apply {
                        val colors = paint.colors.joinToString(",") {
                            "${it.color.rgbaString} ${it.offset * 360 * degrees `in` degrees}deg"
                        }

                        style.background = "conic-gradient(from ${(paint.rotation `in` degrees) + 90.0}deg at ${paint.center.x}px ${paint.center.y}px, $colors)"
                        style.setWidthPercent (100.0)
                        style.setHeightPercent(100.0)
                    })
                }

                gradient
            }

            private fun makeMask(element: SVGGraphicsElement, config: SVGElement.() -> Unit) = createOrUse<SVGElement>("mask").apply {
                if (id.isBlank()) {
                    setId(nextId())
                }

                element.setFill(null)
                appendChild(element.cloneNode(deep = true).also { (it as SVGElement).config() })
            }

            private fun makeFill(renderer: VectorRendererSvg, paint: SweepGradientPaint, element: SVGGraphicsElement) {
                val mask = makeMask(element) {
                    setFill  (White)
                    setStroke(null )
                }

                renderer.completeOperation(mask)
                renderer.completeOperation(makeForeign(mask, paint).apply {
                    val bbox = element.getBBox_(BoundingBoxOptions())

                    setAttribute("width",  "${bbox.x + bbox.width }")
                    setAttribute("height", "${bbox.y + bbox.height}")
                })
            }

            private fun makeStroke(renderer: VectorRendererSvg, paint: SweepGradientPaint, element: SVGGraphicsElement, stroke: Stroke) {
                val mask = makeMask(element) {
                    setFill  (null  )
                    setStroke(Stroke(
                        dashes     = stroke.dashes,
                        lineCap    = stroke.lineCap,
                        lineJoint  = stroke.lineJoint,
                        thickness  = stroke.thickness,
                        dashOffset = stroke.dashOffset,
                    ))
                    setStrokeColor(White)
                }

                renderer.completeOperation(mask                   )
                renderer.completeOperation(makeForeign(mask, paint))
            }

            override fun fill(renderer: VectorRendererSvg, element: SVGGraphicsElement, paint: SweepGradientPaint) {
                makeFill(renderer, paint, element)
            }

            override fun stroke(renderer: VectorRendererSvg, element: SVGGraphicsElement, paint: SweepGradientPaint, stroke: Stroke) {
                makeStroke(renderer, paint, element, stroke)
            }

            override fun textBackgroundFill(
                renderer: VectorRendererSvg,
                element: SVGGraphicsElement,
                paint: SweepGradientPaint
            ) { makeFill(renderer, paint, element) }
        }
    }

    private fun getSharedElement(key: Any, block: () -> SVGElement): SVGElement {
        return previousElementCache.getOrPut(key) {
            block().also {
                elementCache[key] = it
            }
        }
    }

    private fun createImage(image: Image, destination: Rectangle, radius: Double, opacity: Float) = createOrUse<SVGElement>("image").apply {
        /*
         * xlink:href (https://developer.mozilla.org/en-US/docs/Web/SVG/Attribute/xlink:href) is deprecated for SVG 2.0, but Safari doesn't seem to support just href yet
         */
        setAttributeNS("http://www.w3.org/2000/svg",   "xlink", "http://www.w3.org/1999/xlink")
        setAttributeNS("http://www.w3.org/1999/xlink", "href",  image.source)
        setAttribute("preserveAspectRatio", "none")

        setBounds(destination)

        style.apply {
            setOpacity     (opacity)
            setBorderRadius(radius )
        }
    }

    private fun SVGElement.updateStops(stops: List<GradientPaint.Stop>) {
        stops.forEachIndexed { index, stop ->
            when (val child = childAt(index)) {
                is SVGElement -> child.apply {
                    setStopColor (stop.color )
                    setStopOffset(stop.offset)
                }
                else -> add(svgFactory<SVGElement>("stop").apply {
                    setStopColor (stop.color )
                    setStopOffset(stop.offset)
                })
            }
        }
    }

    private companion object {
        private val containerElements = arrayOf("svg", "filter", "linearGradient", "radialGradient", "feMerge")
    }
}
