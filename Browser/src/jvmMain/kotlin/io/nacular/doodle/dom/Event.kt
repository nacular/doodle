package io.nacular.doodle.dom

/**
 * Created by Nicholas Eddy on 3/12/20.
 */
public actual abstract class EventTarget: JsAny

public actual open class Event actual constructor(): JsAny {
    public actual constructor(name: String): this()

    public actual val target: EventTarget? = null

    public actual fun stopPropagation() {}
    public actual fun preventDefault () {}
}

public actual open class UIEvent : Event() {
    public actual val detail: Int = 0
}

public actual open class KeyboardEvent: UIEvent() {
    public actual val ctrlKey : Boolean = false
    public actual val shiftKey: Boolean = false
    public actual val altKey  : Boolean = false
    public actual val metaKey : Boolean = false
    public actual val keyCode : Int     = 0
    public actual val key     : String? = ""
    public actual val code    : String? = ""
}

public actual open class MouseEvent: UIEvent() {
    public actual open val pageX   : Double  = 0.0
    public actual open val pageY   : Double  = 0.0
    public actual open val clientX : Int     = 0
    public actual open val clientY : Int     = 0
    public actual open val ctrlKey : Boolean = false
    public actual open val shiftKey: Boolean = false
    public actual open val altKey  : Boolean = false
    public actual open val metaKey : Boolean = false
    public actual open val button  : Short   = 0.toShort()
    public actual open val buttons : Short   = 0.toShort()
}

public actual open class WheelEvent: MouseEvent() {
    public actual val deltaY: Double = 0.0
    public actual val deltaX: Double = 0.0
}

public actual open class PointerEvent: MouseEvent() {
    public actual val pointerId  : Int    = 0
    public actual val pointerType: String = ""
}

public actual abstract class TouchList {
    public actual abstract val length: Int
}

public actual open class TouchEvent: UIEvent() {
    public actual val touches: TouchList = object: TouchList() {
        override val length = 0
    }
}

public actual class InputEvent: UIEvent()

public actual class FocusEvent: UIEvent() {
    public actual val relatedTarget: EventTarget? = null
}

public actual class DragEvent: MouseEvent() {
    internal actual val dataTransfer: DataTransfer? = null
}