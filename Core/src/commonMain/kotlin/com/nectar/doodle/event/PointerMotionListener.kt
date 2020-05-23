package com.nectar.doodle.event


/**
 * Classes that implement this interface are informed of
 * [PointerEvent]s related to pointer motion when they register with a source that fires
 * these events.
 */
interface PointerMotionListener {
    /**
     * Informs listener that the pointer has been moved while over the source.
     *
     * @param event The event
     */
    fun moved(event: PointerEvent) {}

    /**
     * Informs listener that the pointer has been dragged.
     *
     * @param event The event
     */
    fun dragged(event: PointerEvent) {}
}
