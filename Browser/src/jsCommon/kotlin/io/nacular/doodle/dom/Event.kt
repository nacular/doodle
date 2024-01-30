package io.nacular.doodle.dom

public actual abstract external class EventTarget: JsAny

public actual open external class Event actual constructor(): JsAny {
    public actual constructor(name: String)

    public actual val target: EventTarget?

    public actual fun stopPropagation()
    public actual fun preventDefault ()
}

public actual open external class UIEvent: Event {
    public actual val detail: Int
}

public actual open external class KeyboardEvent: UIEvent {
    public actual val ctrlKey : Boolean
    public actual val shiftKey: Boolean
    public actual val altKey  : Boolean
    public actual val metaKey : Boolean
    public actual val keyCode : Int
    public actual val key     : String?
    public actual val code    : String?
}

public actual open external class MouseEvent: UIEvent {
    public actual open val pageX   : Double
    public actual open val pageY   : Double
    public actual open val clientX : Int
    public actual open val clientY : Int
    public actual open val ctrlKey : Boolean
    public actual open val shiftKey: Boolean
    public actual open val altKey  : Boolean
    public actual open val metaKey : Boolean
    public actual open val button  : Short
    public actual open val buttons : Short
}

public actual open external class PointerEvent: MouseEvent {
    public actual val pointerId  : Int
    public actual val pointerType: String
}

public actual open external class WheelEvent: MouseEvent {
    public actual val deltaX: Double
    public actual val deltaY: Double
}

public actual abstract external class TouchList {
    public actual abstract val length: Int
}

public actual open external class TouchEvent: UIEvent {
    public actual val touches: TouchList
}

public actual external class InputEvent: UIEvent

public actual external class FocusEvent: UIEvent {
    public actual val relatedTarget: EventTarget?
}

public actual external class DragEvent: MouseEvent {
    internal actual val dataTransfer: DataTransfer?
}