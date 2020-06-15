package io.nacular.doodle.dom

import io.nacular.doodle.SVGCircleElement
import io.nacular.doodle.SVGElement
import io.nacular.doodle.SVGEllipseElement
import io.nacular.doodle.SVGGeometryElement
import io.nacular.doodle.SVGGradientElement
import io.nacular.doodle.SVGPatternElement
import io.nacular.doodle.SVGRectElement
import io.nacular.doodle.drawing.AffineTransform
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Renderer
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

inline val SVGElement.parent get() = parentNode


inline fun SVGRectElement.setRX  (value: Double) = setAttribute("rx",     "$value")
inline fun SVGRectElement.setRY  (value: Double) = setAttribute("ry",     "$value")

inline fun SVGElement.setId      (value: String   ) { setAttributeNS(null, "id", value ); }
inline fun SVGElement.setX       (value: Double   ) = setAttribute("x",      "$value")
inline fun SVGElement.setY       (value: Double   ) = setAttribute("y",      "$value")
inline fun SVGElement.setSize    (value: Size     ) { setWidth(value.width); setHeight(value.height) }
inline fun SVGElement.setWidth   (value: Double   ) = setAttribute("width",  "$value")
inline fun SVGElement.setHeight  (value: Double   ) = setAttribute("height", "$value")
inline fun SVGElement.setPosition(value: Point    ) { setX(value.x); setY(value.y) }
inline fun SVGElement.setBounds  (value: Rectangle) { setPosition(value.position); setSize(value.size) }

inline fun SVGGradientElement.setX1(value: Double) = setAttribute("x1", "$value")
inline fun SVGGradientElement.setX2(value: Double) = setAttribute("x2", "$value")
inline fun SVGGradientElement.setY1(value: Double) = setAttribute("y1", "$value")
inline fun SVGGradientElement.setY2(value: Double) = setAttribute("y2", "$value")

inline fun SVGEllipseElement.setRX     (value: Double ) = setAttribute("rx", "$value")
inline fun SVGEllipseElement.setRY     (value: Double ) = setAttribute("ry", "$value")
inline fun SVGEllipseElement.setCX     (value: Double ) = setAttribute("cx", "$value")
inline fun SVGEllipseElement.setCY     (value: Double ) = setAttribute("cy", "$value")
inline fun SVGEllipseElement.setEllipse(value: Ellipse) {
    setCX(value.center.x)
    setCY(value.center.y)
    setRX(value.xRadius)
    setRY(value.yRadius)
}

inline fun SVGCircleElement.setCX    (value: Double) = setAttribute  ("cx", "$value")
inline fun SVGCircleElement.setCY    (value: Double) = setAttribute  ("cy", "$value")
inline fun SVGCircleElement.setR     (value: Double) = setAttribute  ("r",  "$value")
inline fun SVGCircleElement.setCircle(value: Circle) { setCX(value.center.x); setCY(value.center.y); setR(value.radius) }

inline fun SVGElement.setPathData   (value: String      ) = setAttribute  ("d",                  value        )
inline fun SVGElement.setStrokeWidth(value: Double      ) = setAttribute  ("stroke-width",     "$value"       )
inline fun SVGElement.setStrokeDash (value: String      ) = setAttribute  ("stroke-dasharray",   value        )
//inline fun SVGElement.setClipPath   (clipId: String      ) = setAttribute  ("clip-path",        "url(#$clipId)")
//inline fun SVGElement.setXLinkHref  (value : String      ) = setAttributeNS( "http://www.w3.org/1999/xlink", "xlink:href", value )

inline fun SVGGeometryElement.setPoints(vararg points: Point) = setAttribute("points", points.joinToString(" ") { "${it.x},${it.y}" })

fun SVGElement.setStopColor(color: Color) {
    setStopColor("#${color.hexString}")

    if (color.opacity != 1f) {
        setStopOpacity(color.opacity)
    }
}

fun SVGElement.setStopOffset(value: kotlin.Float) {
    setStopOffsetInternal(min(1f, max(0f, value)))
}

fun SVGElement.setGradientRotation(value: Measure<Angle>) { setAttribute("gradientTransform", "rotate(${value `in` degrees})") }

fun SVGElement.setSpreadMethod (value: String) { setAttribute("spreadMethod",  value) }
fun SVGElement.setGradientUnits(value: String) { setAttribute("gradientUnits", value) }


private fun SVGElement.setStopColor         (value: String      ) { setAttribute("stop-color",     value ) }
private fun SVGElement.setStopOpacity       (value: kotlin.Float) { setAttribute("stop-opacity", "$value") }
private fun SVGElement.setStopOffsetInternal(value: kotlin.Float) { setAttribute("offset",       "$value") }

//var SVGElement.shapeRendering
//    get() = when(getAttribute("shape-rendering")) {
//        CrispEdges.value -> CrispEdges
//        else             -> Auto
//    }
//    set(value) = setAttribute("shape-rendering", value.value)


fun convert(color: Color?, block: (String) -> Unit) = block(when (color) {
    null -> none
    else -> "#${color.hexString}"
})

inline fun SVGElement.setDominantBaseline(value: DominantBaseline) {
    setAttribute("dominant-baseline", value.value)
}

fun SVGElement.setFill(color: Color?) = convert(color) {
    setAttribute("fill", it)
    color?.let { setAttribute("fill-opacity", "${it.opacity}") }
}

inline fun SVGElement.setDefaultFill() {
    removeAttribute("fill")
}

fun SVGElement.setFillRule(fillRule: Renderer.FillRule?) {
    setAttribute("fill-rule", when (fillRule) {
        Renderer.FillRule.EvenOdd -> "evenodd"
        Renderer.FillRule.NonZero -> "nonzero"
        else                      -> ""
    })
}

fun SVGElement.setFloodColor(color: Color?) = convert(color) {
    setAttribute("flood-color",  it)
}

fun SVGElement.setFillPattern(pattern: SVGElement?) = setAttribute("fill", when (pattern) {
    null -> none
    else -> "url(#${pattern.id})"
})

fun SVGElement.setStroke(color: Color?) = convert(color) {
    setAttribute("stroke", it)
    color?.let { setAttribute("stroke-opacity", "${it.opacity}") }
}

fun SVGPatternElement.setPatternTransform(transform: AffineTransform?) = when(transform) {
    null -> removeAttribute("patternTransform")
    else -> setAttribute("patternTransform", transform.matrixString)
}

private val AffineTransform.matrixString get() = run { "matrix($scaleX,$shearY,$shearX,$scaleY,$translateX,$translateY)" }

fun SVGElement.setTransform(transform: AffineTransform?) = when(transform) {
    null -> removeTransform()
    else -> setTransform(transform.matrixString)
}

inline fun SVGElement.setTransform(transform: String) = setAttribute("transform", transform)

inline fun SVGElement.removeTransform() = removeAttribute("transform")

enum class DominantBaseline(val value: String) {
    TextBeforeEdge("text-before-edge")
}

enum class ShapeRendering(val value: String) {
    CrispEdges("crispEdges"),
    Auto      ("auto"      )
}

private const val none = "none"