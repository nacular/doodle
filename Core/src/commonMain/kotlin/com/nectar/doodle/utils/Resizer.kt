package com.nectar.doodle.utils

import com.nectar.doodle.core.View
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.event.MouseListener
import com.nectar.doodle.event.MouseMotionListener
import com.nectar.doodle.geometry.Point.Companion.Origin
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size.Companion.Empty
import com.nectar.doodle.system.Cursor
import com.nectar.doodle.utils.Direction.East
import com.nectar.doodle.utils.Direction.North
import com.nectar.doodle.utils.Direction.South
import com.nectar.doodle.utils.Direction.West
import kotlin.math.max

class Resizer(view: View? = null): MouseListener, MouseMotionListener {

    var view: View? by observable(view) { _, old, new ->
        old?.let { it.mouseChanged -= this; it.mouseMotionChanged -= this }
        new?.let { it.mouseChanged += this; it.mouseMotionChanged += this }
    }

    init {
        this.view?.let { it.mouseChanged += this; it.mouseMotionChanged += this }
    }

    var movable     = true
    var directions  = mutableSetOf(North, East, South, West)
    var hotspotSize = 5.0

    private var dragMode             = mutableSetOf<Direction>()
    private var oldCursor            = view?.cursor
    private var initialSize          = Empty
    private var initialPosition      = Origin
    private var ignorePropertyChange = false

//    fun propertyChanged(aPropertyEvent: PropertyEvent) {
//        if (!ignorePropertyChange && aPropertyEvent.getProperty() === View.CURSOR) {
//            oldCursor = if ((aPropertyEvent.getSource() as View).isCursorSet()) aPropertyEvent.getNewValue() as Cursor else null
//        }
//    }

    override fun mouseReleased(event: MouseEvent) {
        dragMode.clear()

        updateCursor(event)
    }

    override fun mousePressed(event: MouseEvent) {
        dragMode.clear()

        view?.let {
            initialPosition = event.location
            initialSize     = it.size

            when {
                initialPosition.y <= hotspotSize             -> dragMode.plusAssign(North)
                initialPosition.y >= it.height - hotspotSize -> dragMode.plusAssign(South)
            }

            when {
                initialPosition.x >= it.width - hotspotSize -> dragMode.plusAssign(East)
                initialPosition.x <= hotspotSize            -> dragMode.plusAssign(West)
            }
        }
    }

    override fun mouseEntered(event: MouseEvent) {
        updateCursor(event)
    }

    override fun mouseExited(event: MouseEvent) {
        if (dragMode.isEmpty()) {
            view?.cursor = oldCursor
        }
    }

    override fun mouseMoved(mouseEvent: MouseEvent) {
        updateCursor(mouseEvent)
    }

    override fun mouseDragged(mouseEvent: MouseEvent) {
        val delta = mouseEvent.location - initialPosition

        view?.let {
            if (dragMode.isEmpty() && movable) {
                it.position += delta
            } else if (dragMode.isNotEmpty()) {
                val bounds = it.bounds

                var x      = bounds.x
                var y      = bounds.y
                var width  = bounds.width
                var height = bounds.height

                val minWidth  = it.minimumSize.width
                val minHeight = it.minimumSize.height

                if (West in dragMode && West in directions) {
                    width  = max(minWidth, it.width - delta.x)
                    x     += bounds.width - width
                } else if (East in dragMode && East in directions) {
                    width = max(minWidth, initialSize.width + delta.x)
                }

                if (North in dragMode && North in directions) {
                    height  = max(minHeight, it.height - delta.y)
                    y      += bounds.height - height
                } else if (South in dragMode && South in directions) {
                    height = max(minHeight, initialSize.height + delta.y)
                }

                it.bounds = Rectangle(x, y, width, height)
            }
        }
    }

    private fun updateCursor(mouseEvent: MouseEvent) {
        if (dragMode.isNotEmpty()) {
            return
        }

        view?.let {
            val x = mouseEvent.location.x
            val y = mouseEvent.location.y
            val mask = mutableSetOf<Direction>()
            var innerX = false
            var innerY = false

            if (x <= hotspotSize) {
                if (West in directions) {
                    mask += West
                }
            } else if (x >= it.width - hotspotSize) {
                if (East in directions) {
                    mask += East
                }
            } else {
                innerX = true
            }
            if (y <= hotspotSize) {
                if (North in directions) {
                    mask += North
                }
            } else if (y >= it.height - hotspotSize) {
                if (directions.contains(South)) {
                    mask += South
                }
            } else {
                innerY = true
            }

            ignorePropertyChange = true

            it.cursor = when {
                North in mask    -> when {
                    East in mask -> Cursor.NeResize
                    West in mask -> Cursor.NwResize
                    else         -> Cursor.NResize
                }
                South in mask           -> when {
                    East in mask -> Cursor.SeResize
                    West in mask -> Cursor.SwResize
                    else         -> Cursor.SResize
                }
                East in mask                -> Cursor.EResize
                West in mask                -> Cursor.WResize
                movable && innerX && innerY -> Cursor.Move
                else                        -> oldCursor
            }

            ignorePropertyChange = false
        }
    }
}