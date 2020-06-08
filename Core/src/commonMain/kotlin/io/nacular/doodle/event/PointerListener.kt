package io.nacular.doodle.event

/**
 * Classes that implement this interface are informed of
 * [PointerEvent]s when they register with a source that fires
 * these events.
 */
interface PointerListener {
    /**
     * Informs listener that the pointer has exited the source.
     *
     * @param event The event
     */
    fun exited(event: PointerEvent) {}

    /**
     * Informs listener that the pointer has entered the source.
     *
     * @param event The event
     */
    fun entered(event: PointerEvent) {}

    /**
     * Informs listener that the pointer was pressed.
     *
     * @param event The event
     */
    fun pressed(event: PointerEvent) {}

    /**
     * Informs listener that the pointer was released.
     *
     * @param event The event
     */
    fun released(event: PointerEvent) {}
}
