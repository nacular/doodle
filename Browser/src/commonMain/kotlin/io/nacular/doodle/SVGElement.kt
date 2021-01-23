package io.nacular.doodle

/**
 * Created by Nicholas Eddy on 8/9/19.
 */
public expect interface SVGBoundingBoxOptions

public expect abstract class SVGElement()      : Element, ElementCSSInlineStyle
public expect abstract class SVGRectElement    : SVGGeometryElement
public expect abstract class SVGPathElement    : SVGGeometryElement
public expect abstract class SVGTextElement    : SVGTextPositioningElement
public expect abstract class SVGCircleElement  : SVGGeometryElement
public expect abstract class SVGEllipseElement : SVGGeometryElement
public expect abstract class SVGPolygonElement : SVGGeometryElement
public expect abstract class SVGPatternElement : SVGElement
public expect abstract class SVGGradientElement: SVGElement

public expect abstract class SVGGraphicsElement: SVGElement {
    public fun getBBox(options: SVGBoundingBoxOptions): DOMRect
}

public expect abstract class SVGGeometryElement       : SVGGraphicsElement
public expect abstract class SVGTextContentElement    : SVGGraphicsElement
public expect abstract class SVGTextPositioningElement: SVGTextContentElement
