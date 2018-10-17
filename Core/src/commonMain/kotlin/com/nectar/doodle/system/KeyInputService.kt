package com.nectar.doodle.system

import com.nectar.doodle.event.KeyState


interface KeyInputService {

    operator fun plusAssign (listener: Listener)
    operator fun minusAssign(listener: Listener)

    operator fun plusAssign (processor: Preprocessor)
    operator fun minusAssign(processor: Preprocessor)

    operator fun plusAssign (processor: Postprocessor)
    operator fun minusAssign(processor: Postprocessor)

    interface Listener {
        operator fun invoke(keyState: KeyState): Boolean
    }

    interface Preprocessor {
        operator fun invoke(keyState: KeyState): Boolean
    }

    interface Postprocessor {
        operator fun invoke(keyState: KeyState): Boolean
    }
}
