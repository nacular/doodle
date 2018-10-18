package com.nectar.doodle.system.impl

import com.nectar.doodle.event.KeyState
import com.nectar.doodle.system.KeyInputService
import com.nectar.doodle.system.KeyInputService.Listener
import com.nectar.doodle.system.KeyInputService.Postprocessor
import com.nectar.doodle.system.KeyInputService.Preprocessor

/**
 * Created by Nicholas Eddy on 3/10/18.
 */
class KeyInputServiceImpl(private val strategy: KeyInputServiceStrategy): KeyInputService {

    private var started        = false
    private var notifying      = false
    private var listeners      = mutableSetOf<Listener>()
    private var preprocessors  = mutableSetOf<Preprocessor>()
    private var postprocessors = mutableSetOf<Postprocessor>()


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

    private val unused: Boolean get() = listeners.isEmpty() && preprocessors.isEmpty() && postprocessors.isEmpty()

    private fun notifyKeyEvent(keyState: KeyState): Boolean {
        notifying = true

        for (preprocessor in preprocessors) {
            if (preprocessor(keyState)) {
                return true
            }
        }

        for (listener in listeners) {
            if (listener(keyState)) {
                return true
            }
        }

        for (postprocessor in postprocessors) {
            if (postprocessor(keyState)) {
                return true
            }
        }

        notifying = false

        return false
    }

    private fun startUp() {
        if (!started) {
            strategy.startUp(object : KeyInputServiceStrategy.EventHandler {
                override fun invoke(event: KeyState): Boolean {
                    return this@KeyInputServiceImpl.notifyKeyEvent(event)
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
