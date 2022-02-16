package io.nacular.doodle.theme.basic.range

import io.nacular.doodle.controls.range.Slider
import io.nacular.doodle.controls.theme.range.AbstractSliderBehavior
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

public class BasicSliderBehavior<T>(
        private val barFill             :  (Slider<T>) -> Paint,
        private val knobFill            :  (Slider<T>) -> Paint,
        private val rangeFill           : ((Slider<T>) -> Paint)? = null,
                    grooveThicknessRatio: Float                   = 0.6f,
        private val showTicks           : TickPresentation?       = null,
                    focusManager        : FocusManager?           = null
): AbstractSliderBehavior<T>(focusManager) where T: Number, T: Comparable<T> {
    public constructor(
            barFill             : Paint             = Lightgray.paint,
            knobFill            : Paint             = Blue.paint,
            rangeFill           : Paint?            = null,
            grooveThicknessRatio: Float             = 0.6f,
            showTicks           : TickPresentation? = null,
            focusManager        : FocusManager?     = null): this(barFill = { barFill }, knobFill = { knobFill }, rangeFill = rangeFill?.let { f -> { f } }, grooveThicknessRatio, showTicks, focusManager)

    private val grooveThicknessRatio = max(0f, min(1f, grooveThicknessRatio))
    private val ticksChanged: (Slider<T>) -> Unit = { it.rerender() }

    public var disabledPaintMapper: PaintMapper = defaultDisabledPaintMapper

    override fun install(view: Slider<T>) {
        super.install(view)

        if (showTicks != null) {
            view.ticksChanged += ticksChanged
        }
    }

    override fun uninstall(view: Slider<T>) {
        super.uninstall(view)

        if (showTicks != null) {
            view.ticksChanged -= ticksChanged
        }
    }

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

        val clipInfo = showTicks?.let { getSnapClip(view.ticks, view.orientation, grooveRect, it) }

        when (clipInfo) {
            null -> canvas.rect(grooveRect, grooveRect.height / 2, adjust(view, barFill(view)))
            else -> canvas.clip(clipInfo.first) {
                rect(grooveRect, grooveRect.height / 2, adjust(view, barFill(view)))
            }
        }

        rangeRect?.let {
            when (clipInfo) {
                null -> canvas.rect(rangeRect, rangeRect.height / 2, adjust(view, rangeFill!!((view))))
                else -> canvas.clip(clipInfo.first) {
                    rect(rangeRect, rangeRect.height / 2, adjust(view, rangeFill!!((view))))
                }
            }
        }

        canvas.circle(Circle(handleRect.center, handleRect.width / 2), adjust(view, knobFill(view)))
    }

    private fun adjust(view: Slider<T>, fill: Paint) = if (view.enabled) fill else disabledPaintMapper(fill)
}