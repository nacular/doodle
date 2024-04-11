@file:Suppress("EXPECTED_EXTERNAL_DECLARATION", "WRONG_MODIFIER_TARGET")

package io.nacular.doodle.dom

internal expect external interface SVGBoundingBoxOptions: JsAny {
    var fill   : Boolean?
    var stroke : Boolean?
    var markers: Boolean?
    var clipped: Boolean?
}

internal expect abstract external class SVGElement(): Element, ElementCSSInlineStyle {
    fun getBBox(options: SVGBoundingBoxOptions): DOMRect
}
internal expect abstract external class SVGRectElement          : SVGGeometryElement
internal expect abstract external class SVGPathElement          : SVGGeometryElement
internal expect abstract external class SVGTextElement          : SVGTextPositioningElement
internal expect abstract external class SVGTSpanElement         : SVGTextPositioningElement
internal expect abstract external class SVGCircleElement        : SVGGeometryElement
internal expect abstract external class SVGEllipseElement       : SVGGeometryElement
internal expect abstract external class SVGPolygonElement       : SVGGeometryElement
internal expect abstract external class SVGPatternElement       : SVGElement
internal expect abstract external class SVGGradientElement      : SVGElement
internal expect abstract external class SVGLinearGradientElement: SVGGradientElement
internal expect abstract external class SVGRadialGradientElement: SVGGradientElement

internal class BoundingBoxOptions(
    var fill   : Boolean? = true,
    var stroke : Boolean? = false,
    var markers: Boolean? = false,
    var clipped: Boolean? = false
)

internal expect abstract external class SVGGraphicsElement: SVGElement

internal expect fun SVGGraphicsElement.getBBox(options: BoundingBoxOptions): DOMRect

internal expect abstract external class SVGGeometryElement: SVGGraphicsElement {
    fun getTotalLength(): Float
}

internal expect abstract external class SVGTextContentElement: SVGGraphicsElement

internal expect abstract external class SVGTextPositioningElement: SVGTextContentElement