package io.nacular.doodle.dom

internal actual abstract external class EventTarget: JsAny

internal actual open external class Event actual constructor(): JsAny {
    actual constructor(name: String)

    actual val target: EventTarget?

    actual fun stopPropagation()
    actual fun preventDefault ()
}

internal actual open external class UIEvent: Event {
    actual val detail: Int
}

internal actual open external class KeyboardEvent: UIEvent {
    actual val ctrlKey : Boolean
    actual val shiftKey: Boolean
    actual val altKey  : Boolean
    actual val metaKey : Boolean
    actual val keyCode : Int
    actual val key     : String?
    actual val code    : String?
}

internal actual open external class MouseEvent: UIEvent {
    actual open val pageX   : Double
    actual open val pageY   : Double
    actual open val clientX : Int
    actual open val clientY : Int
    actual open val ctrlKey : Boolean
    actual open val shiftKey: Boolean
    actual open val altKey  : Boolean
    actual open val metaKey : Boolean
    actual open val button  : Short
    actual open val buttons : Short
}

internal actual open external class PointerEvent: MouseEvent {
    actual val pointerId  : Int
    actual val pointerType: String
}

internal actual open external class WheelEvent: MouseEvent {
    actual val deltaX: Double
    actual val deltaY: Double
}

internal actual abstract external class TouchList {
    actual abstract val length: Int
}

internal actual open external class TouchEvent: UIEvent {
    actual val touches: TouchList
}

internal actual external class InputEvent: UIEvent

internal actual external class FocusEvent: UIEvent {
    actual val relatedTarget: EventTarget?
}

internal actual external class DragEvent: MouseEvent {
    actual val dataTransfer: DataTransfer?
}