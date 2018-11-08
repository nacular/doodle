package com.nectar.doodle.dom

import com.nectar.doodle.dom.ShapeRendering.Auto
import com.nectar.doodle.dom.ShapeRendering.CrispEdges
import com.nectar.doodle.drawing.AffineTransform
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.Renderer
import org.w3c.dom.svg.SVGCircleElement
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.svg.SVGEllipseElement
import org.w3c.dom.svg.SVGRectElement

inline val SVGElement.parent get() = parentNode as SVGElement?


inline fun SVGRectElement.setX      (value: Double) = setAttribute( "x",     "$value")
inline fun SVGRectElement.setY      (value: Double) = setAttribute( "y",     "$value")
inline fun SVGRectElement.setRX     (value: Double) = setAttribute( "rx",    "$value")
inline fun SVGRectElement.setRY     (value: Double) = setAttribute( "ry",    "$value")
inline fun SVGRectElement.setWidth  (value: Double) = setAttribute("width",  "$value")
inline fun SVGRectElement.setHeight (value: Double) = setAttribute("height", "$value")


//inline fun SVGElement.setX1         (value : Double      ) = setAttribute  ("x1",               "$value"       )
//inline fun SVGElement.setX2         (value : Double      ) = setAttribute  ("x2",               "$value"       )
//inline fun SVGElement.setY1         (value : Double      ) = setAttribute  ("y1",               "$value"       )
//inline fun SVGElement.setY2         (value : Double      ) = setAttribute  ("y2",               "$value"       )
//inline fun SVGElement.setX1Percent  (value : Double      ) = setAttribute  ("x1",               "$value%"      )
//inline fun SVGElement.setX2Percent  (value : Double      ) = setAttribute  ("x2",               "$value%"      )
//inline fun SVGElement.setY1Percent  (value : Double      ) = setAttribute  ("y1",               "$value%"      )
//inline fun SVGElement.setY2Percent  (value : Double      ) = setAttribute  ("y2",               "$value%"      )
inline fun SVGEllipseElement.setRX  (value : Double      ) = setAttribute  ("rx",               "$value"       )
inline fun SVGEllipseElement.setRY  (value : Double      ) = setAttribute  ("ry",               "$value"       )
inline fun SVGEllipseElement.setCX  (value : Double      ) = setAttribute  ("cx",               "$value"       )
inline fun SVGEllipseElement.setCY  (value : Double      ) = setAttribute  ("cy",               "$value"       )
inline fun SVGCircleElement.setCX   (value : Double      ) = setAttribute  ("cx",               "$value"       )
inline fun SVGCircleElement.setCY   (value : Double      ) = setAttribute  ("cy",               "$value"       )
inline fun SVGCircleElement.setR    (value : Double      ) = setAttribute  ("r",                "$value"       )
inline fun SVGElement.setPathData   (value : String      ) = setAttribute  ("d",                  value        )
inline fun SVGElement.setOpacity    (value : kotlin.Float) = setAttribute  ("opacity",          "$value"       )
inline fun SVGElement.setStrokeWidth(value : Double      ) = setAttribute  ("stroke-width",     "$value"       )
inline fun SVGElement.setStrokeDash (value : String      ) = setAttribute  ("stroke-dasharray",   value        )
//inline fun SVGElement.setClipPath   (clipId: String      ) = setAttribute  ("clip-path",        "url(#$clipId)")
//inline fun SVGElement.setXLinkHref  (value : String      ) = setAttributeNS( "http://www.w3.org/1999/xlink", "xlink:href", value )


var SVGElement.shapeRendering
    get() = when(getAttribute("shape-rendering")) {
        CrispEdges.value -> CrispEdges
        else             -> Auto
    }
    set(value) = setAttribute("shape-rendering", value.value)


fun convert(color: Color?, block: (String) -> Unit) = block(when (color) {
    null -> none
    else -> "#${color.hexString}"
})

inline fun SVGElement.setFill(color: Color?) = convert(color) {
    setAttribute("fill", it)
}

inline fun SVGElement.setFillRule(fillRule: Renderer.FillRule?) {
    setAttribute("fill-rule", when (fillRule) {
        Renderer.FillRule.EvenOdd -> "evenodd"
        Renderer.FillRule.NonZero -> "nonzero"
        else                      -> ""
    })
}

fun SVGElement.setFillPattern(pattern: SVGElement?) = setAttribute("fill", when (pattern) {
    null -> none
    else -> "url(#${pattern.id})"
})

inline fun SVGElement.setStroke(color: Color?) = convert(color) {
    setAttribute("stroke", it)
}

fun SVGElement.setTransform(transform: AffineTransform?) = when(transform) {
    null -> removeTransform()
    else -> setTransform(transform.run { "matrix($scaleX,$shearY,$shearX,$scaleY,$translateX,$translateY)" })
}

inline fun SVGElement.setTransform(transform: String) = setAttribute("transform", transform)

inline fun SVGElement.removeTransform() = removeAttribute("transform")

enum class ShapeRendering(val value: String) {
    CrispEdges("crispEdges"),
    Auto      ("auto"      )
}

private val none = "none"