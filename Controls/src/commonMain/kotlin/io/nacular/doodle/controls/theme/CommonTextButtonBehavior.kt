package io.nacular.doodle.controls.theme

import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.core.Icon
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.utils.Anchor
import io.nacular.doodle.utils.HorizontalAlignment.Center
import io.nacular.doodle.utils.HorizontalAlignment.Left
import io.nacular.doodle.utils.HorizontalAlignment.Right
import io.nacular.doodle.utils.VerticalAlignment.Bottom
import io.nacular.doodle.utils.VerticalAlignment.Middle
import io.nacular.doodle.utils.VerticalAlignment.Top
import kotlin.math.max
import kotlin.math.min

/**
 * Created by Nicholas Eddy on 10/3/18.
 */
abstract class CommonTextButtonBehavior<T: Button>(
        private val textMetrics: TextMetrics,
        private val defaultFont: Font?  = null,
        private val insets     : Insets = Insets.None): CommonButtonBehavior<T>() {


    protected open val textChanged: (Button, String, String) -> Unit = { button,_,_ ->
        button.rerender()
    }

    override fun install(view: T) {
        super.install(view)

        view.textChanged += textChanged
    }

    override fun uninstall(view: T) {
        super.uninstall(view)

        view.textChanged -= textChanged
    }

    protected fun textPosition(button: Button, text: String = button.text, icon: Icon<Button>? = button.icon, bounds: Rectangle = button.bounds.atOrigin): Point {
        var minX       = insets.left
        val stringSize = textMetrics.size(text, font(button))
        var iconWidth  = 0.0
        var maxX       = bounds.width - stringSize.width - insets.right

        icon?.let {
            iconWidth = it.size(button).width + button.iconTextSpacing

            when (button.iconAnchor) {
                Anchor.Left,  Anchor.Leading  -> minX += iconWidth
                Anchor.Right, Anchor.Trailing -> maxX -= iconWidth
            }
        }

        val x = when (button.horizontalAlignment) {
            Right  -> max(maxX, minX)
            Center -> {
                val iconOffset = when (button.iconAnchor) {
                    Anchor.Leading -> iconWidth
                    else           -> 0.0
                }

                max(minX, min(maxX, (bounds.width - (stringSize.width + iconWidth)) / 2 + iconOffset))
            }
            Left   -> minX
        }

        val y = when (button.verticalAlignment) {
            Bottom -> bounds.height - insets.bottom
            Middle -> max(insets.top, min(bounds.height - insets.bottom, (bounds.height - stringSize.height) / 2))
            Top    -> insets.top
        }

        return Point(x, y) + bounds.position
    }

    protected fun iconPosition(button: Button, text: String = button.text, icon: Icon<Button>, stringPosition: Point = textPosition(button, text, icon), bounds: Rectangle = button.bounds.atOrigin): Point {
        val size = icon.size(button)

        val y = when (button.verticalAlignment) {
            Bottom -> bounds.height - insets.bottom
            Middle -> max(insets.top, min(bounds.height - insets.bottom, (bounds.height - size.height) / 2))
            Top    -> insets.top
        }

        val minX        = insets.left
        val maxX        = bounds.width - size.width - insets.right
        val stringWidth = textMetrics.width(text, font(button))

        val x = when (button.iconAnchor) {
            Anchor.Leading ->
                if (stringWidth > 0) {
                    max(minX, stringPosition.x - size.width - button.iconTextSpacing)
                } else {
                    max(minX, min(maxX, (bounds.width - size.width) / 2))
                }

            Anchor.Right ->
                if (stringWidth > 0) {
                    max(maxX, stringPosition.x + stringWidth + button.iconTextSpacing)
                } else {
                    max(maxX, minX)
                }

            Anchor.Trailing ->
                if (stringWidth > 0) {
                    stringPosition.x + stringWidth + button.iconTextSpacing
                } else {
                    max(minX, min(maxX, (bounds.width - size.width) / 2))
                }
            else -> minX
        }

        return Point(x, y) + bounds.position
    }

    protected fun font(button: Button) = button.font ?: defaultFont
}
