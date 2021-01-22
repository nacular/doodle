package io.nacular.doodle.system

import io.nacular.doodle.event.KeyState


public interface KeyInputService {

    public operator fun plusAssign (listener: Listener)
    public operator fun minusAssign(listener: Listener)

    public operator fun plusAssign (processor: Preprocessor)
    public operator fun minusAssign(processor: Preprocessor)

    public operator fun plusAssign (processor: Postprocessor)
    public operator fun minusAssign(processor: Postprocessor)

    public interface Listener {
        public operator fun invoke(keyState: KeyState): Boolean
    }

    public interface Preprocessor {
        public operator fun invoke(keyState: KeyState): Boolean
    }

    public interface Postprocessor {
        public operator fun invoke(keyState: KeyState): Boolean
    }
}
