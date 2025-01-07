package io.nacular.doodle.controls.theme

import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.buttons.ToggleButton
import io.nacular.doodle.core.Icon
import io.nacular.doodle.core.View
import io.nacular.doodle.core.fixed
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.ColorPaint
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.lighter
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.utils.Anchor
import io.nacular.doodle.utils.HorizontalAlignment.Left
import kotlin.math.max

/**
 * Created by Nicholas Eddy on 4/25/19.
 */
public open class CheckRadioButtonBehavior<T: ToggleButton> protected constructor(
    private val textMetrics        : TextMetrics,
    private val textColor          : Color,
    private val icon               : Icon<T>,
    private val iconTextSpacing    : Double = 2.0,
    private val iconInset          : Double = 2.0,
    private val disabledColorMapper: (Color) -> Color = { it.lighter() },
                focusManager       : FocusManager? = null
): CommonTextButtonBehavior<T>(textMetrics, focusManager = focusManager) {

    private val contentDirectionChanged: (source: View) -> Unit = { it.rerender() }

    protected val insets: Insets = Insets()

    override fun render(view: T, canvas: Canvas) {
        val icon         = icon(view)
        val textColor    = if (view.enabled) textColor else disabledColorMapper(textColor)
        val textPosition = textPosition(view, icon = icon)

        when {
            view.mirrored -> canvas.flipHorizontally(around = textPosition.x + textMetrics.width(view.text, view.font) / 2) {
                text(view.text, font(view), textPosition, ColorPaint(textColor))
            }
            else          -> canvas.text(view.text, font(view), textPosition, ColorPaint(textColor))
        }

        icon?.let {
            @Suppress("UNCHECKED_CAST") val iconPosition = iconPosition(view, icon = this.icon as Icon<Button>)
            when {
                view.mirrored -> canvas.flipHorizontally(around = iconPosition.x + icon.size(view).width / 2) {
                    icon.render(view, canvas, iconPosition(view, icon = this@CheckRadioButtonBehavior.icon))
                }
                else          -> icon.render(view, canvas, iconPosition(view, icon = this.icon))
            }
        }
    }

    override fun install(view: T) {
        super.install(view)

        val iconSize    = icon.size(view)
        val textSize    = textMetrics.size(view.text, font(view))
        val idealWidth  = iconSize.width + 2 * iconInset + if (textSize.width > 0) iconTextSpacing + textSize.width else 0.0

        @Suppress("UNCHECKED_CAST")
        view.icon                     = icon as Icon<Button>
        view.iconAnchor               = Anchor.Left
        view.iconTextSpacing          = iconTextSpacing
        view.horizontalAlignment      = Left
        view.contentDirectionChanged += contentDirectionChanged

        Size(idealWidth, max(iconSize.height + 2 * iconInset, if (!textSize.empty) textSize.height else 0.0)).let {
            view.suggestSize(it)
            view.preferredSize = fixed(it)
        }
    }

    override fun uninstall(view: T) {
        super.uninstall(view)

        view.contentDirectionChanged -= contentDirectionChanged
    }
}