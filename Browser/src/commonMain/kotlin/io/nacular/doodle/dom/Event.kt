package io.nacular.doodle.dom

/**
 * Created by Nicholas Eddy on 3/12/20.
 */
public expect abstract class EventTarget

public expect open class Event {
    public val target: EventTarget?

    public fun stopPropagation()
    public fun preventDefault ()
}

public expect open class UIEvent: Event

public expect open class KeyboardEvent: UIEvent {
    public val ctrlKey : Boolean
    public val shiftKey: Boolean
    public val altKey  : Boolean
    public val metaKey : Boolean
    public val keyCode : Int
    public val key     : String
    public val code    : String
}

public expect open class MouseEvent: UIEvent {
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

public expect open class PointerEvent: MouseEvent {
    public val pointerId  : Int
    public val pointerType: String
}

public expect open class WheelEvent: MouseEvent {
    public val deltaX: Double
    public val deltaY: Double
}

public expect abstract class TouchList {
    public abstract val length: Int
}

public expect open class TouchEvent: UIEvent {
    public val touches: TouchList
}