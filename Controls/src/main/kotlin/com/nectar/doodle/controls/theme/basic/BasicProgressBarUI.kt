package com.nectar.doodle.controls.theme.basic

import com.nectar.doodle.controls.ProgressBar
import com.nectar.doodle.controls.theme.ProgressBarUI
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.ColorBrush
import com.nectar.doodle.drawing.Pen
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.utils.Orientation.Vertical

/**
 * Created by Nicholas Eddy on 2/12/18.
 */
class BasicProgressBarUI(private val defaultBackgroundColor: Color, private val darkBackgroundColor: Color): ProgressBarUI() {
    override fun render(canvas: Canvas, gizmo: ProgressBar) {

        var rect = Rectangle(size = gizmo.size)

        canvas.rect(rect, ColorBrush(gizmo.backgroundColor ?: defaultBackgroundColor))

        if (gizmo.height > 2) {
            canvas.rect(rect, Pen(darkBackgroundColor))
        }

        rect = when (gizmo.orientation) {
            Vertical -> (gizmo.height * gizmo.progress).let { Rectangle(0.0, gizmo.height - it, gizmo.width, it) }
            else     -> Rectangle(width = gizmo.width * gizmo.progress, height = gizmo.height)
        }

        canvas.rect(rect, ColorBrush(darkBackgroundColor))
    }
}
