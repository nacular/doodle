package io.nacular.doodle.dom

internal actual abstract class SVGElement actual constructor(): Element(), ElementCSSInlineStyle {
    actual fun getBBox(options: SVGBoundingBoxOptions): DOMRect = DOMRect()
}
internal actual abstract class SVGPathElement          : SVGGeometryElement()
internal actual abstract class SVGRectElement          : SVGGeometryElement()
internal actual abstract class SVGCircleElement        : SVGGeometryElement()
internal actual abstract class SVGEllipseElement       : SVGGeometryElement()
internal actual abstract class SVGPolygonElement       : SVGGeometryElement()
internal actual abstract class SVGPatternElement       : SVGElement()
internal actual abstract class SVGGradientElement      : SVGElement()
internal actual abstract class SVGGraphicsElement      : SVGElement()
internal actual abstract class SVGLinearGradientElement: SVGGradientElement()
internal actual abstract class SVGRadialGradientElement: SVGGradientElement()

internal actual fun SVGElement.getBBox_(options: BoundingBoxOptions): DOMRect = getBBox(object: SVGBoundingBoxOptions {
    override var fill    = options.fill
    override var stroke  = options.stroke
    override var markers = options.markers
    override var clipped = options.clipped
})

internal actual abstract class SVGGeometryElement: SVGGraphicsElement() {
    actual fun getTotalLength(): Float = 0f
}
internal actual abstract class SVGTextElement           : SVGTextPositioningElement()
internal actual abstract class SVGTSpanElement          : SVGTextPositioningElement()
internal actual abstract class SVGTextContentElement    : SVGGraphicsElement()
internal actual abstract class SVGTextPositioningElement: SVGTextContentElement()