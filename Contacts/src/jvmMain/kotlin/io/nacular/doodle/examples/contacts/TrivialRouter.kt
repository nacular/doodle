package io.nacular.doodle.examples.contacts

import io.nacular.doodle.utils.observable

/**
 * Very simple router that uses a map to track routes.
 */
class TrivialRouter: Router {
    private val history = mutableListOf<String>()
    private val routes  = LinkedHashMap<Regex, RouteHandler>()

    private var route by observable("") { _,_ ->
        fireAction()
    }

    override fun set(route: String, action: RouteHandler?) {
        when (action) {
            null -> routes.remove(Regex(route))
            else -> routes[Regex(route)] = action
        }
    }

    override fun goTo(route: String) {
        history += this.route
        this.route = route
    }

    override fun fireAction() {
        routes.forEach { (regex, handler) ->
            regex.matchEntire(route)?.let {
                handler.invoke(route, it.groupValues.drop(1))
                return
            }
        }
    }

    override fun goBack() {
        history.removeLastOrNull()?.let {
            route = it
        }
    }
}