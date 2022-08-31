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
         * Creates a listener that delegates to the provided lambdas for each event type.
         *
         * @param pressed invoked on pointer-exited
         * @param released invoked on pointer-released
         */
        public fun on(
            pressed : (KeyEvent) -> Unit = {},
            released: (KeyEvent) -> Unit = {},
        ): KeyListener = object: KeyListener {
            inline override fun pressed (event: KeyEvent) = pressed (event)
            inline override fun released(event: KeyEvent) = released(event)
        }

        /**
         * @param block invoked on key-pressed
         * @return a Listener that calls [block] on key-pressed.
         */
        public inline fun pressed(noinline block: (event: KeyEvent) -> Unit): KeyListener = on(pressed = block)

        /**
         * @param block invoked on key-released
         * @return a Listener that calls [block] on key-released.
         */
        public inline fun released(noinline block: (event: KeyEvent) -> Unit): KeyListener = on(released = block)
    }
}