package com.nectar.doodle.drawing.impl

import org.w3c.dom.HTMLElement


typealias NativeEventHandlerFactory = (element: HTMLElement, listener: NativeEventListener) -> NativeEventHandler

interface NativeEventHandler {
    fun startConsumingMouseMoveEvents(onlySelf: Boolean = false)
    fun stopConsumingMouseMoveEvents ()

    fun startConsumingMousePressEvents()
    fun stopConsumingMousePressEvents ()

    fun startConsumingSelectionEvents()
    fun stopConsumingSelectionEvents ()

    fun registerFocusListener  ()
    fun unregisterFocusListener()

    fun registerClickListener  ()
    fun unregisterClickListener()

    fun registerKeyListener  ()
    fun unregisterKeyListener()

    fun registerScrollListener  ()
    fun unregisterScrollListener()
}