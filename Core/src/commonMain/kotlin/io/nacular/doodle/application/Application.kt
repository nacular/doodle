package io.nacular.doodle.application

/**
 * Interface that all Doodle apps must implement.
 */
interface Application {
    /**
     * Called whenever the app is being torn down.
     */
    fun shutdown()
}