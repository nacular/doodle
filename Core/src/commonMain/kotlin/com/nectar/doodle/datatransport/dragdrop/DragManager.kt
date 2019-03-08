package com.nectar.doodle.datatransport.dragdrop

import com.nectar.doodle.core.View
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.geometry.Point

/**
 * Created by Nicholas Eddy on 2/28/19.
 */
interface DragManager {
    fun mouseDown(view: View, event: MouseEvent, targetFinder: (Point) -> View?): Boolean

    fun mouseDrag(view: View, event: MouseEvent, targetFinder: (Point) -> View?)
}