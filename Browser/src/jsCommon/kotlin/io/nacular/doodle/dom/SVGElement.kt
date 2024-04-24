package io.nacular.doodle.dom

internal actual abstract external class SVGElement actual constructor(): Element, ElementCSSInlineStyle {
    actual fun getBBox(options: SVGBoundingBoxOptions): DOMRect
}
internal actual abstract external class SVGRectElement          : SVGGeometryElement
internal actual abstract external class SVGPathElement          : SVGGeometryElement
internal actual abstract external class SVGTextElement          : SVGTextPositioningElement
internal actual abstract external class SVGTSpanElement         : SVGTextPositioningElement
internal actual abstract external class SVGCircleElement        : SVGGeometryElement
internal actual abstract external class SVGEllipseElement       : SVGGeometryElement
internal actual abstract external class SVGPolygonElement       : SVGGeometryElement
internal actual abstract external class SVGPatternElement       : SVGElement
internal actual abstract external class SVGGradientElement      : SVGElement
internal actual abstract external class SVGLinearGradientElement: SVGGradientElement
internal actual abstract external class SVGRadialGradientElement: SVGGradientElement

internal actual abstract external class SVGGraphicsElement: SVGElement

internal actual abstract external class SVGGeometryElement: SVGGraphicsElement {
    actual fun getTotalLength(): Float
}

internal actual abstract external class SVGTextContentElement    : SVGGraphicsElement
internal actual abstract external class SVGTextPositioningElement: SVGTextContentElement

internal actual external interface SVGBoundingBoxOptions: JsAny {
    actual var fill   : Boolean?
    actual var stroke : Boolean?
    actual var markers: Boolean?
    actual var clipped: Boolean?
}