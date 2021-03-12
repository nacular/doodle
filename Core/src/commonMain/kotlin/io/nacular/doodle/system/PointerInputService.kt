package io.nacular.doodle.system


public interface PointerInputService {
    public var cursor     : Cursor?
    public var toolTipText: String

    public operator fun plusAssign (listener: Listener)
    public operator fun minusAssign(listener: Listener)

    public operator fun plusAssign (preprocessor: Preprocessor)
    public operator fun minusAssign(preprocessor: Preprocessor)

    public interface Listener {
        public fun changed(event: SystemPointerEvent)
    }

    public interface Preprocessor {
        public fun preprocess(event: SystemPointerEvent) {}
    }
}
