package io.nacular.doodle.theme.basic

import io.nacular.doodle.controls.range.Slider2
import io.nacular.doodle.controls.theme.SliderBehavior2
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Blue
import io.nacular.doodle.drawing.Color.Companion.Lightgray
import io.nacular.doodle.drawing.ColorPaint
import io.nacular.doodle.drawing.Paint
import io.nacular.doodle.drawing.lighter
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Circle
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.utils.Orientation.Horizontal
import io.nacular.doodle.utils.observable
import kotlin.math.max
import kotlin.math.min

public typealias BasicSliderBehavior = BasicSliderBehavior2<Double>

public class BasicSliderBehavior2<T>(
        private val barFill            : Paint,
        private val knobFill           : Paint,
                    grooveThicknessRatio: Float = 0.6f,
                    focusManager        : FocusManager? = null
): SliderBehavior2<T>(focusManager) where T: Number, T: Comparable<T> {
    public constructor(
            barColor            : Color = Lightgray,
            knobColor           : Color = Blue,
            grooveThicknessRatio: Float = 0.6f,
            focusManager        : FocusManager? = null): this(barColor.paint, knobColor.paint, grooveThicknessRatio, focusManager) {
                disabledPaintMapper = {
                    when (it) {
                        is ColorPaint -> it.color.lighter().paint
                        else          -> it
                    }
                }
            }

    private val grooveThicknessRatio = max(0f, min(1f, grooveThicknessRatio))

    private fun ColorMapper.toPaintMapper(): PaintMapper = {
        when (it) {
            is ColorPaint -> this(it.color).paint
            else          -> it
        }
    }

    @Deprecated("Slider now uses Paint for fills. Use disabledPaintMapper instead.")
    public var disabledColorMapper: ColorMapper by observable({ it }) { _,new ->
        disabledPaintMapper = new.toPaintMapper()
    }

    public var disabledPaintMapper: PaintMapper = {
        when (it) {
            is ColorPaint -> it.color.lighter().paint
            else          -> it
        }
    }

    override fun render(view: Slider2<T>, canvas: Canvas) {
        val rect1: Rectangle
        val rect2: Rectangle

        val barSize     = barSize(view)
        val offset      = barSize / 2
        val barPosition = barPosition(view)

        val grooveInset = (1 - grooveThicknessRatio) * when (view.orientation) {
            Horizontal -> view.height
            else       -> view.width
        }

        when (view.orientation) {
            Horizontal -> {
                rect1 = Rectangle(offset, grooveInset / 2, max(0.0, view.width - barSize), max(0.0, view.height - grooveInset))
                rect2 = Rectangle(barPosition, 0.0, barSize, barSize)
            }
            else   -> {
                rect1 = Rectangle(grooveInset / 2, offset, max(0.0, view.width - grooveInset), max(0.0, view.height - barSize))
                rect2 = Rectangle(0.0, barPosition, barSize, barSize)
            }
        }

        canvas.rect  (rect1, rect1.height / 2,               adjust(view, barFill ))
        canvas.circle(Circle(rect2.center, rect2.width / 2), adjust(view, knobFill))
    }

    private fun adjust(view: Slider2<T>, fill: Paint) = if (view.enabled) fill else disabledPaintMapper(fill)
}