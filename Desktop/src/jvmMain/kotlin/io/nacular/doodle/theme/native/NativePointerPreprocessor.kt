package io.nacular.doodle.theme.native

import io.nacular.doodle.core.View
import io.nacular.doodle.deviceinput.EventPreprocessor
import io.nacular.doodle.event.PointerEvent


internal typealias NativePointerHandler = (event: PointerEvent) -> Unit

internal class NativePointerPreprocessor: EventPreprocessor {
    private val handlers: MutableMap<View, NativePointerHandler> = mutableMapOf()

    override fun invoke(pointerEvent: PointerEvent) {
        handlers[pointerEvent.target]?.invoke(pointerEvent)
    }

    fun get(view: View) = handlers[view]
    fun set(view: View, handler: NativePointerHandler) {
        handlers[view] = handler
    }

    fun remove(view: View): NativePointerHandler? = handlers.remove(view)
}