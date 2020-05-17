package com.nectar.doodle.system.impl

import com.nectar.doodle.dom.EventTarget
import com.nectar.doodle.event.KeyState

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
