package com.nectar.doodle.controls.theme

import com.nectar.doodle.controls.text.Label
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.ColorBrush
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.theme.Behavior
import com.nectar.doodle.utils.HorizontalAlignment
import com.nectar.doodle.utils.VerticalAlignment

/**
 * Created by Nicholas Eddy on 9/25/19.
 */
class LabelBehavior(private val foregroundColor: Color? = null, private val backgroundColor: Color? = null): Behavior<Label> {
    override fun install(view: Label) {
        foregroundColor?.let { if (it != Color.black) view.foregroundColor = it } // TODO: Check default color instead
        backgroundColor?.let {                        view.backgroundColor = it }
    }

    override fun uninstall(view: Label) {
        view.foregroundColor = null // FIXME: This might override a user-pref
    }

    override fun render(view: Label, canvas: Canvas) {
        view.apply {
            val y = when (verticalAlignment) {
                VerticalAlignment.Top    -> 0.0
                VerticalAlignment.Middle -> (height - textSize.height) / 2
                VerticalAlignment.Bottom ->  height - textSize.height
            }

            val x = when (horizontalAlignment) {
                HorizontalAlignment.Left   -> 0.0
                HorizontalAlignment.Center -> (width - textSize.width) / 2
                HorizontalAlignment.Right  ->  width - textSize.width
            }

            backgroundColor?.let {
                canvas.rect(bounds.atOrigin, ColorBrush(it))
            }

            if (wrapsWords) {
                canvas.wrapped(styledText, Point(x, y), 0.0, width)
            } else {
                canvas.text(styledText, Point(x, y))
            }
        }
    }
}