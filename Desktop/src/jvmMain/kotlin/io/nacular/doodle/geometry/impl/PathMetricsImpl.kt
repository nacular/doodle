package io.nacular.doodle.geometry.impl

import io.nacular.doodle.geometry.Path
import io.nacular.doodle.geometry.PathMetrics
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.skia.skija
import org.jetbrains.skija.PathMeasure

public class PathMetricsImpl(private val pathMeasure: PathMeasure): PathMetrics {
    override fun width (path: Path): Double    = path.skija().computeTightBounds().width.toDouble ()
    override fun height(path: Path): Double    = path.skija().computeTightBounds().height.toDouble()
    override fun size  (path: Path): Size      = path.skija().computeTightBounds().run { Size(width, height) }
    override fun bounds(path: Path): Rectangle = path.skija().computeTightBounds().run { Rectangle(left, top, width, height) }

    override fun length(path: Path): Double {
        pathMeasure.setPath(path.skija(), false)

        return pathMeasure.length.toDouble()
    }
}