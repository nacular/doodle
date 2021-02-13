package io.nacular.doodle

/**
 * Created by Nicholas Eddy on 8/9/19.
 */
public actual abstract class SVGElement public actual constructor(): Element(), ElementCSSInlineStyle
public actual abstract class SVGPathElement    : SVGGeometryElement()
public actual abstract class SVGRectElement    : SVGGeometryElement()
public actual abstract class SVGCircleElement  : SVGGeometryElement()
public actual abstract class SVGEllipseElement : SVGGeometryElement()
public actual abstract class SVGPolygonElement : SVGGeometryElement()
public actual abstract class SVGPatternElement : SVGElement        ()
public actual abstract class SVGGradientElement: SVGElement        ()
public actual abstract class SVGGraphicsElement: SVGElement        ()

public actual fun SVGGraphicsElement.getBBox(options: BoundingBoxOptions): DOMRect {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}


public actual abstract class SVGGeometryElement: SVGGraphicsElement() {
    public actual fun getTotalLength(): Float = 0f
}
public actual abstract class SVGTextElement    : SVGTextPositioningElement()
//public actual interface SVGBoundingBoxOptions
public actual abstract class SVGTextContentElement    : SVGGraphicsElement()
public actual abstract class SVGTextPositioningElement: SVGTextContentElement()