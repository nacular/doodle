package io.nacular.doodle.controls.table

import io.nacular.doodle.controls.DynamicListModel
import io.nacular.doodle.controls.IndexedItem
import io.nacular.doodle.controls.ModelObserver
import io.nacular.doodle.controls.SelectionModel
import io.nacular.doodle.controls.itemVisualizer
import io.nacular.doodle.controls.list.DynamicList
import io.nacular.doodle.controls.list.ListBehavior
import io.nacular.doodle.controls.list.ListBehavior.ItemPositioner
import io.nacular.doodle.controls.list.itemGenerator
import io.nacular.doodle.controls.panels.ScrollPanel
import io.nacular.doodle.controls.table.MetaRowVisibility.Always
import io.nacular.doodle.controls.table.MetaRowVisibility.HasContents
import io.nacular.doodle.controls.theme.TreeBehavior
import io.nacular.doodle.controls.theme.rowGenerator
import io.nacular.doodle.controls.tree.Tree
import io.nacular.doodle.controls.tree.TreeLike
import io.nacular.doodle.controls.tree.TreeModel
import io.nacular.doodle.core.Layout.Companion.simpleLayout
import io.nacular.doodle.core.View
import io.nacular.doodle.core.behavior
import io.nacular.doodle.core.scrollToVertical
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.constraints.Bounds
import io.nacular.doodle.layout.constraints.ConstraintDslContext
import io.nacular.doodle.layout.constraints.Strength.Companion.Strong
import io.nacular.doodle.utils.Completable
import io.nacular.doodle.utils.Extractor
import io.nacular.doodle.utils.Path
import io.nacular.doodle.utils.SetObserver
import io.nacular.doodle.utils.SetObservers
import io.nacular.doodle.utils.SetPool
import io.nacular.doodle.utils.diff.Delete
import io.nacular.doodle.utils.diff.Differences
import io.nacular.doodle.utils.diff.Equal
import io.nacular.doodle.utils.diff.Insert
import io.nacular.doodle.utils.observable
import kotlin.Result.Companion.failure
import kotlin.math.max

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

    override fun next() = model[iterator.next()].getOrNull()!!
}

public fun <T, R> Iterator<T>.map(mapper: (T) -> R): Iterator<R> = object: Iterator<R> {
    override fun hasNext() = this@map.hasNext()

    override fun next() = mapper(this@map.next())
}

public fun <T, R: Any> Iterator<T>.mapNotNull(mapper: (T) -> R?): Iterator<R> = this.asSequence().mapNotNull(mapper).iterator()

public fun <T, R> TreeModel<T>.map(mapper: (T) -> R): TreeModel<R> = object: TreeModel<R> {
    override fun get(path: Path<Int>): Result<R> = this@map[path].map(mapper)

    override fun isEmpty() = this@map.isEmpty()

    override fun children(parent: Path<Int>) = this@map.children(parent).map(mapper)

    override fun isLeaf(node: Path<Int>) = this@map.isLeaf(node)

    override fun child(of: Path<Int>, path: Int) = this@map.child(of, path).map(mapper)

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
    override val changed: SetObservers<SelectionModel<R>, R> = SetPool()

    init {
        this@map.changed += { _, removed, added ->
            // FIXME: Can this be optimized?
            (changed as SetPool).forEach {
                it(this, removed.mapNotNull(mapper).toSet(), added.mapNotNull(mapper).toSet())
            }
        }
    }
}

/**
 * A visual component that renders an immutable list of items of type [T] using a [TreeTableBehavior]. Items are obtained via
 * the [model] and selection is managed via the optional [selectionModel]. Large ("infinite") lists are supported
 * efficiently, since Table recycles the Views generated to render its items.
 *
 * Note that this class assumes the given [TreeModel] is immutable and will not automatically respond
 * to changes in the model.
 *
 * TreeTable provides vertical scrolling internally, so it does not need to be embedded in a [ScrollPanel] or similar component,
 * unless horizontal scrolling is desired.
 *
 * @param model that holds the data for the Table
 * @param selectionModel that manages the Table's selection state
 * @param scrollCache determining how many "hidden" items are rendered above and below the Table's view-port. A value of 0 means
 * only visible items are rendered, but quick scrolling is more likely to show blank areas.
 * @param columns factory to define the set of columns for the Table
 *
 * @property model that holds the data for the Table
 * @property selectionModel that manages the Table's selection state
 */
public open class TreeTable<T, M: TreeModel<T>>(
                  model         : M,
    protected val selectionModel: SelectionModel<Path<Int>>? = null,
    private   val scrollCache   : Int                        = 0,
                  columns       : ColumnFactory<T>.() -> Unit): View(), TreeLike {

    override val rootVisible: Boolean get() = tree.rootVisible

    override fun visible(row : Int      ): Boolean = tree.visible(row )
    override fun visible(path: Path<Int>): Boolean = tree.visible(path)
    override fun isLeaf (path: Path<Int>): Boolean = model.isLeaf(path)

    public fun children(parent: Path<Int>): Iterator<T> = model.children(parent)

    public fun child       (of    : Path<Int>, path : Int): Result<T> = model.child       (of,     path )
    public fun numChildren (of    : Path<Int>            ): Int       = model.numChildren (of           )
    public fun indexOfChild(parent: Path<Int>, child: T  ): Int       = model.indexOfChild(parent, child)

    override fun expanded(path: Path<Int>): Boolean = tree.expanded(path)
    override fun collapse(path: Path<Int>): Unit    = tree.collapse(path)
    override fun expand  (path: Path<Int>): Unit    = tree.expand  (path)

    override fun expandAll  (): Unit = tree.expandAll  ()
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

    override val selection      : Set<Path<Int>> get() = tree.selection.toSet()
    override val lastSelection  : Path<Int>?     get() = tree.lastSelection
    override val firstSelection : Path<Int>?     get() = tree.firstSelection
    override val selectionAnchor: Path<Int>?     get() = tree.selectionAnchor
    override val firstSelectable: Path<Int>?     get() = tree.firstSelectable
    override val lastSelectable : Path<Int>?     get() = tree.lastSelectable

    public val expanded : ExpansionObservers<T> = ExpansionObserversImpl(this)
    public val collapsed: ExpansionObservers<T> = ExpansionObserversImpl(this)

    private lateinit var tree: Tree<*, TreeModel<*>>

    private class ExpansionObserversImpl<T>(private val source: TreeTable<T, *>): SetPool<ExpansionObserver<T>>() {
        operator fun invoke(paths: Set<Path<Int>>) = forEach { it(source, paths) }
    }

    private inner class ColumnFactoryImpl: ColumnFactory<T> {
        override fun <R> column(header: View?, extractor: Extractor<T, R>, cellVisualizer: CellVisualizer<T, R>, footer: View?, builder: ColumnBuilder.() -> Unit) = ColumnBuilderImpl().run {
            builder(this)

            if (!::tree.isInitialized) {
                InternalTreeColumn(header, headerAlignment, footer, footerAlignment, cellVisualizer, cellAlignment, width, minWidth, maxWidth, extractor).also {
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
                InternalListColumn(header, headerAlignment, footer, footerAlignment, cellVisualizer, cellAlignment, width, minWidth, maxWidth, extractor).also { internalColumns += it }
            }
        }
    }

    private inner class TableLikeWrapper: TableLike {
        val delegate get() = this@TreeTable

        override val width            get() = this@TreeTable.prospectiveBounds.width
        override val columns          get() = this@TreeTable.columns
        override val internalColumns  get() = this@TreeTable.internalColumns
        override val columnSizePolicy get() = this@TreeTable.columnSizePolicy
        override val header           get() = this@TreeTable.header
        override val footer           get() = this@TreeTable.footer
        override val panel            get() = this@TreeTable.panel

        override var resizingCol get() = this@TreeTable.resizingCol; set(new) {
            this@TreeTable.resizingCol = new
        }

        override fun columnSizeChanged() {
            this@TreeTable.columnSizeChanged()
        }
    }

    private fun columnSizeChanged() {
        header.relayout()
        (panel.content as? TablePanel)?.relayout() // FIXME
        footer.relayout()
        relayout()
    }

    private inner class TableLikeBehaviorWrapper(val delegate: TreeTableBehavior<T>?): TableLikeBehavior<TableLikeWrapper> {
        override fun <B: TableLikeBehavior<TableLikeWrapper>, T, R> columnMoveStart(table: TableLikeWrapper, internalColumn: InternalColumn<TableLikeWrapper, B, T, R>) {
            behavior?.columnMoveStart(table.delegate, internalColumn)
        }

        override fun <B: TableLikeBehavior<TableLikeWrapper>, T, R> columnMoveEnd(table: TableLikeWrapper, internalColumn: InternalColumn<TableLikeWrapper, B, T, R>) {
            behavior?.columnMoveEnd(table.delegate, internalColumn)
        }

        override fun <B: TableLikeBehavior<TableLikeWrapper>, T, R> columnMoved(table: TableLikeWrapper, internalColumn: InternalColumn<TableLikeWrapper, B, T, R>) {
            behavior?.columnMoved(table.delegate, internalColumn)
        }

        override fun moveColumn(table: TableLikeWrapper, distance: Double, function: (Float) -> Unit): Completable? = behavior?.moveColumn(table.delegate, distance, function)
    }

    private inner class InternalTreeColumn<R>(
            header        : View?,
            headerPosition: (ConstraintDslContext.(Bounds) -> Unit)?,
            footer        : View?,
            footerPosition: (ConstraintDslContext.(Bounds) -> Unit)?,
            cellGenerator : CellVisualizer<T, R>,
            cellPosition  : (ConstraintDslContext.(Bounds) -> Unit)?,
            preferredWidth: Double?        = null,
            minWidth      : Double         = 0.0,
            maxWidth      : Double?        = null,
            extractor     : Extractor<T, R>): InternalColumn<TableLikeWrapper, TableLikeBehaviorWrapper, T, R>(
        TableLikeWrapper(),
        TableLikeBehaviorWrapper(behavior),
        header,
        headerPosition,
        footer,
        footerPosition,
        cellGenerator,
        cellPosition,
        preferredWidth,
        minWidth,
        maxWidth,
        numFixedColumns = 1
    ) {

        override val view by lazy {
            Tree(
                model.map(extractor),
                itemVisualizer { item: R, previous: View?, context: IndexedItem ->
                    this@InternalTreeColumn.cellGenerator.invoke(item, previous, object: CellInfo<T, R> {
                        override val item     get() = this@TreeTable[index].getOrThrow()
                        override val index    get() = context.index
                        override val column   get() = this@InternalTreeColumn
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
        }

        override fun behavior(behavior: TableLikeBehaviorWrapper?) {
            behavior?.delegate?.let {
                view.behavior = object: TreeBehavior<R> {
                    override val generator get() = rowGenerator { _: Tree<R, *>, node: R, path: Path<Int>, index: Int, current: View? ->
                        it.treeCellGenerator(this@TreeTable, this@InternalTreeColumn, node, path, index, itemVisualizer { item: R, previous: View?, context: IndexedItem ->
                            this@InternalTreeColumn.cellGenerator.invoke(item, previous, object: CellInfo<T, R> {
                                override val item     get() = this@TreeTable[path].getOrThrow()
                                override val index    get() = context.index
                                override val column   get() = this@InternalTreeColumn
                                override val selected get() = context.selected
                            })
                        }, current)
                    }

                    override val positioner get() = object: TreeBehavior.RowPositioner<R>() {
                        override fun rowBounds(of: Tree<R, *>, node: R, path: Path<Int>, index: Int) = it.rowPositioner.rowBounds(this@TreeTable, path, model[path].getOrNull()!!, index).run { Rectangle(0.0, y, of.width, height) }

                        override fun contentBounds(of: Tree<R, *>, node: R, path: Path<Int>, index: Int) = rowBounds(of, node, path, index) // FIXME

                        override fun row(of: Tree<R, *>, at: Point) = it.rowPositioner.row(this@TreeTable, at)

                        override fun minimumSize(of: Tree<R, *>, below: Path<Int>) = it.rowPositioner.size(this@TreeTable, below)
                    }

                    override fun render(view: Tree<R, *>, canvas: Canvas) {
                        if (this@InternalTreeColumn != internalColumns.last()) {
                            it.renderColumnBody(this@TreeTable, this@InternalTreeColumn, canvas)
                        }
                    }
                }
            }
        }

        override val movable = false

        override fun moveBy(x: Double) {
            // NO-OP
        }

        override fun resetPosition() {
            // NO-OP
        }
    }

    private inner class InternalListColumn<R>(
            header         : View?,
            headerAlignment: (ConstraintDslContext.(Bounds) -> Unit)? = null,
            footer         : View?,
            footerAlignment: (ConstraintDslContext.(Bounds) -> Unit)? = null,
            cellGenerator  : CellVisualizer<T, R>,
            cellAlignment  : (ConstraintDslContext.(Bounds) -> Unit)? = null,
            preferredWidth : Double? = null,
            minWidth       : Double  = 0.0,
            maxWidth       : Double? = null,
            extractor      : Extractor<T, R>): InternalColumn<TableLikeWrapper, TableLikeBehaviorWrapper, T, R>(TableLikeWrapper(), TableLikeBehaviorWrapper(behavior), header, headerAlignment, footer, footerAlignment, cellGenerator, cellAlignment, preferredWidth, minWidth, maxWidth, numFixedColumns = 1) {
        /**
         * Returns all rows below the given [[Path]]; even if path is collapsed/invisible
         */
        private fun allRowsBelow(path: Path<Int>): List<Path<Int>> = (0 until model.numChildren(path)).map { path + it }.flatMap { listOf(it) + if (expanded(it)) allRowsBelow(it) else emptyList() }

        private inner class FieldModel<A>(private val model: M, private val extractor: Extractor<T, A>): DynamicListModel<A> {
            init {
                // FIXME: Centralize to avoid calling allRowsBelow more than once per changed path
                expanded += { _: TreeTable<*,*>, paths: Set<Path<Int>> ->
                    var index = 0

                    val changes = paths.map { rowFromPath(it)!! to it }.sortedBy {
                        it.first
                    }.flatMap { (row, path) ->
                        listOf(
                            Equal((index..row).map  { extractor(model[pathFromRow(it)!!].getOrNull()!!) }),
                            Insert(allRowsBelow(path).map { extractor(model[it].getOrNull()!!) }.also { index += it.size })
                        )
                    }

//                    // FIXME: This is way too expensive for large trees
                    changed.forEach { it(this, Differences(changes)) }
                }

                collapsed += { _: TreeTable<*,*>, paths: Set<Path<Int>> ->
                    var index = 0

                    val changes = paths.map { rowFromPath(it)!! to it }.sortedBy {
                        it.first
                    }.flatMap { (row, path) ->
                        listOf(
                            Equal((index..row).map  { extractor(model[pathFromRow(it)!!].getOrNull()!!) }),
                            Delete(allRowsBelow(path).map { extractor(model[it].getOrNull()!!) }.also { index += it.size })
                        )
                    }

//                    // FIXME: This is way too expensive for large trees
                    changed.forEach { it(this, Differences(changes)) }
                }
            }

            override val changed = SetPool<ModelObserver<A>>()

            override val size get() = numRows

            override fun get(index: Int) = pathFromRow(index)?.let { model[it].map(extractor) } ?: failure(IndexOutOfBoundsException())

            override fun section(range: ClosedRange<Int>) = iterator().asSequence().toList().subList(range.start, range.endInclusive + 1)

            override fun contains(value: A) = value in iterator().asSequence()

            // TODO: Re-use
            override fun iterator() = TreeModelIterator(model.map(extractor), TreePathIterator(this@TreeTable))
        }

        override val view by lazy {
            DynamicList(
                model          = FieldModel(model, extractor),
                itemVisualizer = itemVisualizer { _: R, _: View?, _: Any -> object : View() {} },
                selectionModel = selectionModel?.map({ rowFromPath(it) }, { pathFromRow(it) }),
                scrollCache    = scrollCache,
            ).apply {
                acceptsThemes = false
            }
        }

        override fun behavior(behavior: TableLikeBehaviorWrapper?) {
            behavior?.delegate?.let {
                view.behavior = object: ListBehavior<R> {
                    override val generator get() = itemGenerator { _: io.nacular.doodle.controls.list.List<R, *>, item: R, index: Int, current: View? ->
                        it.cellGenerator(this@TreeTable, this@InternalListColumn, item, pathFromRow(index)!!, index, itemVisualizer { cell: R, previous: View?, context: IndexedItem ->
                            this@InternalListColumn.cellGenerator(cell, previous, object: CellInfo<T, R> {
                                override val item     get() = this@TreeTable[this.index].getOrThrow()
                                override val index    get() = context.index
                                override val column   get() = this@InternalListColumn
                                override val selected get() = context.selected
                            })
                        }, current)
                    }

                    override val positioner get() = object: ItemPositioner<R> {
                        override fun itemBounds (of: io.nacular.doodle.controls.list.List<R, *>, item: R, index: Int, view: View?) = it.rowPositioner.rowBounds(this@TreeTable, pathFromRow(index)!!, model[pathFromRow(index)!!].getOrNull()!!, index).run { Rectangle(0.0, y, of.width, height) }
                        override fun item       (of: io.nacular.doodle.controls.list.List<R, *>, at: Point                       ) = it.rowPositioner.row      (this@TreeTable, at)
                        override fun minimumSize(of: io.nacular.doodle.controls.list.List<R, *>                                  ) = it.rowPositioner.size     (this@TreeTable, below = Path())
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

    public var model: M = model; private set

    override val numRows: Int get() = tree.numRows

    public operator fun get(path: Path<Int>): Result<T> = model[path]

    public operator fun get(row: Int): Result<T> = pathFromRow(row)?.let { model[it] } ?: failure(IndexOutOfBoundsException())

    override fun pathFromRow(index: Int): Path<Int>? = tree.pathFromRow(index)

    override fun rowFromPath(path: Path<Int>): Int? = tree.rowFromPath(path)

    public var columnSizePolicy: ColumnSizePolicy = ConstrainedSizePolicy; set(new) {
        field = new

        if (behavior != null) {
            doLayout()
        }
    }

    @Suppress("UNCHECKED_CAST")
    public var behavior: TreeTableBehavior<T>? by behavior(
        beforeChange = { _, new ->
            new?.also { behavior ->
                this.block?.let {
                    factory.apply(it)

                    // Last, unusable column
                    internalColumns += LastColumn(TableLikeWrapper(), behavior.overflowColumnConfig?.body(this))

                    children += panel

                    this.block = null
                }
            }
        },
        afterChange = { _, new ->
            new?.also { behavior ->
                (internalColumns as List<InternalColumn<TableLikeWrapper, TableLikeBehaviorWrapper, T, *>>).forEach {
                    it.behavior(TableLikeBehaviorWrapper(behavior))
                }

                val usableColumns = internalColumns.dropLast(1)

                header.children.batch {
                    clear()

                    headerItemsToColumns.clear()

                    if (usableColumns.any { it.header != null }) {
                        addAll(usableColumns.map { column ->
                            if (column.header != null) { header.hasContent = true }

                            behavior.headerCellGenerator(this@TreeTable, column).also {
                                headerItemsToColumns[it] = column
                            }
                        })
                    }
                }

                if (header.children.isNotEmpty()) {
                    children += header

                    behavior.headerPositioner.invoke(this@TreeTable).apply {
                        header.suggestY     (insetTop)
                        header.suggestHeight(height  )
                    }
                }

                footer.children.batch {
                    clear()

                    footerItemsToColumns.clear()

                    if (usableColumns.any { it.footer != null }) {
                        addAll(usableColumns.map { column ->
                            if (column.footer != null) { footer.hasContent = true }

                            behavior.footerCellGenerator(this@TreeTable, column).also {
                                footerItemsToColumns[it] = column
                            }
                        })
                    }
                }

                if (footer.children.isNotEmpty()) {
                    children += footer

                    behavior.footerPositioner(this@TreeTable).apply {
                        footer.suggestY     (height - insetTop)
                        footer.suggestHeight(height           )
                    }
                }

                val delegate = tableLayout(this@TreeTable, header, panel, footer, behavior, { headerVisibility }, { headerSticky }, { footerVisibility }, { footerSticky })

                layout = simpleLayout { items, min, current, max, _ ->
                    // explicitly set ideal size of table-panel so the scroll panel layout will update it
                    panel.content?.let {
                        it.preferredSize = fixed(Size(internalColumns.sumOf { it.width }, panel.content?.idealSize?.height ?: 0.0))
                        it.suggestSize(it.idealSize)
                    }

                    resizingCol = null

                    val headerHeight = metaRowHeight(header, headerVisibility, behavior.headerPositioner(this).height)
                    val footerHeight = metaRowHeight(footer, footerVisibility, behavior.footerPositioner(this).height)

                    delegate.layout(items, min, current, max).run {
                        val auditor = SizeAuditor { _,_,new,_,_ ->
                            val w = columnSizePolicy.layout(
                                max(0.0, new.width - panel.verticalScrollBarWidth),
                                internalColumns,
                                resizingCol?.let { it + 1 } ?: 0
                            ) + panel.verticalScrollBarWidth

                            Size(w, new.height)
                        }

                        sizeAuditor = auditor

                        Size(
                            auditor(this@TreeTable, Size.Empty, current, min, max).width,
                            current.height
                        )
                    }
                }
            }
        }
    )

    public val columns: List<Column<*>> get() = internalColumns.dropLast(1)

    public val selectionChanged: SetObservers<TreeTable<T, M>, Path<Int>> = SetPool()

    public var headerVisibility: MetaRowVisibility by observable(Always     ) { _,_ -> doLayout() }
    public var headerSticky    : Boolean           by observable(true       ) { _,_ -> doLayout() }
    public var footerVisibility: MetaRowVisibility by observable(HasContents) { _,_ -> doLayout() }
    public var footerSticky    : Boolean           by observable(true       ) { _,_ -> doLayout() }

    private val internalColumns = mutableListOf<InternalColumn<*,*,*,*>>()

    protected open val factory: ColumnFactory<T> = ColumnFactoryImpl()

    private var block: (ColumnFactory<T>.() -> Unit)? = columns

    private val headerItemsToColumns = mutableMapOf<View, InternalColumn<*,*,*,*>>()
    private val footerItemsToColumns = mutableMapOf<View, InternalColumn<*,*,*,*>>()

    private val header by lazy {
        TableMetaRow(internalColumns) { canvas ->
            behavior?.renderHeader(this@TreeTable, canvas)
        }
    }

    private val footer by lazy {
        TableMetaRow(internalColumns) { canvas ->
            behavior?.renderFooter(this@TreeTable, canvas)
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
                    header.scrollOffset = new.x
                    footer.scrollOffset = new.x
                }
            }
        }).apply {
            contentWidthConstraints = {
                it greaterEq panel.idealSize.width
                it eq        parent.width - verticalScrollBarWidth strength Strong
            }
            contentHeightConstraints = {
                it greaterEq panel.idealSize.height
                it eq        parent.height - horizontalScrollBarHeight strength Strong
            }

            scrollBarDimensionsChanged += {
                columnSizeChanged()
            }
        }
    }

    @Suppress("PropertyName")
    protected open val selectionChanged_: SetObserver<SelectionModel<Path<Int>>, Path<Int>> = { _,removed,added ->
        (selectionChanged as SetPool).forEach {
            it(this, removed, added)
        }
    }

    internal val bodyDirty  : (         ) -> Unit = { panel.content?.rerender() }
    internal val headerDirty: (         ) -> Unit = { header.rerender        () }
    internal val footerDirty: (         ) -> Unit = { footer.rerender        () }
    internal val columnDirty: (Column<*>) -> Unit = { (it as? InternalColumn<*,*,*,*>)?.view?.rerender() }

    init {
        parentChange += { _,_,new ->
            monitorsDisplayRect = when (new) {
                is ScrollPanel -> true
                else           -> false
            }
        }
    }

    override fun handleDisplayRectEvent(old: Rectangle, new: Rectangle) {
        if ((old.y != new.y || old.height != new.height) && header.height > 0.0 || footer.height > 0.0) {
            relayout()
        }
    }

    override fun addedToDisplay() {
        selectionModel?.let { it.changed += selectionChanged_ }

        super.addedToDisplay()
    }

    override fun removedFromDisplay() {
        selectionModel?.let { it.changed -= selectionChanged_ }

        super.removedFromDisplay()
    }

    /**
     * Scrolls [item] into view if the Table is within a [ScrollPanel].
     */
    public fun scrollTo(item: Path<Int>) {
        this[item].onSuccess {
            behavior?.rowPositioner?.rowBounds(this, item, it, rowFromPath(item)!!)?.let { bounds ->
                scrollToVertical(bounds.y .. bounds.bottom + panel.y)
                panel.scrollVerticallyToVisible(bounds.y .. bounds.bottom)
            }
        }
    }

    /**
     * Scrolls the last selected item into view if the Table is within a [ScrollPanel].
     */
    public fun scrollToSelection() {
        lastSelection?.let { scrollTo(it) }
    }

    public override var insets: Insets; get() = super.insets; set(new) { super.insets = new }

    private var resizingCol: Int? = null

    internal fun rowsBelow(path: Path<Int>) = tree.rowsBelow(path)
}