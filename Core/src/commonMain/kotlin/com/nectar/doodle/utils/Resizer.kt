package com.nectar.doodle.utils

import com.nectar.doodle.core.View
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.event.MouseListener
import com.nectar.doodle.event.MouseMotionListener
import com.nectar.doodle.geometry.Point.Companion.Origin
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size.Companion.Empty
import com.nectar.doodle.system.Cursor.Companion.EResize
import com.nectar.doodle.system.Cursor.Companion.Move
import com.nectar.doodle.system.Cursor.Companion.NResize
import com.nectar.doodle.system.Cursor.Companion.NeResize
import com.nectar.doodle.system.Cursor.Companion.NwResize
import com.nectar.doodle.system.Cursor.Companion.SResize
import com.nectar.doodle.system.Cursor.Companion.SeResize
import com.nectar.doodle.system.Cursor.Companion.SwResize
import com.nectar.doodle.system.Cursor.Companion.WResize
import com.nectar.doodle.utils.Direction.East
import com.nectar.doodle.utils.Direction.North
import com.nectar.doodle.utils.Direction.South
import com.nectar.doodle.utils.Direction.West
import kotlin.math.max

class Resizer(private val view: View): MouseListener, MouseMotionListener {

    init {
        view.mouseChanged       += this
        view.mouseMotionChanged += this
    }

    var movable     = true
    var directions  = mutableSetOf(North, East, South, West)
    var hotspotSize = 5.0

    private var dragMode             = mutableSetOf<Direction>()
    private var oldCursor            = view.cursor
    private var initialSize          = Empty
    private var initialPosition      = Origin
    private var ignorePropertyChange = false

    override fun mouseReleased(event: MouseEvent) {
        dragMode.clear()

        updateCursor(event)
    }

    override fun mousePressed(event: MouseEvent) {
        dragMode.clear()

        initialPosition = view.toLocal(event.location, event.target)
        initialSize     = view.size

        when {
            initialPosition.y <= hotspotSize               -> dragMode.plusAssign(North)
            initialPosition.y >= view.height - hotspotSize -> dragMode.plusAssign(South)
        }

        when {
            initialPosition.x >= view.width - hotspotSize -> dragMode.plusAssign(East)
            initialPosition.x <= hotspotSize              -> dragMode.plusAssign(West)
        }
    }

    override fun mouseEntered(event: MouseEvent) {
        updateCursor(event)
    }

    override fun mouseExited(event: MouseEvent) {
        if (dragMode.isEmpty()) {
            view.cursor = oldCursor
        }
    }

    override fun mouseMoved(event: MouseEvent) {
        updateCursor(event)
    }

    override fun mouseDragged(event: MouseEvent) {
        val delta = view.toLocal(event.location, event.target) - initialPosition

        if (dragMode.isEmpty() && movable) {
            view.position += delta
        } else if (dragMode.isNotEmpty()) {
            val bounds = view.bounds

            var x      = bounds.x
            var y      = bounds.y
            var width  = bounds.width
            var height = bounds.height

            val minWidth  = view.minimumSize.width
            val minHeight = view.minimumSize.height

            if (West in dragMode && West in directions) {
                width  = max(minWidth, view.width - delta.x)
                x     += bounds.width - width
            } else if (East in dragMode && East in directions) {
                width = max(minWidth, initialSize.width + delta.x)
            }

            if (North in dragMode && North in directions) {
                height  = max(minHeight, view.height - delta.y)
                y      += bounds.height - height
            } else if (South in dragMode && South in directions) {
                height = max(minHeight, initialSize.height + delta.y)
            }

            view.bounds = Rectangle(x, y, width, height)
        }
    }

    private fun updateCursor(event: MouseEvent) {
        if (dragMode.isNotEmpty()) {
            return
        }

        val location = view.toLocal(event.location, event.target)
        val x        = location.x
        val y        = location.y
        val mask     = mutableSetOf<Direction>()
        var innerX   = false
        var innerY   = false

        if (x <= hotspotSize) {
            if (West in directions) {
                mask += West
            }
        } else if (x >= view.width - hotspotSize) {
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
        } else if (y >= view.height - hotspotSize) {
            if (directions.contains(South)) {
                mask += South
            }
        } else {
            innerY = true
        }

        ignorePropertyChange = true

        view.cursor = when {
            North in mask    -> when {
                East in mask -> NeResize
                West in mask -> NwResize
                else         -> NResize
            }
            South in mask    -> when {
                East in mask -> SeResize
                West in mask -> SwResize
                else         -> SResize
            }
            East in mask                -> EResize
            West in mask                -> WResize
            movable && innerX && innerY -> Move
            else                        -> oldCursor
        }

        ignorePropertyChange = false
    }
}