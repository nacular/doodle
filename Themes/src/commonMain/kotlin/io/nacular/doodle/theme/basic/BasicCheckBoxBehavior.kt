package io.nacular.doodle.theme.basic

import io.nacular.doodle.controls.buttons.CheckBox
import io.nacular.doodle.controls.theme.CheckRadioButtonBehavior
import io.nacular.doodle.core.Icon
import io.nacular.doodle.drawing.AffineTransform
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.Lightgray
import io.nacular.doodle.drawing.ColorFill
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.darker
import io.nacular.doodle.drawing.lighter
import io.nacular.doodle.geometry.ConvexPolygon
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size

/**
 * Created by Nicholas Eddy on 4/25/19.
 */
private class BasicCheckBoxIcon(
        private val foregroundColor    : Color,
        private val backgroundColor    : Color,
        private val darkBackgroundColor: Color,
        private val cornerRadius       : Double,
        private val checkInset         : Double,
        private val iconInset          : Double,
        private val hoverColorMapper   : ColorMapper,
        private val disabledColorMapper: ColorMapper): Icon<CheckBox> {

    override fun size(view: CheckBox) = Size(maxOf(0.0, minOf(view.height - 2.0, view.width - 2.0)))

    private fun fillColor(view: CheckBox): Color {
        val model       = view.model
        var fillColor   = when {
            model.pressed && model.armed -> darkBackgroundColor
            else                         -> backgroundColor
        }

        when {
            !view.enabled     -> fillColor = disabledColorMapper(fillColor)
            model.pointerOver -> fillColor = hoverColorMapper(fillColor)
        }

        return fillColor
    }

    override fun render(view: CheckBox, canvas: Canvas, at: Point) {
        val size = size(view)
        val rect = Rectangle(at, size(view)).inset(iconInset)
        val fill = ColorFill(fillColor(view))

        var updatedPoly = AffineTransform.Identity.scale(CHECK_POLY.points[0], (rect.width - checkInset) / CHECK_BOUNDING_BOX.width, (rect.height - checkInset) / CHECK_BOUNDING_BOX.height).invoke(CHECK_POLY)
        updatedPoly = AffineTransform.Identity.translate(rect.center - updatedPoly.boundingRectangle.center).invoke(updatedPoly)

        when {
            view.indeterminate -> {
                canvas.rect(rect, cornerRadius, fill)
                canvas.rect(Rectangle(at.x + 3, at.y + size.height / 2 - 1, size.width - 6, 2.0), cornerRadius, fill)
            }
            else               -> {
                canvas.rect(rect, cornerRadius, fill)

                if (view.model.selected) {
                    canvas.poly(updatedPoly, ColorFill(foregroundColor))
                }
            }
        }
    }

    companion object {
        val CHECK_POLY = ConvexPolygon(
                Point(3, 5),
                Point(5, 7),
                Point(9, 3),
                Point(9, 5),
                Point(5, 9),
                Point(3, 7)
        )

        val CHECK_BOUNDING_BOX = CHECK_POLY.boundingRectangle
    }
}

class BasicCheckBoxBehavior(
        textMetrics        : TextMetrics,
        foregroundColor    : Color = Black,
        backgroundColor    : Color = Lightgray,
        darkBackgroundColor: Color = backgroundColor.darker(),
        cornerRadius       : Double = 4.0,
        iconSpacing        : Double = 8.0,
        checkInset         : Double = 7.0,
        iconInset          : Double = 2.0,
        hoverColorMapper   : ColorMapper = { it.darker(0.1f) },
        disabledColorMapper: ColorMapper = { it.lighter()    }
): CheckRadioButtonBehavior<CheckBox>(
        textMetrics,
        foregroundColor,
        BasicCheckBoxIcon(
                foregroundColor     = foregroundColor,
                backgroundColor     = backgroundColor,
                darkBackgroundColor = darkBackgroundColor,
                cornerRadius        = cornerRadius,
                checkInset          = checkInset,
                iconInset           = iconInset,
                hoverColorMapper    = hoverColorMapper,
                disabledColorMapper = disabledColorMapper
        ),
        iconSpacing,
        disabledColorMapper
)