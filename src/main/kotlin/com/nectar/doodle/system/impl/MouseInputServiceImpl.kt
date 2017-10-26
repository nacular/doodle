package com.nectar.doodle.system.impl

import com.nectar.doodle.geometry.Point
import com.nectar.doodle.system.Cursor
import com.nectar.doodle.system.MouseInputService
import com.nectar.doodle.system.MouseInputService.Listener
import com.nectar.doodle.system.MouseInputService.Preprocessor
import com.nectar.doodle.system.SystemMouseEvent
import com.nectar.doodle.system.SystemMouseWheelEvent


class MouseInputServiceImpl(private val strategy: MouseInputServiceStrategy): MouseInputService {

    override val mouseLocation: Point
        get() = strategy.mouseLocation

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

//    fun removeListener(aListener: Listener?) {
//        if (aListener != null) {
//            Service.locator().getDelayedDispatcher().add(object : Command() {
//                fun execute() {
//                    if (listeners != null) {
//                        listeners!!.remove(aListener)
//
//                        if (listeners!!.isEmpty()) {
//                            listeners = null
//
//                            if (preprocessors!!.isEmpty()) {
//                                shutdown()
//                            }
//                        }
//                    }
//                }
//            })
//        }
//    }
//
//    fun removePreprocessor(aPreprocessor: Preprocessor?) {
//        if (aPreprocessor != null) {
//            Service.locator().getDelayedDispatcher().add(object : Command() {
//                fun execute() {
//                    if (preprocessors != null) {
//                        preprocessors!!.remove(aPreprocessor)
//
//                        if (preprocessors!!.isEmpty()) {
//                            preprocessors = null
//
//                            if (listeners!!.isEmpty()) {
//                                shutdown()
//                            }
//                        }
//                    }
//                }
//            })
//        }
//    }

    private fun startUp() {
        if (!started) {
            strategy.startUp(object: MouseInputServiceStrategy.EventHandler {
                override fun handle(event: SystemMouseEvent) {
                    this@MouseInputServiceImpl.notifyMouseEvent(event)
                }

                override fun handle(event: SystemMouseWheelEvent) {
                    this@MouseInputServiceImpl.notifyMouseWheelEvent(event)
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

    private fun notifyMouseEvent(event: SystemMouseEvent) {
        preprocessors.takeWhile { !event.consumed }.forEach { it.preprocess(event) }

        listeners.takeWhile { !event.consumed }.forEach { it.changed(event) }
    }

    private fun notifyMouseWheelEvent(event: SystemMouseWheelEvent) {
        preprocessors.takeWhile { !event.consumed }.forEach { it.preprocess(event) }

        listeners.takeWhile { !event.consumed }.forEach { it.changed(event) }
    }
}
