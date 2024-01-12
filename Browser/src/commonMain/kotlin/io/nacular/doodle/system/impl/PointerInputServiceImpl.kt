package io.nacular.doodle.system.impl

import io.nacular.doodle.core.Display
import io.nacular.doodle.system.Cursor
import io.nacular.doodle.system.PointerInputService
import io.nacular.doodle.system.PointerInputService.Listener
import io.nacular.doodle.system.PointerInputService.Preprocessor
import io.nacular.doodle.system.SystemPointerEvent
import io.nacular.doodle.system.impl.PointerInputServiceStrategy.EventHandler


internal class PointerInputServiceImpl(private val strategy: PointerInputServiceStrategy): PointerInputService {
    override fun getCursor(display: Display                 ): Cursor? = strategy.cursor
    override fun setCursor(display: Display, cursor: Cursor?) { strategy.cursor = cursor }

    override fun getToolTipText(display: Display) = strategy.toolTipText
    override fun setToolTipText(display: Display, text: String) { strategy.toolTipText = text }

    private var started       = false
    private val listeners     = mutableSetOf<Listener>()
    private val preprocessors = mutableSetOf<Preprocessor>()

    override fun addListener   (display: Display, listener: Listener) { listeners.plusAssign (listener); if (listeners.size == 1) startUp() }
    override fun removeListener(display: Display, listener: Listener) { listeners.minusAssign(listener); shutdown()                         }

    override fun addPreprocessor   (display: Display, preprocessor: Preprocessor) { preprocessors.plusAssign (preprocessor); if (preprocessors.size == 1) startUp() }
    override fun removePreprocessor(display: Display, preprocessor: Preprocessor) { preprocessors.minusAssign(preprocessor); shutdown()                             }

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
