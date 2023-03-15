package io.nacular.doodle.examples.contacts

import org.w3c.dom.Window

/**
 * Very simple router that uses a map to track routes.
 */
class TrivialRouter(private val window: Window): Router {
    private val routes = LinkedHashMap<Regex, RouteHandler>()

    init {
        window.onhashchange = { fireAction() }
    }

    override fun set(route: String, action: RouteHandler?) {
        when (action) {
            null -> routes.remove(Regex(route))
            else -> routes[Regex(route)] = action
        }
    }

    override fun goTo(route: String) {
        window.location.hash = route
    }

    override fun fireAction() {
        val hash = window.location.hash.drop(1)

        routes.forEach { (regex, handler) ->
            regex.matchEntire(hash)?.let {
                handler.invoke(hash, it.groupValues.drop(1))
                return
            }
        }
    }

    override fun goBack() {
        window.history.back()
    }
}