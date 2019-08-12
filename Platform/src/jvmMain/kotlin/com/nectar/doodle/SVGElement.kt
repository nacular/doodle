package com.nectar.doodle

/**
 * Created by Nicholas Eddy on 8/9/19.
 */
actual abstract class SVGElement        : Element           ()
actual abstract class SVGPathElement    : SVGGeometryElement()
actual abstract class SVGRectElement    : SVGGeometryElement()
actual abstract class SVGCircleElement  : SVGGeometryElement()
actual abstract class SVGEllipseElement : SVGGeometryElement()
actual abstract class SVGPatternElement : SVGElement        ()
actual abstract class SVGGradientElement: SVGElement        ()
actual abstract class SVGGraphicsElement: SVGElement        ()
actual abstract class SVGGeometryElement: SVGGraphicsElement()
