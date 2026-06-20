package io.nacular.doodle.dom

import io.nacular.doodle.drawing.AffineTransform
import io.nacular.doodle.drawing.AffineTransform2D
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Renderer
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.drawing.Stroke.LineCap
import io.nacular.doodle.drawing.Stroke.LineJoint
import io.nacular.doodle.drawing.Stroke.LineJoint.Bevel
import io.nacular.doodle.drawing.Stroke.LineJoint.Miter
import io.nacular.doodle.drawing.Stroke.LineJoint.Round
import io.nacular.doodle.geometry.Circle
import io.nacular.doodle.geometry.Ellipse
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import kotlin.math.max
import kotlin.math.min

internal fun HTMLElement.childAt(index: Int): Node? = when {
    index >= 0 && index < children.length -> children[index]
    else                                  -> null
}

internal fun Node.childAt(index: Int): Node? = when {
    index >= 0 && index < childNodes.length -> childNodes[index]
    else                                    -> null
}

internal inline val Node.parent get() = parentNode

internal inline val Node.numChildren get() = childNodes.length

internal fun Node.index(of: Node) = (0 until childNodes.length).firstOrNull { childNodes[it] == of } ?: -1

internal inline fun Node.add(child: Node) = appendChild(child)

internal fun Node.addIfNotPresent(child: Node, at: Int) {
    if (child !== childNodes[at]) {
        insert(child, at)
    }
}

internal inline fun Node.insert(element: Node, index: Int) = insertBefore(element, childAt(index))

// FIXME: Reinstate once WASM exception handling works
//internal inline fun Node.remove(element: Node) = removeChild(element)

internal expect fun Node.remove(element: Node): Node?

internal inline val HTMLElement.top    get() = offsetTop.toDouble   ()
internal inline val HTMLElement.left   get() = offsetLeft.toDouble  ()
internal inline val HTMLElement.width  get() = offsetWidth.toDouble ()
internal inline val HTMLElement.height get() = offsetHeight.toDouble()

internal inline val HTMLElement.hasScrollOverflow get() = style.run { overflowX.isNotEmpty() || overflowY.isNotEmpty()  }

internal fun HTMLElement.scrollTo(point: Point) {
    try {
        scrollTo(point.x, point.y)
    } catch (_: Throwable) {
        // https://bugzilla.mozilla.org/show_bug.cgi?id=1671283
        scrollTop  = point.y
        scrollLeft = point.x
    }
}

internal inline fun HTMLElement.insert(element: Node, index: Int) = insertBefore(element, childAt(index))

internal inline val SVGElement.parent get() = parentNode

internal inline fun SVGRectElement.setRX    (value: Double) = setAttributeIfDifferent("rx", "$value")
internal inline fun SVGRectElement.setRY    (value: Double) = setAttributeIfDifferent("ry", "$value")
internal inline fun SVGRectElement.setRadius(value: Double) { setRX(value); setRY(value) }

internal inline fun SVGElement.setId      (value: String   ) { setAttributeNS(null, "id", value ); }
internal inline fun SVGElement.setX       (value: Double   ) = setAttributeIfDifferent("x",      "$value")
internal inline fun SVGElement.setY       (value: Double   ) = setAttributeIfDifferent("y",      "$value")
internal inline fun SVGElement.setSize    (value: Size     ) { setWidth(value.width); setHeight(value.height) }
internal inline fun SVGElement.setWidth   (value: Double   ) = setAttributeIfDifferent("width",  "$value")
internal inline fun SVGElement.setHeight  (value: Double   ) = setAttributeIfDifferent("height", "$value")
internal inline fun SVGElement.setPosition(value: Point    ) { setX(value.x); setY(value.y) }
internal inline fun SVGElement.setBounds  (value: Rectangle) { setPosition(value.position); setSize(value.size) }

internal inline fun SVGLinearGradientElement.setX1(value: Double) = setAttributeIfDifferent("x1", "$value")
internal inline fun SVGLinearGradientElement.setX2(value: Double) = setAttributeIfDifferent("x2", "$value")
internal inline fun SVGLinearGradientElement.setY1(value: Double) = setAttributeIfDifferent("y1", "$value")
internal inline fun SVGLinearGradientElement.setY2(value: Double) = setAttributeIfDifferent("y2", "$value")

internal inline fun SVGRadialGradientElement.setStart(value: Circle) {
    setAttributeIfDifferent("fx", "${value.center.x}")
    setAttributeIfDifferent("fy", "${value.center.y}")
    setAttributeIfDifferent("fr", "${value.radius  }")
}
internal inline fun SVGRadialGradientElement.setEnd(value: Circle) {
    setAttributeIfDifferent("cx", "${value.center.x}")
    setAttributeIfDifferent("cy", "${value.center.y}")
    setAttributeIfDifferent("r",  "${value.radius  }")
}

internal inline fun SVGEllipseElement.setRX     (value: Double ) = setAttributeIfDifferent("rx", "$value")
internal inline fun SVGEllipseElement.setRY     (value: Double ) = setAttributeIfDifferent("ry", "$value")
internal inline fun SVGEllipseElement.setCX     (value: Double ) = setAttributeIfDifferent("cx", "$value")
internal inline fun SVGEllipseElement.setCY     (value: Double ) = setAttributeIfDifferent("cy", "$value")
internal inline fun SVGEllipseElement.setEllipse(value: Ellipse) {
    setCX(value.center.x)
    setCY(value.center.y)
    setRX(value.xRadius )
    setRY(value.yRadius )
}

internal inline fun SVGCircleElement.setCX    (value: Double) = setAttributeIfDifferent  ("cx", "$value")
internal inline fun SVGCircleElement.setCY    (value: Double) = setAttributeIfDifferent  ("cy", "$value")
internal inline fun SVGCircleElement.setR     (value: Double) = setAttributeIfDifferent  ("r",  "$value")
internal inline fun SVGCircleElement.setCircle(value: Circle) { setCX(value.center.x); setCY(value.center.y); setR(value.radius) }

internal inline fun SVGElement.setPathData               (value: String      ) = setAttributeIfDifferent   ("d",                   value          )
internal inline fun SVGElement.setStrokeWidth            (value: Double      ) = setAttributeIfDifferent   ("stroke-width",      "$value"         )
internal inline fun SVGElement.setDefaultStrokeWidth     (                   ) = removeAttribute("stroke-width"                        )
internal inline fun SVGElement.setStrokeDash             (value: DoubleArray?) = setAttributeIfDifferent   ("stroke-dasharray",   dashArray(value))
internal inline fun SVGElement.setDefaultStrokeDash      (                   ) = removeAttribute("stroke-dasharray"                    )
internal inline fun SVGElement.setStrokeDashOffset       (value: Double?     ) = setAttributeIfDifferent   ("stroke-dashoffset",  value?.let { "$it" } ?: "")
internal inline fun SVGElement.setDefaultStrokeDashOffset(                   ) = removeAttribute("stroke-dashoffset"                    )

internal inline fun SVGElement.setStrokeJoint(value: LineJoint) = setAttributeIfDifferent("stroke-linejoin", when (value) {
    Miter -> "butt"
    Round -> "round"
    Bevel -> "bevel"
})

internal inline fun SVGElement.setDefaultStrokeJoint() = removeAttribute("stroke-linejoin")

internal inline fun SVGElement.setStrokeLineCap(value: LineCap) = setAttributeIfDifferent("stroke-linecap", when (value) {
    LineCap.Butt   -> "butt"
    LineCap.Round  -> "round"
    LineCap.Square -> "square"
})

internal inline fun SVGElement.setDefaultStrokeLineCap() = removeAttribute("stroke-linecap")

internal inline fun SVGGeometryElement.setPoints(vararg points: Point) = setAttributeIfDifferent("points", points.joinToString(" ") { "${it.x},${it.y}" })

internal fun SVGElement.setStopColor(color: Color) {
    setStopColor("#${color.hexString}")

    if (color.opacity != 1f) {
        setStopOpacity(color.opacity)
    }
}

internal fun SVGElement.setStopOffset(value: Float) {
    setStopOffsetInternal(min(1f, max(0f, value)))
}

internal fun SVGGradientElement.setGradientUnits(value: String) { setAttributeIfDifferent("gradientUnits", value) }

private fun SVGElement.setStopColor         (value: String) { setAttributeIfDifferent("stop-color",     value ) }
private fun SVGElement.setStopOpacity       (value: Float ) { setAttributeIfDifferent("stop-opacity", "$value") }
private fun SVGElement.setStopOffsetInternal(value: Float ) { setAttributeIfDifferent("offset",       "$value") }

internal fun convert(color: Color?, block: (String) -> Unit) = block(when (color) {
    null -> none
    else -> "#${color.hexString}"
})

internal fun SVGElement.setFill(color: Color?) = convert(color) {
    setAttributeIfDifferent("fill", it)
    color?.let { c -> setAttributeIfDifferent("fill-opacity", "${c.opacity}") }
}

internal inline fun SVGElement.setDefaultFill() {
    setFill(Black)
}

internal fun SVGElement.setFillRule(fillRule: Renderer.FillRule?) {
    setAttributeIfDifferent("fill-rule", when (fillRule) {
        Renderer.FillRule.EvenOdd -> "evenodd"
        Renderer.FillRule.NonZero -> "nonzero"
        else                      -> ""
    })
}

internal fun SVGElement.setFloodColor(color: Color?) = convert(color) {
    setAttributeIfDifferent("flood-color",  it)
}

internal fun SVGElement.setFillPattern(id: String?, opacity: Float = 1f) {
    setAttributeIfDifferent("fill", when (id) {
        null -> none
        else -> "url(#$id)"
    })

    if (opacity != 1f) {
        setAttributeIfDifferent("fill-opacity", "$opacity")
    }
}

internal fun SVGElement.setStroke(stroke: Stroke?) {
    when (stroke) {
        null -> {
            setStrokeColor            (null)
            setDefaultStrokeDash      ()
            setDefaultStrokeWidth     ()
            setDefaultStrokeJoint     ()
            setDefaultStrokeLineCap   ()
            setDefaultStrokeDashOffset()
        }
        else -> {
            setStrokeWidth  (stroke.thickness)
            stroke.lineJoint?.let { setStrokeJoint  (it) }
            stroke.lineCap?.let   { setStrokeLineCap(it) }

            when (stroke.dashes) {
                null -> {
                    setDefaultStrokeDash()
                    setDefaultStrokeDashOffset()
                }
                else -> {
                    setStrokeDash      (stroke.dashes)
                    setStrokeDashOffset(stroke.dashOffset)
                }
            }
        }
    }
}

internal fun SVGElement.setStrokePattern(id: String?, opacity: Float = 1f) {
    setAttributeIfDifferent("stroke", when (id) {
        null -> none
        else -> "url(#$id)"
    })

    setAttributeIfDifferent("stroke-opacity", "$opacity")
}

private fun dashArray(dashes: DoubleArray?) = dashes?.map { max(0.0, it) }?.joinToString(",") ?: ""

internal fun SVGElement.setStrokeColor(color: Color?) = convert(color) {
    setAttributeIfDifferent("stroke", it)
    when (color) {
        null -> removeAttribute("stroke-opacity"                    )
        else -> setAttributeIfDifferent   ("stroke-opacity", "${color.opacity}")
    }
}

internal fun SVGPatternElement.setPatternTransform(transform: AffineTransform2D?) = when {
    transform == null || transform.isIdentity -> removeAttribute("patternTransform")
    else                                      -> setAttributeIfDifferent   ("patternTransform", transform.matrixString)
}

private val AffineTransform.matrixString get() = run {
    "matrix($scaleX,$shearY,$shearX,$scaleY,$translateX,$translateY)"
}

internal inline fun SVGElement.setTransform(transform: AffineTransform?) = when {
    transform == null || transform.isIdentity -> removeTransform()
    transform.is3d                            -> { removeAttribute("transform"); style.setTransform(transform) }
    else                                      -> { setTransform(transform.matrixString); style.setTransform(null) }
}

internal inline fun SVGElement.setTransform(transform: String) = setAttributeIfDifferent("transform", transform)

internal inline fun SVGElement.removeTransform() { removeAttribute("transform"); style.setTransform(null) }

internal enum class DominantBaseline(val value: String) {
    TextBeforeEdge("text-before-edge")
}

private const val none = "none"

private fun Element.setAttributeIfDifferent(name: String, value: String) {
    if (value != getAttribute(name)) { setAttribute(name, value) }
}