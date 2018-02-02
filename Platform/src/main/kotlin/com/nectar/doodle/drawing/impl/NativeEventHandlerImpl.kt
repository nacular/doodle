package com.nectar.doodle.drawing.impl

import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event


class NativeEventHandlerImpl: NativeEventHandler {
    private val listeners = mutableListOf<NativeEventListener>()

    override fun startConsumingMouseEvents(element: HTMLElement) {
        registerMuteCallbacksMouseEvents(this, element)
    }

    override fun stopConsumingMouseEvents(element: HTMLElement) {
        unregisterMuteCallbacksMouseEvents(element)
    }

    override fun startConsumingSelectionEvents(element: HTMLElement) {
        registerMuteCallbacksSelectEvents(this, element)
    }

    override fun stopConsumingSelectionEvents(element: HTMLElement) {
        unregisterMuteCallbacksSelectEvents(element)
    }

    override fun registerFocusListener(element: HTMLElement) {
        registerFocusCallbacks(this, element)
    }

    override fun unregisterFocusListener(element: HTMLElement) {
        unregisterFocusCallbacks(element)
    }

    override fun registerKeyListener(element: HTMLElement) {
        registerKeyCallback(this, element)
    }

    override fun unregisterKeyListener(element: HTMLElement) {
        unregisterKeyCallback(element)
    }

    override fun registerClickListener(element: HTMLElement) {
        registerClickCallback(this, element)
    }

    override fun unregisterClickListener(element: HTMLElement) {
        unregisterClickCallback(element)
    }

    override operator fun plusAssign(listener: NativeEventListener) {
        listeners += listener
    }

    override operator fun minusAssign(listener: NativeEventListener) {
        listeners -= listener
    }

    private fun muteEvent(event: Event): Boolean {
//        event.preventDefault()
        event.stopPropagation()

        return false
    }

    private fun onBlur(): Boolean {
        listeners.forEach { it.onFocusLost() }

        return true
    }

    private fun onFocus(): Boolean {
        listeners.forEach { it.onFocusGained() }

        return true
    }

    private fun onKeyUp(): Boolean {
        listeners.forEach { it.onKeyUp() }

        return true
    }

    private fun onKeyDown(): Boolean {
        listeners.forEach { it.onKeyDown() }

        return true
    }

    private fun onKeyPress(): Boolean {
        listeners.forEach { it.onKeyPress() }

        return true
    }

    private fun onClick(): Boolean {
        listeners.forEach { it.onClick() }

        return true
    }

    protected fun registerFocusCallbacks(handler: NativeEventHandlerImpl, element: HTMLElement) {
        element.onblur  = { handler.onBlur()  }
        element.onfocus = { handler.onFocus() }
    }

    protected fun unregisterFocusCallbacks(element: HTMLElement) {
        element.onblur  = null
        element.onfocus = null
    }

    protected fun registerKeyCallback(handler: NativeEventHandlerImpl, element: HTMLElement) {
        element.onkeyup    = { handler.onKeyUp   () }
        element.onkeydown  = { handler.onKeyDown () }
        element.onkeypress = { handler.onKeyPress() }
    }

    protected fun unregisterKeyCallback(element: HTMLElement) {
        element.onkeyup    = null
        element.onkeydown  = null
        element.onkeypress = null
    }

    protected fun registerClickCallback(handler: NativeEventHandlerImpl, element: HTMLElement) {
        element.onclick = { handler.onClick() }
    }

    protected fun unregisterClickCallback(element: HTMLElement) {
        element.onclick = null
    }

    protected fun registerMuteCallbacksMouseEvents(handler: NativeEventHandlerImpl, element: HTMLElement) {
        element.onmouseup   = { handler.muteEvent(it) }
        element.onmousedown = { handler.muteEvent(it) }
        element.ondblclick  = { handler.muteEvent(it) }
    }

    protected fun registerMuteCallbacksSelectEvents(handler: NativeEventHandlerImpl, element: HTMLElement) {
        element.onselect = { handler.muteEvent(it) }
    }

    protected fun unregisterMuteCallbacksMouseEvents(element: HTMLElement) {
        element.onmouseup   = null
        element.onmousedown = null
        element.ondblclick  = null
    }

    protected fun unregisterMuteCallbacksSelectEvents(element: HTMLElement) {
        element.onselect = null
    }
}
