package io.nacular.doodle

/**
 * Created by Nicholas Eddy on 8/9/19.
 */
expect interface SVGBoundingBoxOptions

expect abstract class SVGElement()      : Element, ElementCSSInlineStyle
expect abstract class SVGRectElement    : SVGGeometryElement
expect abstract class SVGPathElement    : SVGGeometryElement
expect abstract class SVGTextElement    : SVGTextPositioningElement
expect abstract class SVGCircleElement  : SVGGeometryElement
expect abstract class SVGEllipseElement : SVGGeometryElement
expect abstract class SVGPolygonElement : SVGGeometryElement
expect abstract class SVGPatternElement : SVGElement
expect abstract class SVGGradientElement: SVGElement

expect abstract class SVGGraphicsElement: SVGElement {
    fun getBBox(options: SVGBoundingBoxOptions): DOMRect
}
expect abstract class SVGGeometryElement       : SVGGraphicsElement
expect abstract class SVGTextContentElement    : SVGGraphicsElement
expect abstract class SVGTextPositioningElement: SVGTextContentElement
