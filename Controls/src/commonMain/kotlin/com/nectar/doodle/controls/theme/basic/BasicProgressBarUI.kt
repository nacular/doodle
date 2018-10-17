package com.nectar.doodle.controls.theme.basic

import com.nectar.doodle.controls.ProgressBar
import com.nectar.doodle.controls.theme.ProgressIndicatorUI
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.ColorBrush
import com.nectar.doodle.drawing.Pen
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.utils.Orientation.Vertical

/**
 * Created by Nicholas Eddy on 2/12/18.
 */
class BasicProgressBarUI(private val defaultBackgroundColor: Color, private val darkBackgroundColor: Color): ProgressIndicatorUI<ProgressBar>() {
    override fun render(gizmo: ProgressBar, canvas: Canvas) {

        val border = 1.0
        var rect   = Rectangle(size = gizmo.size)
        val brush  = ColorBrush(gizmo.backgroundColor ?: defaultBackgroundColor)

        // Draw background with optional outline
        when {
            gizmo.height > 2 -> canvas.rect(rect.inset(border / 2), Pen(darkBackgroundColor, border), brush)
            else             -> canvas.rect(rect,                                                     brush)
        }

        rect = when (gizmo.orientation) {
            Vertical -> (gizmo.height * gizmo.progress).let { Rectangle(0.0, gizmo.height - it, gizmo.width, it) }
            else     -> Rectangle(width = gizmo.width * gizmo.progress, height = gizmo.height)
        }

        // Draw progress
        canvas.rect(rect, ColorBrush(darkBackgroundColor))
    }
}
