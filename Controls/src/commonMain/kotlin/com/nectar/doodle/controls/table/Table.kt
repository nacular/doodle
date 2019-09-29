package com.nectar.doodle.controls.table

import com.nectar.doodle.controls.ItemVisualizer
import com.nectar.doodle.controls.ListModel
import com.nectar.doodle.controls.ListSelectionManager
import com.nectar.doodle.controls.Selectable
import com.nectar.doodle.controls.SelectionModel
import com.nectar.doodle.controls.SimpleListModel
import com.nectar.doodle.controls.list.ListBehavior
import com.nectar.doodle.controls.list.ListLike
import com.nectar.doodle.controls.panels.ScrollPanel
import com.nectar.doodle.core.Box
import com.nectar.doodle.core.Layout
import com.nectar.doodle.core.Positionable
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.layout.Constraints
import com.nectar.doodle.layout.constant
import com.nectar.doodle.layout.constrain
import com.nectar.doodle.utils.Completable
import com.nectar.doodle.utils.Pool
import com.nectar.doodle.utils.SetObserver
import com.nectar.doodle.utils.SetPool
import kotlin.math.max

open class Table<T, M: ListModel<T>>(
        protected val model         : M,
        protected val selectionModel: SelectionModel<Int>? = null,
                      block         : ColumnFactory<T>.() -> Unit): View(), ListLike, Selectable<Int> by ListSelectionManager(selectionModel, { model.size }) {

    private inner class ColumnFactoryImpl: ColumnFactory<T> {
        override fun <R> column(header: View?, extractor: T.() -> R, cellGenerator: ItemVisualizer<R>, builder: ColumnBuilder.() -> Unit): Column<R> = ColumnBuilderImpl().run {
            builder(this)

            InternalListColumn(header, headerAlignment, cellGenerator, cellAlignment, width, minWidth, maxWidth, extractor).also { internalColumns += it }
        }
    }

    internal inner class TableLikeWrapper: TableLike {
        val delegate get() = this@Table

        override val width            get() = this@Table.width
        override val columns          get() = this@Table.columns
        override val internalColumns  get() = this@Table.internalColumns
        override val columnSizePolicy get() = this@Table.columnSizePolicy
        override val header           get() = this@Table.header
        override val panel            get() = this@Table.panel

        override var resizingCol get() = this@Table.resizingCol
            set(new) {
                this@Table.resizingCol = new
            }

        override fun doLayout() {
            this@Table.doLayout()
        }
    }

    internal inner class TableLikeBehaviorWrapper: TableLikeBehavior<TableLikeWrapper> {
        val delegate get() = this@Table.behavior

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

    internal inner class InternalListColumn<R>(
            header         : View?,
            headerAlignment: (Constraints.() -> Unit)? = null,
            cellGenerator  : ItemVisualizer<R>,
            cellAlignment  : (Constraints.() -> Unit)? = null,
            preferredWidth : Double? = null,
            minWidth       : Double  = 0.0,
            maxWidth       : Double? = null,
            extractor      : T.() -> R): InternalColumn<TableLikeWrapper, TableLikeBehaviorWrapper, R>(TableLikeWrapper(), TableLikeBehaviorWrapper(), header, headerAlignment, cellGenerator, cellAlignment, preferredWidth, minWidth, maxWidth) {

        private inner class FieldModel<A>(private val model: M, private val extractor: T.() -> A): ListModel<A> {
            override val size get() = model.size

            override fun get(index: Int) = model[index]?.let(extractor)

            override fun section(range: ClosedRange<Int>) = model.section(range).map(extractor)

            override fun contains(value: A) = value in model.map(extractor)

            override fun iterator() = model.map(extractor).iterator()
        }

        override val view: com.nectar.doodle.controls.list.List<R, *> = com.nectar.doodle.controls.list.List(FieldModel(model, extractor), cellGenerator, selectionModel).apply {
            acceptsThemes = false
        }

        override fun behavior(behavior: TableLikeBehaviorWrapper?) {
            behavior?.delegate?.let {
                view.behavior = object: ListBehavior<R> {
                    override val generator get() = object: ListBehavior.RowGenerator<R> {
                        override fun invoke(list: com.nectar.doodle.controls.list.List<R, *>, row: R, index: Int, current: View?) = it.cellGenerator.invoke(this@Table, this@InternalListColumn, row, index, cellGenerator, current)
                    }

                    override val positioner get() = object: ListBehavior.RowPositioner<R> {
                        override fun invoke(list: com.nectar.doodle.controls.list.List<R, *>, row: R, index: Int) = it.rowPositioner.invoke(this@Table, model[index]!!, index).run { Rectangle(0.0, y, list.width, height) }

                        override fun rowFor(list: com.nectar.doodle.controls.list.List<R, *>, y: Double) = it.rowPositioner.rowFor(this@Table, y)
                    }

                    override fun render(view: com.nectar.doodle.controls.list.List<R, *>, canvas: Canvas) {
                        if (this@InternalListColumn != internalColumns.last()) {
                            it.renderColumnBody(this@Table, this@InternalListColumn, canvas)
                        }
                    }
                }
            }
        }
    }

    val numRows get() = model.size
    val isEmpty get() = model.isEmpty

    var columnSizePolicy: ColumnSizePolicy = ConstrainedSizePolicy()
        set(new) {
            field = new

            doLayout()
        }

    var behavior = null as TableBehavior<T>?
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

    val selectionChanged: Pool<SetObserver<Int>> = SetPool()

    fun contains(value: T) = value in model

    internal val internalColumns = mutableListOf<InternalColumn<*, *, *>>()

    init {
        ColumnFactoryImpl().apply(block)

        // Last, unusable column
        internalColumns += InternalListColumn(header = null, cellGenerator = object : ItemVisualizer<String> {
            override fun invoke(item: String, previous: View?) = object : View() {}
        }) { "" } // FIXME: Use a more robust method to avoid any rendering of the cell contents
    }

    private val headerItemsToColumns = mutableMapOf<View, InternalColumn<*,*,*>>()

    private val header: Box = object: Box() {
        init {
            layout = object : Layout() {
                override fun layout(positionable: Positionable) {
                    var x = 0.0
                    var totalWidth = 0.0

                    positionable.children.forEachIndexed { index, view ->
                        view.bounds = Rectangle(Point(x, 0.0), Size(internalColumns[index].width, positionable.height))

                        x += view.width
                        totalWidth += view.width
                    }

                    positionable.width = totalWidth + internalColumns[internalColumns.size - 1].width
                }
            }
        }

        override fun render(canvas: Canvas) {
            behavior?.renderHeader(this@Table, canvas)
        }
    }

    private val panel = ScrollPanel(object: Box() {
        init {
            children += internalColumns.map { it.view }

            layout = object : Layout() {
                override fun layout(positionable: Positionable) {
                    var x          = 0.0
                    var height     = 0.0
                    var totalWidth = 0.0

                    positionable.children.forEachIndexed { index, view ->
                        view.bounds = Rectangle(Point(x, 0.0), Size(internalColumns[index].width, view.minimumSize.height))

                        x          += view.width
                        height      = max(height, view.height)
                        totalWidth += view.width
                    }

                    positionable.size = Size(max(positionable.parent!!.width, totalWidth), max(positionable.parent!!.height, height))

                    positionable.children.forEach {
                        it.height = positionable.height
                    }
                }
            }
        }

        override fun render(canvas: Canvas) {
            behavior?.renderBody(this@Table, canvas)
        }
    }.apply {
        // FIXME: Use two scroll-panels instead since async scrolling makes this look bad
        boundsChanged += { _,old,new ->
            if (old.x != new.x) {
                header.x = new.x
            }
        }
    })

    @Suppress("PrivatePropertyName")
    protected open val selectionChanged_: SetObserver<Int> = { set,removed,added ->
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
    private val columnDirty: (Column<*>) -> Unit = { (it as? InternalColumn<*,*,*>)?.view?.rerender() }

    operator fun get(index: Int) = model[index]

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
        width = columnSizePolicy.layout(this.width, this.internalColumns, resizingCol ?: 0)
        resizingCol = null

        super.doLayout()

        header.doLayout()
        (panel.content as? Box)?.doLayout()
    }

    companion object {
        operator fun <T> invoke(
                       values        : List<T>,
                       selectionModel: SelectionModel<Int>? = null,
                       block         : ColumnFactory<T>.() -> Unit): Table<T, ListModel<T>> = Table(SimpleListModel(values), selectionModel, block)
    }
}