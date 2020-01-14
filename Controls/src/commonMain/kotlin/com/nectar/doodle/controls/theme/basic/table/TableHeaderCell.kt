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
import com.nectar.doodle.layout.center
import com.nectar.doodle.layout.constrain
import com.nectar.doodle.system.Cursor.Companion.EResize
import com.nectar.doodle.system.Cursor.Companion.EWResize
import com.nectar.doodle.system.Cursor.Companion.Grabbing
import com.nectar.doodle.system.Cursor.Companion.WResize
import com.nectar.doodle.utils.ChangeObserver
import com.nectar.doodle.utils.ChangeObserversImpl
import com.nectar.doodle.utils.Pool

/**
 * Created by Nicholas Eddy on 5/10/19.
 */
class TableHeaderCell(private val column: Column<*>, private val headerColor: Color?): View() {

    private var positioner: Constraints.() -> Unit = column.headerAlignment ?: center
        set(new) {
            if (field == new) {
                return
            }

            field = new

            layout = constrain(children[0]) {
                positioner(it)
            }
        }

    private val alignmentChanged: (Column<*>) -> Unit = {
        it.headerAlignment?.let { positioner = it }
    }

    init {
        var resizing        = false
        var mouseDown       = false
        var initialWidth    = column.width
        var initialPosition = null as Point?
        var moved           = false

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
            cursor = when {
                overHandle(toLocal(event.location, event.target)) -> newCursor()
                else                                              -> null
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
                initialPosition = null

                updateCursor(event)

                backgroundColor = null

                if (mouseDown && !resizing) {
                    column.resetPosition()
                }

                if (mouseDown && !(resizing || moved)) {
                    (toggled as ChangeObserversImpl).forEach { it(this@TableHeaderCell) }
                }

                moved     = false
                resizing  = false
                mouseDown = false
            }
        }

        mouseMotionChanged += object : MouseMotionListener {
            override fun mouseMoved(event: MouseEvent) {
                updateCursor(event)
            }

            override fun mouseDragged(event: MouseEvent) {
                initialPosition?.let {
                    moved     = true
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

            layout = constrain(children[0]) {
                positioner(it)
            }
        }
    }

    val toggled: Pool<ChangeObserver<TableHeaderCell>> by lazy { ChangeObserversImpl(this) }

    override fun addedToDisplay() {
        super.addedToDisplay()

        column.alignmentChanged += alignmentChanged
    }

    override fun removedFromDisplay() {
        super.removedFromDisplay()

        column.alignmentChanged -= alignmentChanged
    }

    override fun render(canvas: Canvas) {
        val x = width - lineThickness / 2

        backgroundColor?.let { canvas.rect(bounds.atOrigin, ColorBrush(it)) }
        canvas.line(Point(x, lineIndent), Point(x, height - lineIndent), Pen(headerColor?.inverted ?: gray, lineThickness))
    }

    companion object {
        private const val lineIndent    = 3.0
        private const val lineThickness = 1.0
    }
}