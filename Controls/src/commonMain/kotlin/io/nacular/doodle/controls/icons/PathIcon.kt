package io.nacular.doodle.controls.icons

import io.nacular.doodle.core.Icon
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.ColorBrush
import io.nacular.doodle.drawing.Pen
import io.nacular.doodle.drawing.Renderer.FillRule
import io.nacular.doodle.drawing.Renderer.FillRule.EvenOdd
import io.nacular.doodle.geometry.Path
import io.nacular.doodle.geometry.PathMetrics
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Size

/**
 * Created by Nicholas Eddy on 12/5/17.
 */
open class PathIcon<in T: View>(
        private val path       : Path,
        private val size       : Size?    = null,
                    fill       : Color?   = null,
                    outline    : Color?   = null,
        private val fillRule   : FillRule = EvenOdd,
                    pathMetrics: PathMetrics): Icon<T> {

    private val pathSize = pathMetrics.size(path)
    override fun size(view: T) = size ?: pathSize

    private val pen   = outline?.let { Pen       (it) }
    private val brush = fill?.let    { ColorBrush(it) }

    override fun render(view: T, canvas: Canvas, at: Point) {
        val brush = this.brush ?: view.foregroundColor?.let { ColorBrush(it) }
        val size  = size(view)

        canvas.scale(size.width / pathSize.width, size.height / pathSize.height) {
            translate(at) {
                when (pen) {
                    null -> path(path,      brush!!, fillRule)
                    else -> when (brush) {
                        null -> path(path, pen                 )
                        else -> path(path, pen, brush, fillRule)
                    }
                }
            }
        }
    }
}