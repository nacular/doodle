package io.nacular.doodle.controls.table

import io.nacular.doodle.controls.DynamicListModel
import io.nacular.doodle.controls.IndexedIem
import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.controls.ModelObserver
import io.nacular.doodle.controls.SelectionModel
import io.nacular.doodle.controls.list.DynamicList
import io.nacular.doodle.controls.list.ListBehavior
import io.nacular.doodle.controls.panels.ScrollPanel
import io.nacular.doodle.controls.theme.TreeBehavior
import io.nacular.doodle.controls.tree.Tree
import io.nacular.doodle.controls.tree.TreeLike
import io.nacular.doodle.controls.tree.TreeModel
import io.nacular.doodle.core.Container
import io.nacular.doodle.core.View
import io.nacular.doodle.core.behavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.layout.Constraints
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.constant
import io.nacular.doodle.layout.constrain
import io.nacular.doodle.utils.Completable
import io.nacular.doodle.utils.Path
import io.nacular.doodle.utils.Pool
import io.nacular.doodle.utils.SetObserver
import io.nacular.doodle.utils.SetPool

/**
 * Created by Nicholas Eddy on 5/5/19.
 */

public typealias ExpansionObserver<T>  = (source: TreeTable<T, *>, paths: Set<Path<Int>>) -> Unit
public typealias ExpansionObservers<T> = SetPool<ExpansionObserver<T>>

private class TreePathIterator(private val tree: TreeLike): Iterator<Path<Int>> {
    private var index = 0

    override fun hasNext() = index < tree.numRows

    override fun next() = tree.pathFromRow(index)!!
}

private class TreeModelIterator<T>(private val model: TreeModel<T>, private val iterator: TreePathIterator): Iterator<T> {
    override fun hasNext() = iterator.hasNext()

    override fun next() = model[iterator.next()]!!
}

public fun <T, R> Iterator<T>.map(mapper: (T) -> R): Iterator<R> = object: Iterator<R> {
    override fun hasNext() = this@map.hasNext()

    override fun next() = mapper(this@map.next())
}

public fun <T, R: Any> Iterator<T>.mapNotNull(mapper: (T) -> R?): Iterator<R> = this.asSequence().mapNotNull(mapper).iterator()

public fun <T, R> TreeModel<T>.map(mapper: (T) -> R): TreeModel<R> = object: TreeModel<R> {
    override fun get(path: Path<Int>): R? = this@map[path]?.let(mapper)

    override fun isEmpty() = this@map.isEmpty()

    override fun children(parent: Path<Int>) = this@map.children(parent).map(mapper)

    override fun isLeaf(node: Path<Int>) = this@map.isLeaf(node)

    override fun child(of: Path<Int>, path: Int) = this@map.child(of, path)?.let(mapper)

    override fun numChildren(of: Path<Int>) = this@map.numChildren(of)

    override fun indexOfChild(parent: Path<Int>, child: R) = children(parent).asSequence().indexOf(child)
}

public fun <T: Any, R: Any> SelectionModel<T>.map(mapper: (T) -> R?, unmapper: (R) -> T?): SelectionModel<R> = object: SelectionModel<R> {
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
    override val changed: Pool<SetObserver<SelectionModel<R>, R>> = SetPool()

    init {
        this@map.changed += { set, removed, added ->
            // FIXME: Can this be optimized?
            (changed as SetPool).forEach {
                it(this, removed.mapNotNull(mapper).toSet(), added.mapNotNull(mapper).toSet())
            }
        }
    }
}

public open class TreeTable<T, M: TreeModel<T>>(model        : M,
                           protected val selectionModel: SelectionModel<Path<Int>>? = null,
                           private   val scrollCache   : Int                        = 10,
                                         block         : ColumnFactory<T>.() -> Unit): View(), TreeLike {

    override val rootVisible: Boolean get() = tree.rootVisible

    override fun visible(row : Int      ): Boolean = tree.visible(row )
    override fun visible(path: Path<Int>): Boolean = tree.visible(path)
    override fun isLeaf (path: Path<Int>): Boolean = model.isLeaf(path)

    public fun children(parent: Path<Int>): Iterator<T> = model.children(parent)

    public fun child       (of    : Path<Int>, path : Int): T?  = model.child       (of,     path )
    public fun numChildren (of    : Path<Int>            ): Int = model.numChildren (of           )
    public fun indexOfChild(parent: Path<Int>, child: T  ): Int = model.indexOfChild(parent, child)

    override fun expanded(path: Path<Int>): Boolean = tree.expanded(path)
    override fun collapse(path: Path<Int>): Unit    = tree.collapse(path)
    override fun expand  (path: Path<Int>): Unit    = tree.expand  (path)

    override fun expandAll  (): Unit = tree.expandAll()
    override fun collapseAll(): Unit = tree.collapseAll()

    override fun selectAll      (                      ): Unit       = tree.selectAll      (      )
    override fun selected       (item  : Path<Int>     ): Boolean    = tree.selected       (item  )
    override fun addSelection   (items : Set<Path<Int>>): Unit       = tree.addSelection   (items )
    override fun setSelection   (items : Set<Path<Int>>): Unit       = tree.setSelection   (items )
    override fun removeSelection(items : Set<Path<Int>>): Unit       = tree.removeSelection(items )
    override fun toggleSelection(items : Set<Path<Int>>): Unit       = tree.toggleSelection(items )
    override fun clearSelection (                      ): Unit       = tree.clearSelection (      )
    override fun next           (after : Path<Int>     ): Path<Int>? = tree.next           (after )
    override fun previous       (before: Path<Int>     ): Path<Int>? = tree.previous       (before)

    override val selection      : Set<Path<Int>> get() = tree.selection
    override val lastSelection  : Path<Int>?     get() = tree.lastSelection
    override val firstSelection : Path<Int>?     get() = tree.firstSelection
    override val selectionAnchor: Path<Int>?     get() = tree.selectionAnchor

    public val expanded : ExpansionObservers<T> by lazy { ExpansionObserversImpl(this) }
    public val collapsed: ExpansionObservers<T> by lazy { ExpansionObserversImpl(this) }

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

    private inner class TableLikeBehaviorWrapper(val delegate: TreeTableBehavior<T>?): TableLikeBehavior<TableLikeWrapper> {
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
            extractor     : Extractor<T, R>): InternalColumn<TableLikeWrapper, TableLikeBehaviorWrapper, R>(TableLikeWrapper(), TableLikeBehaviorWrapper(behavior), header, headerPosition, cellGenerator, cellPosition, preferredWidth, minWidth, maxWidth, numFixedColumns = 1) {

        override val view = Tree(
                model.map(extractor),
                object : ItemVisualizer<R, IndexedIem> {
                    override fun invoke(item: R, previous: View?, context: IndexedIem) = this@InternalTreeColumn.cellGenerator.invoke(item, previous, object : CellInfo<R> {
                        override val column = this@InternalTreeColumn
                        override val index get() = context.index
                        override val selected get() = context.selected
                    })
                },
                selectionModel,
                scrollCache = scrollCache
        ).apply {
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
                        override fun invoke(tree: Tree<R, *>, node: R, path: Path<Int>, index: Int, current: View?) = it.treeCellGenerator(this@TreeTable, this@InternalTreeColumn, node, path, index, object : ItemVisualizer<R, IndexedIem> {
                            override fun invoke(item: R, previous: View?, context: IndexedIem) = this@InternalTreeColumn.cellGenerator.invoke(item, previous, object : CellInfo<R> {
                                override val column = this@InternalTreeColumn
                                override val index = index
                                override val selected get() = context.selected
                            })
                        }, current)
                    }

                    override val positioner get() = object: TreeBehavior.RowPositioner<R>() {
                        override fun rowBounds(tree: Tree<R, *>, node: R, path: Path<Int>, index: Int, current: View?) = it.rowPositioner.rowBounds(this@TreeTable, path, model[path]!!, index).run { Rectangle(0.0, y, tree.width, height) }

                        override fun contentBounds(tree: Tree<R, *>, node: R, path: Path<Int>, index: Int, current: View?) = rowBounds(tree, node, path, index, current) // FIXME

                        override fun row(of: Tree<R, *>, atY: Double) = it.rowPositioner.rowFor(this@TreeTable, atY)

                        override fun height(of: Tree<R, *>, below: Path<Int>) = it.rowPositioner.height(this@TreeTable, below)
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

    private inner class InternalListColumn<R>(
            header         : View?,
            headerAlignment: (Constraints.() -> Unit)? = null,
            cellGenerator  : CellVisualizer<R>,
            cellAlignment  : (Constraints.() -> Unit)? = null,
            preferredWidth : Double? = null,
            minWidth       : Double  = 0.0,
            maxWidth       : Double? = null,
            extractor      : Extractor<T, R>): InternalColumn<TableLikeWrapper, TableLikeBehaviorWrapper, R>(TableLikeWrapper(), TableLikeBehaviorWrapper(behavior), header, headerAlignment, cellGenerator, cellAlignment, preferredWidth, minWidth, maxWidth, numFixedColumns = 1) {
        /**
         * Returns all rows below the given [[Path]]; even if path is collapsed/invisible
         */
        private fun allRowsBelow(path: Path<Int>): List<Path<Int>> = (0 until model.numChildren(path)).map { path + it }.flatMap { listOf(it) + if (expanded(it)) allRowsBelow(it) else emptyList() }

        private inner class FieldModel<A>(private val model: M, private val extractor: Extractor<T, A>): DynamicListModel<A> {
            init {
                // FIXME: Centralize to avoid calling allRowsBelow more than once per changed path
                expanded += { _: TreeTable<*,*>, paths: Set<Path<Int>> ->
                    val added = paths.flatMap { allRowsBelow(it) }

                    // FIXME: This is way too expensive for large trees
                    changed.forEach {
                        it(this, emptyMap(), added.associate { rowFromPath(it)!! to extractor(model[it]!!) }, emptyMap())
                    }
                }

                collapsed += { _: TreeTable<*,*>, paths: Set<Path<Int>> ->
                    val removed = paths.flatMap {
                        var index = rowFromPath(it)!!

                        allRowsBelow(it).map { index++ to it }
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

        override val view = DynamicList(FieldModel(model, extractor), object: ItemVisualizer<R, Any> {
            override fun invoke(item: R, previous: View?, context: Any) = object: View() {}
        }, selectionModel = selectionModel?.map({ rowFromPath(it) }, { pathFromRow(it) }), scrollCache = scrollCache, fitContent = false).apply {
            acceptsThemes = false
        }

        override fun behavior(behavior: TableLikeBehaviorWrapper?) {
            behavior?.delegate?.let {
                view.behavior = object: ListBehavior<R> {
                    override val generator get() = object: ListBehavior.RowGenerator<R> {
                        override fun invoke(list: io.nacular.doodle.controls.list.List<R, *>, row: R, index: Int, current: View?) = it.cellGenerator(this@TreeTable, this@InternalListColumn, row, pathFromRow(index)!!, index, object: ItemVisualizer<R, IndexedIem> {
                            override fun invoke(item: R, previous: View?, context: IndexedIem) = this@InternalListColumn.cellGenerator.invoke(item, previous, object : CellInfo<R> {
                                override val column = this@InternalListColumn
                                override val index = index
                                override val selected get() = context.selected
                            })
                        }, current)
                    }

                    override val positioner get() = object: ListBehavior.RowPositioner<R> {
                        override fun rowBounds(of: io.nacular.doodle.controls.list.List<R, *>, row: R, index: Int, view: View?) = it.rowPositioner.rowBounds(this@TreeTable, pathFromRow(index)!!, model[pathFromRow(index)!!]!!, index).run { Rectangle(0.0, y, of.width, height) }

                        override fun row(of: io.nacular.doodle.controls.list.List<R, *>, atY: Double) = it.rowPositioner.rowFor(this@TreeTable, atY)

                        override fun totalRowHeight(of: io.nacular.doodle.controls.list.List<R, *>) = it.rowPositioner.height(this@TreeTable, below = Path())
                    }

                    override fun render(view: io.nacular.doodle.controls.list.List<R, *>, canvas: Canvas) {
                        if (this@InternalListColumn != internalColumns.last()) {
                            it.renderColumnBody(this@TreeTable, this@InternalListColumn, canvas)
                        }
                    }
                }
            }
        }
    }

    public var model: M = model
        private set

    override val numRows: Int get() = tree.numRows

    public operator fun get(path: Path<Int>): T? = model[path]

    public operator fun get(row: Int): T? = pathFromRow(row)?.let { model[it] }

    override fun pathFromRow(index: Int): Path<Int>? = tree.pathFromRow(index)

    override fun rowFromPath(path: Path<Int>): Int? = tree.rowFromPath(path)

    public var columnSizePolicy: ColumnSizePolicy = ConstrainedSizePolicy()
        set(new) {
            field = new

            relayout()
        }

    public var behavior: TreeTableBehavior<T>? by behavior(
        beforeChange = { _, new ->
            new?.also { behavior ->
                this.block?.let {
                    factory.apply(it)

                    // Last, unusable column
                    internalColumns += LastColumn(TableLikeWrapper(), behavior.overflowColumnConfig?.body(this))

                    children += listOf(header, panel)

                    this.block = null
                }
            }
        },
        afterChange = { _, new ->
            new?.also { behavior ->
                (internalColumns as MutableList<InternalColumn<TableLikeWrapper, TableLikeBehaviorWrapper, *>>).forEach {
                    it.behavior(TableLikeBehaviorWrapper(behavior))
                }

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
    )

//    public var behavior: TreeTableBehavior<T>? = null
//        set(new) {
//            if (new == behavior) { return }
//
//            field?.let {
//                it.bodyDirty   = null
//                it.headerDirty = null
//                it.columnDirty = null
//
//                it.uninstall(this)
//            }
//
//            field = new
//
//            new?.also { behavior ->
//                block?.let {
//                    factory.apply(it)
//
//                    // Last, unusable column
//                    internalColumns += LastColumn(TableLikeWrapper(), behavior.overflowColumnConfig?.body(this))
//
//                    children += listOf(header, panel)
//
//                    block = null
//                }
//
//                behavior.bodyDirty   = bodyDirty
//                behavior.headerDirty = headerDirty
//                behavior.columnDirty = columnDirty
//
//                (internalColumns as MutableList<InternalColumn<TableLikeWrapper, TableLikeBehaviorWrapper, *>>).forEach {
//                    it.behavior(TableLikeBehaviorWrapper())
//                }
//
//                behavior.install(this)
//
//                header.children.batch {
//                    clear()
//
//                    headerItemsToColumns.clear()
//
//                    addAll(internalColumns.dropLast(1).map { column ->
//                        behavior.headerCellGenerator(this@TreeTable, column).also {
//                            headerItemsToColumns[it] = column
//                        }
//                    })
//                }
//
//                behavior.headerPositioner.invoke(this@TreeTable).apply {
//                    header.height = height
//                }
//
//                layout = constrain(header, panel) { header, panel ->
//                    behavior.headerPositioner.invoke(this@TreeTable).apply {
//                        header.top    = header.parent.top + y
//                        header.height = constant(height)
//                    }
//
//                    panel.top    = header.bottom
//                    panel.left   = panel.parent.left
//                    panel.right  = panel.parent.right
//                    panel.bottom = panel.parent.bottom
//                }
//            }
//        }

    public val columns: List<Column<*>> get() = internalColumns.dropLast(1)

    public val selectionChanged: Pool<SetObserver<TreeTable<T, M>, Path<Int>>> = SetPool()

    private val internalColumns = mutableListOf<InternalColumn<*, *, *>>()

    protected open val factory: ColumnFactory<T> = ColumnFactoryImpl()

    private var block: (ColumnFactory<T>.() -> Unit)? = block

    private val headerItemsToColumns = mutableMapOf<View, InternalColumn<*, *, *>>()

    private val header by lazy {
        TableHeader(internalColumns) { canvas ->
            behavior?.renderHeader(this@TreeTable, canvas)
        }
    }

    private val panel by lazy {
        val panel = TablePanel(internalColumns) { canvas ->
            behavior?.renderBody(this@TreeTable, canvas)
        }

        ScrollPanel(panel.apply {
            // FIXME: Use two scroll-panels instead since async scrolling makes this look bad
            boundsChanged += { _, old, new ->
                if (old.x != new.x) {
                    header.x = new.x
                }
            }
        })
    }

    @Suppress("PrivatePropertyName")
    protected open val selectionChanged_: SetObserver<SelectionModel<Path<Int>>, Path<Int>> = { set,removed,added ->
        (selectionChanged as SetPool).forEach {
            it(this, removed, added)
        }
    }

    init {
        selectionModel?.let { it.changed += selectionChanged_ }
    }

    internal val bodyDirty  : (         ) -> Unit = { panel.content?.rerender() }
    internal val headerDirty: (         ) -> Unit = { header.rerender        () }
    internal val columnDirty: (Column<*>) -> Unit = { (it as? InternalColumn<*, *, *>)?.view?.rerender() }

    override fun removedFromDisplay() {
        selectionModel?.let { it.changed -= selectionChanged_ }

        super.removedFromDisplay()
    }

    public override var insets: Insets
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
        (panel.content as? Container)?.relayout() // FIXME
    }

    internal fun rowsBelow(path: Path<Int>) = tree.rowsBelow(path)
}