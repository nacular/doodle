package com.nectar.doodle.system.impl

import com.nectar.doodle.dom.EventTarget
import com.nectar.doodle.event.KeyState
import com.nectar.doodle.system.KeyInputService
import com.nectar.doodle.system.KeyInputService.Listener
import com.nectar.doodle.system.KeyInputService.Postprocessor
import com.nectar.doodle.system.KeyInputService.Preprocessor
import com.nectar.doodle.system.impl.KeyInputServiceStrategy.EventHandler

/**
 * Created by Nicholas Eddy on 3/10/18.
 */
internal class KeyInputServiceImpl(private val strategy: KeyInputServiceStrategy): KeyInputService {
    interface RawListener {
        operator fun invoke(keyState: KeyState, target: EventTarget?): Boolean
    }

    private var started        = false
    private var notifying      = false
    private var listeners      = mutableSetOf<Listener>     ()
    private var preprocessors  = mutableSetOf<Preprocessor> ()
    private var postprocessors = mutableSetOf<Postprocessor>()
    private var rawListeners   = mutableSetOf<RawListener>  ()

    override fun plusAssign(listener: Listener) {
        if (!started) {
            startUp()
        }

        listeners.add(listener)
    }

    override fun minusAssign(listener: Listener) {
        listeners.remove(listener)

        if (unused) {
            shutdown()
        }
    }

    override fun plusAssign(processor: Preprocessor) {
        if (!started) {
            startUp()
        }

        preprocessors.add(processor)
    }

    override fun minusAssign(processor: Preprocessor) {
        preprocessors.remove(processor)

        if (unused) {
            shutdown()
        }
    }

    override fun plusAssign(processor: Postprocessor) {
        if (!started) {
            startUp()
        }

        postprocessors.add(processor)
    }

    override fun minusAssign(processor: Postprocessor) {
        postprocessors.remove(processor)

        if (unused) {
            shutdown()
        }
    }

    operator fun plusAssign(listener: RawListener) {
        if (!started) {
            startUp()
        }

        rawListeners.add(listener)
    }

    operator fun minusAssign(listener: RawListener) {
        rawListeners.remove(listener)

        if (unused) {
            shutdown()
        }
    }

    private val unused: Boolean get() = listeners.isEmpty() && preprocessors.isEmpty() && postprocessors.isEmpty() && rawListeners.isEmpty()

    private fun notifyKeyEvent(keyState: KeyState, target: EventTarget?): Boolean {
        notifying = true

        rawListeners.forEach {
            if (it(keyState, target)) {
                return true
            }
        }

        preprocessors.forEach {
            if (it(keyState)) {
                return true
            }
        }

        listeners.forEach {
            if (it(keyState)) {
                return true
            }
        }

        postprocessors.forEach {
            if (it(keyState)) {
                return true
            }
        }

        notifying = false

        return false
    }

    private fun startUp() {
        if (!started) {
            strategy.startUp(object: EventHandler {
                override fun invoke(event: KeyState, target: EventTarget?): Boolean {
                    return this@KeyInputServiceImpl.notifyKeyEvent(event, target)
                }
            })

            started = true
        }
    }

    private fun shutdown() {
        if (started) {
            strategy.shutdown()

            started = false
        }
    }
}
