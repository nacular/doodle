package com.nectar.doodle.deviceinput

import com.nectar.doodle.core.Display
import com.nectar.doodle.core.View
import com.nectar.doodle.geometry.Point

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