package io.nacular.doodle.event


/**
 * Classes that implement this interface are informed of
 * KeyEvents when they register with a source that fires
 * these events.
 */
public interface KeyListener {
    /**
     * Informs listener that a key was pressed.
     *
     * @param event The event
     */
    public fun pressed(event: KeyEvent) {}

    /**
     * Informs listener that a key was released.
     *
     * @param event The event
     */
    public fun released(event: KeyEvent) {}

    public companion object {
        /**
         * @param block invoked on key-pressed
         * @return a Listener that calls [block] on key-pressed.
         */
        public inline fun pressed(crossinline block: (event: KeyEvent) -> Unit): KeyListener = object: KeyListener {
            override fun pressed(event: KeyEvent) = block(event)
        }

        /**
         * @param block invoked on key-released
         * @return a Listener that calls [block] on key-released.
         */
        public inline fun released(crossinline block: (event: KeyEvent) -> Unit): KeyListener = object: KeyListener {
            override fun released(event: KeyEvent) = block(event)
        }
    }
}