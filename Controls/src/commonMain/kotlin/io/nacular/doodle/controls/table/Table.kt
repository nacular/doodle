package io.nacular.doodle.controls.table

import io.nacular.doodle.controls.IndexedItem
import io.nacular.doodle.controls.ListModel
import io.nacular.doodle.controls.ListSelectionManager
import io.nacular.doodle.controls.Selectable
import io.nacular.doodle.controls.SelectionModel
import io.nacular.doodle.controls.SimpleListModel
import io.nacular.doodle.controls.itemVisualizer
import io.nacular.doodle.controls.list.ListBehavior
import io.nacular.doodle.controls.list.ListBehavior.ItemPositioner
import io.nacular.doodle.controls.list.ListLike
import io.nacular.doodle.controls.list.itemGenerator
import io.nacular.doodle.controls.panels.ScrollPanel
import io.nacular.doodle.controls.table.MetaRowVisibility.Always
import io.nacular.doodle.controls.table.MetaRowVisibility.HasContents
import io.nacular.doodle.core.Container
import io.nacular.doodle.core.Layout.Companion.simpleLayout
import io.nacular.doodle.core.View
import io.nacular.doodle.core.behavior
import io.nacular.doodle.core.scrollToVertical
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.event.PointerListener
import io.nacular.doodle.event.PointerMotionListener
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.constraints.Bounds
import io.nacular.doodle.layout.constraints.ConstraintDslContext
import io.nacular.doodle.layout.constraints.Strength.Companion.Strong
import io.nacular.doodle.utils.Completable
import io.nacular.doodle.utils.Extractor
import io.nacular.doodle.utils.Pool
import io.nacular.doodle.utils.SetObserver
import io.nacular.doodle.utils.SetObservers
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

        override val width            get() = this@Table.prospectiveBounds.width
        override val columns          get() = this@Table.columns
        override val internalColumns  get() = this@Table.internalColumns
        override val columnSizePolicy get() = this@Table.columnSizePolicy
        override val header           get() = this@Table.header as Container
        override val footer           get() = this@Table.footer as Container
        override val panel            get() = this@Table.panel

        override var resizingCol get() = this@Table.resizingCol; set(new) {
            this@Table.resizingCol = new
        }

        override fun columnSizeChanged() {
            this@Table.columnSizeChanged()
        }
    }

    private fun columnSizeChanged() {
        header.relayout()
        (panel.content as? TablePanel)?.relayout() // FIXME
        footer.relayout()
        relayout()
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

        override fun moveColumn(table: TableLikeWrapper, distance: Double, function: (Float) -> Unit): Completable? = behavior?.moveColumn(table.delegate, distance, function)
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
        extractor      : Extractor<T, R>
    ): InternalColumn<TableLikeWrapper, TableLikeBehaviorWrapper, T, R>(
        TableLikeWrapper(),
        TableLikeBehaviorWrapper(behavior),
        header,
        headerAlignment,
        footer,
        footerAlignment,
        itemVisualizer,
        cellAlignment,
        preferredWidth,
        minWidth,
        maxWidth
    ) {

        private inner class FieldModel<A>(private val model: M, private val extractor: Extractor<T, A>): ListModel<A> {
            override val size get() = model.size

            override fun get(index: Int) = model[index].map(extractor)

            override fun section(range: ClosedRange<Int>) = model.section(range).map(extractor)

            override fun contains(value: A) = value in model.map(extractor)

            override fun iterator() = model.map(extractor).iterator()
        }

        override val view: io.nacular.doodle.controls.list.List<R, *> by lazy {
            io.nacular.doodle.controls.list.List(
                FieldModel(model, extractor),
                itemVisualizer { _,_,_ -> object : View() {} },
                selectionModel,
                scrollCache = scrollCache,
            ).apply {
                acceptsThemes = false
            }
        }

        override fun behavior(behavior: TableLikeBehaviorWrapper?) {
            behavior?.delegate?.let {
                view.behavior = object: ListBehavior<R> {
                    override val generator get() = itemGenerator { _: io.nacular.doodle.controls.list.List<R, *>, item: R, index: Int, current: View? ->
                        it.cellGenerator(this@Table, this@InternalListColumn, item, index, itemVisualizer { cell: R, previous: View?, context: IndexedItem ->
                            this@InternalListColumn.cellGenerator(cell, previous, object: CellInfo<T, R> {
                                override val item     get() = this@Table[this.index].getOrThrow()
                                override val index    get() = context.index
                                override val column   get() = this@InternalListColumn
                                override val selected get() = context.selected
                            })
                        }, current)
                    }

                    override val positioner get() = object: ItemPositioner<R> {
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

    override val numItems: Int     get() = model.size
    public   val isEmpty : Boolean get() = model.isEmpty

    public var columnSizePolicy: ColumnSizePolicy = ConstrainedSizePolicy; set(new) {
        field = new

        if (behavior != null) {
            doLayout()
        }
    }

    @Suppress("UNCHECKED_CAST")
    public var behavior: TableBehavior<T>? by behavior(
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

                            behavior.headerCellGenerator(this@Table, column).also {
                                headerItemsToColumns[it] = column
                            }
                        })
                    }
                }

                if (header.children.isNotEmpty()) {
                    children += header

                    behavior.headerPositioner(this@Table).apply {
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

                            behavior.footerCellGenerator(this@Table, column).also {
                                footerItemsToColumns[it] = column
                            }
                        })
                    }
                }

                if (footer.children.isNotEmpty()) {
                    children += footer

                    behavior.footerPositioner(this@Table).apply {
                        footer.suggestY     (height - insetTop)
                        footer.suggestHeight(height           )
                    }
                }

                val delegate = tableLayout(this@Table, header, panel, footer, behavior, { headerVisibility }, { headerSticky }, { footerVisibility }, { footerSticky })

                layout = simpleLayout { items, min, current, max, _ ->
                    // explicitly set ideal size of table-panel so the scroll panel layout will update it
                    panel.content?.let {
                        it.preferredSize = fixed(Size(internalColumns.sumOf { it.width }, panel.content?.idealSize?.height ?: 0.0))
                    }

                    resizingCol = null

                    val headerHeight = metaRowHeight(header, headerVisibility, behavior.headerPositioner(this).height)
                    val footerHeight = metaRowHeight(footer, footerVisibility, behavior.footerPositioner(this).height)

                    delegate.layout(items, min, current, max).run {
                        val auditor = SizeAuditor { _, _, new, _, _ ->
                            val w = columnSizePolicy.layout(
                                max(0.0, new.width - panel.verticalScrollBarWidth),
                                internalColumns,
                                resizingCol?.let { it + 1 } ?: 0
                            ) + panel.verticalScrollBarWidth

                            Size(w, new.height)
                        }

                        sizeAuditor = auditor

                        Size(
                            auditor(this@Table, Size.Empty, current, min, max).width,
                            behavior.rowPositioner.minimumSize(this@Table).height + headerHeight + footerHeight
                        )
                    }
                }
            }
        }
    )

    public val columns: List<Column<*>> get() = internalColumns.dropLast(1)

    public val selectionChanged: SetObservers<Table<T, M>, Int> = SetPool()

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
    protected open val selectionChanged_: SetObserver<SelectionModel<Int>, Int> = { _,removed,added ->
        (selectionChanged as SetPool).forEach {
            it(this, removed, added)
        }
    }

    internal val bodyDirty  : (         ) -> Unit = { panel.content?.rerender() }
    internal val headerDirty: (         ) -> Unit = { header.rerender        () }
    internal val footerDirty: (         ) -> Unit = { footer.rerender        () }
    internal val columnDirty: (Column<*>) -> Unit = { (it as? InternalColumn<*,*,*,*>)?.view?.rerender() }

    internal val bodyPointerFilter        : Pool<PointerListener>       get() = (panel.content ?: panel).pointerFilter
    internal val headerPointerFilter      : Pool<PointerListener>       get() = header.pointerFilter
    internal val footerPointerFilter      : Pool<PointerListener>       get() = footer.pointerFilter
    internal val bodyPointerMotionFilter  : Pool<PointerMotionListener> get() = (panel.content ?: panel).pointerMotionFilter
    internal val headerPointerMotionFilter: Pool<PointerMotionListener> get() = header.pointerMotionFilter
    internal val footerPointerMotionFilter: Pool<PointerMotionListener> get() = footer.pointerMotionFilter

    init {
        parentChanged += { _, _, new ->
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

    /**
     * Scrolls [item] into view if the Table is within a [ScrollPanel].
     */
    public fun scrollTo(item: Int) {
        this[item].onSuccess {
            behavior?.rowPositioner?.rowBounds(this, it, item)?.let { bounds ->
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

    override fun addedToDisplay() {
        selectionModel?.let { it.changed += selectionChanged_ }

        super.addedToDisplay()
    }

    override fun removedFromDisplay() {
        selectionModel?.let { it.changed -= selectionChanged_ }

        super.removedFromDisplay()
    }

    public override var insets: Insets; get() = super.insets; set(new) { super.insets = new }

    private var resizingCol: Int? = null

    public companion object {
        public operator fun <T> invoke(
                       values        : List<T>,
                       selectionModel: SelectionModel<Int>? = null,
                       scrollCache   : Int                  = 0,
                       block         : ColumnFactory<T>.() -> Unit): Table<T, ListModel<T>> = Table(SimpleListModel(values), selectionModel, scrollCache, block)
    }
}