package io.nacular.doodle.deviceinput

import io.nacular.doodle.core.Display
import io.nacular.doodle.core.Internal
import io.nacular.doodle.core.View
import io.nacular.doodle.geometry.Point

/** @suppress */
@Internal
public interface ViewFinder {
    public fun find(at: Point): View?
}

/** @suppress */
@Internal
public class ViewFinderImpl(private val display: Display): ViewFinder {
    override fun find(at: Point): View? {
        var newPoint = at
        var view     = display.child(at)?.takeIf { it.enabled }

        while(view != null) {
            newPoint = view.toLocal(newPoint, view.parent)
            view     = view.child_(at = newPoint)?.takeIf { it.enabled } ?: break
        }

        return view
    }
}