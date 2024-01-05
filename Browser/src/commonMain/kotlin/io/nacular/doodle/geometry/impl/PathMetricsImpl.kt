package io.nacular.doodle.geometry.impl

import io.nacular.doodle.dom.BoundingBoxOptions
import io.nacular.doodle.dom.SVGElement
import io.nacular.doodle.dom.SVGPathElement
import io.nacular.doodle.dom.SvgFactory
import io.nacular.doodle.dom.add
import io.nacular.doodle.dom.getBBox
import io.nacular.doodle.dom.setPathData
import io.nacular.doodle.geometry.Path
import io.nacular.doodle.geometry.PathMetrics
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size

/**
 * Created by Nicholas Eddy on 12/4/17.
 */
internal class PathMetricsImpl(private val svgFactory: SvgFactory): PathMetrics {
    override fun width (path: Path): Double = size(path).width
    override fun height(path: Path): Double = size(path).height

    override fun size(path: Path): Size = bounds(path).size

    override fun bounds(path: Path): Rectangle = measure(path) {
        it.getBBox(BoundingBoxOptions(stroke = true, markers = true)).run { Rectangle(x, y, width, height) }
    }

    override fun length(path: Path): Double = measure(path) { it.getTotalLength().toDouble() }

    private fun <T> measure(path: Path, block: (SVGPathElement) -> T): T {
        val element = svgFactory<SVGPathElement>("path")

        svg.add(element)

        element.setPathData(path.data)

        svgFactory.root.add(svg)

        val result = block(element)

        svgFactory.root.removeChild(svg)

        svg.removeChild(element)

        return result
    }

    private val svg = svgFactory<SVGElement>("svg")
}