package com.nectar.doodle.controls.theme.basic.table

import com.nectar.doodle.controls.table.Column
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.Color.Companion.gray
import com.nectar.doodle.drawing.ColorBrush
import com.nectar.doodle.drawing.Pen
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.event.MouseListener
import com.nectar.doodle.event.MouseMotionListener
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.layout.Constraints
import com.nectar.doodle.layout.constrain
import com.nectar.doodle.system.Cursor.Companion.EResize
import com.nectar.doodle.system.Cursor.Companion.EWResize
import com.nectar.doodle.system.Cursor.Companion.Grabbing
import com.nectar.doodle.system.Cursor.Companion.WResize

/**
 * Created by Nicholas Eddy on 5/10/19.
 */
class TableHeaderCell(column: Column<*>, private val headerColor: Color?): View() {

    var positioner: Constraints.() -> Unit = {
        centerX = parent.centerX
        centerY = parent.centerY
    }
        set(new) {
            field = new

            layout = constrain(children[0]) {
                positioner(it)
            }
        }

    init {
        var resizing                = false
        var mouseDown               = false
        var initialWidth            = column.width
        var initialPosition: Point? = null

        styleChanged += {
            rerender()
        }

        fun newCursor() = when {
            column.width > column.minWidth && column.width < column.maxWidth ?: Double.MAX_VALUE -> EWResize
            column.width < column.maxWidth ?: Double.MAX_VALUE                                   -> EResize
            else                                                                                 -> WResize
        }

        fun overHandle(mouseLocation: Point) = mouseLocation.x in width - 5.0..width

        fun updateCursor(event: MouseEvent) {
            if (overHandle(toLocal(event.location, event.target))) {
                cursor = newCursor()
            } else {
                cursor = null
            }
        }

        mouseChanged += object: MouseListener {
            override fun mouseEntered(event: MouseEvent) {
                if (!mouseDown) {
                    updateCursor(event)
                }
            }

            override fun mousePressed(event: MouseEvent) {
                mouseDown       = true
                initialPosition = toLocal(event.location, event.target)

                if (overHandle(initialPosition!!)) {
                    resizing     = true
                    initialWidth = column.width
                } else {
                    backgroundColor = headerColor?.darker()
                }
            }

            override fun mouseReleased(event: MouseEvent) {
                mouseDown       = false
                initialPosition = null

                updateCursor(event)

                backgroundColor = null

                if (!resizing) {
                    column.resetPosition()
                }

                resizing = false
            }
        }

        mouseMotionChanged += object : MouseMotionListener {
            override fun mouseMoved(event: MouseEvent) {
                updateCursor(event)
            }

            override fun mouseDragged(event: MouseEvent) {
                initialPosition?.let {
                    val delta = (toLocal(event.location, event.target) - it).x

                    cursor = if (resizing) {
                        column.preferredWidth = initialWidth + delta

                        newCursor()
                    } else {
                        column.moveBy(delta)

                        Grabbing
                    }

                    event.consume()
                }
            }
        }

        column.header?.let { header ->
            children += header

            layout = constrain(header) {
                positioner(it)
            }
        }
    }

    override fun render(canvas: Canvas) {
        val thickness = 1.0
        val x         = width - thickness

        backgroundColor?.let { canvas.rect(bounds.atOrigin, ColorBrush(it)) }
        canvas.line(Point(x, 0.0), Point(x, height), Pen(headerColor?.inverted ?: gray, thickness))
    }
}