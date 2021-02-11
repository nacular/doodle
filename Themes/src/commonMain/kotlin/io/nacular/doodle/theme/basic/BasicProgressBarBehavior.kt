package io.nacular.doodle.theme.basic

import io.nacular.doodle.controls.ProgressBar
import io.nacular.doodle.controls.theme.ProgressIndicatorBehavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Fill
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.utils.Orientation.Vertical

/**
 * Created by Nicholas Eddy on 2/12/18.
 */
public class BasicProgressBarBehavior(
        private val background      : Fill?,
        private val foreground      : Fill,
        private val outlineColor    : Color? = null,
        private val cornerRadius    : Double = 0.0,
        private val outlineThickness: Double = 1.0): ProgressIndicatorBehavior<ProgressBar>() {

    override fun install(view: ProgressBar) {
        super.install(view)

        view.rerender()
    }

    override fun render(view: ProgressBar, canvas: Canvas) {
        val rect = Rectangle(size = view.size)

        val stroke = if (outlineColor != null && view.height > 2 * outlineThickness) Stroke(outlineColor, outlineThickness) else null

        // Draw background with optional outline
        when {
            stroke     != null && background != null -> canvas.rect(rect.inset(outlineThickness / 2), cornerRadius, stroke, background)
            stroke     != null                       -> canvas.rect(rect.inset(outlineThickness / 2), cornerRadius, stroke            )
            background != null                       -> canvas.rect(rect,                             cornerRadius,         background)
        }

        val innerRect = when (view.orientation) {
            Vertical -> (view.height * view.progress).let { Rectangle(0.0, view.height - it, view.width, it) }
            else     -> Rectangle(width = view.width * view.progress, height = view.height)
        }

        // Draw progress
        when {
            cornerRadius > 0 -> canvas.clip(rect, cornerRadius) { rect(innerRect, foreground) }
            else             ->                            canvas.rect(innerRect, foreground)
        }
    }
}
