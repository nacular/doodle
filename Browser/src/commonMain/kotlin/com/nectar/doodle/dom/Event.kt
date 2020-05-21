package com.nectar.doodle.dom

/**
 * Created by Nicholas Eddy on 3/12/20.
 */
expect abstract class EventTarget

expect open class Event {
    val target: EventTarget?

    fun stopPropagation()
    fun preventDefault ()
}

expect open class UIEvent: Event

expect open class KeyboardEvent: UIEvent {
    val ctrlKey : Boolean
    val shiftKey: Boolean
    val altKey  : Boolean
    val metaKey : Boolean
    val keyCode : Int
}

expect open class MouseEvent: UIEvent {
    open val clientX : Int
    open val clientY : Int
    open val ctrlKey : Boolean
    open val shiftKey: Boolean
    open val altKey  : Boolean
    open val metaKey : Boolean
    open val button  : Short
    open val buttons : Short
}

expect open class PointerEvent: MouseEvent {
}

expect open class WheelEvent: MouseEvent {
    val deltaY: Double
    val deltaX: Double
}

