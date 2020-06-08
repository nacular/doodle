package io.nacular.doodle

/**
 * Created by Nicholas Eddy on 8/9/19.
 */
actual abstract class SVGElement actual constructor(): Element(), ElementCSSInlineStyle
actual abstract class SVGPathElement    : SVGGeometryElement()
actual abstract class SVGRectElement    : SVGGeometryElement()
actual abstract class SVGCircleElement  : SVGGeometryElement()
actual abstract class SVGEllipseElement : SVGGeometryElement()
actual abstract class SVGPolygonElement : SVGGeometryElement()
actual abstract class SVGPatternElement : SVGElement        ()
actual abstract class SVGGradientElement: SVGElement        ()
actual abstract class SVGGraphicsElement: SVGElement        () {
    actual fun getBBox(options: SVGBoundingBoxOptions): DOMRect {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

actual abstract class SVGGeometryElement: SVGGraphicsElement()
actual abstract class SVGTextElement    : SVGTextPositioningElement()
/**
 * Created by Nicholas Eddy on 8/9/19.
 */
actual interface SVGBoundingBoxOptions

actual abstract class SVGTextContentElement    : SVGGraphicsElement()
actual abstract class SVGTextPositioningElement: SVGTextContentElement()