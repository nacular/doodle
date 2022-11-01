package io.nacular.doodle.theme.basic

import io.nacular.doodle.controls.buttons.RadioButton
import io.nacular.doodle.controls.theme.CheckRadioButtonBehavior
import io.nacular.doodle.core.Icon
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.Lightgray
import io.nacular.doodle.drawing.ColorPaint
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.darker
import io.nacular.doodle.drawing.lighter
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Circle
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Size
import kotlin.math.min

/**
 * Created by Nicholas Eddy on 4/25/19.
 */
private class BasicRadioIcon(
        private val foregroundColor    : Color,
        private val backgroundColor    : Color,
        private val darkBackgroundColor: Color,
        private val innerCircleInset   : (RadioButton) -> Double,
        private val size_              : (RadioButton) -> Size,
        private val hoverColorMapper   : ColorMapper,
        private val disabledColorMapper: ColorMapper): Icon<RadioButton> {

    override fun size(view: RadioButton): Size = size_(view)

    private fun fillColor(view: RadioButton): Color {
        val model       = view.model
        var fillColor   = when {
            model.pressed && model.armed -> darkBackgroundColor
            else                         -> backgroundColor
        }

        when {
            !view.enabled     -> fillColor = disabledColorMapper(fillColor)
            model.pointerOver -> fillColor = hoverColorMapper   (fillColor)
        }

        return fillColor
    }

    override fun render(view: RadioButton, canvas: Canvas, at: Point) {
        val size   = size(view)
        val radius = min(size.width, size.height) / 2
        val fill   = ColorPaint(fillColor(view))

        canvas.circle(Circle(at + Point(radius, radius), radius), fill)

        if (view.model.selected) {
            canvas.circle(Circle(at + Point(radius, radius), radius - innerCircleInset(view)), ColorPaint(foregroundColor))
        }
    }
}

public class BasicRadioBehavior(
        textMetrics        : TextMetrics,
        foregroundColor    : Color                   = Black,
        backgroundColor    : Color                   = Lightgray,
        darkBackgroundColor: Color                   = backgroundColor.darker(),
        iconTextSpacing    : Double                  = 8.0,
        iconInset          : Double                  = 1.0,
        innerCircleInset   : (RadioButton) -> Double = { 4.0 },
        iconSize           : (RadioButton) -> Size   = { Size(maxOf(0.0, minOf(16.0, it.height - 2 * iconInset, it.width - 2 * iconInset))) },
        hoverColorMapper   : ColorMapper             = { it.darker(0.1f) },
        disabledColorMapper: ColorMapper             = { it.lighter()    },
        focusManager       : FocusManager?           = null,
): CheckRadioButtonBehavior<RadioButton>(
        textMetrics,
        foregroundColor,
        BasicRadioIcon(
                foregroundColor     = foregroundColor,
                backgroundColor     = backgroundColor,
                darkBackgroundColor = darkBackgroundColor,
                innerCircleInset    = innerCircleInset,
                size_               = iconSize,
                hoverColorMapper    = hoverColorMapper,
                disabledColorMapper = disabledColorMapper
        ),
        iconTextSpacing,
        iconInset,
        disabledColorMapper,
        focusManager
)