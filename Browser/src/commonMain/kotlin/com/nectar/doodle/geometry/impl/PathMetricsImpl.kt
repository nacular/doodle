package com.nectar.doodle.geometry.impl

import com.nectar.doodle.SVGBoundingBoxOptions
import com.nectar.doodle.SVGElement
import com.nectar.doodle.SVGPathElement
import com.nectar.doodle.dom.SvgFactory
import com.nectar.doodle.dom.add
import com.nectar.doodle.dom.setPathData
import com.nectar.doodle.geometry.Path
import com.nectar.doodle.geometry.PathMetrics
import com.nectar.doodle.geometry.Size

/**
 * Created by Nicholas Eddy on 12/4/17.
 */
class PathMetricsImpl(private val svgFactory: SvgFactory): PathMetrics {
    override fun width (path: Path) = size(path).width
    override fun height(path: Path) = size(path).height

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