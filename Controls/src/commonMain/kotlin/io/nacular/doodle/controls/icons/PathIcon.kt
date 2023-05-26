package io.nacular.doodle.controls.icons

import io.nacular.doodle.core.Icon
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Paint
import io.nacular.doodle.drawing.Renderer.FillRule
import io.nacular.doodle.drawing.Renderer.FillRule.EvenOdd
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.geometry.Path
import io.nacular.doodle.geometry.PathMetrics
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Size

/**
 * Created by Nicholas Eddy on 12/5/17.
 */
public open class PathIcon<in T: Any> private constructor(
        private val path       : Path,
        private val size       : Size?          = null,
        private val fill       : (T) -> Paint?  = { null },
        private val stroke     : (T) -> Stroke? = { null },
        private val fillRule   : FillRule       = EvenOdd,
                    pathMetrics: PathMetrics): Icon<T> {

    private val pathSize = pathMetrics.size(path)

    override fun size(view: T): Size = size ?: pathSize

    override fun render(view: T, canvas: Canvas, at: Point) {
        val fill          = this.fill(view)
        val stroke        = this.stroke(view)
        val size          = size(view)

        canvas.translate(at) {
            scale(size.width / pathSize.width, size.height / pathSize.height) {
                when (stroke) {
                    null -> fill?.let { path(path, fill, fillRule) }
                    else -> when (fill) {
                        null -> path(path, stroke)
                        else -> path(path, stroke, fill, fillRule)
                    }
                }
            }
        }
    }

    public companion object {
        public operator fun <T: Any> invoke(
            path       : Path,
            size       : Size?      = null,
            fill       : Paint,
            fillRule   : FillRule   = EvenOdd,
            pathMetrics: PathMetrics): PathIcon<T> = PathIcon(path, size, { fill }, stroke = { null }, fillRule, pathMetrics)

        public operator fun <T: Any> invoke(
            path       : Path,
            size       : Size?      = null,
            stroke     : Stroke,
            pathMetrics: PathMetrics): PathIcon<T> = PathIcon(
            path,
            size,
            fill        = { if (it is View) it.foregroundColor?.paint else null },
            stroke      = { stroke },
            pathMetrics = pathMetrics
        )

        public operator fun <T: Any> invoke(
            path       : Path,
            size       : Size?      = null,
            fill       : Paint,
            stroke     : Stroke,
            fillRule   : FillRule   = EvenOdd,
            pathMetrics: PathMetrics): PathIcon<T> = PathIcon(path, size, { fill }, { stroke }, fillRule, pathMetrics)

        public operator fun <T: View> invoke(
            path       : Path,
            size       : Size?    = null,
            fill       : Color?   = null,
            outline    : Color?   = null,
            fillRule   : FillRule = EvenOdd,
            pathMetrics: PathMetrics): PathIcon<T> = PathIcon(path, size, { fill?.paint ?: it.foregroundColor?.paint }, { outline?.let { Stroke(it) } }, fillRule, pathMetrics)
    }
}