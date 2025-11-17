package io.nacular.doodle.theme.basic.range

import io.nacular.doodle.controls.range.Slider
import io.nacular.doodle.controls.range.marks
import io.nacular.doodle.controls.theme.range.AbstractSliderBehavior
import io.nacular.doodle.controls.theme.range.SliderBehavior
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
import io.nacular.doodle.theme.basic.range.TickLocation.GrooveAndRange
import io.nacular.doodle.utils.Orientation.Horizontal
import kotlin.math.max
import kotlin.math.min

/**
 * A simple [SliderBehavior] that renders a bar with a round knob and ticks if enabled.
 *
 * @param barFill              paint used to fill the slider's bar
 * @param knobFill             paint used to fill the slider's knob
 * @param rangeFill            paint used to fill the section between the slider's start and knob
 * @param grooveThicknessRatio size of the bar vs the knob diameter
 * @param showTicks            if and how ticks are shown on the slider
 * @param focusManager         used to request focus for the slider
 */
public class BasicSliderBehavior<T>(
    private val barFill             :  (Slider<T>) -> Paint,
    private val knobFill            :  (Slider<T>) -> Paint,
    private val rangeFill           : ((Slider<T>) -> Paint)? = null,
                grooveThicknessRatio: Float                   = 0.6f,
    private val showTicks           : TickPresentation?       = null,
                focusManager        : FocusManager?           = null
): AbstractSliderBehavior<T>(focusManager) where T: Comparable<T> {
    /**
     * A simple [SliderBehavior] that renders a bar with a round knob and ticks if enabled.
     *
     * @param barFill              paint used to fill the slider's bar
     * @param knobFill             paint used to fill the slider's knob
     * @param rangeFill            paint used to fill the section between the slider's start and knob
     * @param grooveThicknessRatio size of the bar vs the knob diameter
     * @param showTicks            if and how ticks are shown on the slider
     * @param focusManager         used to request focus for the slider
     */
    public constructor(
        barFill             : Paint             = Lightgray.paint,
        knobFill            : Paint             = Blue.paint,
        rangeFill           : Paint?            = null,
        grooveThicknessRatio: Float             = 0.6f,
        showTicks           : TickPresentation? = null,
        focusManager        : FocusManager?     = null
    ): this(
        barFill              = { barFill  },
        knobFill             = { knobFill },
        rangeFill            = rangeFill?.let { f -> { f } },
        showTicks            = showTicks,
        focusManager         = focusManager,
        grooveThicknessRatio = grooveThicknessRatio
    )

    private val grooveThicknessRatio = max(0f, min(1f, grooveThicknessRatio))

    public var disabledPaintMapper: PaintMapper = defaultDisabledPaintMapper

    override fun render(view: Slider<T>, canvas: Canvas) {
        val grooveRect     : Rectangle
        var rangeRect      : Rectangle? = null
        val handleRect     = handleBounds(view)
        val handleSize     = handleSize(view)
        val offset         = handleSize / 2
        val handlePosition = handlePosition(view)

        val grooveInset = (1 - grooveThicknessRatio) * when (view.orientation) {
            Horizontal -> view.height
            else       -> view.width
        }

        when (view.orientation) {
            Horizontal -> {
                grooveRect = Rectangle(offset, grooveInset / 2, max(0.0, view.width - handleSize), max(0.0, view.height - grooveInset))

                if (rangeFill != null) {
                    rangeRect = grooveRect.run { Rectangle(x, y, handlePosition, height) }
                }
            }
            else       -> {
                grooveRect = Rectangle(grooveInset / 2, offset, max(0.0, view.width - grooveInset), max(0.0, view.height - handleSize))

                if (rangeFill != null) {
                    rangeRect = grooveRect.run { Rectangle(x, handlePosition, width, view.height - handlePosition - handleSize / 2) }
                }
            }
        }

        val grooveRadius = min(grooveRect.width, grooveRect.height) / 2

        val clipInfo = showTicks?.let { getSnapClip(view.marks, view.orientation, grooveRect, grooveRadius, it) }

        when (clipInfo) {
            null -> canvas.rect(grooveRect, grooveRadius, adjust(view, barFill(view)))
            else -> canvas.clip(clipInfo.first) {
                rect(grooveRect, grooveRadius, adjust(view, barFill(view)))
            }
        }

        rangeRect?.let {
            when {
                clipInfo == null                     -> canvas.rect(rangeRect, grooveRadius, adjust(view, rangeFill!!(view)))
                showTicks.location == GrooveAndRange -> canvas.clip(clipInfo.first) {
                    rect(rangeRect, grooveRadius, adjust(view, rangeFill!!((view))))
                }
            }
        }

        canvas.circle(Circle(handleRect.center, min(handleRect.width, handleRect.height) / 2), adjust(view, knobFill(view)))
    }

    private fun adjust(view: Slider<T>, fill: Paint) = if (view.enabled) fill else disabledPaintMapper(fill)
}