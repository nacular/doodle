package com.nectar.doodle.drawing.impl

import org.w3c.dom.HTMLElement


interface NativeEventHandler {
    fun startConsumingMouseEvents(element: HTMLElement)
    fun stopConsumingMouseEvents (element: HTMLElement)

    fun startConsumingSelectionEvents(element: HTMLElement)
    fun stopConsumingSelectionEvents (element: HTMLElement)

    fun registerFocusListener  (element: HTMLElement)
    fun unregisterFocusListener(element: HTMLElement)

    fun registerClickListener  (element: HTMLElement)
    fun unregisterClickListener(element: HTMLElement)

    fun registerKeyListener  (element: HTMLElement)
    fun unregisterKeyListener(element: HTMLElement)

    operator fun plusAssign(listener: NativeEventListener)
    operator fun minusAssign(listener: NativeEventListener)
}