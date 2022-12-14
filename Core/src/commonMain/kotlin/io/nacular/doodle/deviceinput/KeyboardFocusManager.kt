package io.nacular.doodle.deviceinput

import io.nacular.doodle.core.Internal
import io.nacular.doodle.core.View
import io.nacular.doodle.event.KeyEvent
import io.nacular.doodle.event.KeyState
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.focus.FocusTraversalPolicy.TraversalType
import io.nacular.doodle.focus.FocusTraversalPolicy.TraversalType.Backward
import io.nacular.doodle.focus.FocusTraversalPolicy.TraversalType.Downward
import io.nacular.doodle.focus.FocusTraversalPolicy.TraversalType.Forward
import io.nacular.doodle.focus.FocusTraversalPolicy.TraversalType.Upward
import io.nacular.doodle.system.KeyInputService
import io.nacular.doodle.system.KeyInputService.KeyResponse
import io.nacular.doodle.system.KeyInputService.KeyResponse.Consumed
import io.nacular.doodle.system.KeyInputService.KeyResponse.Ignored
import io.nacular.doodle.system.KeyInputService.Listener
import io.nacular.doodle.utils.contains

/**
 * Created by Nicholas Eddy on 3/10/18.
 */

public interface Listener {
    public operator fun invoke(keyEvent: KeyEvent)
}

public interface Preprocessor {
    public operator fun invoke(keyEvent: KeyEvent)
}

public interface Postprocessor {
    public operator fun invoke(keyEvent: KeyEvent)
}

@Internal
public interface KeyboardFocusManager {
    public fun shutdown()
}

@Internal
public class KeyboardFocusManagerImpl(
        private val keyInputService     : KeyInputService,
        private val focusManager        : FocusManager,
        private val defaultTraversalKeys: Map<TraversalType, Set<KeyState>>): KeyboardFocusManager, Listener {

    private var preprocessors  = mutableListOf<Preprocessor >()
    private var postprocessors = mutableListOf<Postprocessor>()

    init {
        keyInputService += this
    }

    override fun shutdown() {
        keyInputService -= this
    }

    override operator fun invoke(keyState: KeyState): KeyResponse {
        focusManager.focusOwner?.let { focusOwner ->
            val keyEvent = KeyEvent(focusOwner, keyState)

            preprocessKeyEvent(keyEvent)

            if (!keyEvent.consumed) {
                handleKeyEvent(focusOwner, keyState, keyEvent)
            }

            if (!keyEvent.consumed) {
                postprocessKeyEvent(keyEvent)
            }

            if (keyEvent.consumed) return Consumed
        }

        return Ignored
    }

    public operator fun plusAssign (preprocessor: Preprocessor) { preprocessors.add   (preprocessor) }
    public operator fun minusAssign(preprocessor: Preprocessor) { preprocessors.remove(preprocessor) }

    public operator fun plusAssign (postprocessor: Postprocessor) { postprocessors.add   (postprocessor) }
    public operator fun minusAssign(postprocessor: Postprocessor) { postprocessors.remove(postprocessor) }

    private fun handleKeyEvent(view: View, keyState: KeyState, keyEvent: KeyEvent) {
        val upwardKeyEvents   = view[Upward  ] ?: defaultTraversalKeys[Upward  ]
        val forwardKeyEvents  = view[Forward ] ?: defaultTraversalKeys[Forward ]
        val backwardKeyEvents = view[Backward] ?: defaultTraversalKeys[Backward]
        val downwardKeyEvents = if (view.isFocusCycleRoot_) view[Downward] ?: defaultTraversalKeys[Downward] else null

        when (keyState) {
            in forwardKeyEvents  -> { focusManager.moveFocusForward (view); keyEvent.consume() }
            in backwardKeyEvents -> { focusManager.moveFocusBackward(view); keyEvent.consume() }
            in upwardKeyEvents   -> { focusManager.moveFocusUpward  (view); keyEvent.consume() }
            in downwardKeyEvents -> { focusManager.moveFocusDownward(view); keyEvent.consume() }
            else                 -> view.handleKeyEvent_(keyEvent)
        }
    }

    private fun preprocessKeyEvent(event: KeyEvent) {
        preprocessors.forEach {
            it(event)
            if (event.consumed) {
                return
            }
        }
    }

    private fun postprocessKeyEvent(event: KeyEvent) {
        postprocessors.forEach {
            it(event)
            if (event.consumed) {
                return
            }
        }
    }
}
