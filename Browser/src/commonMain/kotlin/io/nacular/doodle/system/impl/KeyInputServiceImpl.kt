package io.nacular.doodle.system.impl

import io.nacular.doodle.dom.EventTarget
import io.nacular.doodle.event.KeyState
import io.nacular.doodle.system.KeyInputService
import io.nacular.doodle.system.KeyInputService.KeyResponse
import io.nacular.doodle.system.KeyInputService.KeyResponse.Consumed
import io.nacular.doodle.system.KeyInputService.KeyResponse.Ignored
import io.nacular.doodle.system.KeyInputService.Listener
import io.nacular.doodle.system.KeyInputService.Postprocessor
import io.nacular.doodle.system.KeyInputService.Preprocessor
import io.nacular.doodle.system.impl.KeyInputServiceStrategy.EventHandler

/**
 * Created by Nicholas Eddy on 3/10/18.
 */
internal class KeyInputServiceImpl(private val strategy: KeyInputServiceStrategy): KeyInputService {
    interface RawListener {
        operator fun invoke(keyState: KeyState, target: EventTarget?): KeyResponse
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

    private fun notifyKeyEvent(keyState: KeyState, target: EventTarget?): KeyResponse {
        notifying = true
        var response = Ignored

        rawListeners.forEach {
            response = it(keyState, target)
            if (response == Consumed) {
                return Consumed
            }
        }

        preprocessors.forEach {
            response = it(keyState)
            if (response == Consumed) {
                return Consumed
            }
        }

        listeners.forEach {
            response = it(keyState)
            if (response == Consumed) {
                return Consumed
            }
        }

        postprocessors.forEach {
            response = it(keyState)
            if (response == Consumed) {
                return Consumed
            }
        }

        notifying = false

        return response
    }

    private fun startUp() {
        if (!started) {
            strategy.startUp(object: EventHandler {
                override fun invoke(event: KeyState, target: EventTarget?) = this@KeyInputServiceImpl.notifyKeyEvent(event, target)
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
