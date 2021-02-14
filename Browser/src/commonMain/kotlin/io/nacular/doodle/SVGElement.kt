package io.nacular.doodle

// Expecting this interface doesn't work for some reason
//public expect interface SVGBoundingBoxOptions {
//    var stroke : Boolean?
//    var markers: Boolean?
//}

public expect abstract class SVGElement()            : Element, ElementCSSInlineStyle
public expect abstract class SVGRectElement          : SVGGeometryElement
public expect abstract class SVGPathElement          : SVGGeometryElement
public expect abstract class SVGTextElement          : SVGTextPositioningElement
public expect abstract class SVGCircleElement        : SVGGeometryElement
public expect abstract class SVGEllipseElement       : SVGGeometryElement
public expect abstract class SVGPolygonElement       : SVGGeometryElement
public expect abstract class SVGPatternElement       : SVGElement
public expect abstract class SVGGradientElement      : SVGElement
public expect abstract class SVGLinearGradientElement: SVGGradientElement
public expect abstract class SVGRadialGradientElement: SVGGradientElement

public data class BoundingBoxOptions(
        public var fill   : Boolean? = true,
        public var stroke : Boolean? = false,
        public var markers: Boolean? = false,
        public var clipped: Boolean? = false
)

public expect abstract class SVGGraphicsElement: SVGElement

internal expect fun SVGGraphicsElement.getBBox(options: BoundingBoxOptions): DOMRect

public expect abstract class SVGGeometryElement       : SVGGraphicsElement {
    public fun getTotalLength(): Float
}
public expect abstract class SVGTextContentElement    : SVGGraphicsElement
public expect abstract class SVGTextPositioningElement: SVGTextContentElement
