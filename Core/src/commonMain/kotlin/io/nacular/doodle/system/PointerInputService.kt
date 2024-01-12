package io.nacular.doodle.system

import io.nacular.doodle.core.Display

public interface PointerInputService {
    public fun getCursor     (display: Display                 ): Cursor?
    public fun setCursor     (display: Display, cursor: Cursor?)
    public fun getToolTipText(display: Display                 ): String
    public fun setToolTipText(display: Display, text: String   )

    public fun addListener   (display: Display, listener: Listener)
    public fun removeListener(display: Display, listener: Listener)

    public fun addPreprocessor   (display: Display, preprocessor: Preprocessor)
    public fun removePreprocessor(display: Display, preprocessor: Preprocessor)

    public interface Listener {
        public operator fun invoke(event: SystemPointerEvent)
    }

    public interface Preprocessor {
        public operator fun invoke(event: SystemPointerEvent)
    }
}
