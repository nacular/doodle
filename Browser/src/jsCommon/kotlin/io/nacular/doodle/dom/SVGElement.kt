package io.nacular.doodle.dom

import org.w3c.dom.svg.SVGBoundingBoxOptions

/**
 * Created by Nicholas Eddy on 8/9/19.
 */
internal actual typealias SVGElement                = org.w3c.dom.svg.SVGElement
internal actual typealias SVGPathElement            = org.w3c.dom.svg.SVGPathElement
internal actual typealias SVGRectElement            = org.w3c.dom.svg.SVGRectElement
internal actual typealias SVGTextElement            = org.w3c.dom.svg.SVGTextElement
internal actual typealias SVGCircleElement          = org.w3c.dom.svg.SVGCircleElement
internal actual typealias SVGEllipseElement         = org.w3c.dom.svg.SVGEllipseElement
internal actual typealias SVGPolygonElement         = org.w3c.dom.svg.SVGPolygonElement
internal actual typealias SVGPatternElement         = org.w3c.dom.svg.SVGPatternElement
internal actual typealias SVGGradientElement        = org.w3c.dom.svg.SVGGradientElement
internal actual typealias SVGGraphicsElement        = org.w3c.dom.svg.SVGGraphicsElement
internal actual typealias SVGGeometryElement        = org.w3c.dom.svg.SVGGeometryElement
internal actual typealias SVGTextContentElement     = org.w3c.dom.svg.SVGTextContentElement
internal actual typealias SVGLinearGradientElement  = org.w3c.dom.svg.SVGLinearGradientElement
internal actual typealias SVGRadialGradientElement  = org.w3c.dom.svg.SVGRadialGradientElement
internal actual typealias SVGTextPositioningElement = org.w3c.dom.svg.SVGTextPositioningElement

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
internal actual fun SVGGraphicsElement.getBBox(options: BoundingBoxOptions): DOMRect {
    return this.getBBox(SVGBoundingBoxOptions(
            fill    = options.fill,
            stroke  = options.stroke,
            markers = options.markers,
            clipped = options.clipped
    ))
}