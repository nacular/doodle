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
    override fun render(view: ProgressBar, canvas: Canvas) {

        val border = 1.0
        var rect   = Rectangle(size = view.size)
        val brush  = ColorBrush(view.backgroundColor ?: defaultBackgroundColor)

        // Draw background with optional outline
        when {
            view.height > 2 -> canvas.rect(rect.inset(border / 2), Pen(darkBackgroundColor, border), brush)
            else            -> canvas.rect(rect,                                                     brush)
        }

        rect = when (view.orientation) {
            Vertical -> (view.height * view.progress).let { Rectangle(0.0, view.height - it, view.width, it) }
            else     -> Rectangle(width = view.width * view.progress, height = view.height)
        }

        // Draw progress
        canvas.rect(rect, ColorBrush(darkBackgroundColor))
    }
}
