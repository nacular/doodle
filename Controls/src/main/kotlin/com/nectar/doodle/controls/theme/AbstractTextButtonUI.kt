package com.nectar.doodle.controls.theme

import com.nectar.doodle.controls.buttons.Button
import com.nectar.doodle.core.Icon
import com.nectar.doodle.drawing.Font
import com.nectar.doodle.drawing.TextMetrics
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.layout.Insets
import com.nectar.doodle.utils.HorizontalAlignment
import com.nectar.doodle.utils.VerticalAlignment
import kotlin.math.max
import kotlin.math.min

/**
 * Created by Nicholas Eddy on 10/3/18.
 */
abstract class AbstractTextButtonUI(
        private val textMetrics: TextMetrics,
        private val defaultFont: Font?  = null,
        private val insets     : Insets = Insets.None): AbstractButtonUI() {

    protected fun textPosition(button: Button, icon: Icon<Button>? = null, bounds: Rectangle = button.bounds): Point {
        val minX       = insets.left
        val stringSize = textMetrics.size(button.text, font(button))
        val maxX       = bounds.width - stringSize.width - insets.right

//        icon?.let {
//            when (button.getIconAnchor()) {
//                Left, LEADING   -> minX += it.size.width + button.iconTextSpacing
//                RIGHT, TRAILING -> maxX -= it.size.width + button.iconTextSpacing
//            }
//        }

        val x = when (button.horizontalAlignment) {
            HorizontalAlignment.Right  -> max(maxX, minX)
            HorizontalAlignment.Center -> max(minX, min(maxX, (bounds.width - stringSize.width) / 2))
            HorizontalAlignment.Left   -> minX
        }

        val y = when (button.verticalAlignment) {
            VerticalAlignment.Bottom -> bounds.height - insets.bottom
            VerticalAlignment.Middle -> max(insets.top, min(bounds.height - insets.bottom, (bounds.height - stringSize.height) / 2))
            VerticalAlignment.Top    -> insets.top
        }

        return Point(x, y)
    }

    protected fun iconPosition(button: Button, icon: Icon<Button>, stringPosition: Point = textPosition(button, icon), bounds: Rectangle = button.bounds): Point {
        val x = insets.left
        val y = when (button.verticalAlignment) {
            VerticalAlignment.Bottom -> bounds.height - insets.bottom
            VerticalAlignment.Middle -> max(insets.top, min(bounds.height - insets.bottom, (bounds.height - icon.size.height) / 2))
            VerticalAlignment.Top    -> insets.top
            else                     -> insets.top
        }

//        val minX        = insets.left
//        val maxX        = bounds.width - icon.size.width - insets.right
//        val stringWidth = font(button)!!.getStringWidth(button.text)

//        when (button.getIconAnchor()) {
//            LEADING  ->
//
//                if (stringWidth > 0) {
//                    x = max(minX, stringPosition.getX() - icon!!.width - button.iconTextSpacing)
//                } else {
//                    x = max(minX, min(maxX, (bounds.width - icon!!.width) / 2))
//                }
//
//            RIGHT    ->
//
//                if (stringWidth > 0) {
//                    x = max(maxX, stringPosition.getX() + stringWidth + button.iconTextSpacing)
//                } else {
//                    x = max(maxX, minX)
//                }
//
//            TRAILING ->
//
//                if (stringWidth > 0) {
//                    x = stringPosition.getX() + stringWidth + button.iconTextSpacing
//                } else {
//                    x = max(minX, min(maxX, (bounds.width - icon!!.width) / 2))
//                }
//        }

        return Point(x, y)
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
