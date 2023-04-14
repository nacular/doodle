package io.nacular.doodle.controls.icons

import io.nacular.doodle.core.Icon
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.ColorPaint
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
public open class PathIcon<in T: View>(
        private val path       : Path,
        private val size       : Size?      = null,
        private val fill       : Paint?     = null,
        private val stroke     : Stroke?    = null,
        private val fillRule   : FillRule   = EvenOdd,
                    pathMetrics: PathMetrics): Icon<T> {
    public constructor(
        path       : Path,
        size       : Size?    = null,
        fill       : Color?   = null,
        outline    : Color?   = null,
        fillRule   : FillRule = EvenOdd,
        pathMetrics: PathMetrics
    ): this(path, size, fill?.paint, outline?.let { Stroke(it) }, fillRule, pathMetrics)

    private val pathSize = pathMetrics.size(path)
    override fun size(view: T): Size = size ?: pathSize

    override fun render(view: T, canvas: Canvas, at: Point) {
        val fill = this.fill ?: view.foregroundColor?.let { ColorPaint(it) }
        val size = size(view)

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
}