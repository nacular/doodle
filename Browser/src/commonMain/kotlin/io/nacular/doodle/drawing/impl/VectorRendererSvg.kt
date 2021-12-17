package io.nacular.doodle.drawing.impl

import io.nacular.doodle.HTMLElement
import io.nacular.doodle.Node
import io.nacular.doodle.SVGCircleElement
import io.nacular.doodle.SVGElement
import io.nacular.doodle.SVGEllipseElement
import io.nacular.doodle.SVGLinearGradientElement
import io.nacular.doodle.SVGPathElement
import io.nacular.doodle.SVGPatternElement
import io.nacular.doodle.SVGPolygonElement
import io.nacular.doodle.SVGRadialGradientElement
import io.nacular.doodle.SVGRectElement
import io.nacular.doodle.clear
import io.nacular.doodle.clipPath
import io.nacular.doodle.dom.DominantBaseline.TextBeforeEdge
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.SvgFactory
import io.nacular.doodle.dom.add
import io.nacular.doodle.dom.addIfNotPresent
import io.nacular.doodle.dom.defaultFontSize
import io.nacular.doodle.dom.left
import io.nacular.doodle.dom.parent
import io.nacular.doodle.dom.remove
import io.nacular.doodle.dom.removeTransform
import io.nacular.doodle.dom.setBorderRadius
import io.nacular.doodle.dom.setBounds
import io.nacular.doodle.dom.setCircle
import io.nacular.doodle.dom.setDefaultFill
import io.nacular.doodle.dom.setDominantBaseline
import io.nacular.doodle.dom.setEllipse
import io.nacular.doodle.dom.setEnd
import io.nacular.doodle.dom.setFill
import io.nacular.doodle.dom.setFillPattern
import io.nacular.doodle.dom.setFillRule
import io.nacular.doodle.dom.setFloodColor
import io.nacular.doodle.dom.setFont
import io.nacular.doodle.dom.setGradientUnits
import io.nacular.doodle.dom.setId
import io.nacular.doodle.dom.setLeft
import io.nacular.doodle.dom.setOpacity
import io.nacular.doodle.dom.setPathData
import io.nacular.doodle.dom.setPatternTransform
import io.nacular.doodle.dom.setPoints
import io.nacular.doodle.dom.setPosition
import io.nacular.doodle.dom.setRX
import io.nacular.doodle.dom.setRY
import io.nacular.doodle.dom.setRadius
import io.nacular.doodle.dom.setSize
import io.nacular.doodle.dom.setStart
import io.nacular.doodle.dom.setStopColor
import io.nacular.doodle.dom.setStopOffset
import io.nacular.doodle.dom.setStroke
import io.nacular.doodle.dom.setStrokeColor
import io.nacular.doodle.dom.setStrokePattern
import io.nacular.doodle.dom.setTextDecoration
import io.nacular.doodle.dom.setTop
import io.nacular.doodle.dom.setTransform
import io.nacular.doodle.dom.setX1
import io.nacular.doodle.dom.setX2
import io.nacular.doodle.dom.setY1
import io.nacular.doodle.dom.setY2
import io.nacular.doodle.dom.top
import io.nacular.doodle.drawing.AffineTransform
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.ColorPaint
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.InnerShadow
import io.nacular.doodle.drawing.LinearGradientPaint
import io.nacular.doodle.drawing.OuterShadow
import io.nacular.doodle.drawing.Paint
import io.nacular.doodle.drawing.PatternPaint
import io.nacular.doodle.drawing.RadialGradientPaint
import io.nacular.doodle.drawing.Renderer.FillRule
import io.nacular.doodle.drawing.Shadow
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.geometry.Circle
import io.nacular.doodle.geometry.Ellipse
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Point.Companion.Origin
import io.nacular.doodle.geometry.Polygon
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.get
import io.nacular.doodle.image.Image
import io.nacular.doodle.image.impl.ImageImpl
import io.nacular.doodle.text.Style
import io.nacular.doodle.text.StyledText
import io.nacular.doodle.utils.IdGenerator
import io.nacular.doodle.utils.splitMatches
import io.nacular.measured.units.Angle
import io.nacular.measured.units.Angle.Companion.cos
import io.nacular.measured.units.Angle.Companion.degrees
import io.nacular.measured.units.Angle.Companion.sin
import io.nacular.measured.units.Measure
import io.nacular.measured.units.times

internal open class VectorRendererSvg constructor(
        protected var context       : CanvasContext,
        private   val svgFactory    : SvgFactory,
        private   val htmlFactory   : HtmlFactory,
        private   val textMetrics   : TextMetrics,
        private   val idGenerator   : IdGenerator,
                      rootSvgElement: SVGElement? = null): VectorRenderer {

    protected var svgElement    : SVGElement? = null
    private   var rootSvgElement: SVGElement? = null

    private val region get() = context.renderRegion

    private var renderPosition: Node? = null

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

    override fun text(text: String, font: Font?, at: Point, fill: Paint) = present(stroke = null, fill = fill) {
        when {
            text.isNotBlank() -> makeText(text, font, at, fill)
            else              -> null
        }
    }

    override fun text(text: StyledText, at: Point) {
        when {
            text.count > 0 -> {
                syncShadows  ()
                updateRootSvg() // Done here since present normally does this
                completeOperation(makeStyledText(text, at))
            }
        }
    }

    override fun wrapped(text: String, font: Font?, at: Point, leftMargin: Double, rightMargin: Double, fill: Paint) {
        syncShadows()

        StyledText(text, font, foreground = fill).first().let { (text, style) ->
            wrappedText(text, style, at, leftMargin, rightMargin)
        }
    }

    override fun wrapped(text: StyledText, at: Point, leftMargin: Double, rightMargin: Double) {
        syncShadows()

        var offset = at

        text.forEach { (text, style) ->
            offset = wrappedText(text, style, offset, leftMargin, rightMargin)
        }
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

        pushClip(Rectangle(size = context.size))

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

        if (renderPosition != null) {
            findSvgDepthFirst(context.renderRegion)?.let {
                rootSvgElement      = it
                svgElement          = it
                this.renderPosition = it.firstChild
            }
        }
    }

    override fun flush() {
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

    protected fun pushClip(rectangle: Rectangle, radius: Double = 0.0) {
        val svg = createOrUse<SVGElement>("svg").apply {
            renderPosition = this.firstChild

            // Here to ensure nested SVG has correct size
            addIfNotPresent(makeRect(rectangle, radius), 0)

            // FIXME: Support rounding
            renderPosition = renderPosition?.nextSibling
        }

        if (svgElement == null || svg.parentNode != svgElement) {
            updateRootSvg()

            completeOperation(svg)
        }

        svgElement = svg
    }

    protected fun popClip() {
        // Clear any remaining items that were previously rendered within the sub-region that won't be rendered anymore
        flush()

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

    protected fun <T: SVGElement> createOrUse(tag: String, possible: Node? = null): T {
        val element: Node? = possible ?: renderPosition

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

    private fun makeText(text: String, font: Font?, at: Point, fill: Paint?) = createOrUse<SVGElement>("text").apply {
        if (innerHTML != text) {
            innerHTML = ""
            add(htmlFactory.createText(text))
        }

        setPosition         (at            )
        setDominantBaseline(TextBeforeEdge)

        this.style.whiteSpace = "pre"

        font?.let {
            style.setFont(it)
        }

        when (fill) {
            null -> setDefaultFill(    )
            else -> setFill       (null)
        }

        setStroke(null)
    }

    private fun makeStyledText(text: StyledText, at: Point) = createOrUse<SVGElement>("text").apply {
        setPosition(at)

        val oldRenderPosition = renderPosition

        text.forEach { (text, style) ->
            val background: SVGElement? = (style.background?.takeIf { it is ColorPaint } as? ColorPaint?)?.let { textBackground(it) }?.also {
                completeOperation(it)
            }

            add(makeTextSegment(text, style).also { segment ->
                background?.let {
                    segment.style.filter = "url(#${it.id})"
                }
            })

            renderPosition = renderPosition?.nextSibling
        }

        flush()

        renderPosition = if (parentNode != null) this else oldRenderPosition
    }

    private fun makeTextSegment(text: String, style: Style) = createOrUse<SVGElement>("tspan").apply {
        if (innerHTML != text) {
            innerHTML = ""
            add(htmlFactory.createText(text))
        }

        setFill            (null          )
        setStroke          (null          )
        setDominantBaseline(TextBeforeEdge)

        this.style.setTextDecoration(style.decoration)

        this.style.whiteSpace = "pre"

        style.font?.let {
            this.style.setFont(it)
        }

        // TODO: Support Background, TextDecoration

        style.foreground?.let {
            fillElement(this, it, true)
        } ?: setDefaultFill()
    }

    private fun wrappedText(text: String, style: Style, at: Point, leftMargin: Double, rightMargin: Double): Point {
        val lines        = mutableListOf<Pair<String, Point>>()
        val words        = text.splitMatches("""\s""".toRegex()).matches
        var line         = ""
        var lineTest     : String
        var currentPoint = at
        var endX         = currentPoint.x

        words.forEach {
            val word      = it.match
            val delimiter = it.delimiter
            lineTest      = line + word + delimiter

            val metric = textMetrics.size(lineTest, style.font)

            endX = currentPoint.x + metric.width

            if (endX > rightMargin) {
                lines += line to currentPoint

                line         = word + delimiter
                currentPoint = Point(leftMargin, at.y + lines.size * (1 + (style.font?.size ?: defaultFontSize).toDouble()))
                endX         = leftMargin
            } else {
                line = lineTest
            }
        }

        if (line.isNotBlank()) {
            endX   = currentPoint.x + textMetrics.width(line, style.font)
            lines += line to currentPoint
        }

        lines.filter { it.first.isNotBlank() }.forEach { (text, at) ->
            text(StyledText(text, style.font, foreground = style.foreground, background = style.background, decoration = style.decoration), at)
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
        when {
            !data.isBlank() -> makePath(data).also { it.setFillRule(fillRule) }
            else            -> null
        }
    }

    private fun present(stroke: Stroke?, fill: Paint?, block: () -> SVGElement?) {
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

    private fun makeArc  (center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>) = withPath(makeArcPathData(center, radius, sweep, rotation))
    private fun makeWedge(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>) = withPath("${makeArcPathData(center, radius, sweep, rotation)} L${center.x},${center.y}")

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

        return "M$startX,$startY A$radius,$radius ${rotation `in` degrees } $largeArc,0 $endX,$endY"
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

    private fun outlineElement(element: SVGElement, stroke: Stroke, clearFill: Boolean = true) {
        if (!stroke.visible) {
            return
        }

        if (clearFill) {
            element.setFill(null)
        }

        strokeElement(element, stroke)
    }

    private fun fillElement(element: SVGElement, fill: Paint, clearOutline: Boolean = true) {
        when (fill) {
            is ColorPaint   -> SolidFillHandler.fill     (this, element, fill)
            is PatternPaint        -> canvasFillHandler.fill    (this, element, fill)
            is LinearGradientPaint -> LinearFillHandler  ().fill(this, element, fill)
            is RadialGradientPaint -> GradientFillHandler().fill(this, element, fill)
        }

        if (clearOutline) {
            element.setStroke(null)
        }
    }

    private fun strokeElement(element: SVGElement, stroke: Stroke) {
        when (val fill = stroke.fill) {
            is ColorPaint          -> SolidFillHandler.stroke     (this, element, fill)
            is PatternPaint        -> canvasFillHandler.stroke    (this, element, fill)
            is LinearGradientPaint -> LinearFillHandler  ().stroke(this, element, fill)
            is RadialGradientPaint -> GradientFillHandler().stroke(this, element, fill)
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
            setAttribute("operator", "and"          )
        }, index)

        renderPosition = renderPosition?.nextSibling

        flush()
        renderPosition = if (parentNode != null) this else oldRenderPosition
    }

    private fun outerShadow(shadow: OuterShadow) = createOrUse<SVGElement>("filter").apply {
        if (id.isBlank()) { setId(nextId()) }

        val oldRenderPosition = renderPosition

        renderPosition = firstChild

        addIfNotPresent(createOrUse<SVGElement>("feDropShadow").apply {
            setAttribute("dx",            "${shadow.horizontal    }"  )
            setAttribute("dy",            "${shadow.vertical      }"  )
            setAttribute("stdDeviation",  "${shadow.blurRadius - 1}"  )
            setFloodColor(shadow.color)
            setAttribute("flood-opacity", "${shadow.color.opacity}"   )
        }, 0)

        renderPosition = renderPosition?.nextSibling

        flush()
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

        flush()
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
        fun fill  (renderer: VectorRendererSvg, element: SVGElement, paint: B)
        fun stroke(renderer: VectorRendererSvg, element: SVGElement, paint: B)
    }

    private object SolidFillHandler: FillHandler<ColorPaint> {
        override fun fill(renderer: VectorRendererSvg, element: SVGElement, paint: ColorPaint) {
            element.setFill(paint.color)
        }

        override fun stroke(renderer: VectorRendererSvg, element: SVGElement, paint: ColorPaint) {
            element.setStrokeColor(paint.color)
        }
    }

    private val canvasFillHandler: FillHandler<PatternPaint> by lazy {
        object: FillHandler<PatternPaint> {
            private fun makeFill(renderer: VectorRendererSvg, paint: PatternPaint): SVGElement {
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

                paint.paint(PatternCanvas(object: CanvasContext {
                    override var size get() = paint.bounds.size; set(@Suppress("UNUSED_PARAMETER") value) {}
                    override val renderRegion = pattern
                    override var renderPosition: Node? = pattern
                    override val shadows get() = context.shadows
                    override fun markDirty() = context.markDirty()
                }, svgFactory, htmlFactory, textMetrics, idGenerator, pattern))

                return pattern
            }

            override fun fill(renderer: VectorRendererSvg, element: SVGElement, paint: PatternPaint) {
                element.setFillPattern(makeFill(renderer, paint))
            }

            override fun stroke(renderer: VectorRendererSvg, element: SVGElement, paint: PatternPaint) {
                element.setStrokePattern(makeFill(renderer, paint))
            }
        }
    }

    private class ContextWrapper(delegate: CanvasContext): CanvasContext by delegate {
        override val shadows: MutableList<Shadow> = mutableListOf()
    }

    private class PatternCanvas(
            context       : CanvasContext,
            svgFactory    : SvgFactory,
            htmlFactory   : HtmlFactory,
            textMetrics   : TextMetrics,
            idGenerator   : IdGenerator,
            patternElement: SVGElement
    ): VectorRendererSvg(context, svgFactory, htmlFactory, textMetrics, idGenerator, patternElement), NativeCanvas {
        private val contextWrapper = ContextWrapper(context)

        init {
            this.context = contextWrapper
        }

        override var size get() = context.size; set(@Suppress("UNUSED_PARAMETER") value) {}

        override fun transform(transform: AffineTransform, block: Canvas.() -> Unit) = when (transform.isIdentity) {
            true -> block(this)
            else -> {
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

        override fun clip(rectangle: Rectangle, radius: Double, block: Canvas.() -> Unit) {
            pushClip(rectangle, radius)
            block   (this             )
            popClip (                 )
        }

        override fun clip(polygon: Polygon, block: Canvas.() -> Unit) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun clip(ellipse: Ellipse, block: Canvas.() -> Unit) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun clip(path: io.nacular.doodle.geometry.Path, block: Canvas.() -> Unit) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun shadow(shadow: Shadow, block: Canvas.() -> Unit) {
            contextWrapper.shadows += shadow
            block (this  )
            contextWrapper.shadows -= shadow
        }

        override fun addData(elements: List<HTMLElement>, at: Point) {
            // FIXME: foreignObject doesn't seem to work when nested in pattern element

            createOrUse<SVGElement>("foreignObject").apply {
                setSize(size)

                elements.asReversed().forEach {
                    if (at.y != 0.0 ) it.style.setTop (it.top  + at.y)
                    if (at.x != 0.0 ) it.style.setLeft(it.left + at.x)

                    addIfNotPresent(it.cloneNode(true), 0)
                }

                completeOperation(this)
            }
        }

        private fun createClip(rectangle: Rectangle, radius: Double = 0.0) = createOrUse<SVGElement>("clipPath").apply {
            if (id.isBlank()) { setId(nextId()) }

            addIfNotPresent(makeRect(rectangle, radius) ,0)
        }

        private fun createImage(image: ImageImpl, destination: Rectangle, radius: Double, opacity: Float) = createOrUse<SVGElement>("image").apply {
            /*
             * xlink:href (https://developer.mozilla.org/en-US/docs/Web/SVG/Attribute/xlink:href) is deprecated for SVG 2.0, but Safari doesn't seem to support just href yet
             */
            setAttributeNS("http://www.w3.org/2000/svg",   "xlink", "http://www.w3.org/1999/xlink")
            setAttributeNS("http://www.w3.org/1999/xlink", "href",  image.source)

            setBounds(destination)

            style.apply {
                setOpacity     (opacity)
                setBorderRadius(radius )
            }
        }
    }

    private inner class LinearFillHandler: FillHandler<LinearGradientPaint> {
        private fun makeFill(renderer: VectorRendererSvg, paint: LinearGradientPaint): SVGLinearGradientElement {
            // FIXME: Re-use elements when possible
            val gradient = createOrUse<SVGLinearGradientElement>("linearGradient").apply {
                if (id.isBlank()) { setId(nextId()) }

                setGradientUnits("userSpaceOnUse")
                setX1(paint.start.x              )
                setY1(paint.start.y              )
                setX2(paint.end.x                )
                setY2(paint.end.y                )
                clear(                           )

                paint.colors.forEach {
                    // FIXME: Re-use elements when possible
                    add(svgFactory<SVGElement>("stop").apply {
                        setStopColor (it.color )
                        setStopOffset(it.offset)
                    })
                }
            }

            renderer.completeOperation(gradient)

            return gradient
        }

        override fun fill(renderer: VectorRendererSvg, element: SVGElement, paint: LinearGradientPaint) {
            element.setFillPattern(makeFill(renderer, paint))
        }

        override fun stroke(renderer: VectorRendererSvg, element: SVGElement, paint: LinearGradientPaint) {
            element.setStrokePattern(makeFill(renderer, paint))
        }
    }

    private inner class GradientFillHandler: FillHandler<RadialGradientPaint> {
        private fun makeFill(renderer: VectorRendererSvg, paint: RadialGradientPaint): SVGRadialGradientElement {
            // FIXME: Re-use elements when possible
            val gradient = createOrUse<SVGRadialGradientElement>("radialGradient").apply {
                if (id.isBlank()) { setId(nextId()) }

                setGradientUnits("userSpaceOnUse")
                setStart(paint.start)
                setEnd  (paint.end  )
                clear   (          )

                paint.colors.forEach {
                    // FIXME: Re-use elements when possible
                    add(svgFactory<SVGElement>("stop").apply {
                        setStopColor (it.color )
                        setStopOffset(it.offset)
                    })
                }
            }

            renderer.completeOperation(gradient)

            return gradient
        }

        override fun fill(renderer: VectorRendererSvg, element: SVGElement, paint: RadialGradientPaint) {
            element.setFillPattern(makeFill(renderer, paint))
        }

        override fun stroke(renderer: VectorRendererSvg, element: SVGElement, paint: RadialGradientPaint) {
            element.setStrokePattern(makeFill(renderer, paint))
        }
    }

    private companion object {
        private val containerElements = setOf("svg", "filter")
    }
}
