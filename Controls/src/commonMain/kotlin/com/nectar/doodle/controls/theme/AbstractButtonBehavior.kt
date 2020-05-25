package com.nectar.doodle.controls.theme

import com.nectar.doodle.controls.buttons.Button
import com.nectar.doodle.controls.buttons.ToggleButton
import com.nectar.doodle.core.View
import com.nectar.doodle.event.KeyEvent
import com.nectar.doodle.event.KeyEvent.Companion.Enter
import com.nectar.doodle.event.KeyEvent.Companion.Space
import com.nectar.doodle.event.KeyListener
import com.nectar.doodle.event.PointerEvent
import com.nectar.doodle.event.PointerListener
import com.nectar.doodle.event.PointerMotionListener
import com.nectar.doodle.system.SystemPointerEvent.Button.Button1
import com.nectar.doodle.theme.Behavior


abstract class AbstractButtonBehavior<T: Button>: Behavior<T>, PointerListener, PointerMotionListener, KeyListener {

    private val enabledChanged: (View, Boolean, Boolean) -> Unit = { button,_,_ ->
        enabledChanged(button as T)
    }

    private val stylesChanged: (View) -> Unit = {
        it.rerender()
    }

    private val selectionChanged: (Button, Boolean, Boolean) -> Unit = { button,_,_ ->
        button.rerender()
    }

    override fun install(view: T) {
        view.keyChanged         += this
        view.pointerChanged       += this
        view.styleChanged       += stylesChanged
        view.enabledChanged     += enabledChanged
        view.pointerMotionChanged += this

        (view as? ToggleButton)?.let { it.selectedChanged += selectionChanged }

        view.rerender()
        // TODO: Handle changes to the model from other places
    }

    override fun uninstall(view: T) {
        view.keyChanged         -= this
        view.pointerChanged       -= this
        view.styleChanged       -= stylesChanged
        view.enabledChanged     -= enabledChanged
        view.pointerMotionChanged -= this

        (view as? ToggleButton)?.let { it.selectedChanged -= selectionChanged }
    }

//    fun keyTyped(aKeyEvent: KeyEvent) {}
//
    override fun keyReleased(event: KeyEvent) {
        val button  = event.source as Button
        val keyCode = event.code

        if (button.enabled && (keyCode == Enter || keyCode == Space)) {
            button.model.apply {
                pressed = false
                armed   = false
            }
        }
    }

    override fun keyPressed(event: KeyEvent) {
        val button  = event.source as Button
        val keyCode = event.code

        if (button.enabled && (keyCode == Enter || keyCode == Space)) {
            button.model.apply {
                armed   = true
                pressed = true
            }
        }
    }

    override fun exited(event: PointerEvent) {
        val button = event.source as T
        val model  = button.model

        model.pointerOver = false

        if (button.enabled) {
            model.armed = false
        }

        pointerChanged(button)
    }

    override fun entered(event: PointerEvent) {
        val button = event.source as T
        val model  = button.model

        model.pointerOver = true

        if (button.enabled && event.buttons == setOf(Button1) && model.pressed) {
            model.armed = true
        }

        pointerChanged(button)
    }

    override fun pressed(event: PointerEvent) {
        val button = event.source as T
        val model  = button.model

        if (button.enabled && event.buttons == setOf(Button1)) {
            model.apply {
                armed = true
                pressed = true
            }

            pointerChanged(button)

            event.consume()
        }
    }

    override fun released(event: PointerEvent) {
        val button = event.source as T
        val model  = button.model

        if (button.enabled && Button1 !in event.buttons) {
            model.apply {
                pressed = false
                armed   = false
            }

            pointerChanged(button)

            event.consume()
        }
    }

    override fun dragged(event: PointerEvent) {
        val button = event.source as T
        val model  = button.model

        if (model.pressed && button.enabled && event.buttons == setOf(Button1)) {
            event.consume()
        }
    }

    protected open fun pointerChanged(button: T) = button.rerender()
    protected open fun enabledChanged(button: T) = button.rerender()
}
