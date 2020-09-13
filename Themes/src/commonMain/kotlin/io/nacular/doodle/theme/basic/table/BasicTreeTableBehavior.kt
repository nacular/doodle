package io.nacular.doodle.theme.basic.table

import io.nacular.doodle.controls.IndexedItemVisualizer
import io.nacular.doodle.controls.list.ListLike
import io.nacular.doodle.controls.table.Column
import io.nacular.doodle.controls.table.ExpansionObserver
import io.nacular.doodle.controls.table.HeaderGeometry
import io.nacular.doodle.controls.table.TreeTable
import io.nacular.doodle.controls.table.TreeTableBehavior
import io.nacular.doodle.controls.table.TreeTableBehavior.CellGenerator
import io.nacular.doodle.controls.table.TreeTableBehavior.HeaderCellGenerator
import io.nacular.doodle.controls.table.TreeTableBehavior.HeaderPositioner
import io.nacular.doodle.controls.table.TreeTableBehavior.OverflowColumnConfig
import io.nacular.doodle.controls.table.TreeTableBehavior.RowPositioner
import io.nacular.doodle.controls.table.TreeTableBehavior.TreeCellGenerator
import io.nacular.doodle.controls.tree.TreeLike
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.Blue
import io.nacular.doodle.drawing.Color.Companion.Lightgray
import io.nacular.doodle.drawing.Color.Companion.White
import io.nacular.doodle.drawing.ColorFill
import io.nacular.doodle.drawing.horizontalStripedFill
import io.nacular.doodle.drawing.lighter
import io.nacular.doodle.drawing.opacity
import io.nacular.doodle.event.KeyEvent
import io.nacular.doodle.event.KeyListener
import io.nacular.doodle.event.PointerEvent
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.system.SystemInputEvent.Modifier.Ctrl
import io.nacular.doodle.system.SystemInputEvent.Modifier.Meta
import io.nacular.doodle.system.SystemInputEvent.Modifier.Shift
import io.nacular.doodle.theme.basic.ListPositioner
import io.nacular.doodle.theme.basic.ListRow
import io.nacular.doodle.theme.basic.SelectableTreeKeyHandler
import io.nacular.doodle.theme.basic.SimpleTreeRowIcon
import io.nacular.doodle.theme.basic.TreeRow
import io.nacular.doodle.utils.Path
import io.nacular.doodle.utils.PropertyObserver
import io.nacular.doodle.utils.SetObserver

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
                    iconColor            : Color  = Black,
        private val selectionColor       : Color? = Blue,
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

    private val canvasFill = horizontalStripedFill(rowHeight, evenRowColor, oddRowColor)

    private val movingColumns = mutableSetOf<Column<*>>()

    override val treeCellGenerator = object: TreeCellGenerator<T> {
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

    override val rowPositioner = object: RowPositioner<T>() {
        private val delegate = ListPositioner(rowHeight)

        override fun rowBounds(of: TreeTable<T, *>, path: Path<Int>, row: T, index: Int) = delegate.rowBounds(of.width, of.insets, index)
        override fun rowFor(of: TreeTable<T, *>, y: Double)                           = delegate.rowFor(of.insets, y)
        override fun height(of: TreeTable<T, *>, below: Path<Int>)                    = delegate.totalHeight(of.rowsBelow(below), of.insets)
    }

    override val headerCellGenerator = object: HeaderCellGenerator<T> {
        override fun <A> invoke(table: TreeTable<T, *>, column: Column<A>) = TableHeaderCell(column, headerColor)
    }

    override val overflowColumnConfig = object: OverflowColumnConfig<T> {
        override fun body(table: TreeTable<T, *>): View? = object: View() {
            init {
                pointerChanged += object: PointerListener {
                    private var pointerOver    = false
                    private var pointerPressed = false

                    override fun entered(event: PointerEvent) {
                        pointerOver = true
                    }

                    override fun exited(event: PointerEvent) {
                        pointerOver = false
                    }

                    override fun pressed(event: PointerEvent) {
                        pointerPressed = true
                    }

                    override fun released(event: PointerEvent) {
                        if (pointerOver && pointerPressed) {
                            val index = rowPositioner.rowFor(table, event.location.y)

                            if (index >= table.numRows) {
                                return
                            }

                            when {
                                Ctrl  in event.modifiers || Meta in event.modifiers     -> table.toggleSelection(setOf(index))
                                Shift in event.modifiers && table.lastSelection != null -> {
                                    table.selectionAnchor?.let { table.rowFromPath(it) }?.let { anchor ->
                                        when {
                                            index  < anchor -> table.setSelection((index.. anchor ).reversed().toSet())
                                            anchor < index  -> table.setSelection((anchor  ..index).           toSet())
                                        }
                                    }
                                }
                                else                                                    -> table.setSelection(setOf(index))
                            }
                        }

                        pointerPressed = false
                    }
                }
            }
        }
    }

    override fun renderHeader(table: TreeTable<T, *>, canvas: Canvas) {
        headerColor?.let { canvas.rect(Rectangle(size = canvas.size), ColorFill(it)) }
    }

    override fun renderBody(table: TreeTable<T, *>, canvas: Canvas) {
        canvas.rect(Rectangle(size = canvas.size), canvasFill)

        val color = if (table.hasFocus) selectionColor else blurredSelectionColor

        if (color != null) {
            // FIXME: Performance can be bad for large lists
            table.selection.map { it to table[it] }.forEach { (path, row) ->
                row?.let {
                    canvas.rect(rowPositioner.rowBounds(table, path, row, table.rowFromPath(path)!!).inset(Insets(top = 1.0)), ColorFill(color))
                }
            }
        }
    }

    override fun <A> renderColumnBody(table: TreeTable<T, *>, column: Column<A>, canvas: Canvas) {
        if (column in movingColumns && headerColor != null) {
            canvas.rect(Rectangle(size = canvas.size), ColorFill(headerColor.opacity(0.2f)))
        }
    }

    // FIXME: Centralize
    override fun install(view: TreeTable<T, *>) {
        view.expanded         += expansionChanged
        view.collapsed        += expansionChanged
        view.keyChanged       += this
        view.focusChanged     += focusChanged
        view.pointerChanged   += this
        view.selectionChanged += selectionChanged

        bodyDirty?.invoke  ()
        headerDirty?.invoke()
    }

    override fun uninstall(view: TreeTable<T, *>) {
        view.expanded         -= expansionChanged
        view.collapsed        -= expansionChanged
        view.keyChanged       -= this
        view.focusChanged     -= focusChanged
        view.pointerChanged   -= this
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