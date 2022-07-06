package io.nacular.doodle.event


/**
 * Classes that implement this interface are informed of
 * [PointerEvent]s related to pointer motion when they register with a source that fires
 * these events.
 */
public interface PointerMotionListener {
    /**
     * Informs listener that the pointer has been moved while over the source.
     *
     * @param event The event
     */
    public fun moved(event: PointerEvent) {}

    /**
     * Informs listener that the pointer has been dragged.
     *
     * @param event The event
     */
    public fun dragged(event: PointerEvent) {}

    public companion object {
        /**
         * Creates a listener that delegates to the provided lambdas for each event type.
         *
         * @param moved invoked on pointer-moved
         * @param dragged invoked on pointer-dragged
         */
        public fun on(
            moved  : (PointerEvent) -> Unit = {},
            dragged: (PointerEvent) -> Unit = {},
        ): PointerMotionListener = object: PointerMotionListener {
            inline override fun moved   (event: PointerEvent) = moved  (event)
            inline override fun dragged (event: PointerEvent) = dragged(event)
        }

        /**
         * @param block invoked on pointer-moved
         * @return a listener that calls [block] on pointer-moved.
         */
        public inline fun moved(noinline block: (event: PointerEvent) -> Unit): PointerMotionListener = on(moved = block)

        /**
         * @param block invoked on pointer-dragged
         * @return a listener that calls [block] on pointer-dragged.
         */
        public inline fun dragged(noinline block: (event: PointerEvent) -> Unit): PointerMotionListener = on(dragged = block)
    }
}