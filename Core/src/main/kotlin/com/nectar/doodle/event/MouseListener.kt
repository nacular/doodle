package com.nectar.doodle.event


/**
 * Classes that implement this interface are informed of
 * MouseEvents when they register with a source that fires
 * these events.
 */

interface MouseListener {
    /**
     * Informs listener that the mouse has exited the source.
     *
     * @param event The event
     */
    fun mouseExited(event: MouseEvent)

    /**
     * Informs listener that the mouse has entered the source.
     *
     * @param event The event
     */
    fun mouseEntered(event: MouseEvent)

    /**
     * Informs listener that the mouse was pressed.
     *
     * @param event The event
     */
    fun mousePressed(event: MouseEvent)

    /**
     * Informs listener that the mouse was released.
     *
     * @param event The event
     */
    fun mouseReleased(event: MouseEvent)
}
