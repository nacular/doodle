package io.nacular.doodle.theme.basic.range

import io.nacular.doodle.controls.range.RangeSlider
import io.nacular.doodle.controls.theme.range.AbstractRangeSliderBehavior
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

public class BasicRangeSliderBehavior<T>(
        private val barFill             : Paint,
        private val knobFill            : Paint,
        private val rangeFill           : Paint = knobFill,
                    grooveThicknessRatio: Float = 0.6f,
                    focusManager        : FocusManager? = null
): AbstractRangeSliderBehavior<T>(focusManager) where T: Number, T: Comparable<T> {
    public constructor(
            barColor            : Color = Lightgray,
            knobColor           : Color = Blue,
            rangeColor          : Color = knobColor,
            grooveThicknessRatio: Float = 0.6f,
            focusManager        : FocusManager? = null): this(barFill = barColor.paint, knobFill = knobColor.paint, rangeFill = rangeColor.paint, grooveThicknessRatio, focusManager)

    private val grooveThicknessRatio = max(0f, min(1f, grooveThicknessRatio))

    public var disabledPaintMapper: PaintMapper = defaultDisabledPaintMapper

    override fun render(view: RangeSlider<T>, canvas: Canvas) {
        val grooveRect    : Rectangle
        val rangeRect     : Rectangle
        val firstKnobRect : Rectangle
        val secondKnobRect: Rectangle

        val barSize          = barSize(view)
        val offset           = barSize / 2
        val startBarPosition = startBarPosition(view)
        val endBarPosition   = endBarPosition  (view)

        val grooveInset = (1 - grooveThicknessRatio) * when (view.orientation) {
            Horizontal -> view.height
            else       -> view.width
        }

        when (view.orientation) {
            Horizontal -> {
                grooveRect     = Rectangle(offset,           grooveInset / 2, max(0.0, view.width - barSize),              max(0.0, view.height - grooveInset))
                firstKnobRect  = Rectangle(startBarPosition, 0.0, barSize, barSize)
                secondKnobRect = Rectangle(endBarPosition,   0.0, barSize, barSize)
                rangeRect      = Rectangle(startBarPosition + firstKnobRect.width / 2, grooveRect.y,    max(0.0, endBarPosition - startBarPosition), grooveRect.height)
            }
            else       -> {
                grooveRect     = Rectangle(grooveInset / 2, offset, max(0.0, view.width - grooveInset), max(0.0, view.height - barSize))
                firstKnobRect  = Rectangle(0.0, startBarPosition, barSize, barSize)
                secondKnobRect = Rectangle(0.0, endBarPosition,   barSize, barSize)
                rangeRect      = Rectangle(grooveRect.x, startBarPosition + firstKnobRect.height / 2, grooveRect.width, max(0.0, endBarPosition - startBarPosition))
            }
        }

        canvas.rect  (grooveRect, grooveRect.height / 2, adjust(view, barFill  ))
        canvas.rect  (rangeRect,                         adjust(view, rangeFill))
        canvas.circle(Circle(firstKnobRect.center,  firstKnobRect.width  / 2), adjust(view, knobFill))
        canvas.circle(Circle(secondKnobRect.center, secondKnobRect.width / 2), adjust(view, knobFill))
    }

    private fun adjust(view: RangeSlider<T>, fill: Paint) = if (view.enabled) fill else disabledPaintMapper(fill)
}