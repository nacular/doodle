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
        public inline fun moved(crossinline block: (event: PointerEvent) -> Unit): PointerMotionListener = object: PointerMotionListener {
            override fun moved(event: PointerEvent) = block(event)
        }

        public inline fun dragged(crossinline block: (event: PointerEvent) -> Unit): PointerMotionListener = object: PointerMotionListener {
            override fun dragged(event: PointerEvent) = block(event)
        }
    }
}

@Deprecated(message = "Migrate to companion version instead.", replaceWith = ReplaceWith("PointerMotionListener.Companion.moved"))
public inline fun moved(crossinline block: (event: PointerEvent) -> Unit): PointerMotionListener = object: PointerMotionListener {
    override fun moved(event: PointerEvent) = block(event)
}

@Deprecated(message = "Migrate to companion version instead.", replaceWith = ReplaceWith("PointerMotionListener.Companion.dragged"))
public inline fun dragged(crossinline block: (event: PointerEvent) -> Unit): PointerMotionListener = object: PointerMotionListener {
    override fun dragged(event: PointerEvent) = block(event)
}
