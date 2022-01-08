package io.nacular.doodle.theme.basic.range

import io.nacular.doodle.controls.range.Slider
import io.nacular.doodle.controls.theme.range.AbstractSliderBehavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Blue
import io.nacular.doodle.drawing.Color.Companion.Lightgray
import io.nacular.doodle.drawing.Paint
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Circle
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.theme.PaintMapper
import io.nacular.doodle.theme.basic.defaultDisabledPaintMapper
import io.nacular.doodle.utils.Orientation.Horizontal
import kotlin.math.max
import kotlin.math.min

public class BasicSliderBehavior<T>(
        private val barFill             : Paint,
        private val knobFill            : Paint,
        private val rangeFill           : Paint? = null,
                    grooveThicknessRatio: Float = 0.6f,
                    focusManager        : FocusManager? = null
): AbstractSliderBehavior<T>(focusManager) where T: Number, T: Comparable<T> {
    public constructor(
            barColor            : Color  = Lightgray,
            knobColor           : Color  = Blue,
            rangeColor          : Color? = null,
            grooveThicknessRatio: Float  = 0.6f,
            focusManager        : FocusManager? = null): this(barFill = barColor.paint, knobFill = knobColor.paint, rangeFill = rangeColor?.paint, grooveThicknessRatio, focusManager)

    private val grooveThicknessRatio = max(0f, min(1f, grooveThicknessRatio))

    public var disabledPaintMapper: PaintMapper = defaultDisabledPaintMapper

    override fun render(view: Slider<T>, canvas: Canvas) {
        val grooveRect: Rectangle
        var rangeRect : Rectangle? = null
        val handleRect: Rectangle

        val barSize     = barSize(view)
        val offset      = barSize / 2
        val barPosition = barPosition(view)

        val grooveInset = (1 - grooveThicknessRatio) * when (view.orientation) {
            Horizontal -> view.height
            else       -> view.width
        }

        when (view.orientation) {
            Horizontal -> {
                grooveRect = Rectangle(offset, grooveInset / 2, max(0.0, view.width - barSize), max(0.0, view.height - grooveInset))
                handleRect = Rectangle(barPosition, 0.0, barSize, barSize)

                if (rangeFill != null) {
                    rangeRect = grooveRect.run { Rectangle(x, y, barPosition, height) }
                }
            }
            else       -> {
                grooveRect = Rectangle(grooveInset / 2, offset, max(0.0, view.width - grooveInset), max(0.0, view.height - barSize))
                handleRect = Rectangle(0.0, barPosition, barSize, barSize)

                if (rangeFill != null) {
                    rangeRect = grooveRect.run { Rectangle(x, y, height, barPosition) }
                }
            }
        }

        canvas.rect(grooveRect, grooveRect.height / 2, adjust(view, barFill))

        rangeRect?.let {
            canvas.rect(rangeRect, rangeRect.height / 2, adjust(view, rangeFill!!))
        }

        canvas.circle(Circle(handleRect.center, handleRect.width / 2), adjust(view, knobFill))
    }

    private fun adjust(view: Slider<T>, fill: Paint) = if (view.enabled) fill else disabledPaintMapper(fill)
}