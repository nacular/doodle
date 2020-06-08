package io.nacular.doodle.theme.basic

import io.nacular.doodle.controls.range.Slider
import io.nacular.doodle.controls.theme.SliderBehavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Blue
import io.nacular.doodle.drawing.Color.Companion.Lightgray
import io.nacular.doodle.drawing.ColorBrush
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Circle
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.utils.Orientation.Horizontal

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