package com.nectar.doodle.controls.theme

import com.nectar.doodle.controls.buttons.Button
import com.nectar.doodle.core.View
import com.nectar.doodle.event.KeyEvent
import com.nectar.doodle.event.KeyEvent.Companion.VK_RETURN
import com.nectar.doodle.event.KeyEvent.Companion.VK_SPACE
import com.nectar.doodle.event.KeyListener
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.event.MouseListener
import com.nectar.doodle.event.MouseMotionListener
import com.nectar.doodle.system.SystemMouseEvent.Button.Button1
import com.nectar.doodle.theme.Behavior


abstract class AbstractButtonBehavior<T: Button>: Behavior<T>, MouseListener, MouseMotionListener, KeyListener {

    private val enabledChanged: (View, Boolean, Boolean) -> Unit = { button,_,_ ->
        enabledChanged(button as T)
    }

    private val stylesChanged: (View) -> Unit = {
        it.rerender()
    }

    override fun install(view: T) {
        view.keyChanged         += this
        view.mouseChanged       += this
        view.styleChanged       += stylesChanged
        view.enabledChanged     += enabledChanged
        view.mouseMotionChanged += this

        view.rerender()
        // TODO: Handle changes to the model from other places
    }

    override fun uninstall(view: T) {
        view.keyChanged         -= this
        view.mouseChanged       -= this
        view.styleChanged       -= stylesChanged
        view.enabledChanged     -= enabledChanged
        view.mouseMotionChanged -= this
    }

//    fun keyTyped(aKeyEvent: KeyEvent) {}
//
    override fun keyReleased(event: KeyEvent) {
        val button  = event.source as Button
        val keyCode = event.code

        if (button.enabled && (keyCode == VK_RETURN || keyCode == VK_SPACE)) {
            button.model.apply {
                pressed = false
                armed   = false
            }
        }
    }

    override fun keyPressed(event: KeyEvent) {
        val button  = event.source as Button
        val keyCode = event.code

        if (button.enabled && (keyCode == VK_RETURN || keyCode == VK_SPACE)) {
            button.model.apply {
                armed   = true
                pressed = true
            }
        }
    }

    override fun mouseExited(event: MouseEvent) {
        val button = event.source as T
        val model  = button.model

        model.mouseOver = false

        if (button.enabled) {
            model.armed = false
        }

        mouseChanged(button)
    }

    override fun mouseEntered(event: MouseEvent) {
        val button = event.source as T
        val model  = button.model

        model.mouseOver = true

        if (button.enabled && event.buttons == setOf(Button1) && model.pressed) {
            model.armed = true
        }

        mouseChanged(button)
    }

    override fun mousePressed(event: MouseEvent) {
        val button = event.source as T
        val model  = button.model

        if (button.enabled && event.buttons == setOf(Button1)) {
            model.apply {
                armed = true
                pressed = true
            }

            mouseChanged(button)

            event.consume()
        }
    }

    override fun mouseReleased(event: MouseEvent) {
        val button = event.source as T
        val model  = button.model

        if (button.enabled && Button1 !in event.buttons) {
            model.apply {
                pressed = false
                armed   = false
            }

            mouseChanged(button)

            event.consume()
        }
    }

    override fun mouseDragged(event: MouseEvent) {
        val button = event.source as T
        val model  = button.model

        if (model.pressed && button.enabled && event.buttons == setOf(Button1)) {
            event.consume()
        }
    }

    protected open fun mouseChanged  (button: T) = button.rerender()
    protected open fun enabledChanged(button: T) = button.rerender()
}
