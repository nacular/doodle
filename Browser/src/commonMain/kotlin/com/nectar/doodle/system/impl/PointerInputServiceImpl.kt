package com.nectar.doodle.system.impl

import com.nectar.doodle.geometry.Point
import com.nectar.doodle.system.Cursor
import com.nectar.doodle.system.PointerInputService
import com.nectar.doodle.system.PointerInputService.Listener
import com.nectar.doodle.system.PointerInputService.Preprocessor
import com.nectar.doodle.system.SystemPointerEvent


internal class PointerInputServiceImpl(private val strategy: PointerInputServiceStrategy): PointerInputService {

    override val pointerLocation: Point
        get() = strategy.pointerLocation

    override var cursor: Cursor
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
            strategy.startUp(object: PointerInputServiceStrategy.EventHandler {
                override fun handle(event: SystemPointerEvent) {
                    notifyPointerEvent(event)
                }
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

    private fun notifyPointerEvent(event: SystemPointerEvent) {
        preprocessors.takeWhile { !event.consumed }.forEach { it.preprocess(event) }
        listeners.takeWhile     { !event.consumed }.forEach { it.changed   (event) }
    }
}
