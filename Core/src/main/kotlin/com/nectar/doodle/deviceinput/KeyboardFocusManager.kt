package com.nectar.doodle.deviceinput

import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.event.KeyEvent
import com.nectar.doodle.event.KeyState
import com.nectar.doodle.focus.FocusManager
import com.nectar.doodle.focus.FocusTraversalPolicy.TraversalType
import com.nectar.doodle.focus.FocusTraversalPolicy.TraversalType.Backward
import com.nectar.doodle.focus.FocusTraversalPolicy.TraversalType.Downward
import com.nectar.doodle.focus.FocusTraversalPolicy.TraversalType.Forward
import com.nectar.doodle.focus.FocusTraversalPolicy.TraversalType.Upward
import com.nectar.doodle.system.KeyInputService

/**
 * Created by Nicholas Eddy on 3/10/18.
 */

interface Listener {
    operator fun invoke(keyEvent: KeyEvent)
}

interface Preprocessor {
    operator fun invoke(keyEvent: KeyEvent)
}

interface Postprocessor {
    operator fun invoke(keyEvent: KeyEvent)
}


class KeyboardFocusManager(
                    keyInputService     : KeyInputService,
        private val focusManager        : FocusManager,
        private val defaultTraversalKeys: Map<TraversalType, Set<KeyState>>): KeyInputService.Listener {

    private var preprocessors  = mutableListOf<Preprocessor >()
    private var postprocessors = mutableListOf<Postprocessor>()

    init {
        keyInputService += this
    }

    override operator fun invoke(keyState: KeyState): Boolean {
        focusManager.focusOwner?.let { focusOwner ->
            val keyEvent = KeyEvent(focusOwner, keyState)

            preprocessKeyEvent(keyEvent)

            if (!keyEvent.consumed) {
                handleKeyEvent(focusOwner, keyEvent)
            }

            if (!keyEvent.consumed) {
                postprocessKeyEvent(keyEvent)
            }

            if (keyEvent.consumed) {
                keyEvent.consume()
            }

            return keyEvent.consumed
        }

        return false
    }

    operator fun plusAssign (preprocessor: Preprocessor) { preprocessors.add   (preprocessor) }
    operator fun minusAssign(preprocessor: Preprocessor) { preprocessors.remove(preprocessor) }

    operator fun plusAssign (postprocessor: Postprocessor) { postprocessors.add   (postprocessor) }
    operator fun minusAssign(postprocessor: Postprocessor) { postprocessors.remove(postprocessor) }

    private fun handleKeyEvent(gizmo: Gizmo, keyEvent: KeyEvent) {
        val keyState = keyEvent.run { KeyState(code, char, modifiers, type) }

        val upwardKeyEvents   = gizmo[Upward  ] ?: defaultTraversalKeys[Upward  ]
        val forwardKeyEvents  = gizmo[Forward ] ?: defaultTraversalKeys[Forward ]
        val backwardKeyEvents = gizmo[Backward] ?: defaultTraversalKeys[Backward]
        val downwardKeyEvents = if (gizmo.isFocusCycleRoot_) gizmo[Downward] else null

        if (forwardKeyEvents?.contains(keyState) == true) {
            focusManager.moveFocusForward(gizmo)
            keyEvent.consume()
        } else if (backwardKeyEvents?.contains(keyState) == true) {
            focusManager.moveFocusBackward(gizmo)
            keyEvent.consume()
        } else if (upwardKeyEvents?.contains(keyState) == true) {
            focusManager.moveFocusUpward(gizmo)
            keyEvent.consume()
        } else if (downwardKeyEvents?.contains(keyState) == true) {
            focusManager.moveFocusDownward(gizmo)
            keyEvent.consume()
        } else {
            var g: Gizmo? = gizmo

            while (g != null) {
                if (g.monitorsKeyboard) {
                    g.handleKeyEvent_(keyEvent)
                    keyEvent.consume()
                    break
                } else {
                    g = gizmo.parent
                }
            }
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
