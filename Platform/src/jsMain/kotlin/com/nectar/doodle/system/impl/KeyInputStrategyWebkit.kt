package com.nectar.doodle.system.impl

import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.event.KeyEvent.Companion.VK_A
import com.nectar.doodle.event.KeyEvent.Companion.VK_ALT
import com.nectar.doodle.event.KeyEvent.Companion.VK_BACKSPACE
import com.nectar.doodle.event.KeyEvent.Companion.VK_C
import com.nectar.doodle.event.KeyEvent.Companion.VK_F
import com.nectar.doodle.event.KeyEvent.Companion.VK_F1
import com.nectar.doodle.event.KeyEvent.Companion.VK_F12
import com.nectar.doodle.event.KeyEvent.Companion.VK_R
import com.nectar.doodle.event.KeyEvent.Companion.VK_TAB
import com.nectar.doodle.event.KeyEvent.Companion.VK_V
import com.nectar.doodle.event.KeyEvent.Companion.VK_X
import com.nectar.doodle.event.KeyState
import com.nectar.doodle.event.KeyState.Type
import com.nectar.doodle.event.KeyState.Type.Down
import com.nectar.doodle.event.KeyState.Type.Press
import com.nectar.doodle.event.KeyState.Type.Up
import com.nectar.doodle.system.SystemInputEvent.Modifier
import com.nectar.doodle.system.SystemInputEvent.Modifier.Alt
import com.nectar.doodle.system.SystemInputEvent.Modifier.Ctrl
import com.nectar.doodle.system.SystemInputEvent.Modifier.Shift
import com.nectar.doodle.system.impl.KeyInputServiceStrategy.EventHandler
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent

/**
 * Created by Nicholas Eddy on 3/10/18.
 */
class KeyInputServiceStrategyWebkit(private val htmlFactory: HtmlFactory): KeyInputServiceStrategy {

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
        // Webkit doesn't send key down events when modifiers are
        // pressed so we do this manually
        if (event.ctrlKey && lastKeyDown?.keyCode != event.keyCode) {
            dispatchKeyEvent(event, Down)
        }

        val aResult = dispatchKeyEvent(event, Up)

        if (event.keyCode == VK_TAB) {
            return suppressEvent(event)
        }

        if (isNativeElement(event.target)) {
            return true
        }

        return if (shouldSuppressKeyEvent(event)) suppressEvent(event) else aResult
    }

    private fun keyDown(event: KeyboardEvent): Boolean {
        if (event.ctrlKey) {
            lastKeyDown = event
        }

        val aReturnValue = dispatchKeyEvent(event, Down)

        if (event.keyCode == VK_TAB) {
            return suppressEvent(event)
        }

        if (isClipboardOperation(event) || isNativeElement(event.target)) {
            return true
        }

        return if (shouldSuppressKeyEvent(event)) suppressEvent(event) else aReturnValue
    }

    private fun keyPress(event: KeyboardEvent): Boolean {
        var result = false

        if (!event.altKey && !event.ctrlKey && event.keyCode != VK_TAB) {
            result = dispatchKeyEvent(event, Press)
        }

        if (isNativeElement(event.target)) {
            return true
        }

        return if (shouldSuppressKeyEvent(event)) suppressEvent(event) else result
    }

    private fun dispatchKeyEvent(event: KeyboardEvent, type: Type): Boolean {
        eventHandler?.let {
            val keyEvent = KeyState(
                    event.keyCode,
                    event.keyCode.toChar(),
                    createModifiers(event),
                    type)

            return it.invoke(keyEvent)
        }

        return true
    }

    private fun isClipboardOperation(event: KeyboardEvent): Boolean {
        val keyCode = event.keyCode

        return event.ctrlKey && (keyCode == VK_V || keyCode == VK_C || keyCode == VK_X)
    }

    private fun shouldSuppressKeyEvent(event: KeyboardEvent): Boolean {
        return event.keyCode == VK_ALT ||
               event.keyCode == VK_TAB ||
               event.keyCode == VK_BACKSPACE ||
               event.keyCode in VK_F1..VK_F12 ||
               event.ctrlKey && event.keyCode == VK_A ||
               event.ctrlKey && event.keyCode == VK_F ||
               event.ctrlKey && event.keyCode == VK_R
    }

    private fun createModifiers(event: KeyboardEvent): Set<Modifier> {
        var modifiers = setOf<Modifier>()

        if (event.altKey) {
            modifiers += Alt
        }
        if (event.ctrlKey) {
            modifiers += Ctrl
        }
        if (event.shiftKey) {
            modifiers += Shift
        }

        return modifiers
    }

    private fun suppressEvent(event: Event): Boolean {
        event.preventDefault ()
        event.stopPropagation()

        return false
    }


    private fun registerCallbacks(element: HTMLElement) = element.apply {
        onkeyup    = { this@KeyInputServiceStrategyWebkit.keyUp   (it as KeyboardEvent) }
        onkeydown  = { this@KeyInputServiceStrategyWebkit.keyDown (it as KeyboardEvent) }
        onkeypress = { this@KeyInputServiceStrategyWebkit.keyPress(it as KeyboardEvent) }
    }

    private fun unregisterCallbacks(element: HTMLElement) = element.apply {
        onkeyup    = null
        onkeydown  = null
        onkeypress = null
    }
}
