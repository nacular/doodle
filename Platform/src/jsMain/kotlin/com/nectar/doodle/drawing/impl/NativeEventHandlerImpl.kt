package com.nectar.doodle.drawing.impl

import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event


class NativeEventHandlerImpl(private val element: HTMLElement, private val listener: NativeEventListener): NativeEventHandler {

    override fun startConsumingMouseMoveEvents(onlySelf: Boolean) = registerMuteCallbacksMouseMoveEvents(onlySelf)
    override fun stopConsumingMouseMoveEvents() = unregisterMuteCallbacksMouseMoveEvents()

    override fun startConsumingMousePressEvents() = registerMuteCallbacksMousePressEvents  ()
    override fun stopConsumingMousePressEvents () = unregisterMuteCallbacksMousePressEvents()

    override fun startConsumingSelectionEvents() = registerMuteCallbacksSelectEvents()
    override fun stopConsumingSelectionEvents () = unregisterMuteCallbacksSelectEvents()

    override fun registerFocusListener  () = registerFocusCallbacks  ()
    override fun unregisterFocusListener() = unregisterFocusCallbacks()

    override fun registerKeyListener  () = registerKeyCallback  ()
    override fun unregisterKeyListener() = unregisterKeyCallback()

    override fun registerClickListener  () = registerClickCallback  ()
    override fun unregisterClickListener() = unregisterClickCallback()

    override fun registerScrollListener  () = registerScrollCallback  ()
    override fun unregisterScrollListener() = unregisterScrollCallback()

    private fun muteEvent(event: Event, onlySelf: Boolean = false): Boolean {
        if (onlySelf && event.target != element) {
            return true
        }

        event.stopPropagation()

        return false
    }

    private fun onBlur    () = true.also { listener.onFocusLost  () }
    private fun onFocus   () = true.also { listener.onFocusGained() }
    private fun onKeyUp   () = true.also { listener.onKeyUp      () }
    private fun onKeyDown () = true.also { listener.onKeyDown    () }
    private fun onKeyPress() = true.also { listener.onKeyPress   () }
    private fun onClick   () = true.also { listener.onClick      () }
    private fun onScroll  () = true.also { listener.onScroll     () }

    private fun registerFocusCallbacks() {
        element.onblur  = { onBlur()  }
        element.onfocus = { onFocus() }
    }

    private fun unregisterFocusCallbacks() {
        element.onblur  = null
        element.onfocus = null
    }

    private fun registerKeyCallback() {
        element.onkeyup    = { onKeyUp   () }
        element.onkeydown  = { onKeyDown () }
        element.onkeypress = { onKeyPress() }
    }

    private fun unregisterKeyCallback() {
        element.onkeyup    = null
        element.onkeydown  = null
        element.onkeypress = null
    }

    private fun registerClickCallback() {
        element.onclick = { onClick() }
    }

    private fun unregisterClickCallback() {
        element.onclick = null
    }

    private fun registerMuteCallbacksMouseMoveEvents(onlySelf: Boolean) {
        element.onmousemove = { muteEvent(it, onlySelf) }
    }

    private fun unregisterMuteCallbacksMouseMoveEvents() {
        element.onmousemove = null
    }

    private fun registerMuteCallbacksMousePressEvents() {
        element.onmouseup   = { muteEvent(it) }
        element.onmousedown = { muteEvent(it) }
        element.ondblclick  = { muteEvent(it) }
    }

    private fun unregisterMuteCallbacksMousePressEvents() {
        element.onmouseup   = null
        element.onmousedown = null
        element.ondblclick  = null
    }

    private fun registerMuteCallbacksSelectEvents() {
        element.onselect = { muteEvent(it) }
    }

    private fun unregisterMuteCallbacksSelectEvents() {
        element.onselect = null
    }

    private fun registerScrollCallback() {
        element.onscroll = { onScroll() }
    }

    private fun unregisterScrollCallback() {
        element.onscroll = null
    }
}
