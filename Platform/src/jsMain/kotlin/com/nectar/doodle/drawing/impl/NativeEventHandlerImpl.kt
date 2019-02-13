package com.nectar.doodle.drawing.impl

import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event


class NativeEventHandlerImpl(private val element: HTMLElement, private val listener: NativeEventListener): NativeEventHandler {

    override fun startConsumingMouseMoveEvents(onlySelf: Boolean) { element.onmousemove = { muteEvent(it, onlySelf) } }
    override fun stopConsumingMouseMoveEvents (                 ) { element.onmousemove = null                        }

    override fun startConsumingMousePressEvents() {
        element.onmouseup   = { muteEvent(it) }
        element.onmousedown = { muteEvent(it) }
        element.ondblclick  = { muteEvent(it) }
    }
    override fun stopConsumingMousePressEvents() {
        element.onmouseup   = null
        element.onmousedown = null
        element.ondblclick  = null
    }

    override fun startConsumingSelectionEvents() { element.onselect = { muteEvent(it) } }
    override fun stopConsumingSelectionEvents () { element.onselect = null              }

    override fun registerFocusListener  () {
        element.onblur  = { onBlur()  }
        element.onfocus = { onFocus() }
    }

    override fun unregisterFocusListener() {
        element.onblur  = null
        element.onfocus = null
    }

    override fun registerKeyListener  () {
        element.onkeyup    = { onKeyUp   () }
        element.onkeydown  = { onKeyDown () }
        element.onkeypress = { onKeyPress() }
    }

    override fun unregisterKeyListener() {
        element.onkeyup    = null
        element.onkeydown  = null
        element.onkeypress = null
    }

    override fun registerClickListener   () { element.onclick  = { onClick() } }
    override fun unregisterClickListener () { element.onclick  = null          }

    override fun registerScrollListener  () { element.onscroll = { onScroll() } }
    override fun unregisterScrollListener() { element.onscroll = null           }

    override fun registerChangeListener  () { element.onchange = { onChange() } }
    override fun unregisterChangeListener() { element.onchange = null          }

    override fun registerInputListener   () { element.oninput  = { onInput() } }
    override fun unregisterInputListener () { element.oninput  = null          }

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
    private fun onChange  () = true.also { listener.onChange     () }
    private fun onInput   () = true.also { listener.onInput      () }
}
