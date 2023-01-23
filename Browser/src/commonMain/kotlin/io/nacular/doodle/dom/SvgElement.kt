@file:Suppress("NOTHING_TO_INLINE")

package io.nacular.doodle.dom

import io.nacular.doodle.SVGCircleElement
import io.nacular.doodle.SVGElement
import io.nacular.doodle.SVGEllipseElement
import io.nacular.doodle.SVGGeometryElement
import io.nacular.doodle.SVGGradientElement
import io.nacular.doodle.SVGLinearGradientElement
import io.nacular.doodle.SVGPatternElement
import io.nacular.doodle.SVGRadialGradientElement
import io.nacular.doodle.SVGRectElement
import io.nacular.doodle.drawing.AffineTransform
import io.nacular.doodle.drawing.AffineTransform2D
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Renderer
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.drawing.Stroke.LineCap
import io.nacular.doodle.drawing.Stroke.LineCap.Butt
import io.nacular.doodle.drawing.Stroke.LineCap.Round
import io.nacular.doodle.drawing.Stroke.LineCap.Square
import io.nacular.doodle.drawing.Stroke.LineJoint
import io.nacular.doodle.geometry.Circle
import io.nacular.doodle.geometry.Ellipse
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.measured.units.Angle
import io.nacular.measured.units.Angle.Companion.degrees
import io.nacular.measured.units.Measure
import kotlin.math.max
import kotlin.math.min

internal inline val SVGElement.parent get() = parentNode

internal inline fun SVGRectElement.setRX    (value: Double) = setAttribute("rx", "$value")
internal inline fun SVGRectElement.setRY    (value: Double) = setAttribute("ry", "$value")
internal inline fun SVGRectElement.setRadius(value: Double) { setRX(value); setRY(value) }

internal inline fun SVGElement.setId      (value: String   ) { setAttributeNS(null, "id", value ); }
internal inline fun SVGElement.setX       (value: Double   ) = setAttribute("x",      "$value")
internal inline fun SVGElement.setY       (value: Double   ) = setAttribute("y",      "$value")
internal inline fun SVGElement.setSize    (value: Size     ) { setWidth(value.width); setHeight(value.height) }
internal inline fun SVGElement.setWidth   (value: Double   ) = setAttribute("width",  "$value")
internal inline fun SVGElement.setHeight  (value: Double   ) = setAttribute("height", "$value")
internal inline fun SVGElement.setPosition(value: Point    ) { setX(value.x); setY(value.y) }
internal inline fun SVGElement.setBounds  (value: Rectangle) { setPosition(value.position); setSize(value.size) }

internal inline fun SVGLinearGradientElement.setX1(value: Double) = setAttribute("x1", "$value")
internal inline fun SVGLinearGradientElement.setX2(value: Double) = setAttribute("x2", "$value")
internal inline fun SVGLinearGradientElement.setY1(value: Double) = setAttribute("y1", "$value")
internal inline fun SVGLinearGradientElement.setY2(value: Double) = setAttribute("y2", "$value")

internal inline fun SVGRadialGradientElement.setStart(value: Circle) {
    setAttribute("fx", "${value.center.x}")
    setAttribute("fy", "${value.center.y}")
    setAttribute("fr", "${value.radius  }")
}
internal inline fun SVGRadialGradientElement.setEnd(value: Circle) {
    setAttribute("cx", "${value.center.x}")
    setAttribute("cy", "${value.center.y}")
    setAttribute("r",  "${value.radius  }")
}

internal inline fun SVGEllipseElement.setRX     (value: Double ) = setAttribute("rx", "$value")
internal inline fun SVGEllipseElement.setRY     (value: Double ) = setAttribute("ry", "$value")
internal inline fun SVGEllipseElement.setCX     (value: Double ) = setAttribute("cx", "$value")
internal inline fun SVGEllipseElement.setCY     (value: Double ) = setAttribute("cy", "$value")
internal inline fun SVGEllipseElement.setEllipse(value: Ellipse) {
    setCX(value.center.x)
    setCY(value.center.y)
    setRX(value.xRadius)
    setRY(value.yRadius)
}

internal inline fun SVGCircleElement.setCX    (value: Double) = setAttribute  ("cx", "$value")
internal inline fun SVGCircleElement.setCY    (value: Double) = setAttribute  ("cy", "$value")
internal inline fun SVGCircleElement.setR     (value: Double) = setAttribute  ("r",  "$value")
internal inline fun SVGCircleElement.setCircle(value: Circle) { setCX(value.center.x); setCY(value.center.y); setR(value.radius) }

internal inline fun SVGElement.setPathData               (value: String      ) = setAttribute   ("d",                   value          )
internal inline fun SVGElement.setStrokeWidth            (value: Double      ) = setAttribute   ("stroke-width",      "$value"         )
internal inline fun SVGElement.setDefaultStrokeWidth     (                   ) = removeAttribute("stroke-width"                        )
internal inline fun SVGElement.setStrokeDash             (value: DoubleArray?) = setAttribute   ("stroke-dasharray",   dashArray(value))
internal inline fun SVGElement.setDefaultStrokeDash      (                   ) = removeAttribute("stroke-dasharray"                    )
internal inline fun SVGElement.setStrokeDashOffset       (value: Double?     ) = setAttribute   ("stroke-dashoffset",  value?.let { "$it" } ?: "")
internal inline fun SVGElement.setDefaultStrokeDashOffset(                   ) = removeAttribute("stroke-dashoffset"                    )

internal inline fun SVGElement.setStrokeJoint(value: LineJoint) = setAttribute("stroke-linejoin", when (value) {
    LineJoint.Miter -> "butt"
    LineJoint.Round -> "round"
    LineJoint.Bevel -> "bevel"
})

internal inline fun SVGElement.setDefaultStrokeJoint() = removeAttribute("stroke-linejoin")

internal inline fun SVGElement.setStrokeLineCap(value: LineCap) = setAttribute("stroke-linecap", when (value) {
    Butt   -> "butt"
    Round  -> "round"
    Square -> "square"
})

internal inline fun SVGElement.setDefaultStrokeLineCap() = removeAttribute("stroke-linecap")

internal inline fun SVGGeometryElement.setPoints(vararg points: Point) = setAttribute("points", points.joinToString(" ") { "${it.x},${it.y}" })

internal fun SVGElement.setStopColor(color: Color) {
    setStopColor("#${color.hexString}")

    if (color.opacity != 1f) {
        setStopOpacity(color.opacity)
    }
}

internal fun SVGElement.setStopOffset(value: Float) {
    setStopOffsetInternal(min(1f, max(0f, value)))
}

internal fun SVGGradientElement.setGradientRotation(value: Measure<Angle>) { setAttribute("gradientTransform", "rotate(${value `in` degrees})") }

internal fun SVGGradientElement.setSpreadMethod (value: String) { setAttribute("spreadMethod",  value) }
internal fun SVGGradientElement.setGradientUnits(value: String) { setAttribute("gradientUnits", value) }

private fun SVGElement.setStopColor         (value: String) { setAttribute("stop-color",     value ) }
private fun SVGElement.setStopOpacity       (value: Float ) { setAttribute("stop-opacity", "$value") }
private fun SVGElement.setStopOffsetInternal(value: Float ) { setAttribute("offset",       "$value") }

internal fun convert(color: Color?, block: (String) -> Unit) = block(when (color) {
    null -> none
    else -> "#${color.hexString}"
})

internal inline fun SVGElement.setDominantBaseline(value: DominantBaseline) {
    setAttribute("dominant-baseline", value.value)
}

internal fun SVGElement.setFill(color: Color?) = convert(color) {
    setAttribute("fill", it)
    color?.let { c -> setAttribute("fill-opacity", "${c.opacity}") }
}

internal inline fun SVGElement.setDefaultFill() {
    removeAttribute("fill")
}

internal fun SVGElement.setFillRule(fillRule: Renderer.FillRule?) {
    setAttribute("fill-rule", when (fillRule) {
        Renderer.FillRule.EvenOdd -> "evenodd"
        Renderer.FillRule.NonZero -> "nonzero"
        else                      -> ""
    })
}

internal fun SVGElement.setFloodColor(color: Color?) = convert(color) {
    setAttribute("flood-color",  it)
}

internal fun SVGElement.setFillPattern(pattern: SVGElement?) = setAttribute("fill", when (pattern) {
    null -> none
    else -> "url(#${pattern.id})"
})

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

internal fun SVGElement.setStrokePattern(pattern: SVGElement?) = setAttribute("stroke", when (pattern) {
    null -> none
    else -> "url(#${pattern.id})"
})

private fun dashArray(dashes: DoubleArray?) = dashes?.map { max(0.0, it) }?.joinToString(",") ?: ""

internal fun SVGElement.setStrokeColor(color: Color?) = convert(color) {
    setAttribute("stroke", it)
    when (color) {
        null -> removeAttribute("stroke-opacity")
        else -> setAttribute("stroke-opacity", "${color.opacity}")
    }
}

internal fun SVGPatternElement.setPatternTransform(transform: AffineTransform2D?) = when {
    transform == null || transform.isIdentity -> removeAttribute("patternTransform")
    else                                      -> setAttribute   ("patternTransform", transform.matrixString)
}

private val AffineTransform.matrixString get() = run {
    "matrix($scaleX,$shearY,$shearX,$scaleY,$translateX,$translateY)"
}

internal inline fun SVGElement.setTransform(transform: AffineTransform?) = when {
    transform == null || transform.isIdentity -> removeTransform()
    transform.is3d                            -> { removeAttribute("transform"); style.setTransform(transform) }
    else                                      -> { setTransform(transform.matrixString); style.setTransform(null) }
}

internal inline fun SVGElement.setTransform(transform: String) = setAttribute("transform", transform)

internal inline fun SVGElement.removeTransform() { removeAttribute("transform"); style.setTransform(null) }

internal enum class DominantBaseline(val value: String) {
    TextBeforeEdge("text-before-edge")
}

private const val none = "none"