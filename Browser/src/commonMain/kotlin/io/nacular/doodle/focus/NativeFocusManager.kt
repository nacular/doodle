package io.nacular.doodle.focus

/**
 * Tracks whether an element within the App has focus. This is different to
 * [FocusManager][io.nacular.doodle.focus.FocusManager], which tracks [View]
 * focus. The use case is for embedded or nested apps where focus in the
 * Browser must be explicitly managed.
 */
interface NativeFocusManager {
    var hasFocusOwner: Boolean
}

class NativeFocusManagerImpl: NativeFocusManager {
    override var hasFocusOwner = false
}