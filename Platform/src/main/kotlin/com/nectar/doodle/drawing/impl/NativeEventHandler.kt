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

    fun addListener   (listener: NativeEventListener)
    fun removeListener(listener: NativeEventListener)
}

class NativeEventHandlerImpl: NativeEventHandler {
    override fun startConsumingMouseEvents(element: HTMLElement) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun stopConsumingMouseEvents(element: HTMLElement) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun startConsumingSelectionEvents(element: HTMLElement) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun stopConsumingSelectionEvents(element: HTMLElement) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun registerFocusListener(element: HTMLElement) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun unregisterFocusListener(element: HTMLElement) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun registerClickListener(element: HTMLElement) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun unregisterClickListener(element: HTMLElement) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun registerKeyListener(element: HTMLElement) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun unregisterKeyListener(element: HTMLElement) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addListener(listener: NativeEventListener) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeListener(listener: NativeEventListener) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}