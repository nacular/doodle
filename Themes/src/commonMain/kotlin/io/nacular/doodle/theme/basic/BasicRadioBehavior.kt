package io.nacular.doodle.theme.basic

import io.nacular.doodle.controls.buttons.RadioButton
import io.nacular.doodle.controls.theme.CheckRadioButtonBehavior
import io.nacular.doodle.core.Icon
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.Lightgray
import io.nacular.doodle.drawing.ColorFill
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.darker
import io.nacular.doodle.drawing.lighter
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
        private val innerCircleInset   : Double,
        private val hoverColorMapper   : ColorMapper,
        private val disabledColorMapper: ColorMapper): Icon<RadioButton> {

    override fun size(view: RadioButton) = Size(maxOf(0.0, minOf(view.height - 2.0, view.width - 2.0)))

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
        val fill   = ColorFill(fillColor(view))

        canvas.circle(Circle(at + Point(radius, radius), radius), fill)

        if (view.model.selected) {
            canvas.circle(Circle(at + Point(radius, radius), radius - innerCircleInset), ColorFill(foregroundColor))
        }
    }
}

public class BasicRadioBehavior(
        textMetrics        : TextMetrics,
        foregroundColor    : Color = Black,
        backgroundColor    : Color = Lightgray,
        darkBackgroundColor: Color = backgroundColor.darker(),
        iconSpacing        : Double = 8.0,
        innerCircleInset   : Double = 4.0,
        hoverColorMapper   : ColorMapper = { it.darker(0.1f) },
        disabledColorMapper: ColorMapper = { it.lighter()    }
): CheckRadioButtonBehavior<RadioButton>(
        textMetrics,
        foregroundColor,
        BasicRadioIcon(
                foregroundColor     = foregroundColor,
                backgroundColor     = backgroundColor,
                darkBackgroundColor = darkBackgroundColor,
                innerCircleInset    = innerCircleInset,
                hoverColorMapper    = hoverColorMapper,
                disabledColorMapper = disabledColorMapper
        ),
        iconSpacing,
        disabledColorMapper
)