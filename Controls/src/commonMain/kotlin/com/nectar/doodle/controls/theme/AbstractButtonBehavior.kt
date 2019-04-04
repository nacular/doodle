package com.nectar.doodle.controls.theme

import com.nectar.doodle.controls.buttons.Button
import com.nectar.doodle.core.View
import com.nectar.doodle.event.KeyListener
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.event.MouseListener
import com.nectar.doodle.system.SystemMouseEvent.Button.Button1
import com.nectar.doodle.theme.Behavior


abstract class AbstractButtonBehavior: Behavior<Button>, MouseListener, KeyListener {

    private val enabledChanged: (View, Boolean, Boolean) -> Unit = { view,_,_ ->
        enabledChanged(view as Button)
    }

    override fun install(view: Button) {
//        view.addKeyListener(this)
        view.mouseChanged   += this
        view.enabledChanged += enabledChanged

        view.rerender()
        // TODO: Handle changes to the model from other places
    }

    override fun uninstall(view: Button) {
//        view.removeKeyListener(this)
        view.mouseChanged   -= this
        view.enabledChanged -= enabledChanged
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

    protected open fun mouseChanged  (button: Button) = button.rerender()
    protected open fun enabledChanged(button: Button) = button.rerender()
}
