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
         * Creates a listener that delegates to the provided lambdas for each event type.
         *
         * @param entered invoked on pointer-entered
         * @param exited invoked on pointer-exited
         * @param pressed invoked on pointer-exited
         * @param released invoked on pointer-released
         * @param clicked invoked on pointer-clicked
         */
        public fun on(
            entered : (PointerEvent) -> Unit = {},
            exited  : (PointerEvent) -> Unit = {},
            pressed : (PointerEvent) -> Unit = {},
            released: (PointerEvent) -> Unit = {},
            clicked : (PointerEvent) -> Unit = {},
        ): PointerListener = object: PointerListener {
            inline override fun entered (event: PointerEvent) = entered (event)
            inline override fun exited  (event: PointerEvent) = exited  (event)
            inline override fun pressed (event: PointerEvent) = pressed (event)
            inline override fun released(event: PointerEvent) = released(event)
            inline override fun clicked (event: PointerEvent) = clicked (event)
        }

        /**
         * @param block invoked on pointer-exited
         * @return a listener that calls [block] on pointer-exited.
         */
        public inline fun exited(noinline block: (event: PointerEvent) -> Unit): PointerListener = on(exited = block)

        /**
         * @param block invoked on pointer-entered
         * @return a listener that calls [block] on pointer-entered.
         */
        public inline fun entered(noinline block: (event: PointerEvent) -> Unit): PointerListener = on(entered = block)

        /**
         * @param block invoked on pointer-pressed
         * @return a listener that calls [block] on pointer-pressed.
         */
        public inline fun pressed(noinline block: (event: PointerEvent) -> Unit): PointerListener = on(pressed = block)

        /**
         * @param block invoked on pointer-released
         * @return a listener that calls [block] on pointer-released.
         */
        public inline fun released(noinline block: (event: PointerEvent) -> Unit): PointerListener = on(released = block)

        /**
         * @param block invoked on pointer-clicked
         * @return a listener that calls [block] on pointer-clicked.
         */
        public inline fun clicked(noinline block: (event: PointerEvent) -> Unit): PointerListener = on(clicked = block)
    }
}