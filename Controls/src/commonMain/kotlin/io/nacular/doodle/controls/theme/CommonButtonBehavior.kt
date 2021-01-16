package io.nacular.doodle.controls.theme

import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.buttons.ToggleButton
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.Icon
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.event.KeyCode.Companion.Space
import io.nacular.doodle.event.KeyEvent
import io.nacular.doodle.event.KeyListener
import io.nacular.doodle.event.KeyText.Companion.Enter
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.event.PointerMotionListener
import io.nacular.doodle.system.SystemPointerEvent.Button.Button1


abstract class CommonButtonBehavior<T: Button>: Behavior<T>, PointerListener, PointerMotionListener, KeyListener {

    private val enabledChanged: (View, Boolean, Boolean) -> Unit = { button,_,_ ->
        enabledChanged(button as T)
    }

    private val stylesChanged: (View) -> Unit = {
        stylesChanged(it as T)
    }

    protected open val selectionChanged: (Button, Boolean, Boolean) -> Unit = { button,_,_ ->
        button.rerender()
    }

    override fun install(view: T) {
        view.keyChanged           += this
        view.pointerChanged       += this
        view.styleChanged         += stylesChanged
        view.enabledChanged       += enabledChanged
        view.pointerMotionChanged += this

        (view as? ToggleButton)?.let { it.selectedChanged += selectionChanged }

        view.rerender()
        // TODO: Handle changes to the model from other places
    }

    override fun uninstall(view: T) {
        view.keyChanged           -= this
        view.pointerChanged       -= this
        view.styleChanged         -= stylesChanged
        view.enabledChanged       -= enabledChanged
        view.pointerMotionChanged -= this

        (view as? ToggleButton)?.let { it.selectedChanged -= selectionChanged }
    }

//    fun keyTyped(aKeyEvent: KeyEvent) {}
//
    override fun keyReleased(event: KeyEvent) {
        val button = event.source as Button

        if (button.enabled && (event.key == Enter || event.code == Space)) {
            button.model.apply {
                pressed = false
                armed   = false
            }
        }
    }

    override fun keyPressed(event: KeyEvent) {
        val button = event.source as Button

        if (button.enabled && (event.key == Enter || event.code == Space)) {
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
                armed   = true
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

    protected open fun stylesChanged (button: T) = button.rerender()
    protected open fun pointerChanged(button: T) = button.rerender()
    protected open fun enabledChanged(button: T) = button.rerender()

    protected fun icon(button: Button): Icon<Button>? {
        val model = button.model

        return when {
            !button.enabled   -> if (model.selected) button.disabledSelectedIcon else button.disabledIcon
            model.pressed     -> button.pressedIcon
            model.selected    -> button.selectedIcon
            model.pointerOver -> if (model.selected) button.pointerOverSelectedIcon else button.pointerOverIcon
            else              -> button.icon
        }
    }
}

inline fun <T: Button> simpleButtonRenderer(crossinline render: (button: T, canvas: Canvas) -> Unit) = object: CommonButtonBehavior<T>() {
    override fun render(view: T, canvas: Canvas) = render(view, canvas)
}