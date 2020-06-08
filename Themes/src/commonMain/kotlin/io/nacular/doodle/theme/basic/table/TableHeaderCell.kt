package io.nacular.doodle.theme.basic.table

import io.nacular.doodle.controls.table.Column
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Gray
import io.nacular.doodle.drawing.ColorBrush
import io.nacular.doodle.drawing.Pen
import io.nacular.doodle.drawing.darker
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.event.PointerMotionListener
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.layout.Constraints
import io.nacular.doodle.layout.center
import io.nacular.doodle.layout.constrain
import io.nacular.doodle.system.Cursor.Companion.EResize
import io.nacular.doodle.system.Cursor.Companion.EWResize
import io.nacular.doodle.system.Cursor.Companion.Grabbing
import io.nacular.doodle.system.Cursor.Companion.WResize
import io.nacular.doodle.utils.ChangeObserver
import io.nacular.doodle.utils.ChangeObserversImpl
import io.nacular.doodle.utils.Pool

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
        var pointerDown     = false
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

        fun overHandle(pointerLocation: Point) = pointerLocation.x in width - 5.0..width

        fun updateCursor(event: PointerEvent) {
            cursor = when {
                overHandle(toLocal(event.location, event.target)) -> newCursor()
                else                                              -> null
            }
        }

        pointerChanged += object: PointerListener {
            override fun entered(event: PointerEvent) {
                if (!pointerDown) {
                    updateCursor(event)
                }
            }

            override fun pressed(event: PointerEvent) {
                pointerDown       = true
                initialPosition = toLocal(event.location, event.target)

                if (overHandle(initialPosition!!)) {
                    resizing     = true
                    initialWidth = column.width
                } else {
                    backgroundColor = headerColor?.darker()
                }
            }

            override fun released(event: PointerEvent) {
                initialPosition = null

                updateCursor(event)

                backgroundColor = null

                if (pointerDown && !resizing) {
                    column.resetPosition()
                }

                if (pointerDown && !(resizing || moved)) {
                    (toggled as ChangeObserversImpl).forEach { it(this@TableHeaderCell) }
                }

                moved     = false
                resizing  = false
                pointerDown = false
            }
        }

        pointerMotionChanged += object : PointerMotionListener {
            override fun moved(event: PointerEvent) {
                updateCursor(event)
            }

            override fun dragged(event: PointerEvent) {
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
        canvas.line(Point(x, lineIndent), Point(x, height - lineIndent), Pen(headerColor?.inverted ?: Gray, lineThickness))
    }

    companion object {
        private const val lineIndent    = 3.0
        private const val lineThickness = 1.0
    }
}