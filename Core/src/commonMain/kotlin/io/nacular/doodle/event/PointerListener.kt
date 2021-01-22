package io.nacular.doodle.event

/**
 * Classes that implement this interface are informed of
 * [PointerEvent]s when they register with a source that fires
 * these events.
 */
public interface PointerListener {
    /**
     * Informs listener that the pointer has exited the source.
     *
     * @param event The event
     */
    public fun exited(event: PointerEvent) {}

    /**
     * Informs listener that the pointer has entered the source.
     *
     * @param event The event
     */
    public fun entered(event: PointerEvent) {}

    /**
     * Informs listener that the pointer was pressed.
     *
     * @param event The event
     */
    public fun pressed(event: PointerEvent) {}

    /**
     * Informs listener that the pointer was released.
     *
     * @param event The event
     */
    public fun released(event: PointerEvent) {}

    /**
     * Informs listener that the pointer was "clicked": pressed and released
     * inside the same target.
     *
     * @param event The Event
     */
    public fun clicked(event: PointerEvent) {}

    public companion object {
        /**
         * @param block invoked on pointer-exited
         * @return a Listener that calls [block] on pointer-exited.
         */
        public inline fun exited(crossinline block: (event: PointerEvent) -> Unit): PointerListener = object: PointerListener {
            override fun exited(event: PointerEvent) = block(event)
        }

        /**
         * @param block invoked on pointer-entered
         * @return a Listener that calls [block] on pointer-entered.
         */
        public inline fun entered(crossinline block: (event: PointerEvent) -> Unit): PointerListener = object: PointerListener {
            override fun entered(event: PointerEvent) = block(event)
        }

        /**
         * @param block invoked on pointer-pressed
         * @return a Listener that calls [block] on pointer-pressed.
         */
        public inline fun pressed(crossinline block: (event: PointerEvent) -> Unit): PointerListener = object: PointerListener {
            override fun pressed(event: PointerEvent) = block(event)
        }

        /**
         * @param block invoked on pointer-released
         * @return a Listener that calls [block] on pointer-released.
         */
        public inline fun released(crossinline block: (event: PointerEvent) -> Unit): PointerListener = object: PointerListener {
            override fun released(event: PointerEvent) = block(event)
        }

        /**
         * @param block invoked on pointer-clicked
         * @return a Listener that calls [block] on pointer-clicked.
         */
        public inline fun clicked(crossinline block: (event: PointerEvent) -> Unit): PointerListener = object: PointerListener {
            override fun clicked(event: PointerEvent) = block(event)
        }
    }
}