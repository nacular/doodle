package com.nectar.doodle.theme.basic.table

import com.nectar.doodle.controls.IndexedItemVisualizer
import com.nectar.doodle.controls.list.ListLike
import com.nectar.doodle.controls.table.Column
import com.nectar.doodle.controls.table.ExpansionObserver
import com.nectar.doodle.controls.table.HeaderGeometry
import com.nectar.doodle.controls.table.TreeTable
import com.nectar.doodle.controls.table.TreeTableBehavior
import com.nectar.doodle.controls.table.TreeTableBehavior.CellGenerator
import com.nectar.doodle.controls.table.TreeTableBehavior.HeaderCellGenerator
import com.nectar.doodle.controls.table.TreeTableBehavior.HeaderPositioner
import com.nectar.doodle.controls.table.TreeTableBehavior.RowPositioner
import com.nectar.doodle.controls.tree.TreeLike
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.Color.Companion.Green
import com.nectar.doodle.drawing.Color.Companion.Lightgray
import com.nectar.doodle.drawing.Color.Companion.White
import com.nectar.doodle.drawing.ColorBrush
import com.nectar.doodle.drawing.horizontalStripedBrush
import com.nectar.doodle.drawing.lighter
import com.nectar.doodle.event.KeyEvent
import com.nectar.doodle.event.KeyListener
import com.nectar.doodle.event.PointerEvent
import com.nectar.doodle.event.PointerListener
import com.nectar.doodle.focus.FocusManager
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.layout.Insets
import com.nectar.doodle.theme.basic.ListPositioner
import com.nectar.doodle.theme.basic.ListRow
import com.nectar.doodle.theme.basic.SelectableTreeKeyHandler
import com.nectar.doodle.theme.basic.SimpleTreeRowIcon
import com.nectar.doodle.theme.basic.TreeRow
import com.nectar.doodle.utils.Path
import com.nectar.doodle.utils.PropertyObserver
import com.nectar.doodle.utils.SetObserver

/**
 * Created by Nicholas Eddy on 4/8/19.
 */
fun TreeLike.map(mapper: (Int) -> Path<Int>, unmapper: (Path<Int>) -> Int) = object: ListLike {
    override val hasFocus     get() = this@map.hasFocus
    override val focusChanged get() = this@map.focusChanged

    override fun selectAll     () = this@map.selectAll     ()
    override fun clearSelection() = this@map.clearSelection()

    override fun selected       (item : Int     ) = this@map.selected       (mapper(item))
    override fun setSelection   (items: Set<Int>) = this@map.setSelection   (items.map(mapper).toSet())
    override fun addSelection   (items: Set<Int>) = this@map.addSelection   (items.map(mapper).toSet())
    override fun removeSelection(items: Set<Int>) = this@map.removeSelection(items.map(mapper).toSet())
    override fun toggleSelection(items: Set<Int>) = this@map.toggleSelection(items.map(mapper).toSet())

    override fun next    (after : Int): Int? = this@map.next    (mapper(after ))?.let(unmapper)
    override fun previous(before: Int): Int? = this@map.previous(mapper(before))?.let(unmapper)

    override val selection      : Set<Int> get() = this@map.selection.map(unmapper).toSet()
    override val lastSelection  : Int?     get() = this@map.lastSelection?.let(unmapper)
    override val firstSelection : Int?     get() = this@map.firstSelection?.let(unmapper)
    override val selectionAnchor: Int?     get() = this@map.selectionAnchor?.let(unmapper)
}

open class BasicTreeTableBehavior<T>(
        private val focusManager         : FocusManager?,
        private val rowHeight            : Double = 20.0,
        private val headerColor          : Color? = Lightgray,
                    evenRowColor         : Color? = White,
                    oddRowColor          : Color? = Lightgray.lighter().lighter(),
                    iconColor            : Color  = Color.Black,
        private val selectionColor       : Color? = Green.lighter(),
        private val blurredSelectionColor: Color? = Lightgray): TreeTableBehavior<T>, PointerListener, KeyListener, SelectableTreeKeyHandler {

    override var headerDirty: ((         ) -> Unit)? = null
    override var bodyDirty  : ((         ) -> Unit)? = null
    override var columnDirty: ((Column<*>) -> Unit)? = null

    private val selectionChanged: SetObserver<Path<Int>> = { _,_,_ ->
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

    private val canvasBrush = horizontalStripedBrush(rowHeight, evenRowColor, oddRowColor)

    private val movingColumns = mutableSetOf<Column<*>>()

    override val treeCellGenerator = object: TreeTableBehavior.TreeCellGenerator<T> {
        override fun <A> invoke(table: TreeTable<T, *>, column: Column<A>, cell: A, path: Path<Int>, row: Int, itemGenerator: IndexedItemVisualizer<A>, current: View?): View = when (current) {
            is TreeRow<*> -> (current as TreeRow<A>).apply { update(table, cell, path, table.rowFromPath(path)!!) }
            else          -> TreeRow(table, cell, path, table.rowFromPath(path)!!, selectionColor = null, itemVisualizer = object: IndexedItemVisualizer<A> {
                override fun invoke(item: A, index: Int, previous: View?, isSelected: () -> Boolean) = itemGenerator(item, index, previous, isSelected)
            }, iconFactory = { SimpleTreeRowIcon(iconColor) })
        }
    }

    override val cellGenerator = object: CellGenerator<T> {
        override fun <A> invoke(table: TreeTable<T, *>, column: Column<A>, cell: A, path: Path<Int>, row: Int, itemGenerator: IndexedItemVisualizer<A>, current: View?): View = when (current) {
            is ListRow<*> -> (current as ListRow<A>).apply { update(table.map({ table.pathFromRow(it)!! }, { table.rowFromPath(it)!! }), cell, row) }
            else          -> ListRow(table.map({ table.pathFromRow(it)!! }, { table.rowFromPath(it)!! }), cell, row, itemGenerator, backgroundSelectionColor = null)
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
        override fun <A> invoke(table: TreeTable<T, *>, column: Column<A>) = TableHeaderCell(column, headerColor)
    }

    override fun renderHeader(table: TreeTable<T, *>, canvas: Canvas) {
        headerColor?.let { canvas.rect(Rectangle(size = canvas.size), ColorBrush(it)) }
    }

    override fun renderBody(table: TreeTable<T, *>, canvas: Canvas) {
        canvas.rect(Rectangle(size = canvas.size), canvasBrush)

        val color = if (table.hasFocus) selectionColor else blurredSelectionColor

        if (color != null) {
            // FIXME: Performance can be bad for large lists
            table.selection.map { it to table[it] }.forEach { (path, row) ->
                row?.let {
                    canvas.rect(rowPositioner(table, path, row, table.rowFromPath(path)!!).inset(Insets(top = 1.0)), ColorBrush(color))
                }
            }
        }
    }

    override fun <A> renderColumnBody(table: TreeTable<T, *>, column: Column<A>, canvas: Canvas) {
        if (column in movingColumns && headerColor != null) {
            canvas.rect(Rectangle(size = canvas.size), ColorBrush(headerColor.opacity(0.2f)))
        }
    }

    // FIXME: Centralize
    override fun install(view: TreeTable<T, *>) {
        view.expanded         += expansionChanged
        view.collapsed        += expansionChanged
        view.keyChanged       += this
        view.pointerChanged   += this
        view.focusChanged     += focusChanged
        view.selectionChanged += selectionChanged

        bodyDirty?.invoke  ()
        headerDirty?.invoke()
    }

    override fun uninstall(view: TreeTable<T, *>) {
        view.expanded         -= expansionChanged
        view.collapsed        -= expansionChanged
        view.keyChanged       -= this
        view.pointerChanged   -= this
        view.focusChanged     -= focusChanged
        view.selectionChanged -= selectionChanged
    }

    override fun pressed(event: PointerEvent) {
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