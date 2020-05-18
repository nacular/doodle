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
     * @param event The event
     */
    fun mouseMoved(event: MouseEvent) {}

    /**
     * Informs listener that the mouse has been dragged.
     *
     * @param event The event
     */
    fun mouseDragged(event: MouseEvent) {}
}
