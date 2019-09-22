package com.nectar.doodle.drawing.impl

import com.nectar.doodle.Node
import com.nectar.doodle.SVGCircleElement
import com.nectar.doodle.SVGElement
import com.nectar.doodle.SVGEllipseElement
import com.nectar.doodle.SVGGradientElement
import com.nectar.doodle.SVGPathElement
import com.nectar.doodle.SVGPatternElement
import com.nectar.doodle.SVGPolygonElement
import com.nectar.doodle.SVGRectElement
import com.nectar.doodle.clear
import com.nectar.doodle.clipPath
import com.nectar.doodle.dom.AlignmentBaseline.TextBeforeEdge
import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.dom.SvgFactory
import com.nectar.doodle.dom.add
import com.nectar.doodle.dom.addIfNotPresent
import com.nectar.doodle.dom.defaultFontSize
import com.nectar.doodle.dom.parent
import com.nectar.doodle.dom.remove
import com.nectar.doodle.dom.removeTransform
import com.nectar.doodle.dom.setAlignmentBaseline
import com.nectar.doodle.dom.setBorderRadius
import com.nectar.doodle.dom.setBounds
import com.nectar.doodle.dom.setCircle
import com.nectar.doodle.dom.setDefaultFill
import com.nectar.doodle.dom.setEllipse
import com.nectar.doodle.dom.setFill
import com.nectar.doodle.dom.setFillPattern
import com.nectar.doodle.dom.setFillRule
import com.nectar.doodle.dom.setFloodColor
import com.nectar.doodle.dom.setFont
import com.nectar.doodle.dom.setGradientUnits
import com.nectar.doodle.dom.setId
import com.nectar.doodle.dom.setOpacity
import com.nectar.doodle.dom.setPathData
import com.nectar.doodle.dom.setPoints
import com.nectar.doodle.dom.setPosition
import com.nectar.doodle.dom.setRX
import com.nectar.doodle.dom.setRY
import com.nectar.doodle.dom.setSize
import com.nectar.doodle.dom.setStopColor
import com.nectar.doodle.dom.setStopOffset
import com.nectar.doodle.dom.setStroke
import com.nectar.doodle.dom.setStrokeDash
import com.nectar.doodle.dom.setStrokeWidth
import com.nectar.doodle.dom.setTransform
import com.nectar.doodle.dom.setX1
import com.nectar.doodle.dom.setX2
import com.nectar.doodle.dom.setY1
import com.nectar.doodle.dom.setY2
import com.nectar.doodle.drawing.AffineTransform
import com.nectar.doodle.drawing.Brush
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.ColorBrush
import com.nectar.doodle.drawing.Font
import com.nectar.doodle.drawing.InnerShadow
import com.nectar.doodle.drawing.LinearGradientBrush
import com.nectar.doodle.drawing.OuterShadow
import com.nectar.doodle.drawing.PatternBrush
import com.nectar.doodle.drawing.Pen
import com.nectar.doodle.drawing.Renderer.FillRule
import com.nectar.doodle.drawing.Shadow
import com.nectar.doodle.drawing.TextMetrics
import com.nectar.doodle.geometry.Circle
import com.nectar.doodle.geometry.ConvexPolygon
import com.nectar.doodle.geometry.Ellipse
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Point.Companion.Origin
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.get
import com.nectar.doodle.image.Image
import com.nectar.doodle.image.impl.ImageImpl
import com.nectar.doodle.text.Style
import com.nectar.doodle.text.StyledText
import com.nectar.doodle.utils.isEven
import com.nectar.doodle.utils.splitMatches
import com.nectar.measured.units.Angle
import com.nectar.measured.units.Measure
import com.nectar.measured.units.cos
import com.nectar.measured.units.degrees
import com.nectar.measured.units.sin
import com.nectar.measured.units.times
import kotlin.math.max

internal open class VectorRendererSvg constructor(
        private val context    : CanvasContext,
        private val svgFactory : SvgFactory,
        private val htmlFactory: HtmlFactory,
        private val textMetrics: TextMetrics): VectorRenderer {

    protected lateinit var svgElement    : SVGElement
    private lateinit var rootSvgElement: SVGElement

    private val region get() = context.renderRegion

    private var renderPosition: Node? = null

    protected fun nextId() = "${id++}"

    override fun line(point1: Point, point2: Point, pen: Pen) = drawPath(pen, null, null, point1, point2)

    override fun path(points: List<Point>,           brush: Brush, fillRule: FillRule?) = drawPath(null, brush, fillRule, *points.toTypedArray())
    override fun path(points: List<Point>, pen: Pen                                   ) = drawPath(pen,  null,  null,     *points.toTypedArray())
    override fun path(points: List<Point>, pen: Pen, brush: Brush, fillRule: FillRule?) = drawPath(pen,  brush, fillRule, *points.toTypedArray())

    override fun path(path: com.nectar.doodle.geometry.Path,           brush: Brush, fillRule: FillRule?) = drawPath(path.data, null, brush, fillRule)
    override fun path(path: com.nectar.doodle.geometry.Path, pen: Pen                                   ) = drawPath(path.data, pen,  null,  null    )
    override fun path(path: com.nectar.doodle.geometry.Path, pen: Pen, brush: Brush, fillRule: FillRule?) = drawPath(path.data, pen,  brush, fillRule)

    override fun rect(rectangle: Rectangle,           brush: Brush ) = drawRect(rectangle, null, brush)
    override fun rect(rectangle: Rectangle, pen: Pen, brush: Brush?) = drawRect(rectangle, pen,  brush)

    override fun poly(polygon: ConvexPolygon,           brush: Brush ) = drawPoly(polygon, null, brush)
    override fun poly(polygon: ConvexPolygon, pen: Pen, brush: Brush?) = drawPoly(polygon, pen,  brush)

    override fun rect(rectangle: Rectangle, radius: Double,           brush: Brush ) = drawRect(rectangle, radius, null, brush)
    override fun rect(rectangle: Rectangle, radius: Double, pen: Pen, brush: Brush?) = drawRect(rectangle, radius, pen,  brush)

    override fun arc(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>,           brush: Brush ) = drawArc(center, radius, sweep, rotation, null, brush)
    override fun arc(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, pen: Pen, brush: Brush?) = drawArc(center, radius, sweep, rotation, pen,  brush)

    override fun wedge(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>,           brush: Brush ) = drawWedge(center, radius, sweep, rotation, null, brush)
    override fun wedge(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, pen: Pen, brush: Brush?) = drawWedge(center, radius, sweep, rotation, pen,  brush)

    override fun circle(circle: Circle,           brush: Brush ) = drawCircle(circle, null, brush)
    override fun circle(circle: Circle, pen: Pen, brush: Brush?) = drawCircle(circle, pen,  brush)

    override fun ellipse(ellipse: Ellipse,           brush: Brush ) = drawEllipse(ellipse, null, brush)
    override fun ellipse(ellipse: Ellipse, pen: Pen, brush: Brush?) = drawEllipse(ellipse, pen,  brush)

    override fun text(text: String, font: Font?, at: Point, brush: Brush) = present(pen = null, brush = brush) {
        when {
            text.isNotBlank() -> makeText(text, font, at, brush)
            else              -> null
        }
    }

    override fun text(text: StyledText, at: Point) {
        when {
            text.count > 0 -> {
                updateRootSvg() // Done here since present normally does this
                completeOperation(makeStyledText(text, at))
            }
        }
    }

    override fun wrapped(text: String, font: Font?, at: Point, leftMargin: Double, rightMargin: Double, brush: Brush) {
        StyledText(text, font, foreground = brush).first().let { (text, style) ->
            wrappedText(text, style, at, leftMargin, rightMargin)
        }
    }

    override fun wrapped(text: StyledText, at: Point, leftMargin: Double, rightMargin: Double) {
        var offset = at

        text.forEach { (text, style) ->
            offset = wrappedText(text, style, offset, leftMargin, rightMargin)
        }
    }

    private fun makeText(text: String, font: Font?, at: Point, brush: Brush?) = createOrUse<SVGElement>("text").apply {
        if (innerHTML != text) {
            innerHTML = ""
            add(htmlFactory.createText(text))
        }

        setPosition         (at            )
        setAlignmentBaseline(TextBeforeEdge)

        this.style.whiteSpace = "pre"

        font?.let {
            style.setFont(it)
        }

        when (brush) {
            null -> setDefaultFill(    )
            else -> setFill       (null)
        }

        setStroke(null)
    }

    private fun makeStyledText(text: StyledText, at: Point) = createOrUse<SVGElement>("text").apply {
        setPosition(at)

        val oldRenderPosition = renderPosition

        text.forEach { (text, style) ->
            val background: SVGElement? = (style.background?.takeIf { it is ColorBrush } as? ColorBrush?)?.let { textBackground(it) }?.also {
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

        renderPosition = if (this.parentNode != null) this else oldRenderPosition
    }

    private fun makeTextSegment(text: String, style: Style) = createOrUse<SVGElement>("tspan").apply {
        if (innerHTML != text) {
            innerHTML = ""
            add(htmlFactory.createText(text))
        }

        setFill             (null          )
        setStroke           (null          )
        setAlignmentBaseline(TextBeforeEdge)

        this.style.whiteSpace = "pre"

        style.font?.let {
            this.style.setFont(it)
        }

        // TODO: Support Background

        style.foreground?.let {
            fillElement(this, it, true)
        } ?: setDefaultFill()
    }

    private fun wrappedText(text: String, style: Style, at: Point, leftMargin: Double, rightMargin: Double): Point {
        val lines        = mutableListOf<Pair<String, Point>>()
        val words        = text.splitMatches("""\s""".toRegex())
        var line         = ""
        var lineTest     : String
        var currentPoint = at
        var endX         = currentPoint.x

        words.forEach { (word, delimiter) ->
            lineTest = line + word + delimiter

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
            text(StyledText(text, style.font, foreground = style.foreground, background = style.background), at)
        }

        return Point(endX, currentPoint.y)
    }

    protected fun pushClip(rectangle: Rectangle) {
        val svg = createOrUse<SVGElement>("svg").apply {
            renderPosition = this.firstChild

            // Here to ensure nested SVG has correct size
            addIfNotPresent(makeRect(rectangle), 0)

            renderPosition = renderPosition?.nextSibling
        }

        if (!::svgElement.isInitialized || svg.parentNode != svgElement) {
            updateRootSvg()

            completeOperation(svg)
        }

        svgElement = svg
    }

    protected fun popClip() {
        // Clear any remaining items that were previously rendered within the sub-region that won't be rendered anymore
        flush()

        renderPosition = svgElement.nextSibling

        svgElement.parentNode?.let {
            svgElement = it as SVGElement
        }
    }

    override fun add(shadow: Shadow) {
        pushClip(Rectangle(size = context.size))

        when (shadow) {
            is InnerShadow -> innerShadow(shadow)
            is OuterShadow -> outerShadow(shadow)
        }.let {
            completeOperation(it)
            svgElement.style.filter = "url(#${it.id})"
        }
    }

    override fun remove(shadow: Shadow) {
        popClip()
    }

    override fun clear() {
        (0 until context.renderRegion.childNodes.length).map { context.renderRegion.childNodes[it] }.firstOrNull { isCompatibleSvgElement(it) }?.let {
            rootSvgElement = it as SVGElement
            renderPosition = it.firstChild
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

    protected open fun completeOperation(element: SVGElement) {
        if (context.renderPosition == null && svgElement.parent == null) {
            region.add(svgElement)
        } else if (context.renderPosition !== rootSvgElement) {
            context.renderPosition?.parent?.replaceChild(rootSvgElement, context.renderPosition!!)
        }

        if (renderPosition == null) {
            svgElement.add(element)
        } else {
            if (renderPosition !== element) {
                renderPosition?.parent?.replaceChild(element, renderPosition!!)
            }

            renderPosition = element.nextSibling
        }

        context.renderPosition = rootSvgElement.nextSibling
    }

    private fun drawPath(pen: Pen?, brush: Brush? = null, fillRule: FillRule? = null, vararg points: Point) = present(pen, brush) {
        when {
            points.isNotEmpty() -> makePath(*points).also { it.setFillRule(fillRule) }
            else                -> null
        }
    }

    private fun drawPath(data: String, pen: Pen?, brush: Brush?, fillRule: FillRule?) = present(pen, brush ) {
        when {
            !data.isBlank() -> makePath(data).also { it.setFillRule(fillRule) }
            else            -> null
        }
    }

    private fun present(pen: Pen?, brush: Brush?, block: () -> SVGElement?) {
        if (visible(pen, brush)) {
            // Update SVG Element to enable re-use if the top-level cursor has moved to a new place
            updateRootSvg()

            block()?.let {
                // make sure element is in dom first since some brushes add new elements to the dom
                // this means we get better re-use of nodes since the order in the dom is the element creation order
                completeOperation(it)

                if (brush != null) {
                    fillElement(it, brush, pen == null || !pen.visible)
                }
                if (pen != null) {
                    outlineElement(it, pen, brush == null || !brush.visible)
                }
            }
        }
    }

    private fun drawRect(rectangle: Rectangle, pen: Pen?, brush: Brush?) = present(pen, brush) {
        when {
            !rectangle.empty -> makeClosedPath(
                    Point(rectangle.x,                   rectangle.y                   ),
                    Point(rectangle.x + rectangle.width, rectangle.y                   ),
                    Point(rectangle.x + rectangle.width, rectangle.y + rectangle.height),
                    Point(rectangle.x,                   rectangle.y + rectangle.height))
            else -> null
        }
    }

    private fun drawRect(rectangle: Rectangle, radius: Double, pen: Pen?, brush: Brush?) = present(pen, brush) {
        when {
            !rectangle.empty -> makeRoundedRect(rectangle, radius)
            else             -> null
        }
    }

    private fun visible(pen: Pen?, brush: Brush?) = (pen?.visible ?: false) || (brush?.visible ?: false)

    private fun drawPoly(polygon: ConvexPolygon, pen: Pen?, brush: Brush?) = present(pen, brush) {
        when {
            !polygon.empty -> makeClosedPath(*polygon.points.toTypedArray())
            else           -> null
        }
    }

    private fun drawArc(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, pen: Pen?, brush: Brush?) = present(pen, brush) {
        when {
            radius <= 0 || sweep == 0 * degrees -> null
            sweep < 360 * degrees               -> makeArc(center, radius, sweep, rotation)
            else                                -> makeCircle(Circle(center, radius))
        }
    }

    private fun drawWedge(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, pen: Pen?, brush: Brush?) = present(pen, brush) {
        when {
            radius <= 0 || sweep == 0 * degrees -> null
            sweep < 360 * degrees               -> makeWedge(center, radius, sweep, rotation)
            else                                -> makeCircle(Circle(center, radius))
        }
    }

    private fun drawCircle(circle: Circle, pen: Pen?, brush: Brush?) = present(pen, brush) {
        when {
            !circle.empty -> makeCircle(circle)
            else          -> null
        }
    }

    private fun drawEllipse(ellipse: Ellipse, pen: Pen?, brush: Brush?) = present(pen, brush) {
        when {
            !ellipse.empty -> makeEllipse(ellipse)
            else           -> null
        }
    }

    protected fun makeRect(rectangle: Rectangle): SVGRectElement = createOrUse<SVGRectElement>("rect").apply {
        setBounds(rectangle)

        setFill  (null)
        setStroke(null)
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

    private fun makePath(path: Path): SVGPathElement = makePath(path.data)

    private fun makePath(pathData: String): SVGPathElement = createOrUse<SVGPathElement>("path").apply {
        setPathData(pathData)
    }

    private fun outlineElement(element: SVGElement, pen: Pen, clearFill: Boolean = true) {
        if (!pen.visible) {
            return
        }

        if (clearFill) {
            element.setFill(null)
        }

        element.setStroke(pen.color)
        element.setStrokeWidth(pen.thickness)

        pen.dashes?.let {
            element.setStrokeDash(dashArray(pen))
        }
    }

    private fun fillElement(element: SVGElement, brush: Brush, clearOutline: Boolean = true) {
        when (brush) {
            is ColorBrush          -> SolidFillHandler.fill   (this, element, brush)
            is PatternBrush        -> canvasFillHandler.fill  (this, element, brush)
            is LinearGradientBrush -> LinearFillHandler().fill(this, element, brush)
        }

        if (clearOutline) {
            element.setStroke(null)
        }
    }

    private fun dashArray(pen: Pen) = pen.dashes?.mapIndexed { index, dash ->
        max(0, dash + if (index.isEven) -1 else 1)
    }?.joinToString(",") ?: ""

    private val containerElements = setOf(svgTag, "filter")

    protected fun <T: SVGElement> createOrUse(tag: String, possible: Node? = null): T {
        val element: Node? = possible ?: renderPosition

        return when {
            element == null || element.nodeName != tag -> svgFactory(tag)
            element is SVGElement                      -> {
                if (tag !in containerElements) { element.clear() }
                element.style.filter = ""
                element.removeTransform()
                @Suppress("UNCHECKED_CAST")
                element as T
            }
            else -> throw Exception("Error") // FIXME: handle better
        }
    }

    private fun isCompatibleSvgElement(node: Node?) = node is SVGElement && svgTag == node.nodeName

    private fun textBackground(brush: ColorBrush) = createOrUse<SVGElement>("filter").apply {
        if (id.isBlank()) { setId(nextId()) }

        setBounds(Rectangle(size = Size(1)))

        var index = 0
        val `in`  = "in"

        val oldRenderPosition = renderPosition

        renderPosition = firstChild

        addIfNotPresent(createOrUse<SVGElement>("feFlood").apply {
            setFloodColor(brush.color)
            setAttribute("flood-opacity", "${brush.color.opacity}")
        }, index++)

        renderPosition = renderPosition?.nextSibling

        addIfNotPresent(createOrUse<SVGElement>("feComposite").apply {
            setAttribute(`in`,       "SourceGraphic")
            setAttribute("operator", "and"          )
        }, index)

        renderPosition = renderPosition?.nextSibling

        flush()
        renderPosition = if (this.parentNode != null) this else oldRenderPosition
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
        renderPosition = if (this.parentNode != null) this else oldRenderPosition
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
            setAttribute("in",       "shadow"       )
            setAttribute("in2",      "SourceGraphic")
        }, index)

        renderPosition = renderPosition?.nextSibling

        flush()
        renderPosition = if (this.parentNode != null) this else oldRenderPosition
    }

    protected fun updateRootSvg() {
        if (!::rootSvgElement.isInitialized || (context.renderPosition !== rootSvgElement && context.renderPosition !== rootSvgElement.nextSibling)) {
            // Initialize new SVG root if
            // 1) not initialized
            // 2) it is not longer the active element
            svgElement     = createOrUse("svg", context.renderPosition)
            rootSvgElement = svgElement
            renderPosition = svgElement.firstChild
        }
    }

    private class SVGPath: Path("M", "L", "Z")

    private interface FillHandler<B: Brush> {
        fun fill(renderer: VectorRendererSvg, element: SVGElement, brush: B)
    }

    private object SolidFillHandler: FillHandler<ColorBrush> {
        override fun fill(renderer: VectorRendererSvg, element: SVGElement, brush: ColorBrush) {
            element.setFill(brush.color)
        }
    }

    private val canvasFillHandler: FillHandler<PatternBrush> by lazy {
        object: FillHandler<PatternBrush> {
            override fun fill(renderer: VectorRendererSvg, element: SVGElement, brush: PatternBrush) {

                // FIXME: Re-use elements when possible
                val pattern = createOrUse<SVGPatternElement>("pattern").apply {
                    if (id.isBlank()) { setId(nextId()) }

                    setAttribute("patternUnits", "userSpaceOnUse")
                    setSize(brush.size       )
                    clear  (                 )

                    brush.fill(PatternCanvas(object: CanvasContext {
                        override var size: Size
                            get() = brush.size
                            set(value) {}
                        override val renderRegion = this@apply
                        override var renderPosition: Node? = null
                        override val shadows get() = context.shadows
                    }, svgFactory, htmlFactory, textMetrics))
                }

                renderer.completeOperation(pattern)

                element.setFillPattern(pattern)
            }
        }
    }

    private class PatternCanvas(private val context: CanvasContext, svgFactory: SvgFactory, htmlFactory: HtmlFactory, textMetrics: TextMetrics): VectorRendererSvg(context, svgFactory, htmlFactory, textMetrics), Canvas {
        override var size: Size
            get() = context.size
            set(value) {}

        override fun transform(transform: AffineTransform, block: Canvas.() -> Unit) = when (transform.isIdentity) {
            true -> block(this)
            else -> {
                updateRootSvg()

                pushClip(Rectangle(size = context.size))

                svgElement.setTransform(transform)

                block(this)

                popClip()
            }
        }

        override fun image(image: Image, destination: Rectangle, opacity: Float, radius: Double, source: Rectangle) {
            if (image is ImageImpl && opacity > 0 && !(source.empty || destination.empty)) {
                updateRootSvg()

                if (source.size == image.size && source.position == Origin) {
                    completeOperation(createImage(image, destination, radius, opacity))
                } else {
                    val xRatio = destination.width / source.width
                    val yRatio = destination.height / source.height

                    val imageElement = createImage(image,
                            Rectangle(0 - xRatio * source.x,
                                    0 - yRatio * source.y,
                                    xRatio * image.size.width,
                                    yRatio * image.size.height),
                            0.0,
                            opacity)

                    createClip(destination).let {
                        completeOperation(it)
                        imageElement.style.clipPath = "url(#${it.id})"
                    }

                    completeOperation(imageElement)
                }
            }
        }

        override fun clip(rectangle: Rectangle, block: Canvas.() -> Unit) {
            pushClip(rectangle)
            block   (this     )
            popClip (         )
        }

        override fun shadow(shadow: Shadow, block: Canvas.() -> Unit) {
            add   (shadow)
            block (this  )
            remove(shadow)
        }

        private fun createClip(rectangle: Rectangle) = createOrUse<SVGElement>("clipPath").apply {
            if (id.isBlank()) { setId(nextId()) }

            addIfNotPresent(makeRect(rectangle) ,0)
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

    private inner class LinearFillHandler: FillHandler<LinearGradientBrush> {
        override fun fill(renderer: VectorRendererSvg, element: SVGElement, brush: LinearGradientBrush) {
            // FIXME: Re-use elements when possible
            val gradient = createOrUse<SVGGradientElement>("linearGradient").apply {
                if (id.isBlank()) { setId(nextId()) }

                setGradientUnits("userSpaceOnUse")
                setX1(brush.start.x              )
                setY1(brush.start.y              )
                setX2(brush.end.x                )
                setY2(brush.end.y                )
                clear(                           )

                brush.colors.forEach {
                    // FIXME: Re-use elements when possible
                    add(svgFactory<SVGElement>("stop").apply {
                        setStopColor (it.color )
                        setStopOffset(it.offset)
                    })
                }
            }

            renderer.completeOperation(gradient)

            element.setFillPattern(gradient)
        }
    }

    companion object {
        private       var id     = 0
        private const val svgTag = "svg"
    }
}
