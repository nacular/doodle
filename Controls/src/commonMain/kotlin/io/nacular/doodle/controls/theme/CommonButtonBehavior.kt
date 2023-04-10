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
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.system.SystemPointerEvent.Button.Button1


public abstract class CommonButtonBehavior<in T: Button>(private val focusManager: FocusManager? = null): Behavior<T>, PointerListener, PointerMotionListener, KeyListener {

    @Suppress("UNCHECKED_CAST")
    private val enabledChanged: (View, Boolean, Boolean) -> Unit = { button,_,_ ->
        enabledChanged(button as T)
    }

    @Suppress("UNCHECKED_CAST")
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

    override fun released(event: KeyEvent) {
        val button = event.source as Button

        if (button.enabled && (event.key == Enter || event.code == Space)) {
            button.model.apply {
                pressed = false
                armed   = false
            }

            event.consume()
        }
    }

    override fun pressed(event: KeyEvent) {
        val button = event.source as Button

        if (button.enabled && (event.key == Enter || event.code == Space)) {
            button.model.apply {
                armed   = true
                pressed = true
            }

            event.consume()
        }
    }

    override fun exited(event: PointerEvent) {
        @Suppress("UNCHECKED_CAST")
        val button = event.source as T
        val model  = button.model

        model.pointerOver = false

        if (button.enabled) {
            model.armed = false
        }

        pointerChanged(button)
    }

    override fun entered(event: PointerEvent) {
        @Suppress("UNCHECKED_CAST")
        val button = event.source as T
        val model  = button.model

        model.pointerOver = true

        if (button.enabled && event.buttons == setOf(Button1) && model.pressed) {
            model.armed = true
        }

        pointerChanged(button)
    }

    override fun pressed(event: PointerEvent) {
        @Suppress("UNCHECKED_CAST")
        val button = event.source as T
        val model  = button.model

        if (button.enabled && event.buttons == setOf(Button1)) {
            model.apply {
                armed   = true
                pressed = true
            }

            pointerChanged(button)

            event.consume()

            focusManager?.requestFocus(button)
        }
    }

    override fun released(event: PointerEvent) {
        @Suppress("UNCHECKED_CAST")
        val button = event.source as T
        val model  = button.model

        if (button.enabled && Button1 !in event.buttons) {
            model.pressed = false
            model.armed   = false

            pointerChanged(button)

            event.consume()
        }
    }

    override fun dragged(event: PointerEvent) {
        @Suppress("UNCHECKED_CAST")
        val button = event.source as T
        val model  = button.model

        if (model.pressed && button.enabled && event.buttons == setOf(Button1)) {
            event.consume()
        }
    }

    protected open fun stylesChanged (button: T): Unit = button.rerender()
    protected open fun pointerChanged(button: T): Unit = button.rerender()
    protected open fun enabledChanged(button: T): Unit = button.rerender()

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

public inline fun <T: Button> simpleButtonRenderer(focusManager: FocusManager?, crossinline render: (button: T, canvas: Canvas) -> Unit): CommonButtonBehavior<T> = object: CommonButtonBehavior<T>(focusManager) {
    override fun render(view: T, canvas: Canvas) = render(view, canvas)
}

public inline fun <T: Button> simpleButtonRenderer(crossinline render: (button: T, canvas: Canvas) -> Unit): CommonButtonBehavior<T> = object: CommonButtonBehavior<T>(null) {
    override fun render(view: T, canvas: Canvas) = render(view, canvas)
}