package io.nacular.doodle.theme.basic

import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.theme.CommonTextButtonBehavior
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Lightgray
import io.nacular.doodle.drawing.Color.Companion.White
import io.nacular.doodle.drawing.ColorPaint
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.darker
import io.nacular.doodle.drawing.lighter
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.Insets
import kotlin.math.max


/**
 * Created by Nicholas Eddy on 3/17/18.
 */
public open class BasicButtonBehavior(
        private val textMetrics        : TextMetrics,
        private val backgroundColor    : Color         = Lightgray,
        private val darkBackgroundColor: Color         = backgroundColor.darker(),
        private val foregroundColor    : Color         = White,
        private val borderColor        : Color?        = null,
        private val borderWidth        : Double        = 0.0,
        private val cornerRadius       : Double        = 4.0,
                    insets             : Double        = 4.0,
                    focusManager       : FocusManager? = null): CommonTextButtonBehavior<Button>(textMetrics, focusManager = focusManager) {

    public var hoverColorMapper   : ColorMapper = { it.darker(0.1f) }
    public var disabledColorMapper: ColorMapper = { it.lighter()    }

    private var insets = Insets(insets)
    private val contentDirectionChanged: (source: View) -> Unit = {
        it.rerender()
    }

    protected class RenderColors(public val fillColor: Color, public val textColor: Color, public val borderColor: Color?)

    protected fun colors(view: Button): RenderColors {
        val model       = view.model
        var fillColor   = if (model.selected || model.pressed && model.armed) darkBackgroundColor else view.backgroundColor ?: backgroundColor
        var textColor   = view.foregroundColor ?: foregroundColor
        var borderColor = borderColor

        if (!view.enabled) {
            textColor   = disabledColorMapper(textColor)
            fillColor   = disabledColorMapper(fillColor)
            borderColor = borderColor?.let { disabledColorMapper(it) }
        } else if (model.pointerOver) {
            fillColor = hoverColorMapper(fillColor)
        }

        return RenderColors(fillColor, textColor, borderColor)
    }

    override fun render(view: Button, canvas: Canvas) {
        val model  = view.model
        val colors = colors(view)

        val penWidth = if (view.enabled && (model.pressed || model.pointerOver)) 2 * borderWidth else borderWidth

        when {
            penWidth > 0 && colors.borderColor != null -> canvas.rect(Rectangle(size = view.size).inset(penWidth / 2), cornerRadius, Stroke(colors.borderColor, penWidth), colors.fillColor.paint)
            else                                       -> canvas.rect(Rectangle(size = view.size),                     cornerRadius,                                       colors.fillColor.paint)
        }

        val icon = icon(view)
        val text = view.text
        var textPosition: Point? = null

        if (text.isNotBlank()) {
            textPosition = textPosition(view, icon = icon)

            when {
                view.mirrored -> canvas.flipHorizontally(around = textPosition.x + textMetrics.width(view.text, view.font) / 2) {
                    text(text, font(view), textPosition, ColorPaint(colors.textColor))
                }
                else          -> canvas.text(text, font(view), textPosition, ColorPaint(colors.textColor))
            }
        }

        icon?.let {
            val iconPosition = when (textPosition) {
                null -> iconPosition(view, icon = it)
                else -> iconPosition(view, icon = it, stringPosition = textPosition)
            }

            when {
                view.mirrored -> canvas.flipHorizontally(around = iconPosition.x + icon.size(view).width / 2) {
                    it.render(view, this, iconPosition)
                }
                else          -> it.render(view, canvas, iconPosition)
            }
        }
    }

    override fun stylesChanged(button: Button) {
        recalculateSize(button)

        super.stylesChanged(button)
    }

    override fun install(view: Button) {
        super.install(view)

        view.contentDirectionChanged += contentDirectionChanged
        recalculateSize(view)
    }

    override fun uninstall(view: Button) {
        super.uninstall(view)

        view.contentDirectionChanged -= contentDirectionChanged
    }

    private fun recalculateSize(button: Button) {
        val icon   = icon(button)
        var size   = textMetrics.size(button.text, font(button))
        var width  = size.width
        var height = size.height

        if (icon != null) {
            val iconSize  = icon.size(button)
            val iconWidth = iconSize.width

            if (iconWidth > 0) {
                width += button.iconTextSpacing
            }

            width  += iconWidth
            height  = max(height, iconSize.height)
        }

        size = Size(width + insets.left + insets.right, height + insets.top + insets.bottom)

        button.idealSize = size

//        button.setSize(size)
    }
}
