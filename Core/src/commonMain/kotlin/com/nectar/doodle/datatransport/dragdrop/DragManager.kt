package com.nectar.doodle.datatransport.dragdrop

import com.nectar.doodle.core.View
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.system.SystemMouseEvent

/**
 * Created by Nicholas Eddy on 2/28/19.
 */

/**
 * Responsible for managing drag-drop operations
 */
interface DragManager {
    fun mouseDown(view: View, event: MouseEvent, targetFinder: (Point) -> View?)

    fun mouseDrag(view: View, event: MouseEvent, targetFinder: (Point) -> View?)

    fun mouseUp(event: SystemMouseEvent)
}