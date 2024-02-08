package io.nacular.doodle.dom

/**
 * Created by Nicholas Eddy on 3/12/20.
 */
internal actual abstract class EventTarget: JsAny

internal actual open class Event actual constructor(): JsAny {
    actual constructor(name: String): this()

    actual val target: EventTarget? = null

    actual fun stopPropagation() {}
    actual fun preventDefault () {}
}

internal actual open class UIEvent : Event() {
    actual val detail: Int = 0
}

internal actual open class KeyboardEvent: UIEvent() {
    actual val ctrlKey : Boolean = false
    actual val shiftKey: Boolean = false
    actual val altKey  : Boolean = false
    actual val metaKey : Boolean = false
    actual val keyCode : Int     = 0
    actual val key     : String? = ""
    actual val code    : String? = ""
}

internal actual open class MouseEvent: UIEvent() {
    actual open val pageX   : Double  = 0.0
    actual open val pageY   : Double  = 0.0
    actual open val clientX : Int     = 0
    actual open val clientY : Int     = 0
    actual open val ctrlKey : Boolean = false
    actual open val shiftKey: Boolean = false
    actual open val altKey  : Boolean = false
    actual open val metaKey : Boolean = false
    actual open val button  : Short   = 0.toShort()
    actual open val buttons : Short   = 0.toShort()
}

internal actual open class WheelEvent: MouseEvent() {
    actual val deltaY: Double = 0.0
    actual val deltaX: Double = 0.0
}

internal actual open class PointerEvent: MouseEvent() {
    actual val pointerId  : Int    = 0
    actual val pointerType: String = ""
}

internal actual abstract class TouchList {
    actual abstract val length: Int
}

internal actual open class TouchEvent: UIEvent() {
    actual val touches: TouchList = object: TouchList() {
        override val length = 0
    }
}

internal actual class InputEvent: UIEvent()

internal actual class FocusEvent: UIEvent() {
    actual val relatedTarget: EventTarget? = null
}

internal actual class DragEvent: MouseEvent() {
    actual val dataTransfer: DataTransfer? = null
}