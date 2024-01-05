@file:Suppress("EXPECTED_EXTERNAL_DECLARATION", "WRONG_MODIFIER_TARGET")

package io.nacular.doodle.dom

/**
 * Created by Nicholas Eddy on 3/12/20.
 */
public expect abstract external class EventTarget: JsAny

public expect open external class Event(): JsAny {
    public constructor(name: String)

    public val target: EventTarget?

    public fun stopPropagation()
    public fun preventDefault ()
}

public expect open external class UIEvent: Event {
    public val detail: Int
}

public expect open external class KeyboardEvent: UIEvent {
    public val ctrlKey : Boolean
    public val shiftKey: Boolean
    public val altKey  : Boolean
    public val metaKey : Boolean
    public val keyCode : Int
    public val key     : String
    public val code    : String
}

public expect open external class MouseEvent: UIEvent {
    public open val pageX   : Double
    public open val pageY   : Double
    public open val clientX : Int
    public open val clientY : Int
    public open val ctrlKey : Boolean
    public open val shiftKey: Boolean
    public open val altKey  : Boolean
    public open val metaKey : Boolean
    public open val button  : Short
    public open val buttons : Short
}

public expect open external class PointerEvent: MouseEvent {
    public val pointerId  : Int
    public val pointerType: String
}

public expect open external class WheelEvent: MouseEvent {
    public val deltaX: Double
    public val deltaY: Double
}

public expect abstract external class TouchList {
    public abstract val length: Int
}

public expect open external class TouchEvent: UIEvent {
    public val touches: TouchList
}

public expect external class InputEvent: UIEvent

public expect external class FocusEvent: UIEvent {
    public val relatedTarget: EventTarget?
}

public expect external class DragEvent: MouseEvent {
    internal val dataTransfer: DataTransfer?
}