package io.nacular.doodle.event


/**
 * Classes that implement this interface are informed of
 * KeyEvents when they register with a source that fires
 * these events.
 */
interface KeyListener {
    /**
     * Informs listener that a key was pressed.
     *
     * @param event The event
     */
    fun pressed(event: KeyEvent) {}

    /**
     * Informs listener that a key was released.
     *
     * @param event The event
     */
    fun released(event: KeyEvent) {}

    companion object {
        /**
         * @param block invoked on key-pressed
         * @return a Listener that calls [block] on key-pressed.
         */
        inline fun pressed(crossinline block: (event: KeyEvent) -> Unit) = object: KeyListener {
            override fun pressed(event: KeyEvent) = block(event)
        }

        /**
         * @param block invoked on key-released
         * @return a Listener that calls [block] on key-released.
         */
        inline fun released(crossinline block: (event: KeyEvent) -> Unit) = object: KeyListener {
            override fun released(event: KeyEvent) = block(event)
        }
    }
}