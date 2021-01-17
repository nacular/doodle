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
    fun keyPressed(event: KeyEvent) {}

    /**
     * Informs listener that a key was released.
     *
     * @param event The event
     */
    fun keyReleased(event: KeyEvent) {}
}

inline fun keyPressed(crossinline block: (event: KeyEvent) -> Unit) = object: KeyListener {
    override fun keyPressed(event: KeyEvent) = block(event)
}

inline fun keyReleased(crossinline block: (event: KeyEvent) -> Unit) = object: KeyListener {
    override fun keyReleased(event: KeyEvent) = block(event)
}