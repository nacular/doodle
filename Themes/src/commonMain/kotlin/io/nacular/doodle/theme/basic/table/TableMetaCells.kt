package io.nacular.doodle.theme.basic.table

import io.nacular.doodle.controls.table.Column
import io.nacular.doodle.core.View
import io.nacular.doodle.core.renderProperty
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Gray
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.drawing.darker
import io.nacular.doodle.drawing.lighter
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.event.PointerListener.Companion.on
import io.nacular.doodle.event.PointerMotionListener
import io.nacular.doodle.event.PointerMotionListener.Companion.on
import io.nacular.doodle.geometry.Circle
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.inscribed
import io.nacular.doodle.geometry.map
import io.nacular.doodle.layout.constraints.Bounds
import io.nacular.doodle.layout.constraints.ConstraintDslContext
import io.nacular.doodle.layout.constraints.center
import io.nacular.doodle.layout.constraints.constrain
import io.nacular.doodle.system.Cursor.Companion.EResize
import io.nacular.doodle.system.Cursor.Companion.EWResize
import io.nacular.doodle.system.Cursor.Companion.Grabbing
import io.nacular.doodle.system.Cursor.Companion.WResize
import io.nacular.doodle.theme.basic.ColorMapper
import io.nacular.doodle.utils.ChangeObserver
import io.nacular.doodle.utils.ChangeObserversImpl
import io.nacular.doodle.utils.Pool
import io.nacular.doodle.utils.SortOrder
import io.nacular.doodle.utils.SortOrder.Ascending
import io.nacular.doodle.utils.SortOrder.Descending
import io.nacular.measured.units.Angle.Companion.degrees
import io.nacular.measured.units.times

/**
 * Created by Nicholas Eddy on 5/10/19.
 */
public class TableHeaderCell(private val column: Column<*>, private val fillColor: Color?): View() {

    private var positioner: ConstraintDslContext.(Bounds) -> Unit = column.headerAlignment ?: center; set(new) {
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

    private var disabledColorMapper: ColorMapper = { it.lighter() }

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

        pointerChanged += on(
            entered  = {
                if (!pointerDown) {
                    updateCursor(it)
                }
            },

            pressed  = {
                pointerDown     = true
                initialPosition = toLocal(it.location, it.target)

                when {
                    overHandle(initialPosition!!) -> {
                        resizing     = true
                        initialWidth = column.width
                    }
                    else                          -> backgroundColor = fillColor?.darker()
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
                    (toggled as ChangeObserversImpl).forEach { it(this@TableHeaderCell) }
                }

                moved     = false
                resizing  = false
                pointerDown = false
            }
        )

        pointerMotionChanged += on (
            moved   = { updateCursor(it) },
            dragged = { event ->
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
        )

        column.header?.let { header ->
            children += header

            layout = constrain(children[0]) {
                positioner(it)
            }
        }
    }

    public val toggled: Pool<ChangeObserver<TableHeaderCell>> by lazy { ChangeObserversImpl(this) }

    internal var sortOrder: SortOrder? by renderProperty(null)

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

        when (sortOrder) {
            Ascending  -> canvas.poly(ascending.map  { it + Point(width - iconCircle.radius - iconHeight, (height - iconHeight) / 2) - Point(iconCircle.center.x, ascendingTop    ) },  fill = strokeColor.paint) //canvas.text("+", at = Point(width - 10), color = strokeColor)
            Descending -> canvas.poly(descending.map { it + Point(width - iconCircle.radius - iconHeight, (height + iconHeight) / 2) - Point(iconCircle.center.x, descendingBottom) }, fill = strokeColor.paint) //canvas.text("-", at = Point(width - 10), color = strokeColor)
            else       -> {}
        }
    }

    private companion object {
        private const val lineIndent    = 3.0
        private const val lineThickness = 1.0

        private val iconCircle = Circle(center = Point(10, 10), radius = 5.0)

        private val ascending        = iconCircle.inscribed(3)!!
        private val descending       = iconCircle.inscribed(3, rotation = 180 * degrees)!!
        private val ascendingTop     = ascending.points.minBy  { it.y }.y
        private val descendingBottom = descending.points.maxBy { it.y }.y
        private val iconHeight       = ascending.points.maxBy  { it.y }.y - ascendingTop
    }
}

public class TableFooterCell(private val column: Column<*>, private val fillColor: Color?): View() {

    private var positioner: ConstraintDslContext.(Bounds) -> Unit = column.footerAlignment ?: center
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
        it.footerAlignment?.let { positioner = it }
    }

    private var disabledColorMapper: ColorMapper = { it.lighter() }

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

        pointerChanged += object: PointerListener {
            override fun entered(event: PointerEvent) {
                if (!pointerDown) {
                    updateCursor(event)
                }
            }

            override fun pressed(event: PointerEvent) {
                pointerDown     = true
                initialPosition = toLocal(event.location, event.target)

                if (overHandle(initialPosition!!)) {
                    resizing     = true
                    initialWidth = column.width
                } else {
                    backgroundColor = fillColor?.darker()
                }

                event.consume()
            }

            override fun released(event: PointerEvent) {
                initialPosition = null

                updateCursor(event)

                backgroundColor = null

                if (pointerDown && !resizing) {
                    column.resetPosition()
                }

                if (pointerDown && !(resizing || moved)) {
                    (toggled as ChangeObserversImpl).forEach { it(this@TableFooterCell) }
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

        column.footer?.let { footer ->
            children += footer

            layout = constrain(children[0]) {
                positioner(it)
            }
        }
    }

    public val toggled: Pool<ChangeObserver<TableFooterCell>> by lazy { ChangeObserversImpl(this) }

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

    private companion object {
        private const val lineIndent    = 3.0
        private const val lineThickness = 1.0
    }
}