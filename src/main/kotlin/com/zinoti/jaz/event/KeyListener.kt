package com.zinoti.jaz.event


/**
 * Classes that implement this interface are informed of
 * KeyEvents when they register with a source that fires
 * these events.
 */

interface KeyListener {
    /**
     * Informs listener that a key was typed.
     *
     * @param event The event
     */

    fun keyTyped(event: KeyEvent)

    /**
     * Informs listener that a key was pressed.
     *
     * @param event The event
     */

    fun keyPressed(event: KeyEvent)

    /**
     * Informs listener that a key was released.
     *
     * @param event The event
     */

    fun keyReleased(event: KeyEvent)
}
