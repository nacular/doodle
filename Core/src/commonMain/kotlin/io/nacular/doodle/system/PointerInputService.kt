package io.nacular.doodle.system


public interface PointerInputService {
    public var cursor     : Cursor?
    public var toolTipText: String

    public operator fun plusAssign (listener: Listener)
    public operator fun minusAssign(listener: Listener)

    public operator fun plusAssign (preprocessor: Preprocessor)
    public operator fun minusAssign(preprocessor: Preprocessor)

    public interface Listener {
        @Deprecated("Use invoke instead", replaceWith = ReplaceWith("invoke(event)"))
        public fun changed(event: SystemPointerEvent) {}

        public operator fun invoke(event: SystemPointerEvent): Unit = changed(event)
    }

    public interface Preprocessor {
        @Deprecated("Use invoke instead", replaceWith = ReplaceWith("invoke(event)"))
        public fun preprocess(event: SystemPointerEvent) {}

        public operator fun invoke(event: SystemPointerEvent): Unit = preprocess(event)
    }
}
