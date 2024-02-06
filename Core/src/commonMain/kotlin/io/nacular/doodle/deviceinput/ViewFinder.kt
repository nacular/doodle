package io.nacular.doodle.deviceinput

import io.nacular.doodle.core.Display
import io.nacular.doodle.core.Internal
import io.nacular.doodle.core.View
import io.nacular.doodle.geometry.Point

/** @suppress */
@Internal
public interface ViewFinder {
    public fun find(within: Display, at: Point): View?
    public fun find(within: Display, at: Point, starting: View?, predicate: (View) -> Boolean): View?
}

/** @suppress */
@Internal
public class ViewFinderImpl: ViewFinder {
    override fun find(within: Display, at: Point): View? = findDescendant(within.child(at)?.takeIf { it.enabled }, at)

    override fun find(within: Display, at: Point, starting: View?, predicate: (View) -> Boolean): View? {
        val combinedFilter = { it: View -> it.enabled && predicate(it) }

        var parent = starting
        var view   : View?
        var point  = at

        do {
            view = findDescendant(parent, point, combinedFilter).takeIf { it != parent }

            if (view != null) {
                break
            }

            parent?.parent?.let {
                parent = it
                point  = it.toLocal(point, view)
            } ?: break
        } while (true)

        return view ?: find(within, at, combinedFilter)
    }

    private fun find(within: Display, at: Point, predicate: (View) -> Boolean): View? = findDescendant(within.child(at, predicate)?.takeIf { it.enabled }, at, predicate)

    private fun findDescendant(of: View?, at: Point, predicate: (View) -> Boolean = { true }): View? {
        var view     = of
        var newPoint = at

        while(view != null) {
            newPoint = view.toLocal(newPoint, view.parent)
            view     = view.child_(at = newPoint, predicate)?.takeIf { it.enabled } ?: break
        }

        return view
    }
}