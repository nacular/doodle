package io.nacular.doodle.drawing.impl

import io.nacular.doodle.dom.Event
import io.nacular.doodle.dom.HTMLElement
import io.nacular.doodle.focus.NativeFocusManager


internal class NativeEventHandlerImpl(
    private val focusManager: NativeFocusManager?,
    private val element     : HTMLElement,
    private val listener    : NativeEventListener
): NativeEventHandler {

    override fun startConsumingPointerMoveEvents(onlySelf: Boolean) { element.onpointermove = { muteEvent(it, onlySelf) } }
    override fun stopConsumingPointerMoveEvents (                 ) { element.onpointermove = null                        }

    override fun startConsumingPointerPressEvents() {
        element.onpointerup   = { muteEvent(it) }
        element.onpointerdown = { muteEvent(it) }
        element.ondblclick    = { muteEvent(it) }
    }
    override fun stopConsumingPointerPressEvents() {
        element.onpointerup   = null
        element.onpointerdown = null
        element.ondblclick    = null
    }

    override fun registerFocusListener  () {
        element.onblur  = { onBlur (it) }
        element.onfocus = { onFocus(it) }
    }

    override fun unregisterFocusListener() {
        element.onblur  = null
        element.onfocus = null
    }

    private fun focusIn(event: Event) { onFocusIn(event) }

    private fun focusOut(event: Event) { onFocusOut(event) }

    override fun registerFocusInListener() {
        element.addEventListener("focusin",  ::focusIn )
        element.addEventListener("focusout", ::focusOut)
    }

    override fun unregisterFocusInListener() {
        element.removeEventListener("focusin",  ::focusIn )
        element.removeEventListener("focusout", ::focusOut)
    }

    override fun registerKeyListener  () {
        element.onkeyup    = { onKeyUp   (it) }
        element.onkeydown  = { onKeyDown (it) }
        element.onkeypress = { onKeyPress(it) }
    }

    override fun unregisterKeyListener() {
        element.onkeyup    = null
        element.onkeydown  = null
        element.onkeypress = null
    }

    override fun registerClickListener   () { element.onclick  = { onClick(it) } }
    override fun unregisterClickListener () { element.onclick  = null            }

    override fun registerScrollListener  () { element.onscroll = { onScroll(it) } }
    override fun unregisterScrollListener() { element.onscroll = null             }

    override fun registerChangeListener  () { element.onchange = { onChange(it) } }
    override fun unregisterChangeListener() { element.onchange = null             }

    override fun registerInputListener   () { element.oninput  = { onInput(it) } }
    override fun unregisterInputListener () { element.oninput  = null            }

    override fun registerSelectionListener  () { element.onselect = { onSelect(it) } }
    override fun unregisterSelectionListener() { element.onselect = null }

    private fun muteEvent(event: Event, onlySelf: Boolean = false): Boolean {
        if (onlySelf && event.target != element) {
            return true
        }

        event.stopPropagation()

        return false
    }

    private fun onBlur    (event: Event) = true.also { focusManager?.hasFocusOwner = false; listener.onFocusLost  (event) }
    private fun onFocus   (event: Event) = true.also { focusManager?.hasFocusOwner = true;  listener.onFocusGained(event) }
    private fun onKeyUp   (event: Event) = true.also { listener.onKeyUp      (event) }
    private fun onKeyDown (event: Event) = true.also { listener.onKeyDown    (event) }
    private fun onKeyPress(event: Event) = true.also { listener.onKeyPress   (event) }
    private fun onClick   (event: Event) = true.also { listener.onClick      (event) }
    private fun onScroll  (event: Event) = true.also { listener.onScroll     (event) }
    private fun onChange  (event: Event) = true.also { listener.onChange     (event) }
    private fun onInput   (event: Event) = true.also { listener.onInput      (event) }
    private fun onSelect  (event: Event) = true.also { listener.onSelect     (event) }

    private fun onFocusIn (event: Event) = true.also { focusManager?.hasFocusOwner = true;  listener.onFocusGained(event) }
    private fun onFocusOut(event: Event) = true.also { focusManager?.hasFocusOwner = false; listener.onFocusLost  (event) }
}
