package com.nectar.doodle.geometry.impl

import com.nectar.doodle.dom.SvgFactory
import com.nectar.doodle.dom.add
import com.nectar.doodle.dom.setPathData
import com.nectar.doodle.geometry.Path
import com.nectar.doodle.geometry.PathMetrics
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.svg.SVGPathElement

/**
 * Created by Nicholas Eddy on 12/4/17.
 */
class PathMetricsImpl(private val svgFactory: SvgFactory): PathMetrics {
    override fun width (path: Path) = size(path).width
    override fun height(path: Path) = size(path).height

    override fun size(path: Path): Size {
        val element = svgFactory.create<SVGPathElement>("path")

        svg.add(element)

        element.setPathData(path.data)

        svgFactory.root.add(svg)

        val rect = element.getBoundingClientRect().run { Rectangle(x, y, width, height) }

        svgFactory.root.removeChild(svg)

        svg.removeChild(element)

        return rect.size
    }

    private val svg = svgFactory.create<SVGElement>("svg")
}