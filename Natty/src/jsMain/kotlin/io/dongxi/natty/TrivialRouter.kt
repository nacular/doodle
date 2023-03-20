package io.dongxi.natty

import org.w3c.dom.Window

/**
 * Very simple router that uses a map to track routes.
 */
class TrivialRouter(private val window: Window) : Router {
    private val routes = mutableMapOf<String, RouteHandler>()

    init {
        window.onhashchange = { fireAction() }
    }

    override fun set(route: String, action: RouteHandler?) {
        when (action) {
            null -> routes.remove(route)
            else -> routes[route] = action
        }
    }

    override fun fireAction() {
        val hash = window.location.hash.drop(1)
        routes[hash]?.let { it(hash) }
    }
}
