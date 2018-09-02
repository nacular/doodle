package com.nectar.doodle.controls.theme

import com.nectar.doodle.controls.buttons.Button
import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.core.Icon
import com.nectar.doodle.drawing.Font
import com.nectar.doodle.drawing.TextMetrics
import com.nectar.doodle.event.KeyListener
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.event.MouseListener
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.layout.Insets
import com.nectar.doodle.system.SystemMouseEvent.Button.Button1
import com.nectar.doodle.theme.Renderer
import com.nectar.doodle.utils.HorizontalAlignment.Center
import com.nectar.doodle.utils.HorizontalAlignment.Left
import com.nectar.doodle.utils.HorizontalAlignment.Right
import com.nectar.doodle.utils.VerticalAlignment.Bottom
import com.nectar.doodle.utils.VerticalAlignment.Middle
import com.nectar.doodle.utils.VerticalAlignment.Top
import kotlin.math.max
import kotlin.math.min


abstract class AbstractButtonUI(
        private val textMetrics: TextMetrics,
        private val defaultFont: Font?  = null,
        private val insets     : Insets = Insets.None): Renderer<Button>, MouseListener, KeyListener {

    private val enabledChanged: (Gizmo, Boolean, Boolean) -> Unit = { gizmo,_,_ ->
        enabledChanged(gizmo as Button)
    }

    override fun install(gizmo: Button) {
//        gizmo.addKeyListener(this)
        gizmo.mouseChanged   += this
        gizmo.enabledChanged += enabledChanged

        gizmo.rerender()
        // TODO: Handle changes to the model from other places
    }

    override fun uninstall(gizmo: Button) {
//        gizmo.removeKeyListener(this)
        gizmo.mouseChanged   -= this
        gizmo.enabledChanged -= enabledChanged
    }

//    fun keyTyped(aKeyEvent: KeyEvent) {}
//
//    fun keyReleased(aKeyEvent: KeyEvent) {
//        val button = aKeyEvent.source as Button
//        val aKeyCode = aKeyEvent.getKeyCode()
//
//        if (button.enabled && (aKeyCode == KeyEvent.VK_RETURN || aKeyCode == KeyEvent.VK_SPACE)) {
//            val model = button.model
//
//            model.setPressed(false)
//            model.setArmed(false)
//        }
//    }
//
//    fun keyPressed(aKeyEvent: KeyEvent) {
//        val button = aKeyEvent.source as Button
//        val aKeyCode = aKeyEvent.getKeyCode()
//
//        if (button.enabled && (aKeyCode == KeyEvent.VK_RETURN || aKeyCode == KeyEvent.VK_SPACE)) {
//            val model = button.model
//
//            model.setArmed(true)
//            model.setPressed(true)
//        }
//    }

    override fun mouseExited(event: MouseEvent) {
        val button = event.source as Button
        val model  = button.model

        model.mouseOver = false

        if (button.enabled) {
            model.armed = false
        }

        mouseChanged(button)
    }

    override fun mouseEntered(event: MouseEvent) {
        val button = event.source as Button
        val model  = button.model

        model.mouseOver = true

        if (button.enabled && event.buttons == setOf(Button1) && model.pressed) {
            model.armed = true
        }

        mouseChanged(button)
    }

    override fun mousePressed(event: MouseEvent) {
        val button = event.source as Button
        val model  = button.model

        if (button.enabled && event.buttons == setOf(Button1)) {
            model.armed   = true
            model.pressed = true

            mouseChanged(button)
        }
    }

    override fun mouseReleased(event: MouseEvent) {
        val button = event.source as Button
        val model  = button.model

        if (button.enabled && Button1 !in event.buttons) {
            model.pressed = false
            model.armed   = false

            mouseChanged(button)
        }
    }

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
            Right  -> max(maxX, minX)
            Center -> max(minX, min(maxX, (bounds.width - stringSize.width) / 2))
            Left   -> minX
        }

        val y = when (button.verticalAlignment) {
            Bottom -> bounds.height - insets.bottom
            Middle -> max(insets.top, min(bounds.height - insets.bottom, (bounds.height - stringSize.height) / 2))
            Top    -> insets.top
        }

        return Point(x, y)
    }

    protected fun iconPosition(button: Button, icon: Icon<Button>, stringPosition: Point = textPosition(button, icon), bounds: Rectangle = button.bounds): Point {
        val x = insets.left
        val y = when (button.verticalAlignment) {
            Bottom -> bounds.height - insets.bottom
            Middle -> max(insets.top, min(bounds.height - insets.bottom, (bounds.height - icon.size.height) / 2))
            Top    -> insets.top
            else   -> insets.top
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

    protected open fun mouseChanged  (button: Button) = button.rerender()
    protected open fun enabledChanged(button: Button) = button.rerender()
}
