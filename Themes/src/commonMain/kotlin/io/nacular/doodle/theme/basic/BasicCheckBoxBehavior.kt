package io.nacular.doodle.theme.basic

import io.nacular.doodle.controls.buttons.CheckBox
import io.nacular.doodle.controls.theme.CheckRadioButtonBehavior
import io.nacular.doodle.core.Icon
import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.Lightgray
import io.nacular.doodle.drawing.ColorPaint
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.darker
import io.nacular.doodle.drawing.lighter
import io.nacular.doodle.drawing.paint
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
        private val checkInset         : (CheckBox) -> Float,
        private val size_              : (CheckBox) -> Size,
        private val hoverColorMapper   : ColorMapper,
        private val disabledColorMapper: ColorMapper
): Icon<CheckBox> {

    override fun size(view: CheckBox) = size_(view) //Size(maxOf(0.0, minOf(view.height * (1 - iconInset), view.width * (1 - iconInset))))

    private fun fillColor(view: CheckBox): Color {
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

    override fun render(view: CheckBox, canvas: Canvas, at: Point) {
        val rect       = Rectangle(at, size(view))
        val background = ColorPaint(fillColor(view))

        val xScale = (rect.width  * (1 - checkInset(view))) / CHECK_BOUNDING_BOX.width
        val yScale = (rect.height * (1 - checkInset(view))) / CHECK_BOUNDING_BOX.height

        var updatedPoly = Identity.scale(CHECK_POLY.points[0], xScale, yScale).invoke(CHECK_POLY)
        updatedPoly     = Identity.translate(rect.center - updatedPoly.boundingRectangle.center).invoke(updatedPoly)

        canvas.rect(rect, cornerRadius, background)

        val foreground = when {
            view.enabled -> foregroundColor.paint
            else         -> disabledColorMapper(foregroundColor).paint
        }

        when {
            view.indeterminate  -> {
                val updatedSize = INDETERMINATE_RECT_SIZE.run { Size(width * xScale, height * yScale) }
                canvas.rect(Rectangle(
                        position = rect.center - updatedSize.run { Point(width / 2, height / 2) },
                        size     = updatedSize
                ), cornerRadius, foreground)
            }
            view.model.selected -> canvas.poly(updatedPoly, foreground)
        }
    }

    companion object {
        // This isn't really a convex polygon, but none of the operations done to it cause issues
        private val CHECK_POLY = ConvexPolygon(
                Point(3, 5),
                Point(5, 7),
                Point(9, 3),
                Point(9, 5),
                Point(5, 9),
                Point(3, 7)
        )

        private val CHECK_BOUNDING_BOX = CHECK_POLY.boundingRectangle

        private val INDETERMINATE_RECT_SIZE = Size(7.0, 1.5)
    }
}

public class BasicCheckBoxBehavior private constructor(
        textMetrics        : TextMetrics,
        foregroundColor    : Color,
        backgroundColor    : Color,
        darkBackgroundColor: Color,
        cornerRadius       : Double,
        iconSpacing        : Double,
        checkInset         : (CheckBox) -> Float,
        size               : (CheckBox) -> Size = { Size(maxOf(0.0, minOf(16.0, it.height - 2.0, it.width - 2.0))) },
        hoverColorMapper   : ColorMapper,
        disabledColorMapper: ColorMapper
): CheckRadioButtonBehavior<CheckBox>(
        textMetrics,
        foregroundColor,
        BasicCheckBoxIcon(
                foregroundColor     = foregroundColor,
                backgroundColor     = backgroundColor,
                darkBackgroundColor = darkBackgroundColor,
                cornerRadius        = cornerRadius,
                checkInset          = checkInset,
                size_               = size,
                hoverColorMapper    = hoverColorMapper,
                disabledColorMapper = disabledColorMapper
        ),
        iconSpacing,
        disabledColorMapper
) {
    @Deprecated("Use constructor with size instead")
    public constructor(
            textMetrics        : TextMetrics,
            foregroundColor    : Color       = Black,
            backgroundColor    : Color       = Lightgray,
            darkBackgroundColor: Color       = backgroundColor.darker(),
            cornerRadius       : Double      = 4.0,
            iconSpacing        : Double      = 8.0,
            checkInset         : Float       = 0.5f,
            iconInset          : Float       = 0.0f,
            hoverColorMapper   : ColorMapper = { it.darker(0.1f) },
            disabledColorMapper: ColorMapper = { it.lighter() }) : this(
            textMetrics         = textMetrics,
            foregroundColor     = foregroundColor,
            backgroundColor     = backgroundColor,
            darkBackgroundColor = darkBackgroundColor,
            cornerRadius        = cornerRadius,
            iconSpacing         = iconSpacing,
            checkInset          = { checkInset },
            size                = { Size(maxOf(0.0, minOf(it.height * (1 - iconInset), it.width * (1 - iconInset)))) },
            hoverColorMapper    = hoverColorMapper,
            disabledColorMapper = disabledColorMapper
    )

    override fun mirrorWhenRightToLeft(view: CheckBox): Boolean = false

    public companion object {
        public operator fun invoke(
            textMetrics        : TextMetrics,
            foregroundColor    : Color = Black,
            backgroundColor    : Color = Lightgray,
            darkBackgroundColor: Color = backgroundColor.darker(),
            cornerRadius       : Double = 4.0,
            iconSpacing        : Double = 8.0,
            checkInset         : Float = 0.5f,
            iconInset          : Float = 0.0f,
            hoverColorMapper   : ColorMapper = { it.darker(0.1f) },
            disabledColorMapper: ColorMapper = { it.lighter() }
        ): BasicCheckBoxBehavior = BasicCheckBoxBehavior(
                textMetrics,
                foregroundColor,
                backgroundColor,
                darkBackgroundColor,
                cornerRadius,
                iconSpacing,
                checkInset,
                iconInset,
                hoverColorMapper,
                disabledColorMapper
        )

        public operator fun invoke(
            textMetrics        : TextMetrics,
            foregroundColor    : Color  = Black,
            backgroundColor    : Color  = Lightgray,
            darkBackgroundColor: Color  = backgroundColor.darker(),
            cornerRadius       : Double = 4.0,
            iconSpacing        : Double = 8.0,
            checkInset         : (CheckBox) -> Float = { 0.5f },
            size               : (CheckBox) -> Size = { Size(maxOf(0.0, minOf(16.0, it.height - 2.0, it.width - 2.0))) },
            hoverColorMapper   : ColorMapper = { it.darker(0.1f) },
            disabledColorMapper: ColorMapper = { it.lighter()    }
        ): BasicCheckBoxBehavior = BasicCheckBoxBehavior(
                textMetrics,
                foregroundColor,
                backgroundColor,
                darkBackgroundColor,
                cornerRadius,
                iconSpacing,
                checkInset,
                size,
                hoverColorMapper,
                disabledColorMapper
        )
    }
}