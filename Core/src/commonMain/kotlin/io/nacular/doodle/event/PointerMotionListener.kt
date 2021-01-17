package io.nacular.doodle.event


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

inline fun moved(crossinline block: (event: PointerEvent) -> Unit) = object: PointerMotionListener {
    override fun moved(event: PointerEvent) = block(event)
}

inline fun dragged(crossinline block: (event: PointerEvent) -> Unit) = object: PointerMotionListener {
    override fun dragged(event: PointerEvent) = block(event)
}
