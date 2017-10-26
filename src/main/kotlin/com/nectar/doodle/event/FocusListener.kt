package com.nectar.doodle.event

/**
 * Classes that implement this interface are informed of
 * FocusEvents when they register with a source that fires
 * these events.
 */

interface FocusListener {
    /**
     * Informs listener that the source has gained focus.
     *
     * @param event The event
     */
    fun focusGained(event: FocusEvent)

    /**
     * Informs listener that the source has lost focus.
     *
     * @param event The event
     */
    fun focusLost(event: FocusEvent)
}
