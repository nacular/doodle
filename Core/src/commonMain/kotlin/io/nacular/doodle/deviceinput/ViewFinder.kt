package io.nacular.doodle.deviceinput

import io.nacular.doodle.core.Display
import io.nacular.doodle.core.Internal
import io.nacular.doodle.core.View
import io.nacular.doodle.geometry.Point
import kotlin.jvm.JvmName

/** @suppress */
@Internal
public sealed interface ViewFinder {
    public fun find(within: Display, at: Point, predicate: (View) -> Boolean = { it.enabled }): View?
    public fun find(within: Display, at: Point, starting: View?, predicate: (View) -> Boolean): View?
    public fun find(within: View, at: Point): View
}

/** @suppress */
@Internal
public data object ViewFinderImpl: ViewFinder {
    override fun find(within: Display, at: Point, predicate: (View) -> Boolean): View? = findDescendant(within.child(at)?.takeIf(predicate), at)

    override fun find(within: Display, at: Point, starting: View?, predicate: (View) -> Boolean): View? {
        var parent = starting
        var view   : View?
        var point  = at

        do {
            view = findDescendant(parent, point, predicate).takeIf { it != parent }

            if (view != null) {
                break
            }

            parent?.parent?.let {
                parent = it
                point  = it.toLocal(point, view)
            } ?: break
        } while (true)

        return view ?: findDescendant(within.child(at, predicate)?.takeIf { it.enabled }, at, predicate)
    }

    override fun find(within: View, at: Point): View = findDescendant(within, at)

    private fun findDescendant(of: View, at: Point, predicate: (View) -> Boolean = { true }): View {
        var view     = of
        var newPoint = at

        while (true) {
            newPoint = view.toLocal(newPoint, view.parent)
            view = view.child_(at = newPoint, predicate) ?: break
        }

        return view
    }

    @JvmName("findDescendantNullable")
    private fun findDescendant(of: View?, at: Point, predicate: (View) -> Boolean = { true }): View? = when (of) {
        null -> null
        else -> findDescendant(of, at, predicate)
    }
}