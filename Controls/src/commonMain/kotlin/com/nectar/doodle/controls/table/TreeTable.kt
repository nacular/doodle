package com.nectar.doodle.controls.table

import com.nectar.doodle.controls.DynamicListModel
import com.nectar.doodle.controls.IndexedItemVisualizer
import com.nectar.doodle.controls.ModelObserver
import com.nectar.doodle.controls.SelectionModel
import com.nectar.doodle.controls.list.DynamicList
import com.nectar.doodle.controls.list.ListBehavior
import com.nectar.doodle.controls.panels.ScrollPanel
import com.nectar.doodle.controls.theme.TreeBehavior
import com.nectar.doodle.controls.tree.Tree
import com.nectar.doodle.controls.tree.TreeLike
import com.nectar.doodle.controls.tree.TreeModel
import com.nectar.doodle.core.Box
import com.nectar.doodle.core.Layout
import com.nectar.doodle.core.PositionableContainer
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.layout.Constraints
import com.nectar.doodle.layout.constant
import com.nectar.doodle.layout.constrain
import com.nectar.doodle.utils.Completable
import com.nectar.doodle.utils.ObservableSet
import com.nectar.doodle.utils.Path
import com.nectar.doodle.utils.Pool
import com.nectar.doodle.utils.SetObserver
import com.nectar.doodle.utils.SetPool
import kotlin.math.max

/**
 * Created by Nicholas Eddy on 5/5/19.
 */

typealias ExpansionObserver<T>  = (source: TreeTable<T, *>, paths: Set<Path<Int>>) -> Unit
typealias ExpansionObservers<T> = SetPool<ExpansionObserver<T>>


private class TreePathIterator(private val tree: TreeLike): Iterator<Path<Int>> {
    private var index = 0

    override fun hasNext() = index < tree.numRows

    override fun next() = tree.pathFromRow(index)!!
}

private class TreeModelIterator<T>(private val model: TreeModel<T>, private val iterator: TreePathIterator): Iterator<T> {
    override fun hasNext() = iterator.hasNext()

    override fun next() = model[iterator.next()]!!
}

fun <T, R> Iterator<T>.map(mapper: (T) -> R) = object: Iterator<R> {
    override fun hasNext() = this@map.hasNext()

    override fun next() = mapper(this@map.next())
}

fun <T, R: Any> Iterator<T>.mapNotNull(mapper: (T) -> R?) = this.asSequence().mapNotNull(mapper).iterator()

fun <T, R> TreeModel<T>.map(mapper: (T) -> R) = object: TreeModel<R> {
    override fun get(path: Path<Int>): R? = this@map[path]?.let(mapper)

    override fun isEmpty() = this@map.isEmpty()

    override fun children(parent: Path<Int>) = this@map.children(parent).map(mapper)

    override fun isLeaf(node: Path<Int>) = this@map.isLeaf(node)

    override fun child(of: Path<Int>, path: Int) = this@map.child(of, path)?.let(mapper)

    override fun numChildren(of: Path<Int>) = this@map.numChildren(of)

    override fun indexOfChild(parent: Path<Int>, child: R) = children(parent).asSequence().indexOf(child)
}

fun <T: Any, R: Any> SelectionModel<T>.map(mapper: (T) -> R?, unmapper: (R) -> T?) = object: SelectionModel<R> {
    override val first   get() = this@map.first?.let(mapper)
    override val last    get() = this@map.last?.let(mapper)
    override val anchor  get() = this@map.anchor?.let(mapper)
    override val size    get() = this@map.size
    override val isEmpty get() = this@map.isEmpty

    override fun add(item: R) = /*unmapper(item)?.let { this@map.add(it) } ?:*/ false

    override fun clear() {} //= this@map.clear()

    override fun addAll(items: Collection<R>) = /*this@map.addAll(items.mapNotNull(unmapper))*/ false

    override fun remove(item: R) = /*unmapper(item)?.let { this@map.remove(it) } ?:*/ false

    override fun contains(item: R) = unmapper(item) in this@map

    override fun removeAll(items: Collection<R>) = /*this@map.removeAll(items.mapNotNull(unmapper))*/ false

    override fun retainAll(items: Collection<R>) = /*this@map.retainAll(items.mapNotNull(unmapper))*/ false

    override fun replaceAll(items: Collection<R>) = /*this@map.replaceAll(items.mapNotNull(unmapper))*/ false

    override fun containsAll(items: Collection<R>) = this@map.containsAll(items.mapNotNull(unmapper))

    override fun toggle(items: Collection<R>) = /*this@map.toggle(items.mapNotNull(unmapper))*/ false

    override fun iterator() = this@map.iterator().mapNotNull(mapper)

    // FIXME: This is pretty inefficient
    override val changed: Pool<SetObserver<R>> = SetPool()

    init {
        this@map.changed += { set, removed, added ->
            // FIXME: Can this be optimized?
            (changed as SetPool).forEach {
                it(ObservableSet(set.mapNotNull(mapper).toMutableSet()), removed.mapNotNull(mapper).toSet(), added.mapNotNull(mapper).toSet())
            }
        }
    }
}

open class TreeTable<T, M: TreeModel<T>>(model        : M,
                           protected val selectionModel: SelectionModel<Path<Int>>? = null,
                           private   val scrollCache   : Int                        = 10,
                                         block         : ColumnFactory<T>.() -> Unit): View(), TreeLike {

    override val rootVisible get() = tree.rootVisible

    override fun visible(row : Int      ) = tree.visible(row )
    override fun visible(path: Path<Int>) = tree.visible(path)

    override fun isLeaf  (path: Path<Int>) = tree.isLeaf  (path)
    override fun expanded(path: Path<Int>) = tree.expanded(path)
    override fun collapse(path: Path<Int>) = tree.collapse(path)
    override fun expand  (path: Path<Int>) = tree.expand  (path)

    override fun expandAll  () = tree.expandAll()
    override fun collapseAll() = tree.collapseAll()

    override fun selectAll      (                      ) = tree.selectAll      (      )
    override fun selected       (item  : Path<Int>     ) = tree.selected       (item  )
    override fun addSelection   (items : Set<Path<Int>>) = tree.addSelection   (items )
    override fun setSelection   (items : Set<Path<Int>>) = tree.setSelection   (items )
    override fun removeSelection(items : Set<Path<Int>>) = tree.removeSelection(items )
    override fun toggleSelection(items : Set<Path<Int>>) = tree.toggleSelection(items )
    override fun clearSelection (                      ) = tree.clearSelection (      )
    override fun next           (after : Path<Int>     ) = tree.next           (after )
    override fun previous       (before: Path<Int>     ) = tree.previous       (before)

    override val selection       get() = tree.selection
    override val lastSelection   get() = tree.lastSelection
    override val firstSelection  get() = tree.firstSelection
    override val selectionAnchor get() = tree.selectionAnchor

    val expanded : ExpansionObservers<T> by lazy { ExpansionObserversImpl(this) }
    val collapsed: ExpansionObservers<T> by lazy { ExpansionObserversImpl(this) }

    private lateinit var tree: Tree<*, TreeModel<*>>

    private class ExpansionObserversImpl<T>(private val source: TreeTable<T, *>, mutableSet: MutableSet<ExpansionObserver<T>> = mutableSetOf()): SetPool<ExpansionObserver<T>>(mutableSet) {
        operator fun invoke(paths: Set<Path<Int>>) = delegate.forEach { it(source, paths) }
    }

    private inner class ColumnFactoryImpl: ColumnFactory<T> {
        override fun <R> column(header: View?, extractor: Extractor<T, R>, cellVisualizer: CellVisualizer<R>, builder: ColumnBuilder.() -> Unit) = ColumnBuilderImpl().run {
            builder(this)

            if (!::tree.isInitialized) {
                InternalTreeColumn(header, headerAlignment, cellVisualizer, cellAlignment, width, minWidth, maxWidth, extractor).also {
                    internalColumns += it
                    tree = it.view.apply {
                        expanded += { _: Tree<*, *>, paths: Set<Path<Int>> ->
                            this@TreeTable.expanded.forEach { it(this@TreeTable, paths) }
                        }

                        collapsed += { _: Tree<*, *>, paths: Set<Path<Int>> ->
                            this@TreeTable.collapsed.forEach { it(this@TreeTable, paths) }
                        }
                    }
                }
            } else {
                InternalListColumn(header, headerAlignment, cellVisualizer, cellAlignment, width, minWidth, maxWidth, extractor).also { internalColumns += it }
            }
        }
    }

    private inner class TableLikeWrapper: TableLike {
        val delegate get() = this@TreeTable

        override val width            get() = this@TreeTable.width
        override val columns          get() = this@TreeTable.columns
        override val internalColumns  get() = this@TreeTable.internalColumns
        override val columnSizePolicy get() = this@TreeTable.columnSizePolicy
        override val header           get() = this@TreeTable.header
        override val panel            get() = this@TreeTable.panel

        override var resizingCol get() = this@TreeTable.resizingCol
            set(new) {
                this@TreeTable.resizingCol = new
            }

        override fun relayout() {
            this@TreeTable.relayout()
        }
    }

    private inner class TableLikeBehaviorWrapper: TableLikeBehavior<TableLikeWrapper> {
        val delegate get() = this@TreeTable.behavior

        override fun <B : TableLikeBehavior<TableLikeWrapper>, R> columnMoveStart(table: TableLikeWrapper, internalColumn: InternalColumn<TableLikeWrapper, B, R>) {
            behavior?.columnMoveStart(table.delegate, internalColumn)
        }

        override fun <B : TableLikeBehavior<TableLikeWrapper>, R> columnMoveEnd(table: TableLikeWrapper, internalColumn: InternalColumn<TableLikeWrapper, B, R>) {
            behavior?.columnMoveEnd(table.delegate, internalColumn)
        }

        override fun <B : TableLikeBehavior<TableLikeWrapper>, R> columnMoved(table: TableLikeWrapper, internalColumn: InternalColumn<TableLikeWrapper, B, R>) {
            behavior?.columnMoved(table.delegate, internalColumn)
        }

        override fun moveColumn(table: TableLikeWrapper, function: (Float) -> Unit): Completable? = behavior?.moveColumn(table.delegate, function)
    }

    private inner class InternalTreeColumn<R>(
            header        : View?,
            headerPosition: (Constraints.() -> Unit)?,
            cellGenerator : CellVisualizer<R>,
            cellPosition  : (Constraints.() -> Unit)?,
            preferredWidth: Double?        = null,
            minWidth      : Double         = 0.0,
            maxWidth      : Double?        = null,
            extractor     : Extractor<T, R>): InternalColumn<TableLikeWrapper, TableLikeBehaviorWrapper, R>(TableLikeWrapper(), TableLikeBehaviorWrapper(), header, headerPosition, cellGenerator, cellPosition, preferredWidth, minWidth, maxWidth, numFixedColumns = 1) {

        override val view = Tree(model.map(extractor), object: IndexedItemVisualizer<R> {
            override fun invoke(item: R, index: Int, previous: View?, isSelected: () -> Boolean) = this@InternalTreeColumn.cellGenerator(this@InternalTreeColumn, item, index, previous, isSelected)
        }, selectionModel, scrollCache = scrollCache).apply {
            acceptsThemes = false

//            expanded += { _,_ ->
//                this@InternalTreeColumn.minWidth       = minimumSize.width
//                this@InternalTreeColumn.preferredWidth = minimumSize.width
//            }
//
//            collapsed += { _,_ ->
//                this@InternalTreeColumn.minWidth       = minimumSize.width
//                this@InternalTreeColumn.preferredWidth = minimumSize.width
//            }
        }

        override fun behavior(behavior: TableLikeBehaviorWrapper?) {
            behavior?.delegate?.let {
                view.behavior = object: TreeBehavior<R> {
                    override val generator get() = object: TreeBehavior.RowGenerator<R> {
                        override fun invoke(tree: Tree<R, *>, node: R, path: Path<Int>, index: Int, current: View?) = it.treeCellGenerator(this@TreeTable, this@InternalTreeColumn, node, path, index, object: IndexedItemVisualizer<R> {
                            override fun invoke(item: R, index: Int, previous: View?, isSelected: () -> Boolean) = this@InternalTreeColumn.cellGenerator(this@InternalTreeColumn, item, index, previous, isSelected)
                        }, current)
                    }

                    override val positioner get() = object: TreeBehavior.RowPositioner<R> {
                        override fun rowBounds(tree: Tree<R, *>, node: R, path: Path<Int>, index: Int, current: View?) = it.rowPositioner.invoke(this@TreeTable, path, model[path]!!, index).run { Rectangle(0.0, y, tree.width, height) }

                        override fun contentBounds(tree: Tree<R, *>, node: R, path: Path<Int>, index: Int, current: View?) = rowBounds(tree, node, path, index, current) // FIXME

                        override fun row(of: Tree<R, *>, atY: Double) = it.rowPositioner.rowFor(this@TreeTable, atY)
                    }

                    override fun render(view: Tree<R, *>, canvas: Canvas) {
                        if (this@InternalTreeColumn != internalColumns.last()) {
                            it.renderColumnBody(this@TreeTable, this@InternalTreeColumn, canvas)
                        }
                    }
                }
            }
        }

        override fun moveBy(x: Double) {
            // NO-OP
        }

        override fun resetPosition() {
            // NO-OP
        }
    }

    /**
     * Returns all rows below the given [[Path]]; even if path is collapsed/invisible
     */
    private fun rowsBelow(path: Path<Int>): List<Path<Int>> = (0 until model.numChildren(path)).map { path + it }.flatMap { listOf(it) + if (expanded(it)) rowsBelow(it) else emptyList() }

    private inner class InternalListColumn<R>(
            header         : View?,
            headerAlignment: (Constraints.() -> Unit)? = null,
            cellGenerator  : CellVisualizer<R>,
            cellAlignment  : (Constraints.() -> Unit)? = null,
            preferredWidth : Double? = null,
            minWidth       : Double  = 0.0,
            maxWidth       : Double? = null,
            extractor      : Extractor<T, R>): InternalColumn<TableLikeWrapper, TableLikeBehaviorWrapper, R>(TableLikeWrapper(), TableLikeBehaviorWrapper(), header, headerAlignment, cellGenerator, cellAlignment, preferredWidth, minWidth, maxWidth, numFixedColumns = 1) {

        private inner class FieldModel<A>(private val model: M, private val extractor: Extractor<T, A>): DynamicListModel<A> {
            init {
                // FIXME: Centralize to avoid calling rowsBelow more than once per changed path
                expanded += { _: TreeTable<*,*>, paths: Set<Path<Int>> ->
                    val added = paths.flatMap { rowsBelow(it) }

                    // FIXME: This is way too expensive for large trees
                    changed.forEach {
                        it(this, emptyMap(), added.associate { rowFromPath(it)!! to extractor(model[it]!!) }, emptyMap())
                    }
                }

                collapsed += { _: TreeTable<*,*>, paths: Set<Path<Int>> ->
                    val removed = paths.flatMap {
                        var index = rowFromPath(it)!!

                        rowsBelow(it).map { index++ to it }
                    }

                    // FIXME: This is way too expensive for large trees
                    changed.forEach {
                        it(this, removed.associate { (index, path) -> index to extractor(model[path]!!) }, emptyMap(), emptyMap())
                    }
                }
            }

            override val changed = SetPool<ModelObserver<A>>()

            override val size get() = numRows

            override fun get(index: Int) = pathFromRow(index)?.let { model[it]?.let(extractor) }

            override fun section(range: ClosedRange<Int>) = iterator().asSequence().toList().subList(range.start, range.endInclusive + 1)

            override fun contains(value: A) = value in iterator().asSequence()

            // TODO: Re-use
            override fun iterator() = TreeModelIterator(model.map(extractor), TreePathIterator(this@TreeTable))
        }

        override val view = DynamicList(FieldModel(model, extractor), object: IndexedItemVisualizer<R> {
            override fun invoke(item: R, index: Int, previous: View?, isSelected: () -> Boolean) = object: View() {}
        }, selectionModel = selectionModel?.map({ rowFromPath(it) }, { pathFromRow(it) }), scrollCache = scrollCache).apply {
            acceptsThemes = false
        }

        override fun behavior(behavior: TableLikeBehaviorWrapper?) {
            behavior?.delegate?.let {
                view.behavior = object: ListBehavior<R> {
                    override val generator get() = object: ListBehavior.RowGenerator<R> {
                        override fun invoke(list: com.nectar.doodle.controls.list.List<R, *>, row: R, index: Int, current: View?) = it.cellGenerator(this@TreeTable, this@InternalListColumn, row, pathFromRow(index)!!, index, object: IndexedItemVisualizer<R> {
                            override fun invoke(item: R, index: Int, previous: View?, isSelected: () -> Boolean) = this@InternalListColumn.cellGenerator(this@InternalListColumn, item, index, previous, isSelected)
                        }, current)
                    }

                    override val positioner get() = object: ListBehavior.RowPositioner<R> {
                        override fun invoke(list: com.nectar.doodle.controls.list.List<R, *>, row: R, index: Int) = it.rowPositioner.invoke(this@TreeTable, pathFromRow(index)!!, model[pathFromRow(index)!!]!!, index).run { Rectangle(0.0, y, list.width, height) }

                        override fun rowFor(list: com.nectar.doodle.controls.list.List<R, *>, y: Double) = it.rowPositioner.rowFor(this@TreeTable, y)
                    }

                    override fun render(view: com.nectar.doodle.controls.list.List<R, *>, canvas: Canvas) {
                        if (this@InternalListColumn != internalColumns.last()) {
                            it.renderColumnBody(this@TreeTable, this@InternalListColumn, canvas)
                        }
                    }
                }
            }
        }
    }

    var model = model
        private set

    override val numRows: Int get() = tree.numRows

    operator fun get(path: Path<Int>): T? = model[path]

    operator fun get(row: Int): T? = pathFromRow(row)?.let { model[it] }

    override fun pathFromRow(index: Int) = tree.pathFromRow(index)

    override fun rowFromPath(path: Path<Int>) = tree.rowFromPath(path)

    var columnSizePolicy: ColumnSizePolicy = ConstrainedSizePolicy()
        set(new) {
            field = new

            relayout()
        }

    var behavior = null as TreeTableBehavior<T>?
        set(new) {
            if (new == behavior) { return }

            field?.let {
                it.bodyDirty   = null
                it.headerDirty = null
                it.columnDirty = null

                it.uninstall(this)
            }

            field = new

            new?.also { behavior ->
                behavior.bodyDirty   = bodyDirty
                behavior.headerDirty = headerDirty
                behavior.columnDirty = columnDirty

                (internalColumns as MutableList<InternalColumn<TableLikeWrapper, TableLikeBehaviorWrapper, *>>).forEach {
                    it.behavior(TableLikeBehaviorWrapper())
                }

                behavior.install(this)

                header.children.batch {
                    clear()

                    headerItemsToColumns.clear()

                    addAll(internalColumns.dropLast(1).map { column ->
                        behavior.headerCellGenerator(this@TreeTable, column).also {
                            headerItemsToColumns[it] = column
                        }
                    })
                }

                behavior.headerPositioner.invoke(this@TreeTable).apply {
                    header.height = height
                }

                layout = constrain(header, panel) { header, panel ->
                    behavior.headerPositioner.invoke(this@TreeTable).apply {
                        header.top    = header.parent.top + y
                        header.height = constant(height)
                    }

                    panel.top    = header.bottom
                    panel.left   = panel.parent.left
                    panel.right  = panel.parent.right
                    panel.bottom = panel.parent.bottom
                }
            }
        }

    val columns: List<Column<*>> get() = internalColumns.dropLast(1)

    val selectionChanged: Pool<SetObserver<Path<Int>>> = SetPool()

    private val internalColumns = mutableListOf<InternalColumn<*, *, *>>()

    init {
        ColumnFactoryImpl().apply(block)

        internalColumns += InternalListColumn(header = null, cellGenerator = object: CellVisualizer<Unit> {
            override fun invoke(column: Column<Unit>, item: Unit, row: Int, previous: View?, isSelected: () -> Boolean) = previous ?: object: View() {}
        }) {} // FIXME: Use a more robust method to avoid any rendering of the cell contents
    }

    private val headerItemsToColumns = mutableMapOf<View, InternalColumn<*, *, *>>()

    private val header: Box = object: Box() {
        init {
            layout = object: Layout {
                override fun layout(container: PositionableContainer) {
                    var x = 0.0
                    var totalWidth = 0.0

                    container.children.forEachIndexed { index, view ->
                        view.bounds = Rectangle(Point(x, 0.0), Size(internalColumns[index].width, container.height))

                        x += view.width
                        totalWidth += view.width
                    }

                    container.width = totalWidth + internalColumns[internalColumns.size - 1].width
                }
            }
        }

        override fun render(canvas: Canvas) {
            behavior?.renderHeader(this@TreeTable, canvas)
        }
    }

    private val panel = ScrollPanel(object: Box() {
        init {
            children += internalColumns.map { it.view }

            layout = object: Layout {
                override fun layout(container: PositionableContainer) {
                    var x          = 0.0
                    var height     = 0.0
                    var totalWidth = 0.0

                    container.children.forEachIndexed { index, view ->
                        view.bounds = Rectangle(Point(x, 0.0), Size(internalColumns[index].width, view.minimumSize.height))

                        x          += view.width
                        height      = max(height, view.height)
                        totalWidth += view.width
                    }

                    container.height = max(container.parent!!.height, height)

                    container.children.forEach {
                        it.height = container.height
                    }
                }
            }
        }

        override fun render(canvas: Canvas) {
            behavior?.renderBody(this@TreeTable, canvas)
        }
    }.apply {
        // FIXME: Use two scroll-panels instead since async scrolling makes this look bad
        boundsChanged += { _,old,new ->
            if (old.x != new.x) {
                header.x = new.x
            }
        }
    }).apply {
        scrollsHorizontally = false
    }

    @Suppress("PrivatePropertyName")
    protected open val selectionChanged_: SetObserver<Path<Int>> = { set,removed,added ->
        (selectionChanged as SetPool).forEach {
            it(set, removed, added)
        }
    }

    init {
        children += listOf(header, panel)

        selectionModel?.let { it.changed += selectionChanged_ }
    }

    private val bodyDirty  : (         ) -> Unit = { panel.content?.rerender() }
    private val headerDirty: (         ) -> Unit = { header.rerender        () }
    private val columnDirty: (Column<*>) -> Unit = { (it as? InternalColumn<*, *, *>)?.view?.rerender() }

    override fun removedFromDisplay() {
        selectionModel?.let { it.changed -= selectionChanged_ }

        super.removedFromDisplay()
    }

    public override var insets
        get(   ) = super.insets
        set(new) { super.insets = new }

    private var resizingCol: Int? = null

    override fun doLayout() {
        resizingCol = resizingCol ?: 0
        width       = columnSizePolicy.layout(this.width, this.internalColumns, resizingCol ?: 0)
        resizingCol = null

        super.doLayout()

        // Needed b/c width of header isn't constrained
        header.relayout()
        (panel.content as? Box)?.relayout() // FIXME
    }
}