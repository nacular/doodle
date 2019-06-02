package com.nectar.doodle.controls.theme.basic.table

import com.nectar.doodle.controls.ItemGenerator
import com.nectar.doodle.controls.Selectable
import com.nectar.doodle.controls.table.Column
import com.nectar.doodle.controls.table.ExpansionObserver
import com.nectar.doodle.controls.table.HeaderGeometry
import com.nectar.doodle.controls.table.TreeTable
import com.nectar.doodle.controls.table.TreeTableBehavior
import com.nectar.doodle.controls.table.TreeTableBehavior.CellGenerator
import com.nectar.doodle.controls.table.TreeTableBehavior.HeaderCellGenerator
import com.nectar.doodle.controls.table.TreeTableBehavior.HeaderPositioner
import com.nectar.doodle.controls.table.TreeTableBehavior.RowPositioner
import com.nectar.doodle.controls.theme.basic.ContentGenerator
import com.nectar.doodle.controls.theme.basic.ListPositioner
import com.nectar.doodle.controls.theme.basic.ListRow
import com.nectar.doodle.controls.theme.basic.SelectableTreeKeyHandler
import com.nectar.doodle.controls.theme.basic.SimpleTreeRowIcon
import com.nectar.doodle.controls.theme.basic.TreeRow
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
import com.nectar.doodle.utils.Path
import com.nectar.doodle.utils.PropertyObserver
import com.nectar.doodle.utils.SetObserver

/**
 * Created by Nicholas Eddy on 4/8/19.
 */
fun <T, R> Selectable<T>.map(mapper: (R) -> T, unmapper: (T) -> R) = object: Selectable<R> {

    override fun selectAll     () = this@map.selectAll     ()
    override fun clearSelection() = this@map.clearSelection()

    override fun selected       (item : R     ) = this@map.selected       (mapper(item))
    override fun setSelection   (items: Set<R>) = this@map.setSelection   (items.map(mapper).toSet())
    override fun addSelection   (items: Set<R>) = this@map.addSelection   (items.map(mapper).toSet())
    override fun removeSelection(items: Set<R>) = this@map.removeSelection(items.map(mapper).toSet())
    override fun toggleSelection(items: Set<R>) = this@map.toggleSelection(items.map(mapper).toSet())

    override fun next    (after : R): R? = this@map.next    (mapper(after ))?.let(unmapper)
    override fun previous(before: R): R? = this@map.previous(mapper(before))?.let(unmapper)

    override val selection      : Set<R> get() = this@map.selection.map(unmapper).toSet()
    override val lastSelection  : R?     get() = this@map.lastSelection?.let(unmapper)
    override val firstSelection : R?     get() = this@map.firstSelection?.let(unmapper)
    override val selectionAnchor: R?     get() = this@map.selectionAnchor?.let(unmapper)
}

open class BasicTreeTableBehavior<T>(
        private val focusManager         : FocusManager?,
        private val rowHeight            : Double = 20.0,
        private val headerColor          : Color? = lightgray,
                    evenRowColor         : Color? = white,
                    oddRowColor          : Color? = lightgray.lighter().lighter(),
        private val selectionColor       : Color? = green.lighter(),
        private val blurredSelectionColor: Color? = lightgray): TreeTableBehavior<T>, MouseListener, KeyListener, SelectableTreeKeyHandler {

    override var headerDirty: ((         ) -> Unit)? = null
    override var bodyDirty  : ((         ) -> Unit)? = null
    override var columnDirty: ((Column<*>) -> Unit)? = null

    private val selectionChanged: SetObserver<TreeTable<T, *>, Path<Int>> = { _,_,_ ->
        bodyDirty?.invoke()
    }

    private val focusChanged: PropertyObserver<View, Boolean> = { _,_,_ ->
        bodyDirty?.invoke()
    }

    private val expansionChanged: ExpansionObserver<T> = { treeTable,_ ->
        if (treeTable.selection.isNotEmpty()) {
            bodyDirty?.invoke()
        }
    }

    private val canvasBrush = stripedBrush(rowHeight, evenRowColor, oddRowColor)

    private val movingColumns = mutableSetOf<Column<*>>()

    override val treeCellGenerator = object: TreeTableBehavior.TreeCellGenerator<T> {
        override fun <A> invoke(table: TreeTable<T, *>, column: Column<A>, cell: A, path: Path<Int>, row: Int, itemGenerator: ItemGenerator<A>, current: View?): View = when (current) {
            is TreeRow<*> -> (current as TreeRow<A>).apply { update(table, cell, path, table.rowFromPath(path)!!) }
            else          -> TreeRow(table, cell, path, table.rowFromPath(path)!!, object: ContentGenerator<A> {
                override fun invoke(item: A, previous: View?) = itemGenerator(item, previous)
            }, iconFactory = { SimpleTreeRowIcon() }, selectionColor = null)
        }
    }

    override val cellGenerator = object: CellGenerator<T> {
        override fun <A> invoke(table: TreeTable<T, *>, column: Column<A>, cell: A, path: Path<Int>, row: Int, itemGenerator: ItemGenerator<A>, current: View?): View = when (current) {
            is ListRow<*> -> (current as ListRow<A>).apply { update(table.map({ table.pathFromRow(it)!! }, { table.rowFromPath(it)!! }), cell, row) }
            else          -> ListRow(table.map({ table.pathFromRow(it)!! }, { table.rowFromPath(it)!! }), cell, row, itemGenerator, selectionColor = null)
        }.apply { column.cellAlignment?.let { positioner = it } }
    }

    override val headerPositioner = object: HeaderPositioner<T> {
        override fun invoke(table: TreeTable<T, *>) = HeaderGeometry(0.0, 1.1 * rowHeight)
    }

    override val rowPositioner = object: RowPositioner<T> {
        private val delegate = ListPositioner(rowHeight)

        override fun invoke(table: TreeTable<T, *>, path: Path<Int>, row: T, index: Int) = delegate(table, table.insets, index)
        override fun rowFor(table: TreeTable<T, *>, y: Double)                           = delegate.rowFor(table.insets, y)
    }

    override val headerCellGenerator = object: HeaderCellGenerator<T> {
        override fun <A> invoke(table: TreeTable<T, *>, column: Column<A>) = TableHeaderCell(column, headerColor).apply { column.headerAlignment?.let { positioner = it } }
    }

    override fun renderHeader(table: TreeTable<T, *>, canvas: Canvas) {
        headerColor?.let { canvas.rect(Rectangle(size = canvas.size), ColorBrush(it)) }
    }

    override fun renderBody(table: TreeTable<T, *>, canvas: Canvas) {
        canvas.rect(Rectangle(size = canvas.size), canvasBrush)

        val color = if (table.hasFocus) selectionColor else blurredSelectionColor

        if (color != null) {
            table.selection.map { it to table[it] }.forEach { (path, row) ->
                row?.let {
                    canvas.rect(rowPositioner(table, path, row, table.rowFromPath(path)!!), ColorBrush(color))
                }
            }
        }
    }

    override fun <A> renderColumnBody(table: TreeTable<T, *>, column: Column<A>, canvas: Canvas) {
        if (column in movingColumns && headerColor != null) {
            canvas.rect(Rectangle(size = canvas.size), ColorBrush(headerColor.with(0.2f)))
        }
    }

    // FIXME: Centralize
    override fun install(view: TreeTable<T, *>) {
        view.expanded         += expansionChanged
        view.collapsed        += expansionChanged
        view.keyChanged       += this
        view.mouseChanged     += this
        view.focusChanged     += focusChanged
        view.selectionChanged += selectionChanged
    }

    override fun uninstall(view: TreeTable<T, *>) {
        view.expanded         -= expansionChanged
        view.collapsed        -= expansionChanged
        view.keyChanged       -= this
        view.mouseChanged     -= this
        view.focusChanged     -= focusChanged
        view.selectionChanged -= selectionChanged
    }

    override fun mousePressed(event: MouseEvent) {
        focusManager?.requestFocus(event.source)
    }

    override fun keyPressed(event: KeyEvent) {
        super<SelectableTreeKeyHandler>.keyPressed(event)
    }

    override fun <A> columnMoveStart(table: TreeTable<T, *>, column: Column<A>) {
        if (headerColor == null) {
            return
        }

        movingColumns += column

        columnDirty?.invoke(column)
    }

    override fun <A> columnMoveEnd(table: TreeTable<T, *>, column: Column<A>) {
        if (headerColor == null) {
            return
        }

        movingColumns -= column

        columnDirty?.invoke(column)
    }
}