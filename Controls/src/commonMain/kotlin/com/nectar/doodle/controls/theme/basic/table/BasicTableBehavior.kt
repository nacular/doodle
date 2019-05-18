package com.nectar.doodle.controls.theme.basic.table

import com.nectar.doodle.controls.ItemGenerator
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
import com.nectar.doodle.controls.theme.basic.SelectableListKeyHandler
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.Color.Companion.green
import com.nectar.doodle.drawing.Color.Companion.lightgray
import com.nectar.doodle.drawing.Color.Companion.white
import com.nectar.doodle.drawing.ColorBrush
import com.nectar.doodle.drawing.stripedBrush
import com.nectar.doodle.event.KeyEvent
import com.nectar.doodle.event.KeyListener
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.event.MouseListener
import com.nectar.doodle.focus.FocusManager
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.utils.SetObserver

/**
 * Created by Nicholas Eddy on 4/8/19.
 */
open class BasicTableBehavior<T>(
        private val focusManager  : FocusManager?,
        private val rowHeight     : Double = 20.0,
        private val headerColor   : Color? = lightgray,
                    evenRowColor  : Color? = white,
                    oddRowColor   : Color? = lightgray.lighter().lighter(),
        private val selectionColor: Color? = green.lighter()): TableBehavior<T>, KeyListener, SelectableListKeyHandler {

    override var headerDirty: (() -> Unit)? = null
    override var bodyDirty  : (() -> Unit)? = null

    private val selectionChanged: SetObserver<Table<T, *>, Int> = { _,_,_ ->
        bodyDirty?.invoke()
    }

    private val canvasBrush = stripedBrush(rowHeight, evenRowColor, oddRowColor)

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

        override fun invoke(table: Table<T, *>, row: T, index: Int) = delegate(table, table.insets, index)
        override fun rowFor(table: Table<T, *>, y: Double)          = delegate.rowFor(table.insets, y)
    }

    override val headerCellGenerator = object: HeaderCellGenerator<T> {
        override fun <A> invoke(table: Table<T, *>, column: Column<A>) = TableHeaderCell(column, headerColor)
    }

    override fun renderHeader(table: Table<T, *>, canvas: Canvas) {
        headerColor?.let { canvas.rect(Rectangle(size = canvas.size), ColorBrush(it)) }
    }

    override fun renderBody(table: Table<T, *>, canvas: Canvas) {
        canvas.rect(Rectangle(size = canvas.size), canvasBrush)

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
        super<SelectableListKeyHandler>.keyPressed(event)
    }
}