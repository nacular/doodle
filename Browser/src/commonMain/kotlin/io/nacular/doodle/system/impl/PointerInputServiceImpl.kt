package io.nacular.doodle.system.impl

import io.nacular.doodle.system.Cursor
import io.nacular.doodle.system.PointerInputService
import io.nacular.doodle.system.PointerInputService.Listener
import io.nacular.doodle.system.PointerInputService.Preprocessor
import io.nacular.doodle.system.SystemPointerEvent
import io.nacular.doodle.system.impl.PointerInputServiceStrategy.EventHandler


internal class PointerInputServiceImpl(private val strategy: PointerInputServiceStrategy): PointerInputService {
    override var cursor: Cursor?
        get(     ) = strategy.cursor
        set(value) { strategy.cursor = value }

    override var toolTipText: String
        get(     ) = strategy.toolTipText
        set(value) { strategy.toolTipText = value }

    private var started       = false
    private val listeners     = mutableSetOf<Listener>()
    private val preprocessors = mutableSetOf<Preprocessor>()

    override operator fun plusAssign (listener: Listener) { listeners.plusAssign (listener); if (listeners.size == 1) startUp() }
    override operator fun minusAssign(listener: Listener) { listeners.minusAssign(listener); shutdown()                         }

    override operator fun plusAssign (preprocessor: Preprocessor) { preprocessors.plusAssign (preprocessor); if (preprocessors.size == 1) startUp() }
    override operator fun minusAssign(preprocessor: Preprocessor) { preprocessors.minusAssign(preprocessor); shutdown()                             }

    private fun startUp() {
        if (!started) {
            strategy.startUp(object: EventHandler {
                override fun invoke(event: SystemPointerEvent) = notifyPointerEvent(event)
            })

            started = true
        }
    }

    private fun shutdown() {
        if (started && listeners.isEmpty() && preprocessors.isEmpty()) {
            strategy.shutdown()

            started = false
        }
    }

    private fun notifyPointerEvent(event: SystemPointerEvent): Boolean {
        preprocessors.takeWhile { !event.consumed }.forEach { it(event) }
        listeners.takeWhile     { !event.consumed }.forEach { it(event) }

        return event.consumed
    }
}
