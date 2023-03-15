package io.nacular.doodle.examples.contacts

/** Callback for route changes */
typealias RouteHandler = ((path: String, matches: List<String>) -> Unit)

/**
 * Simple router with a map-like interface.
 */
interface Router {
    /** Adds a route */
    operator fun set(route: String, action: RouteHandler?)

    /**
     * Navigates to the given route
     */
    fun goTo(route: String)

    /** Notifies handlers of the current route, as though it was changed */
    fun fireAction()

    /**
     * Goes to the previous route
     */
    fun goBack()
}