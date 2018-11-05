package com.nectar.doodle.controls.theme.basic

import com.nectar.doodle.controls.Slider
import com.nectar.doodle.controls.theme.SliderUI
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.ColorBrush
import com.nectar.doodle.drawing.Pen
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.utils.Orientation.Horizontal
import com.nectar.doodle.utils.Orientation.Vertical

/**
 * Created by Nicholas Eddy on 2/13/18.
 */
class BasicSliderUI(slider: Slider, private val defaultBackgroundColor: Color, private val darkBackgroundColor: Color): SliderUI(slider) {
    override fun render(view: Slider, canvas: Canvas) {
        val rect1: Rectangle
        val rect2: Rectangle

        val border      = 1.0
        val grooveInset = view.height / 2
        val barSize     = barSize
        val offset      = barSize / 2
        val barPosition = barPosition

        when (view.orientation) {
            Horizontal -> {
                rect1 = Rectangle(offset, grooveInset / 2, view.width - barSize, view.height - grooveInset)
                rect2 = Rectangle(barPosition, 0.0, barSize, barSize)
            }
            Vertical                    -> {
                rect1 = Rectangle(grooveInset / 2, offset, view.width - grooveInset, view.height - barSize)
                rect2 = Rectangle(0.0, barPosition, barSize, barSize)
            }
        }

        Pen(darkBackgroundColor, border).also { pen ->
            canvas.rect(rect1.inset(border / 2), pen)
            canvas.rect(rect2.inset(border / 2), pen, ColorBrush(defaultBackgroundColor))
        }
    }
}