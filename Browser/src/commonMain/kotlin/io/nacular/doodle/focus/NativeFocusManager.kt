package io.nacular.doodle.focus

import io.nacular.doodle.focus.impl.FocusManagerImpl
import io.nacular.doodle.utils.observable

/**
 * Tracks whether an element within the App has focus. This is different to
 * [FocusManager][io.nacular.doodle.focus.FocusManager], which tracks [View]
 * focus. The use case is for embedded or nested apps where focus in the
 * Browser must be explicitly managed.
 */
public interface NativeFocusManager {
    public var hasFocusOwner: Boolean
}

internal class NativeFocusManagerImpl(private val focusManager: FocusManagerImpl): NativeFocusManager {
    override var hasFocusOwner by observable(false) { _,new ->
        when {
            new && !focusManager.enabled -> focusManager.enabled = true
        }
    }
}