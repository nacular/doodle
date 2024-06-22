package io.nacular.doodle.theme.basic.range

import io.nacular.doodle.controls.range.RangeSlider
import io.nacular.doodle.controls.theme.range.AbstractRangeSliderBehavior
import io.nacular.doodle.drawing.Canvas
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
        private val barFill             : (RangeSlider<T>) -> Paint,
        private val startKnobFill       : (RangeSlider<T>) -> Paint,
        private val endKnobFill         : (RangeSlider<T>) -> Paint = startKnobFill,
        private val rangeFill           : (RangeSlider<T>) -> Paint = endKnobFill,
                    grooveThicknessRatio: Float                     = 0.6f,
        private val showTicks           : TickPresentation?         = null,
                    focusManager        : FocusManager?             = null
): AbstractRangeSliderBehavior<T>(focusManager) where T: Comparable<T> {
    public constructor(
            barFill             : Paint             = Lightgray.paint,
            startKnobFill       : Paint             = Blue.paint,
            endKnobFill         : Paint             = startKnobFill,
            rangeFill           : Paint             = endKnobFill,
            grooveThicknessRatio: Float             = 0.6f,
            showTicks           : TickPresentation? = null,
            focusManager        : FocusManager?     = null): this(barFill = { barFill }, startKnobFill = { startKnobFill }, endKnobFill = { endKnobFill }, rangeFill = { rangeFill }, grooveThicknessRatio, showTicks, focusManager)

    private val grooveThicknessRatio = max(0f, min(1f, grooveThicknessRatio))

    public var disabledPaintMapper: PaintMapper = defaultDisabledPaintMapper

    override fun render(view: RangeSlider<T>, canvas: Canvas) {
        val grooveRect    : Rectangle
        val rangeRect     : Rectangle
        val firstKnobRect : Rectangle
        val secondKnobRect: Rectangle

        val handleSize          = handleSize(view)
        val offset              = handleSize / 2
        val startHandlePosition = startHandlePosition(view)
        val endHandlePosition   = endHandlePosition  (view)

        val grooveInset = (1 - grooveThicknessRatio) * when (view.orientation) {
            Horizontal -> view.height
            else       -> view.width
        }

        when (view.orientation) {
            Horizontal -> {
                grooveRect     = Rectangle(offset, grooveInset / 2, max(0.0, view.width - handleSize), max(0.0, view.height - grooveInset))
                firstKnobRect  = Rectangle(startHandlePosition, 0.0, handleSize, handleSize)
                secondKnobRect = Rectangle(endHandlePosition,   0.0, handleSize, handleSize)
                rangeRect      = Rectangle(startHandlePosition + firstKnobRect.width / 2, grooveRect.y, max(0.0, endHandlePosition - startHandlePosition), grooveRect.height)
            }
            else       -> {
                grooveRect     = Rectangle(grooveInset / 2, offset, max(0.0, view.width - grooveInset), max(0.0, view.height - handleSize))
                firstKnobRect  = Rectangle(0.0, startHandlePosition, handleSize, handleSize)
                secondKnobRect = Rectangle(0.0, endHandlePosition,   handleSize, handleSize)
                rangeRect      = Rectangle(grooveRect.x, min(startHandlePosition, endHandlePosition) + firstKnobRect.height / 2, grooveRect.width, max(0.0, startHandlePosition - endHandlePosition))
            }
        }

        val grooveRadius = min(grooveRect.width, grooveRect.height) / 2

        when (val clipInfo = showTicks?.let { getSnapClip(view.ticks, view.orientation, grooveRect, grooveRadius, it) }) {
            null -> {
                canvas.rect(grooveRect, grooveRadius, adjust(view, barFill  (view)))
                canvas.rect(rangeRect,                adjust(view, rangeFill(view)))
            }
            else -> canvas.clip(clipInfo.first) {
                rect(grooveRect, grooveRadius, adjust(view, barFill  (view)))
                rect(rangeRect,                adjust(view, rangeFill(view)))
            }
        }

        canvas.circle(Circle(firstKnobRect.center,  min(firstKnobRect.width,  firstKnobRect.height)  / 2), adjust(view, startKnobFill(view)))
        canvas.circle(Circle(secondKnobRect.center, min(secondKnobRect.width, secondKnobRect.height) / 2), adjust(view, endKnobFill  (view)))
    }

    private fun adjust(view: RangeSlider<T>, fill: Paint) = if (view.enabled) fill else disabledPaintMapper(fill)

    public companion object {
        public operator fun <T> invoke(
            barFill             : Paint         = Lightgray.paint,
            knobFill            : Paint         = Blue.paint,
            rangeFill           : Paint         = knobFill,
            grooveThicknessRatio: Float         = 0.6f,
            focusManager        : FocusManager? = null): BasicRangeSliderBehavior<T> where T: Number, T: Comparable<T> {
            return BasicRangeSliderBehavior(
                barFill              = barFill,
                startKnobFill        = knobFill,
                endKnobFill          = knobFill,
                rangeFill            = rangeFill,
                grooveThicknessRatio = grooveThicknessRatio,
                focusManager         = focusManager
            )
        }
    }
}