package com.nectar.doodle.system.impl

import com.nectar.doodle.HTMLElement
import com.nectar.doodle.dom.Event
import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.dom.KeyboardEvent
import com.nectar.doodle.event.KeyCode
import com.nectar.doodle.event.KeyEvent.Companion.AltLeft
import com.nectar.doodle.event.KeyEvent.Companion.AltRight
import com.nectar.doodle.event.KeyEvent.Companion.Backspace
import com.nectar.doodle.event.KeyEvent.Companion.F1
import com.nectar.doodle.event.KeyEvent.Companion.F10
import com.nectar.doodle.event.KeyEvent.Companion.F11
import com.nectar.doodle.event.KeyEvent.Companion.F12
import com.nectar.doodle.event.KeyEvent.Companion.F2
import com.nectar.doodle.event.KeyEvent.Companion.F3
import com.nectar.doodle.event.KeyEvent.Companion.F4
import com.nectar.doodle.event.KeyEvent.Companion.F5
import com.nectar.doodle.event.KeyEvent.Companion.F6
import com.nectar.doodle.event.KeyEvent.Companion.F7
import com.nectar.doodle.event.KeyEvent.Companion.F8
import com.nectar.doodle.event.KeyEvent.Companion.F9
import com.nectar.doodle.event.KeyEvent.Companion.KeyA
import com.nectar.doodle.event.KeyEvent.Companion.KeyC
import com.nectar.doodle.event.KeyEvent.Companion.KeyF
import com.nectar.doodle.event.KeyEvent.Companion.KeyR
import com.nectar.doodle.event.KeyEvent.Companion.KeyV
import com.nectar.doodle.event.KeyEvent.Companion.KeyX
import com.nectar.doodle.event.KeyEvent.Companion.Tab
import com.nectar.doodle.event.KeyState
import com.nectar.doodle.event.KeyState.Type
import com.nectar.doodle.event.KeyState.Type.Down
import com.nectar.doodle.event.KeyState.Type.Up
import com.nectar.doodle.system.SystemInputEvent.Modifier
import com.nectar.doodle.system.SystemInputEvent.Modifier.Alt
import com.nectar.doodle.system.SystemInputEvent.Modifier.Ctrl
import com.nectar.doodle.system.SystemInputEvent.Modifier.Meta
import com.nectar.doodle.system.SystemInputEvent.Modifier.Shift
import com.nectar.doodle.system.impl.KeyInputServiceStrategy.EventHandler
import com.nectar.doodle.utils.ifTrue

/**
 * Created by Nicholas Eddy on 3/10/18.
 */
internal class KeyInputStrategyWebkit(private val htmlFactory: HtmlFactory): KeyInputServiceStrategy {

    private var lastKeyDown  = null as KeyboardEvent?
    private var eventHandler = null as EventHandler?
    private var inputDevice  = null as HTMLElement?

    override fun startUp(handler: EventHandler) {
        eventHandler = handler

        if (inputDevice == null) {
            inputDevice = htmlFactory.root.also {
                registerCallbacks(it)
            }
        }
    }

    override fun shutdown() {
        inputDevice?.let {
            unregisterCallbacks(it)

            inputDevice = null
        }
    }

    private fun keyUp(event: KeyboardEvent): Boolean {
        // Webkit doesn't send key down events when modifiers are pressed so we do this manually
        if (event.ctrlKey && lastKeyDown?.keyCode != event.keyCode) {
            dispatchKeyEvent(event, Down)
        }

        val result = dispatchKeyEvent(event, Up)

        if (event.key == Tab.key) {
            return suppressEvent(event)
        }

        if (isNativeElement(event.target)) {
            return true
        }

        return if (shouldSuppressKeyEvent(event)) suppressEvent(event) else result
    }

    private fun keyDown(event: KeyboardEvent): Boolean {
        if (event.ctrlKey) {
            lastKeyDown = event
        }

        val returnValue = dispatchKeyEvent(event, Down)

        if (event.key == Tab.key) {
            return suppressEvent(event)
        }

        if (isClipboardOperation(event) || isNativeElement(event.target)) {
            return true
        }

        return if (shouldSuppressKeyEvent(event)) suppressEvent(event) else returnValue
    }

    private fun dispatchKeyEvent(event: KeyboardEvent, type: Type) = eventHandler?.let {
        val keyEvent = KeyState(
                KeyCode(event.code),
                event.key,
                createModifiers(event),
                type)

        return it(keyEvent, event.target)
    } ?: true

    private fun isClipboardOperation(event: KeyboardEvent) = event.ctrlKey && KeyCode(event.code).let { it == KeyV || it == KeyC || it == KeyX }

    // FIXME: Can this be removed?
    private fun shouldSuppressKeyEvent(event: KeyboardEvent) = event.run {
        val code = KeyCode(code)

        code == AltLeft         ||
        code == AltRight        ||
        code == Tab             ||
        code == Backspace       ||
        code == KeyA && ctrlKey ||
        code == KeyF && ctrlKey ||
        code == KeyR && ctrlKey ||
        code in setOf(F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12)
    }

    private fun createModifiers(event: KeyboardEvent) = mutableSetOf<Modifier>().also {
        event.altKey.ifTrue   { it += Alt   }
        event.ctrlKey.ifTrue  { it += Ctrl  }
        event.metaKey.ifTrue  { it += Meta  }
        event.shiftKey.ifTrue { it += Shift }
    }

    private fun suppressEvent(event: Event) = false.also {
        event.preventDefault ()
        event.stopPropagation()
    }

    private fun registerCallbacks(element: HTMLElement) = element.apply {
        onkeyup   = { this@KeyInputStrategyWebkit.keyUp  (it) }
        onkeydown = { this@KeyInputStrategyWebkit.keyDown(it) }
    }

    private fun unregisterCallbacks(element: HTMLElement) = element.apply {
        onkeyup   = null
        onkeydown = null
    }
}