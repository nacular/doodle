package com.nectar.doodle.system.impl

import com.nectar.doodle.HTMLElement
import com.nectar.doodle.dom.Event
import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.dom.KeyboardEvent
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
import com.nectar.doodle.system.SystemInputEvent.Modifier.Meta
import com.nectar.doodle.system.SystemInputEvent.Modifier.Shift
import com.nectar.doodle.system.impl.KeyInputServiceStrategy.EventHandler
import com.nectar.doodle.utils.ifTrue

/**
 * Created by Nicholas Eddy on 3/10/18.
 */
class KeyInputStrategyWebkit(private val htmlFactory: HtmlFactory): KeyInputServiceStrategy {

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

        val result = dispatchKeyEvent(event, Up)

        if (event.keyCode == VK_TAB) {
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

        if (event.keyCode == VK_TAB) {
            return suppressEvent(event)
        }

        if (isClipboardOperation(event) || isNativeElement(event.target)) {
            return true
        }

        return if (shouldSuppressKeyEvent(event)) suppressEvent(event) else returnValue
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

    private fun dispatchKeyEvent(event: KeyboardEvent, type: Type) = eventHandler?.let {
        val keyEvent = KeyState(
                event.keyCode,
                event.keyCode.toChar(),
                createModifiers(event),
                type)

        return it(keyEvent)
    } ?: true

    private fun isClipboardOperation(event: KeyboardEvent) = event.ctrlKey && event.keyCode.let { it == VK_V || it == VK_C || it == VK_X }

    private fun shouldSuppressKeyEvent(event: KeyboardEvent) = event.run {
        keyCode == VK_ALT          ||
        keyCode == VK_TAB          ||
        keyCode == VK_BACKSPACE    ||
        keyCode in VK_F1..VK_F12   ||
        keyCode == VK_A && ctrlKey ||
        keyCode == VK_F && ctrlKey ||
        keyCode == VK_R && ctrlKey
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
        onkeyup    = { this@KeyInputStrategyWebkit.keyUp   (it) }
        onkeydown  = { this@KeyInputStrategyWebkit.keyDown (it) }
        onkeypress = { this@KeyInputStrategyWebkit.keyPress(it) }
    }

    private fun unregisterCallbacks(element: HTMLElement) = element.apply {
        onkeyup    = null
        onkeydown  = null
        onkeypress = null
    }
}