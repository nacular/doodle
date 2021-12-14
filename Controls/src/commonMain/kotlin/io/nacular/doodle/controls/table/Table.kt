package io.nacular.doodle.controls.table

import io.nacular.doodle.controls.IndexedItem
import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.controls.ListModel
import io.nacular.doodle.controls.ListSelectionManager
import io.nacular.doodle.controls.Selectable
import io.nacular.doodle.controls.SelectionModel
import io.nacular.doodle.controls.SimpleListModel
import io.nacular.doodle.controls.list.ListBehavior
import io.nacular.doodle.controls.list.ListLike
import io.nacular.doodle.controls.panels.ScrollPanel
import io.nacular.doodle.core.Container
import io.nacular.doodle.core.View
import io.nacular.doodle.core.behavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.layout.Constraints
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.constant
import io.nacular.doodle.layout.constrain
import io.nacular.doodle.layout.max
import io.nacular.doodle.utils.Completable
import io.nacular.doodle.utils.Pool
import io.nacular.doodle.utils.SetObserver
import io.nacular.doodle.utils.SetPool

public open class Table<T, M: ListModel<T>>(
        protected val model         : M,
        protected val selectionModel: SelectionModel<Int>? = null,
        private   val scrollCache   : Int                  = 10,
                      block         : ColumnFactory<T>.() -> Unit): View(), ListLike, Selectable<Int> by ListSelectionManager(selectionModel, { model.size }) {

    private inner class ColumnFactoryImpl: ColumnFactory<T> {
        override fun <R> column(header: View?, extractor: Extractor<T, R>, cellVisualizer: CellVisualizer<R>, builder: ColumnBuilder.() -> Unit) = ColumnBuilderImpl().run {
            builder(this)

            InternalListColumn(header, headerAlignment, cellVisualizer, cellAlignment, width, minWidth, maxWidth, extractor).also { internalColumns += it }
        }
    }

    internal inner class TableLikeWrapper: TableLike {
        val delegate get() = this@Table

        override val width            get() = this@Table.width
        override val columns          get() = this@Table.columns
        override val internalColumns  get() = this@Table.internalColumns
        override val columnSizePolicy get() = this@Table.columnSizePolicy
        override val header           get() = this@Table.header as Container
        override val panel            get() = this@Table.panel

        override var resizingCol get() = this@Table.resizingCol
            set(new) {
                this@Table.resizingCol = new
            }

        override fun relayout() {
            this@Table.relayout()
        }
    }

    internal inner class TableLikeBehaviorWrapper(val delegate: TableBehavior<T>?): TableLikeBehavior<TableLikeWrapper> {
        override fun <B: TableLikeBehavior<TableLikeWrapper>, R> columnMoveStart(table: TableLikeWrapper, internalColumn: InternalColumn<TableLikeWrapper, B, R>) {
            behavior?.columnMoveStart(table.delegate, internalColumn)
        }

        override fun <B: TableLikeBehavior<TableLikeWrapper>, R> columnMoveEnd(table: TableLikeWrapper, internalColumn: InternalColumn<TableLikeWrapper, B, R>) {
            behavior?.columnMoveEnd(table.delegate, internalColumn)
        }

        override fun <B: TableLikeBehavior<TableLikeWrapper>, R> columnMoved(table: TableLikeWrapper, internalColumn: InternalColumn<TableLikeWrapper, B, R>) {
            behavior?.columnMoved(table.delegate, internalColumn)
        }

        override fun moveColumn(table: TableLikeWrapper, function: (Float) -> Unit): Completable? = behavior?.moveColumn(table.delegate, function)
    }

    internal open inner class InternalListColumn<R>(
            header         : View?,
            headerAlignment: (Constraints.() -> Unit)? = null,
            itemVisualizer : CellVisualizer<R>,
            cellAlignment  : (Constraints.() -> Unit)? = null,
            preferredWidth : Double?                   = null,
            minWidth       : Double                    = 0.0,
            maxWidth       : Double?                   = null,
            extractor      : Extractor<T, R>): InternalColumn<TableLikeWrapper, TableLikeBehaviorWrapper, R>(TableLikeWrapper(), TableLikeBehaviorWrapper(behavior), header, headerAlignment, itemVisualizer, cellAlignment, preferredWidth, minWidth, maxWidth) {

        private inner class FieldModel<A>(private val model: M, private val extractor: Extractor<T, A>): ListModel<A> {
            override val size get() = model.size

            override fun get(index: Int) = model[index].map(extractor)

            override fun section(range: ClosedRange<Int>) = model.section(range).map(extractor)

            override fun contains(value: A) = value in model.map(extractor)

            override fun iterator() = model.map(extractor).iterator()
        }

        override val view: io.nacular.doodle.controls.list.List<R, *> = io.nacular.doodle.controls.list.List(FieldModel(model, extractor), object: ItemVisualizer<R, Any> {
            override fun invoke(item: R, previous: View?, context: Any) = object: View() {}
        }, selectionModel, scrollCache = scrollCache, fitContent = false).apply {
            acceptsThemes = false
        }

        override fun behavior(behavior: TableLikeBehaviorWrapper?) {
            behavior?.delegate?.let {
                view.behavior = object: ListBehavior<R> {
                    override val generator get() = object: ListBehavior.RowGenerator<R> {
                        override fun invoke(list: io.nacular.doodle.controls.list.List<R, *>, row: R, index: Int, current: View?) = it.cellGenerator(this@Table, this@InternalListColumn, row, index, object: ItemVisualizer<R, IndexedItem> {
                            override fun invoke(item: R, previous: View?, context: IndexedItem) = this@InternalListColumn.cellGenerator(item, previous, object: CellInfo<R> {
                                override val column         = this@InternalListColumn
                                override val index          = index
                                override val selected get() = context.selected
                            })
                        }, current)
                    }

                    override val positioner get() = object: ListBehavior.RowPositioner<R> {
                        override fun rowBounds(of: io.nacular.doodle.controls.list.List<R, *>, row: R, index: Int, view: View?) = it.rowPositioner.rowBounds(this@Table, model[index].getOrNull()!!, index).run { Rectangle(0.0, y, of.width, height) }

                        override fun row(of: io.nacular.doodle.controls.list.List<R, *>, atY: Double) = it.rowPositioner.row(this@Table, atY)

                        override fun totalRowHeight(of: io.nacular.doodle.controls.list.List<R, *>) = it.rowPositioner.totalRowHeight(this@Table)
                    }

                    override fun render(view: io.nacular.doodle.controls.list.List<R, *>, canvas: Canvas) {
                        if (this@InternalListColumn != internalColumns.last()) {
                            it.renderColumnBody(this@Table, this@InternalListColumn, canvas)
                        }
                    }
                }
            }
        }
    }

    override val numRows: Int   get() = model.size
    public val isEmpty: Boolean get() = model.isEmpty

    public var columnSizePolicy: ColumnSizePolicy = ConstrainedSizePolicy()
        set(new) {
            field = new

            doLayout()
        }

    public var behavior: TableBehavior<T>? by behavior(
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
                        behavior.headerCellGenerator(this@Table, column).also {
                            headerItemsToColumns[it] = column
                        }
                    })
                }

                behavior.headerPositioner.invoke(this@Table).apply {
                    header.height = height
                }

                layout = constrain(header, panel) { header, panel ->
                    behavior.headerPositioner.invoke(this@Table).apply {
                        header.top = header.parent.top + y
                        header.height = constant(height)
                    }

                    panel.top = header.bottom
                    panel.left = panel.parent.left
                    panel.right = panel.parent.right
                    panel.bottom = panel.parent.bottom
                }
            }
        }
    )

    public val columns: List<Column<*>> get() = internalColumns.dropLast(1)

    public val selectionChanged: Pool<SetObserver<Table<T, M>, Int>> = SetPool()

    public fun contains(value: T): Boolean = value in model

    internal val internalColumns = mutableListOf<InternalColumn<*, *, *>>()

    protected open val factory: ColumnFactory<T> = ColumnFactoryImpl()

    private var block: (ColumnFactory<T>.() -> Unit)? = block

    private val headerItemsToColumns = mutableMapOf<View, InternalColumn<*,*,*>>()

    private val header by lazy {
        TableHeader(internalColumns) { canvas ->
            behavior?.renderHeader(this@Table, canvas)
        }
    }

    private val panel by lazy {
        val panel = TablePanel(internalColumns) { canvas ->
            behavior?.renderBody(this@Table, canvas)
        }

        ScrollPanel(panel.apply {
            // FIXME: Use two scroll-panels instead since async scrolling makes this look bad
            boundsChanged += { _, old, new ->
                if (old.x != new.x) {
                    header.x = new.x
                }
            }
        }).apply {
            contentWidthConstraints  = { max(idealWidth  or width,  parent.width ) }
            contentHeightConstraints = { max(idealHeight or height, parent.height) }
        }
    }

    @Suppress("PrivatePropertyName")
    protected open val selectionChanged_: SetObserver<SelectionModel<Int>, Int> = { _,removed,added ->
        (selectionChanged as SetPool).forEach {
            it(this, removed, added)
        }
    }

    internal val bodyDirty  : (         ) -> Unit = { panel.content?.rerender() }
    internal val headerDirty: (         ) -> Unit = { header.rerender        () }
    internal val columnDirty: (Column<*>) -> Unit = { (it as? InternalColumn<*,*,*>)?.view?.rerender() }

    public operator fun get(index: Int): Result<T> = model[index]

    override fun addedToDisplay() {
        selectionModel?.let { it.changed += selectionChanged_ }

        super.addedToDisplay()
    }

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
        width       = columnSizePolicy.layout(width, internalColumns, resizingCol?.let { it + 1 } ?: 0)
        resizingCol = null

        super.doLayout()

        header.doLayout()
        (panel.content as? TablePanel)?.doLayout()

        resizingCol = null
    }

    public companion object {
        public operator fun <T> invoke(
                       values        : List<T>,
                       selectionModel: SelectionModel<Int>? = null,
                       scrollCache   : Int                  = 10,
                       block         : ColumnFactory<T>.() -> Unit): Table<T, ListModel<T>> = Table(SimpleListModel(values), selectionModel, scrollCache, block)
    }
}