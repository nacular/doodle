package com.nectar.doodle.controls.theme

import com.nectar.doodle.controls.buttons.Button
import com.nectar.doodle.core.Icon
import com.nectar.doodle.core.height
import com.nectar.doodle.core.width
import com.nectar.doodle.drawing.Font
import com.nectar.doodle.drawing.TextMetrics
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.layout.Insets
import com.nectar.doodle.utils.Anchor
import com.nectar.doodle.utils.HorizontalAlignment.Center
import com.nectar.doodle.utils.HorizontalAlignment.Left
import com.nectar.doodle.utils.HorizontalAlignment.Right
import com.nectar.doodle.utils.VerticalAlignment.Bottom
import com.nectar.doodle.utils.VerticalAlignment.Middle
import com.nectar.doodle.utils.VerticalAlignment.Top
import kotlin.math.max
import kotlin.math.min

/**
 * Created by Nicholas Eddy on 10/3/18.
 */
abstract class AbstractTextButtonBehavior<T: Button>(
        private val textMetrics: TextMetrics,
        private val defaultFont: Font?  = null,
        private val insets     : Insets = Insets.None): AbstractButtonBehavior<T>() {

    protected fun textPosition(button: Button, text: String = button.text, icon: Icon<Button>? = button.icon, bounds: Rectangle = button.bounds.atOrigin): Point {
        var minX       = insets.left
        val stringSize = textMetrics.size(text, font(button))
        var maxX       = bounds.width - stringSize.width - insets.right

        icon?.let {
            when (button.iconAnchor) {
                Anchor.Left,  Anchor.Leading  -> minX += it.width + button.iconTextSpacing
                Anchor.Right, Anchor.Trailing -> maxX -= it.width + button.iconTextSpacing
            }
        }

        val x = when (button.horizontalAlignment) {
            Right  -> max(maxX, minX)
            Center -> max(minX, min(maxX, (bounds.width - stringSize.width) / 2))
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
        val y = when (button.verticalAlignment) {
            Bottom -> bounds.height - insets.bottom
            Middle -> max(insets.top, min(bounds.height - insets.bottom, (bounds.height - icon.height) / 2))
            Top    -> insets.top
        }

        val minX        = insets.left
        val maxX        = bounds.width - icon.width - insets.right
        val stringWidth = textMetrics.width(text, font(button))

        val x = when (button.iconAnchor) {
            Anchor.Leading ->
                if (stringWidth > 0) {
                    max(minX, stringPosition.x - icon.width - button.iconTextSpacing)
                } else {
                    max(minX, min(maxX, (bounds.width - icon.width) / 2))
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
                    max(minX, min(maxX, (bounds.width - icon.width) / 2))
                }
            else -> insets.left
        }

        return Point(x, y) + bounds.position
    }

    protected fun font(button: Button) = button.font ?: defaultFont

    protected fun icon(button: Button): Icon<Button>? {
        val model = button.model

        return when {
            !button.enabled -> if (model.selected) button.disabledSelectedIcon else button.disabledIcon
            model.pressed   -> button.pressedIcon
            model.selected  -> button.selectedIcon
            model.mouseOver -> if (model.selected) button.mouseOverSelectedIcon else button.mouseOverIcon
            else            -> button.icon
        }
    }
}
