package com.nectar.doodle.controls.theme.basic.table

import com.nectar.doodle.controls.table.Column
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.Pen
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.event.MouseListener
import com.nectar.doodle.event.MouseMotionListener
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.layout.constrain
import com.nectar.doodle.system.Cursor

/**
 * Created by Nicholas Eddy on 5/10/19.
 */
class TableHeaderCell(column: Column<*>, val headerColor: Color?): View() {
    init {
        var initialWidth            = column.width
        var initialPosition: Point? = null
        var mouseDown               = false

        fun newCursor() = when {
            column.width > column.minWidth && column.width < column.maxWidth ?: Double.MAX_VALUE -> Cursor.EWResize
            column.width < column.maxWidth ?: Double.MAX_VALUE                                   -> Cursor.EResize
            else                                                                                 -> Cursor.WResize
        }

        fun overHandle(mouseLocation: Point) = mouseLocation.x in width - 5.0..width

        fun updateCursor(mouseLocation: Point) {
            if (overHandle(mouseLocation)) {
                cursor = newCursor()
            } else {
                cursor = null
            }
        }

        mouseChanged += object: MouseListener {
            override fun mouseEntered(event: MouseEvent) {
                if (!mouseDown) {
                    updateCursor(event.location)
                }
            }

            override fun mousePressed(event: MouseEvent) {
                mouseDown = true

                if (overHandle(event.location)) {
                    initialWidth    = column.width
                    initialPosition = event.location
                }
            }

            override fun mouseReleased(event: MouseEvent) {
                mouseDown       = false
                initialPosition = null

                updateCursor(event.location)
            }
        }

        mouseMotionChanged += object : MouseMotionListener {
            override fun mouseMoved(event: MouseEvent) {
                updateCursor(event.location)
            }

            override fun mouseDragged(event: MouseEvent) {
                initialPosition?.let {
                    cursor = newCursor()

                    val delta = event.location - it

                    column.preferredWidth = initialWidth + delta.x
                }
            }
        }

        column.header?.let {
            children += it

            layout = constrain(it) {
                it.centerX = it.parent.centerX
                it.centerY = it.parent.centerY
            }
        }
    }

    override fun render(canvas: Canvas) {
        val thickness = 1.0
        val x         = width - thickness

        canvas.line(Point(x, 0.0), Point(x, height), Pen(headerColor?.darker(0.25f) ?: Color.gray))
    }
}