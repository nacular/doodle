package com.nectar.doodle.datatransport.dragdrop

import com.nectar.doodle.core.View
import com.nectar.doodle.datatransport.DataBundle
import com.nectar.doodle.drawing.Renderable
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Point.Companion.Origin

/**
 * Created by Nicholas Eddy on 2/28/19.
 */
interface DragManager {
    fun startDrag(view        : View,
                  event       : MouseEvent,
                  bundle      : DataBundle,
                  visual      : Renderable?,
                  visualOffset: Point = Origin,
                  observer    : (DropCompleteEvent) -> Unit)
}