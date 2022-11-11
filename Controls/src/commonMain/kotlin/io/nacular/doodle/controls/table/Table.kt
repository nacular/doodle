package io.nacular.doodle.controls.table

import io.nacular.doodle.controls.IndexedItem
import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.controls.ListModel
import io.nacular.doodle.controls.ListSelectionManager
import io.nacular.doodle.controls.Selectable
import io.nacular.doodle.controls.SelectionModel
import io.nacular.doodle.controls.SimpleListModel
import io.nacular.doodle.controls.itemVisualizer
import io.nacular.doodle.controls.list.ListBehavior
import io.nacular.doodle.controls.list.ListLike
import io.nacular.doodle.controls.list.itemGenerator
import io.nacular.doodle.controls.panels.ScrollPanel
import io.nacular.doodle.controls.table.MetaRowVisibility.Always
import io.nacular.doodle.controls.table.MetaRowVisibility.HasContents
import io.nacular.doodle.core.Container
import io.nacular.doodle.core.View
import io.nacular.doodle.core.behavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.constraints.Bounds
import io.nacular.doodle.layout.constraints.ConstraintDslContext
import io.nacular.doodle.utils.Completable
import io.nacular.doodle.utils.Extractor
import io.nacular.doodle.utils.Pool
import io.nacular.doodle.utils.SetObserver
import io.nacular.doodle.utils.SetPool
import io.nacular.doodle.utils.observable
import kotlin.math.max

/**
 * A visual component that renders an immutable list of items of type [T] using a [TableBehavior]. Items are obtained via
 * the [model] and selection is managed via the optional [selectionModel]. Large ("infinite") lists are supported
 * efficiently, since Table recycles the Views generated to render its items.
 *
 * Note that this class assumes the given [ListModel] is immutable and will not automatically respond
 * to changes in the model. See [DynamicTable] or [MutableTable] if this behavior is desirable.
 *
 * Table provides vertical scrolling internally, so it does not need to be embedded in a [ScrollPanel] or similar component,
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
public open class Table<T, M: ListModel<T>>(
        protected val model         : M,
        protected val selectionModel: SelectionModel<Int>? = null,
        private   val scrollCache   : Int                  = 0,
                      columns       : ColumnFactory<T>.() -> Unit): View(), ListLike, Selectable<Int> by ListSelectionManager(selectionModel, { model.size }) {

    private inner class ColumnFactoryImpl: ColumnFactory<T> {
        override fun <R> column(header: View?, extractor: Extractor<T, R>, cellVisualizer: CellVisualizer<T, R>, footer: View?, builder: ColumnBuilder.() -> Unit) = ColumnBuilderImpl().run {
            builder(this)

            InternalListColumn(header, headerAlignment, footer, footerAlignment, cellVisualizer, cellAlignment, width, minWidth, maxWidth, extractor).also { internalColumns += it }
        }
    }

    internal inner class TableLikeWrapper: TableLike {
        val delegate get() = this@Table

        override val width            get() = this@Table.width
        override val columns          get() = this@Table.columns
        override val internalColumns  get() = this@Table.internalColumns
        override val columnSizePolicy get() = this@Table.columnSizePolicy
        override val header           get() = this@Table.header as Container
        override val footer           get() = this@Table.footer as Container
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
        override fun <B: TableLikeBehavior<TableLikeWrapper>, T, R> columnMoveStart(table: TableLikeWrapper, internalColumn: InternalColumn<TableLikeWrapper, B, T, R>) {
            behavior?.columnMoveStart(table.delegate, internalColumn)
        }

        override fun <B: TableLikeBehavior<TableLikeWrapper>, T, R> columnMoveEnd(table: TableLikeWrapper, internalColumn: InternalColumn<TableLikeWrapper, B, T, R>) {
            behavior?.columnMoveEnd(table.delegate, internalColumn)
        }

        override fun <B: TableLikeBehavior<TableLikeWrapper>, T, R> columnMoved(table: TableLikeWrapper, internalColumn: InternalColumn<TableLikeWrapper, B, T, R>) {
            behavior?.columnMoved(table.delegate, internalColumn)
        }

        override fun moveColumn(table: TableLikeWrapper, function: (Float) -> Unit): Completable? = behavior?.moveColumn(table.delegate, function)
    }

    internal open inner class InternalListColumn<R>(
            header         : View?,
            headerAlignment: (ConstraintDslContext.(Bounds) -> Unit)? = null,
            footer         : View?,
            footerAlignment: (ConstraintDslContext.(Bounds) -> Unit)? = null,
            itemVisualizer : CellVisualizer<T, R>,
            cellAlignment  : (ConstraintDslContext.(Bounds) -> Unit)? = null,
            preferredWidth : Double?                                  = null,
            minWidth       : Double                                   = 0.0,
            maxWidth       : Double?                                  = null,
            extractor      : Extractor<T, R>): InternalColumn<TableLikeWrapper, TableLikeBehaviorWrapper, T, R>(TableLikeWrapper(), TableLikeBehaviorWrapper(behavior), header, headerAlignment, footer, footerAlignment, itemVisualizer, cellAlignment, preferredWidth, minWidth, maxWidth) {

        private inner class FieldModel<A>(private val model: M, private val extractor: Extractor<T, A>): ListModel<A> {
            override val size get() = model.size

            override fun get(index: Int) = model[index].map(extractor)

            override fun section(range: ClosedRange<Int>) = model.section(range).map(extractor)

            override fun contains(value: A) = value in model.map(extractor)

            override fun iterator() = model.map(extractor).iterator()
        }

        override val view: io.nacular.doodle.controls.list.List<R, *> = io.nacular.doodle.controls.list.List(FieldModel(model, extractor), object: ItemVisualizer<R, Any> {
            override fun invoke(item: R, previous: View?, context: Any) = object: View() {}
        }, selectionModel, scrollCache = scrollCache, fitContent = emptySet()).apply {
            acceptsThemes = false
        }

        override fun behavior(behavior: TableLikeBehaviorWrapper?) {
            behavior?.delegate?.let {
                view.behavior = object: ListBehavior<R> {
                    override val generator get() = itemGenerator { _: io.nacular.doodle.controls.list.List<R, *>, item: R, index: Int, current: View? ->
                        it.cellGenerator(this@Table, this@InternalListColumn, item, index, itemVisualizer { item: R, previous: View?, context: IndexedItem ->
                            this@InternalListColumn.cellGenerator(item, previous, object: CellInfo<T, R> {
                                override val item     get() = this@Table[this.index].getOrThrow()
                                override val index    get() = context.index
                                override val column   get() = this@InternalListColumn
                                override val selected get() = context.selected
                            })
                        }, current)
                    }

                    override val positioner get() = object: ListBehavior.ItemPositioner<R> {
                        override fun itemBounds (of: io.nacular.doodle.controls.list.List<R, *>, item: R, index: Int, view: View?) = it.rowPositioner.rowBounds  (this@Table, model[index].getOrNull()!!, index).run { Rectangle(0.0, y, of.width, height) }
                        override fun item       (of: io.nacular.doodle.controls.list.List<R, *>, at: Point                       ) = it.rowPositioner.row        (this@Table, at)
                        override fun minimumSize(of: io.nacular.doodle.controls.list.List<R, *>                                  ) = it.rowPositioner.minimumSize(this@Table)
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

    override val numItems: Int   get() = model.size
    public val isEmpty: Boolean get() = model.isEmpty

    public var columnSizePolicy: ColumnSizePolicy = ConstrainedSizePolicy; set(new) {
        field = new

        if (behavior != null) {
            doLayout()
        }
    }

    public var behavior: TableBehavior<T>? by behavior(
        beforeChange = { _, new ->
            new?.also { behavior ->
                this.block?.let {
                    factory.apply(it)

                    // Last, unusable column
                    internalColumns += LastColumn(TableLikeWrapper(), behavior.overflowColumnConfig?.body(this))

                    children += listOf(panel, footer, header)

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

                    addAll(usableColumns.map { column ->
                        if (column.header != null) { header.hasContent = true }

                        behavior.headerCellGenerator(this@Table, column).also {
                            headerItemsToColumns[it] = column
                        }
                    })
                }

                behavior.headerPositioner(this@Table).apply {
                    header.y      = insetTop
                    header.height = height
                }

                footer.children.batch {
                    clear()

                    footerItemsToColumns.clear()

                    addAll(usableColumns.map { column ->
                        if (column.footer != null) { footer.hasContent = true }

                        behavior.footerCellGenerator(this@Table, column).also {
                            footerItemsToColumns[it] = column
                        }
                    })
                }

                behavior.footerPositioner(this@Table).apply {
                    footer.y      = this@Table.height - insetTop
                    footer.height = height
                }

                layout = tableLayout(this@Table, header, panel, footer, behavior, { headerVisibility }, { headerSticky }, { footerVisibility }, { footerSticky })
            }
        }
    )

    public val columns: List<Column<*>> get() = internalColumns.dropLast(1)

    public val selectionChanged: Pool<SetObserver<Table<T, M>, Int>> = SetPool()

    public var headerVisibility: MetaRowVisibility by observable(Always     ) { _,_ -> doLayout() }
    public var headerSticky    : Boolean           by observable(true       ) { _,_ -> doLayout() }
    public var footerVisibility: MetaRowVisibility by observable(HasContents) { _,_ -> doLayout() }
    public var footerSticky    : Boolean           by observable(true       ) { _,_ -> doLayout() }

    public fun contains(value: T): Boolean = value in model

    internal val internalColumns = mutableListOf<InternalColumn<*,*,*,*>>()

    protected open val factory: ColumnFactory<T> = ColumnFactoryImpl()

    private var block: (ColumnFactory<T>.() -> Unit)? = columns

    private val headerItemsToColumns = mutableMapOf<View, InternalColumn<*,*,*,*>>()
    private val footerItemsToColumns = mutableMapOf<View, InternalColumn<*,*,*,*>>()

    private val header by lazy {
        TableMetaRow(internalColumns) { canvas ->
            behavior?.renderHeader(this@Table, canvas)
        }
    }

    private val footer by lazy {
        TableMetaRow(internalColumns) { canvas ->
            behavior?.renderFooter(this@Table, canvas)
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
                    footer.x = new.x
                }
            }
        }).apply {
            contentWidthConstraints  = { it eq max(content?.idealSize?.width  ?: it.readOnly, width ) - verticalScrollBarWidth }
            contentHeightConstraints = { it eq max(content?.idealSize?.height ?: it.readOnly, height)                          }

            scrollBarDimensionsChanged += {
                doLayout()
            }
        }
    }

    protected open val selectionChanged_: SetObserver<SelectionModel<Int>, Int> = { _,removed,added ->
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
        width       = columnSizePolicy.layout(max(0.0, width - panel.verticalScrollBarWidth), internalColumns, resizingCol?.let { it + 1 } ?: 0) + panel.verticalScrollBarWidth
        resizingCol = null

        super.doLayout()

        header.doLayout()
        (panel.content as? TablePanel)?.doLayout()
        footer.doLayout()

        resizingCol = null
    }

    public companion object {
        public operator fun <T> invoke(
                       values        : List<T>,
                       selectionModel: SelectionModel<Int>? = null,
                       scrollCache   : Int                  = 0,
                       block         : ColumnFactory<T>.() -> Unit): Table<T, ListModel<T>> = Table(SimpleListModel(values), selectionModel, scrollCache, block)
    }
}