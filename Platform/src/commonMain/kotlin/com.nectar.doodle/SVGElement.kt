package com.nectar.doodle

/**
 * Created by Nicholas Eddy on 8/9/19.
 */
expect abstract class SVGElement        : Element, ElementCSSInlineStyle
expect abstract class SVGRectElement    : SVGGeometryElement
expect abstract class SVGPathElement    : SVGGeometryElement
expect abstract class SVGCircleElement  : SVGGeometryElement
expect abstract class SVGEllipseElement : SVGGeometryElement
expect abstract class SVGPatternElement : SVGElement
expect abstract class SVGGradientElement: SVGElement

expect abstract class SVGGraphicsElement: SVGElement
expect abstract class SVGGeometryElement: SVGGraphicsElement
