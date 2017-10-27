package com.nectar.doodle.event


/**
 * Classes that implement this interface are informed of
 * MouseEvents related to mouse motion when they register with a source that fires
 * these events.
 */

interface MouseMotionListener {
    /**
     * Informs listener that the mouse has been moved while over the source.
     *
     * @param mouseEvent The event
     */
    fun mouseMoved(mouseEvent: MouseEvent)

    /**
     * Informs listener that the mouse has been dragged.
     *
     * @param mouseEvent The event
     */
    fun mouseDragged(mouseEvent: MouseEvent)
}
