package io.nacular.doodle.theme.native

import io.nacular.doodle.core.View
import io.nacular.doodle.deviceinput.EventPreprocessor
import io.nacular.doodle.event.PointerEvent


internal interface NativeMouseHandler {
    operator fun invoke(event: PointerEvent)
}

internal class NativePointerPreprocessor: EventPreprocessor {
    private val handlers: MutableMap<View, NativeMouseHandler> = mutableMapOf()

    override fun invoke(pointerEvent: PointerEvent) {
        handlers[pointerEvent.target]?.invoke(pointerEvent)
    }

    fun get(view: View) = handlers[view]
    fun set(view: View, handler: NativeMouseHandler) {
        handlers[view] = handler
    }

    fun remove(view: View): NativeMouseHandler? = handlers.remove(view)
}