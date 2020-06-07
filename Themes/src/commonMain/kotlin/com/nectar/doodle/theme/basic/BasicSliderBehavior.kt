package com.nectar.doodle.theme.basic

import com.nectar.doodle.controls.range.Slider
import com.nectar.doodle.controls.theme.SliderBehavior
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.Color.Companion.Blue
import com.nectar.doodle.drawing.Color.Companion.Lightgray
import com.nectar.doodle.drawing.ColorBrush
import com.nectar.doodle.focus.FocusManager
import com.nectar.doodle.geometry.Circle
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.utils.Orientation.Horizontal

class BasicSliderBehavior(private val barColor: Color = Lightgray, private val knobColor: Color = Blue, focusManager: FocusManager? = null): SliderBehavior(focusManager) {
    override fun render(view: Slider, canvas: Canvas) {
        val rect1: Rectangle
        val rect2: Rectangle

        val grooveInset = view.height * 0.6
        val barSize     = barSize(view)
        val offset      = barSize / 2
        val barPosition = barPosition(view)

        when (view.orientation) {
            Horizontal -> {
                rect1 = Rectangle(offset, grooveInset / 2, view.width - barSize, view.height - grooveInset)
                rect2 = Rectangle(barPosition, 0.0, barSize, barSize)
            }
            else   -> {
                rect1 = Rectangle(grooveInset / 2, offset, view.width - grooveInset, view.height - barSize)
                rect2 = Rectangle(0.0, barPosition, barSize, barSize)
            }
        }

        canvas.rect(rect1, rect1.height / 2, ColorBrush(barColor))
        canvas.circle(Circle(rect2.center, rect2.width / 2), ColorBrush(knobColor))
    }
}