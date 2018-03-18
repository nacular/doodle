package com.nectar.doodle.theme.system

import com.nectar.doodle.controls.buttons.Button
import com.nectar.doodle.controls.buttons.ButtonRenderer
import com.nectar.doodle.event.KeyListener
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.event.MouseListener
import com.nectar.doodle.system.SystemMouseEvent.Button.Button1


abstract class AbstractButtonUI(button: Button): ButtonRenderer, MouseListener, KeyListener {
    init {
//        button.addKeyListener(this)
        button.mouseChanged += this
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
    }

    override fun mouseEntered(event: MouseEvent) {
        val button = event.source as Button
        val model  = button.model

        model.mouseOver = true

        if (button.enabled && event.buttons == setOf(Button1) && model.pressed) {
            model.armed = true
        }
    }

    override fun mousePressed(event: MouseEvent) {
        val button = event.source as Button
        val model  = button.model

        if (button.enabled && event.buttons == setOf(Button1)) {
            model.armed   = true
            model.pressed = true
        }
    }

    override fun mouseReleased(event: MouseEvent) {
        val button = event.source as Button
        val model  = button.model

        if (button.enabled && event.buttons == setOf(Button1)) {
            model.pressed = false
            model.armed   = false
        }
    }
}
