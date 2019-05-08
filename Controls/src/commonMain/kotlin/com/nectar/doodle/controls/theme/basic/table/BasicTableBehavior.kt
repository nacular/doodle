package com.nectar.doodle.controls.theme.basic.table

import com.nectar.doodle.controls.ItemGenerator
import com.nectar.doodle.controls.Selectable
import com.nectar.doodle.controls.table.Column
import com.nectar.doodle.controls.table.HeaderGeometry
import com.nectar.doodle.controls.table.Table
import com.nectar.doodle.controls.table.TableBehavior
import com.nectar.doodle.controls.table.TableBehavior.CellGenerator
import com.nectar.doodle.controls.table.TableBehavior.HeaderCellGenerator
import com.nectar.doodle.controls.table.TableBehavior.HeaderPositioner
import com.nectar.doodle.controls.table.TableBehavior.RowPositioner
import com.nectar.doodle.controls.theme.basic.ListPositioner
import com.nectar.doodle.controls.theme.basic.ListRow
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.CanvasBrush
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.Color.Companion.gray
import com.nectar.doodle.drawing.Color.Companion.green
import com.nectar.doodle.drawing.Color.Companion.lightgray
import com.nectar.doodle.drawing.Color.Companion.white
import com.nectar.doodle.drawing.ColorBrush
import com.nectar.doodle.drawing.Pen
import com.nectar.doodle.event.KeyEvent
import com.nectar.doodle.event.KeyListener
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.event.MouseListener
import com.nectar.doodle.event.MouseMotionListener
import com.nectar.doodle.focus.FocusManager
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.layout.constrain
import com.nectar.doodle.system.Cursor.Companion.EResize
import com.nectar.doodle.system.Cursor.Companion.EWResize
import com.nectar.doodle.system.Cursor.Companion.WResize
import com.nectar.doodle.system.SystemInputEvent
import com.nectar.doodle.utils.SetObserver

/**
 * Created by Nicholas Eddy on 4/8/19.
 */
class BasicTableBehavior<T>(
        private val focusManager  : FocusManager?,
                val rowHeight     : Double = 20.0,
                val headerColor   : Color? = lightgray,
                val evenRowColor  : Color? = white,
                val oddRowColor   : Color? = lightgray.lighter().lighter(),
                val selectionColor: Color? = green.lighter()): TableBehavior<T>, KeyListener {

    override var headerDirty: (() -> Unit)? = null
    override var bodyDirty  : (() -> Unit)? = null

    private val selectionChanged: SetObserver<Table<T, *>, Int> = { set,_,_ ->
        bodyDirty?.invoke()
    }

    override val cellGenerator = object: CellGenerator<T> {
        override fun <A> invoke(table: Table<T, *>, cell: A, row: Int, itemGenerator: ItemGenerator<A>, current: View?): View = when (current) {
            is ListRow<*> -> (current as ListRow<A>).apply { update(table, cell, row) }
            else          -> ListRow(table, cell, row, itemGenerator, selectionColor = null).apply {
                mouseChanged += object: MouseListener {
                    override fun mouseReleased(event: MouseEvent) {
                        focusManager?.requestFocus(table)
                    }
                }
            }
        }
    }

    override val headerPositioner = object: HeaderPositioner<T> {
        override fun invoke(table: Table<T, *>) = HeaderGeometry(0.0, 1.1 * rowHeight)
    }

    override val rowPositioner = object: RowPositioner<T> {
        private val delegate = ListPositioner(rowHeight)

        override fun invoke(table: Table<T, *>, row: T, index: Int) = delegate.invoke(table, table.insets, index)
        override fun rowFor(table: Table<T, *>, y: Double)          = delegate.rowFor(table.insets, y)
    }

    override val headerCellGenerator = object: HeaderCellGenerator<T> {
        override fun invoke(table: Table<T, *>, column: Column<T>) = object: View() {
            init {
                var initialWidth            = column.width
                var initialPosition: Point? = null
                var mouseDown               = false

                fun newCursor() = when {
                    column.width > column.minWidth && column.width < column.maxWidth ?: Double.MAX_VALUE -> EWResize
                    column.width < column.maxWidth ?: Double.MAX_VALUE                                   -> EResize
                    else                                                                                 -> WResize
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

                canvas.line(Point(x, 0.0), Point(x, height), Pen(headerColor?.darker(0.25f) ?: gray))
            }
        }
    }

    override fun renderHeader(table: Table<T, *>, canvas: Canvas) {
        headerColor?.let { canvas.rect(Rectangle(size = canvas.size), ColorBrush(it)) }
    }

    override fun renderBody(table: Table<T, *>, canvas: Canvas) {
        canvas.rect(Rectangle(size = canvas.size), CanvasBrush(Size(rowHeight, 2 * rowHeight)) {
            evenRowColor?.let { rect(Rectangle(                rowHeight, rowHeight), ColorBrush(it)) }
            oddRowColor?.let  { rect(Rectangle(0.0, rowHeight, rowHeight, rowHeight), ColorBrush(it)) }
        })

        if (selectionColor != null) {
            table.selection.map { it to table[it] }.forEach { (index, row) ->
                row?.let {
                    canvas.rect(rowPositioner(table, row, index), ColorBrush(selectionColor))
                }
            }
        }
    }

    // FIXME: Centralize
    override fun install(view: Table<T, *>) {
        view.keyChanged       += this
        view.selectionChanged += selectionChanged
    }

    override fun uninstall(view: Table<T, *>) {
        view.keyChanged       -= this
        view.selectionChanged -= selectionChanged
    }

    override fun keyPressed(event: KeyEvent) {
        (event.source as Selectable<Int>).let { list ->
            when (event.code) {
                KeyEvent.VK_UP, KeyEvent.VK_DOWN -> {
                    when (SystemInputEvent.Modifier.Shift) {
                        in event -> {
                            list.selectionAnchor?.let { anchor ->
                                list.lastSelection?.let { if (event.code == KeyEvent.VK_UP) list.previous(it) else list.next(it) }?.let { current ->
                                    when {
                                        current < anchor  -> list.setSelection((current .. anchor ).reversed().toSet())
                                        anchor  < current -> list.setSelection((anchor  .. current).           toSet())
                                        else              -> list.setSelection(setOf(current))
                                    }
                                }
                            }
                        }
                        else -> list.lastSelection?.let { if (event.code == KeyEvent.VK_UP) list.previous(it) else list.next(it) }?.let { list.setSelection(setOf(it)) }
                    }?.let { Unit } ?: Unit
                }

                KeyEvent.VK_A                    -> {
                    if (SystemInputEvent.Modifier.Ctrl in event || SystemInputEvent.Modifier.Meta in event) {
                        list.selectAll()
                    }
                }
            }
        }
    }
}