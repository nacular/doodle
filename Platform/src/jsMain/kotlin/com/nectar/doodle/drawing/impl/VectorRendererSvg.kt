package com.nectar.doodle.drawing.impl

import com.nectar.doodle.dom.ShapeRendering
import com.nectar.doodle.dom.SvgFactory
import com.nectar.doodle.dom.add
import com.nectar.doodle.dom.childAt
import com.nectar.doodle.dom.index
import com.nectar.doodle.dom.numChildren
import com.nectar.doodle.dom.parent
import com.nectar.doodle.dom.remove
import com.nectar.doodle.dom.removeTransform
import com.nectar.doodle.dom.setCX
import com.nectar.doodle.dom.setCY
import com.nectar.doodle.dom.setFill
import com.nectar.doodle.dom.setFillPattern
import com.nectar.doodle.dom.setFillRule
import com.nectar.doodle.dom.setHeight
import com.nectar.doodle.dom.setId
import com.nectar.doodle.dom.setPathData
import com.nectar.doodle.dom.setR
import com.nectar.doodle.dom.setRX
import com.nectar.doodle.dom.setRY
import com.nectar.doodle.dom.setStopColor
import com.nectar.doodle.dom.setStopOffset
import com.nectar.doodle.dom.setStroke
import com.nectar.doodle.dom.setStrokeDash
import com.nectar.doodle.dom.setStrokeWidth
import com.nectar.doodle.dom.setWidth
import com.nectar.doodle.dom.setX
import com.nectar.doodle.dom.setX1Percent
import com.nectar.doodle.dom.setX2Percent
import com.nectar.doodle.dom.setY
import com.nectar.doodle.dom.setY1Percent
import com.nectar.doodle.dom.setY2Percent
import com.nectar.doodle.dom.shapeRendering
import com.nectar.doodle.drawing.Brush
import com.nectar.doodle.drawing.CanvasBrush
import com.nectar.doodle.drawing.ColorBrush
import com.nectar.doodle.drawing.LinearGradientBrush
import com.nectar.doodle.drawing.Pen
import com.nectar.doodle.drawing.Renderer.FillRule
import com.nectar.doodle.drawing.Renderer.Optimization
import com.nectar.doodle.drawing.Shadow
import com.nectar.doodle.geometry.Circle
import com.nectar.doodle.geometry.Ellipse
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.ConvexPolygon
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.utils.isEven
import com.nectar.measured.units.Angle
import com.nectar.measured.units.Measure
import com.nectar.measured.units.cos
import com.nectar.measured.units.degrees
import com.nectar.measured.units.sin
import com.nectar.measured.units.times
import org.w3c.dom.Node
import org.w3c.dom.svg.SVGCircleElement
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.svg.SVGEllipseElement
import org.w3c.dom.svg.SVGPathElement
import org.w3c.dom.svg.SVGRectElement
import kotlin.dom.clear
import kotlin.math.max

class VectorRendererSvg constructor(private val context: CanvasContext, private val svgFactory: SvgFactory, private val vectorBackgroundFactory: VectorBackgroundFactory): VectorRenderer {
    private val svgElement: SVGElement
        get() {
            // Clear the remaining elements from existing SVG element
            // or they will be missed on the final flush

            var renderPosition = context.renderPosition

            while (renderPosition != null && isCompatibleSvgElement(renderPosition.previousSibling)) {
                renderPosition = renderPosition.previousSibling
            }

            if (renderPosition is SVGElement && renderPosition.nodeName == svgTag) {
                if (renderPosition.shapeRendering === shapeRendering) {
                    return renderPosition
                }
            } else {
                var lastChild = region.lastChild
                var potential = null as SVGElement?

//                if (lastChild is SVGElement && lastChild.nodeName == svgTag && lastChild.shapeRendering === shapeRendering) {
//                    return lastChild
//                }

                while(lastChild is SVGElement && lastChild.nodeName == svgTag && lastChild.shapeRendering === shapeRendering) {
                    potential = lastChild
                    lastChild = lastChild.lastChild
                }

                if (potential != null) return potential
            }

            flush()

            val svg = svgFactory<SVGElement>(svgTag)

            svg.shapeRendering = shapeRendering

            return svg
        }

    private val region get() = context.renderRegion

    private val shapeRendering get() = when (context.optimization) {
        Optimization.Speed -> ShapeRendering.CrispEdges
        else               -> ShapeRendering.Auto
    }

    private var renderPosition: Node? = null

    fun nextId() = "${id++}"

    override fun line(point1: Point, point2: Point, pen: Pen) = drawPath(pen, point1, point2)

    override fun path(points: List<Point>, pen: Pen) = drawPath(pen, *points.toTypedArray())

    override fun path(path: com.nectar.doodle.geometry.Path,           brush: Brush,  fillRule: FillRule?) = drawPath(path.data, null, brush, fillRule)
    override fun path(path: com.nectar.doodle.geometry.Path, pen: Pen, brush: Brush?, fillRule: FillRule?) = drawPath(path.data, pen,  brush, fillRule)

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

    override fun clip(rectangle: Rectangle, block: VectorRenderer.() -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun clear() {}

    override fun flush() {
        // Remove all elements after the current render position
        renderPosition?.let {
            val index = region.index(it)

            if (index >= 0) {
                while (index < region.numChildren) {
                    region.remove(region.childAt(index)!!)
                }
            }
        }

        renderPosition = null
    }

    private fun drawPath(pen: Pen, vararg points: Point) {
        if (pen.visible && points.isNotEmpty()) {
            val element = makePath(*points)

            outlineElement(element, pen)

            completeOperation(element)
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
            !rectangle.empty -> makeClosedPath(Point(rectangle.x, rectangle.y),
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
            polygon.points.size > 1 -> makeClosedPath(*polygon.points.toTypedArray())
            else                    -> null
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

    private fun makeRect(rectangle: Rectangle): SVGRectElement {
        val element = createElement<SVGRectElement>("rect")

        element.setX    (rectangle.x      )
        element.setY    (rectangle.y      )
        element.setWidth(rectangle.width  )
        element.setHeight(rectangle.height)

        element.setFill  (null)
        element.setStroke(null)

        return element
    }

    private fun makeRoundedRect(aRectangle: Rectangle, radius: Double): SVGElement {
        val element: SVGRectElement = makeRect(aRectangle)

        element.setRX(radius)
        element.setRY(radius)

        element.setFill  (null)
        element.setStroke(null)

        return element
    }

    private fun makeCircle(circle: Circle): SVGElement {
        val element: SVGCircleElement = createElement("circle")

        element.setCX(circle.center.x)
        element.setCY(circle.center.y)
        element.setR(circle.radius)

        element.setFill(null)
        element.setStroke(null)

        return element
    }

    private fun makeEllipse(ellipse: Ellipse): SVGElement {
        val element: SVGEllipseElement = createElement("ellipse")

        element.setCX(ellipse.center.x)
        element.setCY(ellipse.center.y)
        element.setRX(ellipse.xRadius)
        element.setRY(ellipse.yRadius)

        element.setFill(null)
        element.setStroke(null)

        return element
    }

    private fun makeArc  (center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>) = withPath(makeArcPathData(center, radius, sweep, rotation))
    private fun makeWedge(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>) = withPath("${makeArcPathData(center, radius, sweep, rotation)} L${center.x},${center.y}")

    private fun withPath(path: String): SVGElement {
        val element: SVGPathElement = createElement("path")

        element.setPathData(path)

        element.setFill  (null)
        element.setStroke(null)

        return element
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

    private fun makeClosedPath(vararg points: Point): SVGPathElement {
//        val path = SVGPath()

        return createElement<SVGPathElement>("polygon").apply {
            setAttribute("points", points.joinToString(" ") { "${it.x},${it.y}" })
        }

//        path.addPath(*points)
//        path.close()
//
//        return makePath(path)
    }

    private fun makePath(path: Path): SVGPathElement = makePath(path.data)

    private fun makePath(pathData: String): SVGPathElement = createElement<SVGPathElement>("path").apply {
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

        pen.dashStyle?.let {
            if (it.dashes.size > 1) {
                element.setStrokeDash(dashArray(pen))
            }
        }
    }

    private fun fillElement(element: SVGElement, brush: Brush, clearOutline: Boolean = true) {
        when (brush) {
            is ColorBrush          -> SolidFillHandler.fill(this, element, brush)
//          is TextureFillHandler  ->
            is CanvasBrush         -> CanvasFillHandler().fill(this, element, brush)
            is LinearGradientBrush -> LinearFillHandler().fill(this, element, brush)
        }

        if (clearOutline) {
            element.setStroke(null)
        }
    }

    private fun dashArray(pen: Pen) = pen.dashStyle?.dashes?.mapIndexed { index, dash ->
        max(0, dash + if (index.isEven) -1 else 1)
    }?.joinToString(",") ?: ""

    private fun <T: SVGElement> createElement(tag: String): T {
        var element: Node? = context.renderPosition

        if (element != null) {
            val previousSibling = element.previousSibling

            when {
                isCompatibleSvgElement(previousSibling) -> {
                    context.renderPosition = previousSibling

                    element = renderPosition
                }
                isCompatibleSvgElement(element)         -> {
                    renderPosition = element.childAt(0)

                    element = renderPosition
                }
                else                                    -> flush()
            }
        } else if (region.numChildren > 0) {
            val last = region.lastChild

            if (last != null && last is SVGElement && last.nodeName == svgTag && last.shapeRendering === shapeRendering) {
                context.renderPosition = last

                element = renderPosition
            }
        }

        return when {
            element == null || element.nodeName != tag -> svgFactory(tag)
            element is SVGElement                      -> { element.clear(); element.removeTransform(); @Suppress("UNCHECKED_CAST")
            element as T
            }
            else                                       -> throw Exception("Error") // FIXME: handle better
        }
    }

    private fun isCompatibleSvgElement(node: Node?) = node is SVGElement && svgTag == node.nodeName && node.shapeRendering == shapeRendering

    override fun add(shadow: Shadow) {
//        val svg = createElement<SVGElement>("svg").apply {
//            setAttribute("width",  "${context.size.width }")
//            setAttribute("height", "${context.size.height}")
//        }
//
//        val filter = createElement<SVGElement>("filter").apply {
//            setId(nextId())
//            clear()
//
//            add(createElement<SVGElement>("feDropShadow").apply {
//                setAttribute("dx",            "${shadow.horizontal}"   )
//                setAttribute("dy",            "${shadow.vertical  }"   )
//                setAttribute("stdDeviation",  "${shadow.blurRadius}"   )
//                setAttribute("flood-color",   shadow.color.hexString   )
//                setAttribute("flood-opacity", "${shadow.color.opacity}")
//            })
//        }
//
//        // Work-around to ensure root is in DOM, otherwise filters will not be retained
//        if (svgElement.parent == null) {
//            region.add(svgElement)
//        }
//
//        svgElement.add(filter)
//        svg.setAttribute("filter", "url(#${filter.id})")
//
////        svg.style.filter = "drop-shadow(${shadow.horizontal} ${shadow.vertical} ${shadow.blurRadius}px ${shadow.color.hexString})"
//
//        completeOperation(svg)
//
//        renderPosition = svg
//
//        completeOperation(makeRect(Rectangle(size = context.size)))
    }

    override fun remove(shadow: Shadow) {}


    private fun completeOperation(element: SVGElement) {
        val svg = svgElement

        /*
        <filter id="shadow" x="0" y="0" width="200%" height="200%">
            <feDropShadow dx="40" dy="40" stdDeviation="35" flood-color="#ff0000" flood-opacity="1" />
        </filter>
         */

//        var element = e

//        context.shadows.forEachIndexed { index, shadow ->
//            // Need to use groups to add multiple filters
//            if (index > 0) {
//                val old = element
//                element = createElement("g")
//                element.add(old)
//            }
//
//            val filter = createElement<SVGElement>("filter").apply {
//                setId(nextId())
//                clear()
//
//                add(createElement<SVGElement>("feDropShadow").apply {
//                    setAttribute("dx",            "${shadow.horizontal}"   )
//                    setAttribute("dy",            "${shadow.vertical  }"   )
//                    setAttribute("stdDeviation",  "${shadow.blurRadius}"   )
//                    setAttribute("flood-color",   shadow.color.hexString   )
//                    setAttribute("flood-opacity", "${shadow.color.opacity}")
//                })
//            }
//
//            svg.add(filter)
//            element.setAttribute("filter", "url(#${filter.id})")
//        }

        if (context.renderPosition == null) {
            if (svg.parent == null) {
                region.add(svg)
            }
        } else if (context.renderPosition !== svg) {
            context.renderPosition?.parent?.replaceChild(svg, context.renderPosition!!)
        }

        if (renderPosition == null) {
            svg.add(element)
        } else {
            val newRenderPosition = renderPosition?.nextSibling

            if (renderPosition !== element && renderPosition!!.parent === svg) {
                svg.replaceChild(element, renderPosition!!)
            }

            renderPosition = newRenderPosition
        }

        context.renderPosition = svg.nextSibling
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

    private inner class CanvasFillHandler: FillHandler<CanvasBrush> {
        override fun fill(renderer: VectorRendererSvg, element: SVGElement, brush: CanvasBrush) {
            element.style.background = vectorBackgroundFactory(brush)
        }
    }

//    private class TextureFillHandler : FillHandler {
//        override fun fill(aRenderer: VectorRendererSvg, element: SVGElement, brush: Brush): Boolean {
//            if (brush is TextureBrush) {
//                val aImage = brush.image
//                val aDestinationRect = brush.destinationRect
//
//                val aPattern = aRenderer.createElement("pattern")
//
//                aPattern.setId(SVGElement.getNextId())
//                aPattern.setWidth(aDestinationRect.width)
//                aPattern.setHeight(aDestinationRect.height)
//                aPattern.setPatternUnits(Units.userSpaceOnUse)
//
//                val aImageElement = SVGElement.create("image")
//
//                aImageElement.setWidth(aDestinationRect.width)
//                aImageElement.setHeight(aDestinationRect.height)
//                aImageElement.setXLinkHref(aImage.source)
//                aImageElement.setPreserveAspectRatio(false)
//
//                aPattern.removeAll()
//                aPattern.add(aImageElement)
//
//                aRenderer.completeOperation(aPattern)
//
//                element.setFill(null)
//                element.setOpacity(brush.opacity)
//                element.setFillPattern(aPattern)
//
//                val aTask = Service.locator().getTaskFactory().createTask(object : Command() {
//                    fun execute() {
//                        val aBoundingBox = element.getBoundingBox()
//
//                        aPattern.setX(aBoundingBox.x + aDestinationRect.x)
//                        aPattern.setY(aBoundingBox.y + aDestinationRect.y)
//                    }
//                })
//
//                aTask.schedule(0)
//
//                return true
//            }
//
//            return false
//        }
//    }
//
    private inner class LinearFillHandler: FillHandler<LinearGradientBrush> {
        override fun fill(renderer: VectorRendererSvg, element: SVGElement, brush: LinearGradientBrush) {
            // FIXME: Re-use elements when possible
            val gradient = svgFactory<SVGElement>("linearGradient").apply {
                setId(renderer.nextId())
                clear(                 )
            }

            brush.colors.forEach {
                // FIXME: Re-use elements when possible
                gradient.add(svgFactory<SVGElement>("stop").apply {
                    setStopColor (it.color )
                    setStopOffset(it.offset)
                })
            }

            var rotation = brush.rotation

            rotation = ((rotation `in` degrees) % 360) * degrees

            if (rotation < 0 * degrees) {
                rotation = 180 * degrees - rotation
            }

            val fortyFive = 45 * degrees

            if (rotation > fortyFive && rotation < 3 * fortyFive || rotation > 5 * fortyFive && rotation < 7 * fortyFive) {
                configureGradient2(gradient, cos(rotation), rotation, 5 * fortyFive)
            } else {
                configureGradient1(gradient, sin(rotation), rotation, 3 * fortyFive)
            }

            renderer.completeOperation(gradient)

            element.setFillPattern(gradient)
        }

        private fun configureGradient1(gradient: SVGElement, delta: Double, rotation: Measure<Angle>, rotationThreshold: Measure<Angle>) {
            gradient.setY1Percent(50 * (1 - delta))
            gradient.setY2Percent(50 * (1 + delta))

            gradient.setX1Percent(if (rotation > rotationThreshold) 100.0 else   0.0)
            gradient.setX2Percent(if (rotation > rotationThreshold)   0.0 else 100.0)
        }

        private fun configureGradient2(gradient: SVGElement, delta: Double, rotation: Measure<Angle>, rotationThreshold: Measure<Angle>) {
            gradient.setX1Percent(50 * (1 - delta))
            gradient.setX2Percent(50 * (1 + delta))

            gradient.setY2Percent(if (rotation > rotationThreshold) 100.0 else   0.0)
            gradient.setY1Percent(if (rotation > rotationThreshold)   0.0 else 100.0)
        }
    }

    companion object {
        private var id     = 0
        private val svgTag = "svg"

//        private var sClipId = 0.0
    }
}
