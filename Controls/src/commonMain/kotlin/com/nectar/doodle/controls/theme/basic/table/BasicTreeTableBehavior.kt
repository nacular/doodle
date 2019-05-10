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
import com.nectar.doodle.controls.theme.basic.TreeRow
import com.nectar.doodle.controls.tree.TreeLike
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
import com.nectar.doodle.event.KeyEvent.Companion.VK_A
import com.nectar.doodle.event.KeyEvent.Companion.VK_DOWN
import com.nectar.doodle.event.KeyEvent.Companion.VK_LEFT
import com.nectar.doodle.event.KeyEvent.Companion.VK_RIGHT
import com.nectar.doodle.event.KeyEvent.Companion.VK_UP
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
import com.nectar.doodle.system.SystemInputEvent.Modifier.Ctrl
import com.nectar.doodle.system.SystemInputEvent.Modifier.Meta
import com.nectar.doodle.system.SystemInputEvent.Modifier.Shift
import com.nectar.doodle.utils.Path
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

class BasicTreeTableBehavior<T>(
        private val focusManager  : FocusManager?,
                val rowHeight     : Double = 20.0,
                val headerColor   : Color? = lightgray,
                val evenRowColor  : Color? = white,
                val oddRowColor   : Color? = lightgray.lighter().lighter(),
                val selectionColor: Color? = green.lighter()): TreeTableBehavior<T>, KeyListener {

    override var headerDirty: (() -> Unit)? = null
    override var bodyDirty  : (() -> Unit)? = null

    private val selectionChanged: SetObserver<TreeTable<T, *>, Path<Int>> = { _,_,_ ->
        bodyDirty?.invoke()
    }

    private val expansionChanged: ExpansionObserver<T> = { treeTable,_ ->
        if (treeTable.selection.isNotEmpty()) {
            bodyDirty?.invoke()
        }
    }

    override val treeCellGenerator = object: TreeTableBehavior.TreeCellGenerator<T> {
        override fun <A> invoke(table: TreeTable<T, *>, cell: A, path: Path<Int>, row: Int, itemGenerator: ItemGenerator<A>, current: View?): View = when (current) {
            is TreeRow<*> -> (current as TreeRow<A>).apply { update(table, cell, path, table.rowFromPath(path)!!) }
            else          -> TreeRow(table, cell, path, table.rowFromPath(path)!!, object: ContentGenerator<A> {
                override fun invoke(tree: TreeLike, node: A, path: Path<Int>, index: Int, previous: View?): View = itemGenerator(node, previous)
            }, selectionColor = null).apply {
                mouseChanged += object: MouseListener {
                    override fun mouseReleased(event: MouseEvent) {
                        focusManager?.requestFocus(table)
                    }
                }
            }
        }
    }

    override val cellGenerator = object: CellGenerator<T> {
        override fun <A> invoke(table: TreeTable<T, *>, cell: A, path: Path<Int>, row: Int, itemGenerator: ItemGenerator<A>, current: View?): View = when (current) {
            is ListRow<*> -> (current as ListRow<A>).apply { update(table.map({ table.pathFromRow(it)!! }, { table.rowFromPath(it)!! }), cell, row) }
            else          -> ListRow(table.map({ table.pathFromRow(it)!! }, { table.rowFromPath(it)!! }), cell, row, itemGenerator, selectionColor = null).apply {
                mouseChanged += object: MouseListener {
                    override fun mouseReleased(event: MouseEvent) {
                        focusManager?.requestFocus(table)
                    }
                }
            }
        }
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
        override fun invoke(table: TreeTable<T, *>, column: Column<T>) = object: View() {
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

    override fun renderHeader(table: TreeTable<T, *>, canvas: Canvas) {
        headerColor?.let { canvas.rect(Rectangle(size = canvas.size), ColorBrush(it)) }
    }

    override fun renderBody(table: TreeTable<T, *>, canvas: Canvas) {
        canvas.rect(Rectangle(size = canvas.size), CanvasBrush(Size(rowHeight, 2 * rowHeight)) {
            evenRowColor?.let { rect(Rectangle(                rowHeight, rowHeight), ColorBrush(it)) }
            oddRowColor?.let  { rect(Rectangle(0.0, rowHeight, rowHeight, rowHeight), ColorBrush(it)) }
        })

        if (selectionColor != null) {
            table.selection.map { it to table[it] }.forEach { (path, row) ->
                row?.let {
                    canvas.rect(rowPositioner(table, path, row, table.rowFromPath(path)!!), ColorBrush(selectionColor))
                }
            }
        }
    }

    // FIXME: Centralize
    override fun install(view: TreeTable<T, *>) {
        view.expanded         += expansionChanged
        view.collapsed        += expansionChanged
        view.keyChanged       += this
        view.selectionChanged += selectionChanged
    }

    override fun uninstall(view: TreeTable<T, *>) {
        view.expanded         -= expansionChanged
        view.collapsed        -= expansionChanged
        view.keyChanged       -= this
        view.selectionChanged -= selectionChanged
    }

    override fun keyPressed(event: KeyEvent) {
        (event.source as TreeLike).let { tree ->
            when (event.code) {
                VK_UP, VK_DOWN -> {
                    when (Shift) {
                        in event -> {
                            tree.selectionAnchor?.let { anchor ->
                                tree.lastSelection?.let { if (event.code == VK_UP) tree.previous(it) else tree.next(it) }?.let { current ->
                                    val currentRow = tree.rowFromPath(current)
                                    val anchorRow  = tree.rowFromPath(anchor )

                                    if (currentRow != null && anchorRow != null) {
                                        when {
                                            currentRow < anchorRow -> tree.setSelection((currentRow..anchorRow).reversed().toSet())
                                            anchorRow < currentRow -> tree.setSelection((anchorRow..currentRow).toSet())
                                            else                   -> tree.setSelection(setOf(currentRow))
                                        }
                                    }
                                }
                            }
                        }
                        else -> tree.lastSelection?.let { if (event.code == VK_UP) tree.previous(it) else tree.next(it) }?.let { tree.setSelection(setOf(it)) }
                    }?.let { Unit } ?: Unit
                }
                VK_LEFT        -> tree.selection.firstOrNull()?.also { if (tree.expanded(it)) { tree.collapse(it) } else it.parent?.let { tree.setSelection(setOf(it)) } }?.let { Unit } ?: Unit
                VK_RIGHT       -> tree.selection.firstOrNull()?.also { tree.expand(it) }?.let { Unit } ?: Unit
                VK_A           -> {
                    if (Ctrl in event || Meta in event) {
                        tree.selectAll()
                    }
                }
            }
        }
    }
}