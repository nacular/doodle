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

    /**
     * Informs listener that the pointer was "clicked": pressed and released
     * inside the same target.
     *
     * @param event The Event
     */
    fun clicked(event: PointerEvent) {}
}

inline fun exited(crossinline block: (event: PointerEvent) -> Unit) = object: PointerListener {
    override fun exited(event: PointerEvent) = block(event)
}

inline fun entered(crossinline block: (event: PointerEvent) -> Unit) = object: PointerListener {
    override fun entered(event: PointerEvent) = block(event)
}

inline fun pressed(crossinline block: (event: PointerEvent) -> Unit) = object: PointerListener {
    override fun pressed(event: PointerEvent) = block(event)
}

inline fun released(crossinline block: (event: PointerEvent) -> Unit) = object: PointerListener {
    override fun released(event: PointerEvent) = block(event)
}

inline fun clicked(crossinline block: (event: PointerEvent) -> Unit) = object: PointerListener {
    override fun clicked(event: PointerEvent) = block(event)
}