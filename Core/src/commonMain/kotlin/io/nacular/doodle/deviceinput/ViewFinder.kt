package io.nacular.doodle.deviceinput

import io.nacular.doodle.core.Display
import io.nacular.doodle.core.View
import io.nacular.doodle.geometry.Point

/**
 * Created by Nicholas Eddy on 3/12/19.
 */
interface ViewFinder {
    fun find(at: Point): View?
}

class ViewFinderImpl(private val display: Display): ViewFinder {
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