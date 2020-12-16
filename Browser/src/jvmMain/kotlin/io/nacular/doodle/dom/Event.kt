package io.nacular.doodle.dom

/**
 * Created by Nicholas Eddy on 3/12/20.
 */
actual abstract class EventTarget

actual open class Event {
    actual val target = null as EventTarget?

    actual fun stopPropagation() {}
    actual fun preventDefault () {}
}

actual open class UIEvent : Event()
actual open class KeyboardEvent: UIEvent() {
    actual val ctrlKey  = false
    actual val shiftKey = false
    actual val altKey   = false
    actual val metaKey  = false
    actual val keyCode  = 0
    actual val key      = ""
    actual val code     = ""
}

actual open class MouseEvent: UIEvent() {
    actual open val pageX    = 0.0
    actual open val pageY    = 0.0
    actual open val clientX  = 0
    actual open val clientY  = 0
    actual open val ctrlKey  = false
    actual open val shiftKey = false
    actual open val altKey   = false
    actual open val metaKey  = false
    actual open val button   = 0.toShort()
    actual open val buttons  = 0.toShort()
}

actual open class WheelEvent: MouseEvent() {
    actual val deltaY = 0.0
    actual val deltaX = 0.0
}

actual open class PointerEvent: MouseEvent()

actual open class TouchEvent: UIEvent()