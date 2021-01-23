package io.nacular.doodle.geometry.impl

import io.nacular.doodle.SVGBoundingBoxOptions
import io.nacular.doodle.SVGElement
import io.nacular.doodle.SVGPathElement
import io.nacular.doodle.dom.SvgFactory
import io.nacular.doodle.dom.add
import io.nacular.doodle.dom.setPathData
import io.nacular.doodle.geometry.Path
import io.nacular.doodle.geometry.PathMetrics
import io.nacular.doodle.geometry.Size

/**
 * Created by Nicholas Eddy on 12/4/17.
 */
public class PathMetricsImpl(private val svgFactory: SvgFactory): PathMetrics {
    override fun width (path: Path): Double = size(path).width
    override fun height(path: Path): Double = size(path).height

    override fun size(path: Path): Size {
        val element = svgFactory<SVGPathElement>("path")

        svg.add(element)

        element.setPathData(path.data)

        svgFactory.root.add(svg)

        val size = element.getBBox(object: SVGBoundingBoxOptions {}).run { Size(width, height) }

        svgFactory.root.removeChild(svg)

        svg.removeChild(element)

        return size
    }

    private val svg = svgFactory<SVGElement>("svg")
}