package io.nacular.doodle.application

/**
 * Interface that all Doodle apps must implement.
 */
public interface Application {
    /**
     * Called whenever the app is being torn down.
     */
    public fun shutdown()
}