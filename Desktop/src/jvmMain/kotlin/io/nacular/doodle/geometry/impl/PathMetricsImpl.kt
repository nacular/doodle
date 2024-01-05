package io.nacular.doodle.geometry.impl

import io.nacular.doodle.geometry.Path
import io.nacular.doodle.geometry.PathMetrics
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.skia.skia
import org.jetbrains.skia.PathMeasure

internal class PathMetricsImpl(private val pathMeasure: PathMeasure): PathMetrics {
    override fun width (path: Path): Double    = path.skia().computeTightBounds().width.toDouble ()
    override fun height(path: Path): Double    = path.skia().computeTightBounds().height.toDouble()
    override fun size  (path: Path): Size      = path.skia().computeTightBounds().run { Size(width, height) }
    override fun bounds(path: Path): Rectangle = path.skia().computeTightBounds().run { Rectangle(left, top, width, height) }

    override fun length(path: Path): Double {
        pathMeasure.setPath(path.skia(), false)

        return pathMeasure.length.toDouble()
    }
}