package com.nectar.doodle.controls.icons

import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.core.Icon
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.ColorBrush
import com.nectar.doodle.drawing.Pen
import com.nectar.doodle.drawing.Renderer.FillRule
import com.nectar.doodle.drawing.Renderer.FillRule.EvenOdd
import com.nectar.doodle.geometry.Path
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Size

/**
 * Created by Nicholas Eddy on 12/5/17.
 */
open class PathIcon<in T: Gizmo>(
        private val path    : Path,
                    size    : Size?    = null,
                    fill    : Color?   = null,
                    outline : Color?   = null,
        private val fillRule: FillRule = EvenOdd): Icon<T> {

    override val size = size ?: path.size

    private val pen   = outline?.let { Pen       (it) }
    private val brush = fill?.let    { ColorBrush(it) }

    override fun render(gizmo: T, canvas: Canvas, at: Point) {
        val brush = this.brush ?: gizmo.foregroundColor?.let { ColorBrush(it) }

        if (brush != null) {
            canvas.scale(Point(size.width / path.size.width, size.height / path.size.height)) {
                translate(at) {
                    when (pen) {
                        null -> path(path,      brush, fillRule)
                        else -> path(path, pen, brush, fillRule)
                    }
                }
            }
        }
    }
}