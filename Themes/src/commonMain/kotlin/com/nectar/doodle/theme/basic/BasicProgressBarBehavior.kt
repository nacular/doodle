package com.nectar.doodle.theme.basic

import com.nectar.doodle.controls.ProgressBar
import com.nectar.doodle.controls.theme.ProgressIndicatorBehavior
import com.nectar.doodle.drawing.Brush
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.Pen
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.utils.Orientation.Vertical

/**
 * Created by Nicholas Eddy on 2/12/18.
 */
class BasicProgressBarBehavior(
        private val backgroundBrush: Brush,
        private val fillBrush      : Brush,
        private val outlineColor   : Color? = null,
        private val cornerRadius   : Double = 0.0): ProgressIndicatorBehavior<ProgressBar>() {

    override fun install(view: ProgressBar) {
        super.install(view)

        view.rerender()
    }

    override fun render(view: ProgressBar, canvas: Canvas) {

        val border = 1.0
        val rect   = Rectangle(size = view.size)
        val brush  = backgroundBrush

        // Draw background with optional outline
        when {
            view.height > 2 && outlineColor != null -> canvas.rect(rect.inset(border / 2), cornerRadius, Pen(outlineColor, border), brush)
            else                                    -> canvas.rect(rect,                   cornerRadius,                            brush)
        }

        val innerRect = when (view.orientation) {
            Vertical -> (view.height * view.progress).let { Rectangle(0.0, view.height - it, view.width, it) }
            else     -> Rectangle(width = view.width * view.progress, height = view.height)
        }

        // Draw progress
        when {
            cornerRadius > 0 -> canvas.clip(rect, cornerRadius) { rect(innerRect, fillBrush) }
            else             ->                            canvas.rect(innerRect, fillBrush)
        }
    }
}
