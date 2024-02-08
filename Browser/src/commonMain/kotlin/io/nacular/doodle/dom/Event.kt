@file:Suppress("EXPECTED_EXTERNAL_DECLARATION", "WRONG_MODIFIER_TARGET")

package io.nacular.doodle.dom

internal expect abstract external class EventTarget: JsAny

internal expect open external class Event(): JsAny {
    constructor(name: String)

    val target: EventTarget?

    fun stopPropagation()
    fun preventDefault ()
}

internal expect open external class UIEvent: Event {
    val detail: Int
}

internal expect open external class KeyboardEvent: UIEvent {
    val ctrlKey : Boolean
    val shiftKey: Boolean
    val altKey  : Boolean
    val metaKey : Boolean
    val keyCode : Int
    val key     : String? // Chrome makes these null on form autofill for some reason
    val code    : String? // Chrome makes these null on form autofill for some reason
}

internal expect open external class MouseEvent: UIEvent {
    open val pageX   : Double
    open val pageY   : Double
    open val clientX : Int
    open val clientY : Int
    open val ctrlKey : Boolean
    open val shiftKey: Boolean
    open val altKey  : Boolean
    open val metaKey : Boolean
    open val button  : Short
    open val buttons : Short
}

internal expect open external class PointerEvent: MouseEvent {
    val pointerId  : Int
    val pointerType: String
}

internal expect open external class WheelEvent: MouseEvent {
    val deltaX: Double
    val deltaY: Double
}

internal expect abstract external class TouchList {
    abstract val length: Int
}

internal expect open external class TouchEvent: UIEvent {
    val touches: TouchList
}

internal expect external class InputEvent: UIEvent

internal expect external class FocusEvent: UIEvent {
    val relatedTarget: EventTarget?
}

internal expect external class DragEvent: MouseEvent {
    val dataTransfer: DataTransfer?
}