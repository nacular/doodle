package io.nacular.doodle.application

/**
 * Interface that all Doodle apps must implement. Apps have a simple lifecycle. They area created using a constructor
 * when launched, and [shutdown] when they are being retired.
 */
public interface Application {
    /**
     * Called whenever the app is being torn down.
     */
    public fun shutdown()
}