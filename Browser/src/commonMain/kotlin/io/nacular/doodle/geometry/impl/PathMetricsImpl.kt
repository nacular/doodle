package io.nacular.doodle.geometry.impl

import io.nacular.doodle.dom.BoundingBoxOptions
import io.nacular.doodle.dom.SVGElement
import io.nacular.doodle.dom.SVGPathElement
import io.nacular.doodle.dom.SvgFactory
import io.nacular.doodle.dom.add
import io.nacular.doodle.dom.getBBox_
import io.nacular.doodle.dom.parent
import io.nacular.doodle.dom.setPathData
import io.nacular.doodle.dom.setPosition
import io.nacular.doodle.geometry.Path
import io.nacular.doodle.geometry.PathMetrics
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.scheduler.Scheduler
import io.nacular.doodle.scheduler.Task
import io.nacular.doodle.utils.autoCanceling
import io.nacular.measured.units.Measure
import io.nacular.measured.units.Time
import io.nacular.measured.units.Time.Companion.seconds
import io.nacular.measured.units.times

/**
 * Created by Nicholas Eddy on 12/4/17.
 */
internal class PathMetricsImpl(
    private val svgFactory  : SvgFactory,
    private val scheduler   : Scheduler,
    private val cleanupDelay: Measure<Time> = 5 * seconds,
): PathMetrics {

    private val path = svgFactory<SVGPathElement>("path")
    private val svg = svgFactory<SVGElement>("svg").apply {
        add(path)
    }
    private var cleanupTask: Task? by autoCanceling()

    override fun width (path: Path): Double = size(path).width
    override fun height(path: Path): Double = size(path).height

    override fun size(path: Path): Size = bounds(path).size

    override fun bounds(path: Path): Rectangle = measure(path) {
        it.getBBox_(BoundingBoxOptions(stroke = true, markers = true)).run { Rectangle(x, y, width, height) }
    }

    override fun length(path: Path): Double = measure(path) { it.getTotalLength().toDouble() }

    private fun <T> measure(path: Path, block: (SVGPathElement) -> T): T {
        if (svg.parent == null) {
            svg.style.setPosition(Point(0, 10_000))
            svgFactory.root.add(svg)
        }

        cleanupTask = scheduler.after(cleanupDelay) { svg.remove() }

        this.path.setPathData(path.data)

        val result = block(this.path)

        return result
    }
}