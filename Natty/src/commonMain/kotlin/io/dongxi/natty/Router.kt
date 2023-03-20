package io.dongxi.natty

/** Callback for route changes */
typealias RouteHandler = ((path: String) -> Unit)

/**
 * Simple router with a map-like interface.
 */
interface Router {
    /** Adds a route */
    operator fun set(route: String, action: RouteHandler?)

    /** Notifies handlers of the current route, as though it was changed */
    fun fireAction()
}
