package io.nacular.doodle.theme.basic.table

import io.nacular.doodle.controls.table.Column
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Gray
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.drawing.darker
import io.nacular.doodle.drawing.lighter
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.event.PointerMotionListener.Companion.on
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.layout.constraints.Bounds
import io.nacular.doodle.layout.constraints.ConstraintDslContext
import io.nacular.doodle.layout.constraints.center
import io.nacular.doodle.layout.constraints.constrain
import io.nacular.doodle.layout.constraints.withSizeInsets
import io.nacular.doodle.system.Cursor.Companion.EResize
import io.nacular.doodle.system.Cursor.Companion.EWResize
import io.nacular.doodle.system.Cursor.Companion.WResize
import io.nacular.doodle.theme.basic.ColorMapper
import io.nacular.doodle.utils.ChangeObservers
import io.nacular.doodle.utils.ChangeObserversImpl

public class TableFooterCell(
    private val column: Column<*>,
    private val fillColor: Color?,
    private val indent   : Double = 0.0,
): View() {

    private var positioner: ConstraintDslContext.(Bounds) -> Unit = column.footerAlignment ?: center; set(new) {
        if (field == new) {
            return
        }

        field = new

        updateLayout()
    }

    private val alignmentChanged: (Column<*>) -> Unit = { col ->
        col.footerAlignment?.let { positioner = it }
    }

    private var disabledColorMapper: ColorMapper = { it.lighter() }

    init {
        var resizing        = false
        var pointerDown     = false
        var initialWidth    = column.width
        var initialPosition = null as Point?
        var moved           = false

        styleChanged   += {          rerender() }
        enabledChanged += { _,_,_ -> rerender() }

        fun newCursor() = when {
            column.width > column.minWidth && column.width < (column.maxWidth ?: Double.MAX_VALUE) -> EWResize
            column.width < (column.maxWidth ?: Double.MAX_VALUE)                                   -> EResize
            else                                                                                   -> WResize
        }

        fun overHandle(pointerLocation: Point) = pointerLocation.x in width - 5.0..width

        fun updateCursor(event: PointerEvent) {
            cursor = when {
                overHandle(toLocal(event.location, event.target)) -> newCursor()
                else                                              -> null
            }
        }

        pointerChanged += PointerListener.on(
            entered = {
                if (!pointerDown) {
                    updateCursor(it)
                }
            },

            pressed = {
                pointerDown     = true
                initialPosition = toLocal(it.location, it.target)

                when {
                    overHandle(initialPosition!!) -> {
                        resizing     = true
                        initialWidth = column.width
                    }
                    column.movable -> backgroundColor = fillColor?.darker()
                    else -> {}
                }

                it.consume()
            },

            released = {
                initialPosition = null

                updateCursor(it)

                backgroundColor = null

                if (pointerDown && !resizing) {
                    column.resetPosition()
                }

                if (pointerDown && !(resizing || moved)) {
                    (toggled as ChangeObserversImpl).forEach { it(this@TableFooterCell) }
                }

                moved       = false
                resizing    = false
                pointerDown = false
            }
        )

        pointerMotionChanged += on(
            moved = { updateCursor(it) },
            dragged = { event ->
                initialPosition?.let {
                    moved     = true
                    val delta = (toLocal(event.location, event.target) - it).x

                    cursor = when {
                        resizing -> newCursor().also { column.preferredWidth = initialWidth + delta }
                        column.movable -> null.also { column.moveBy(delta) }
                        else -> null
                    }

                    event.consume()
                }
            }
        )

        column.footer?.let { footer ->
            children += footer

            updateLayout()
        }
    }

    public val toggled: ChangeObservers<TableFooterCell> = ChangeObserversImpl(this)

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

        backgroundColor?.let {
            canvas.rect(bounds.atOrigin, (if (enabled) it else disabledColorMapper(it)).paint)
        }

        val strokeColor = (fillColor?.inverted ?: Gray).let { if (enabled) it else disabledColorMapper(it) }

        canvas.line(Point(x, lineIndent), Point(x, height - lineIndent), Stroke(strokeColor, lineThickness))
    }

    private fun updateLayout() {
        layout = constrain(children[0]) {
            when (indent) {
                0.0 -> positioner(it)
                else -> withSizeInsets(width = { indent }) {
                    positioner(it.withOffset(left = { indent }))
                }
            }
        }
    }

    private companion object {
        private const val lineIndent    = 3.0
        private const val lineThickness = 1.0
    }
}