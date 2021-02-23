package io.nacular.doodle.theme.basic

import io.nacular.doodle.controls.ProgressBar
import io.nacular.doodle.controls.theme.ProgressIndicatorBehavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.ColorPaint
import io.nacular.doodle.drawing.Paint
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.utils.Orientation.Vertical

/**
 * Created by Nicholas Eddy on 2/12/18.
 */
public class BasicProgressBarBehavior(
        private val background      : Paint?,
        private val foreground      : Paint?,
        private val outlineColor    : Color? = null,
        private val backgroundRadius: Double = 0.0,
        private val foregroundRadius: Double = backgroundRadius,
        private val outlineThickness: Double = 1.0): ProgressIndicatorBehavior<ProgressBar>() {

    override fun install(view: ProgressBar) {
        super.install(view)

        view.rerender()
    }

    override fun render(view: ProgressBar, canvas: Canvas) {
        val bGround = background ?: view.backgroundColor?.let { ColorPaint(it) }
        val fGround = foreground ?: view.foregroundColor?.let { ColorPaint(it) }

        if (bGround == null && fGround == null) {
            return
        }

        val rect = Rectangle(size = view.size)

        val stroke = if (outlineColor != null && view.height > 2 * outlineThickness) Stroke(outlineColor, outlineThickness) else null

        // Draw background with optional outline
        when {
            stroke  != null && bGround != null -> canvas.rect(rect.inset(outlineThickness / 2), backgroundRadius, stroke, bGround)
            stroke  != null                    -> canvas.rect(rect.inset(outlineThickness / 2), backgroundRadius, stroke         )
            bGround != null                    -> canvas.rect(rect,                             backgroundRadius,         bGround)
        }

        val innerRect = when (view.orientation) {
            Vertical -> (view.height * view.progress).let { Rectangle(0.0, view.height - it, view.width, it) }
            else     -> Rectangle(width = view.width * view.progress, height = view.height)
        }

        // Draw progress
        if (fGround != null) {
            when {
                foregroundRadius < backgroundRadius -> canvas.clip(rect, backgroundRadius) { rect(innerRect, foregroundRadius, fGround) }
                else                                ->                                canvas.rect(innerRect, foregroundRadius, fGround)
            }
        }
    }
}
