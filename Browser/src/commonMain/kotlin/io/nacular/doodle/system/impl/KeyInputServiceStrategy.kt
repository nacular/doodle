package io.nacular.doodle.system.impl

import io.nacular.doodle.dom.EventTarget
import io.nacular.doodle.event.KeyState

/**
 * Created by Nicholas Eddy on 3/10/18.
 */
internal interface KeyInputServiceStrategy {
    fun startUp (handler: EventHandler)
    fun shutdown()

    interface EventHandler {
        operator fun invoke(event: KeyState, target: EventTarget?): Boolean
    }
}
