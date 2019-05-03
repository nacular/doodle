package com.nectar.doodle.controls

import com.nectar.doodle.controls.Table.Column
import com.nectar.doodle.controls.list.ListBehavior
import com.nectar.doodle.controls.panels.ScrollPanel
import com.nectar.doodle.core.Box
import com.nectar.doodle.core.Layout
import com.nectar.doodle.core.Positionable
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.layout.constrain
import com.nectar.doodle.scheduler.Strand
import com.nectar.doodle.theme.Behavior
import com.nectar.doodle.utils.AdaptingObservableSet
import com.nectar.doodle.utils.ObservableSet
import com.nectar.doodle.utils.Pool
import com.nectar.doodle.utils.SetObserver
import com.nectar.doodle.utils.SetPool
import kotlin.math.max
import kotlin.math.min

/**
 * Created by Nicholas Eddy on 4/6/19.
 */
interface Model<T>: Iterable<T> {
    val size: Int

    operator fun get(index: Int): T?

    fun isEmpty (                       ) = size == 0
    fun section (range: ClosedRange<Int>): kotlin.collections.List<T>
    fun contains(value: T               ): Boolean
}

interface TableBehavior<T>: Behavior<Table<T, *>> {
    interface CellGenerator<T> {
        operator fun <A> invoke(table: Table<T, *>, cell: A, row: Int, itemGenerator: ItemGenerator<A>, current: View? = null): View
    }

    interface RowPositioner<T> {
        operator fun invoke(table: Table<T, *>, row: T, index: Int): Rectangle

        fun rowFor(table: Table<T, *>, y: Double): Int
    }

    interface HeaderPositioner<T> {
        operator fun invoke(table: Table<T, *>): HeaderGeometry
    }

    interface HeaderCellGenerator<T> {
        operator fun invoke(table: Table<T, *>, column: Column): View
    }

    data class HeaderGeometry(val y: Double, val height: Double)

    val cellGenerator      : CellGenerator<T>
    val rowPositioner      : RowPositioner<T>
    val headerPositioner   : HeaderPositioner<T>
    val headerCellGenerator: HeaderCellGenerator<T>

    var headerDirty: (() -> Unit)?
    var bodyDirty  : (() -> Unit)?

    fun renderHeader(table: Table<T, *>, canvas: Canvas) {}
    fun renderBody  (table: Table<T, *>, canvas: Canvas) {}
}

interface ColumnSizePolicy<T> {
    fun layout(table: Table<T, *>, columns: List<Column>)

    fun widthChanged(table: Table<T, *>, columns: List<Column>, index: Int)
}

class ConstrainedSizePolicy<T>: ColumnSizePolicy<T> {
    override fun layout(table: Table<T, *>, columns: List<Column>) {
        var numNull          = columns.filter { it.width == null }.size
        val totalColumnWidth = columns.fold(0.0) { sum, column -> sum + column.renderWidth }
        var remainingWidth   = table.width - totalColumnWidth

        if (remainingWidth > 0) {
            val sortedColumns = columns.sortedWith(compareByDescending<Column>{ it.width }.thenByDescending { it.maxWidth }).toMutableList()

            sortedColumns.iterator().let {
                while (it.hasNext() && remainingWidth > 0) {
                    val column = it.next()
                    val old    = column.renderWidth

                    column.width?.let { width ->
                        column.renderWidth += min(remainingWidth, width - column.renderWidth)
                    } ?: {
                        column.renderWidth += remainingWidth / numNull
                        --numNull
                    }()

                    remainingWidth -= column.renderWidth - old

                    if (column.renderWidth == column.maxWidth) {
                        it.remove()
                    }
                }
            }

            if (remainingWidth > 0) {
                sortedColumns.forEach { column ->
                    if (remainingWidth > 0) {
                        val old = column.renderWidth

                        column.maxWidth?.let { maxWidth ->
                            column.renderWidth += min(remainingWidth, maxWidth - column.renderWidth)
                        }

                        remainingWidth -= column.renderWidth - old
                    }
                }
            }
        } else if (remainingWidth < 0) {
            val sortedColumns = columns.sortedWith(compareBy<Column>{ it.width }.thenBy { it.maxWidth }).toMutableList()

            sortedColumns.iterator().let {
                while (it.hasNext() && remainingWidth < 0) {
                    val column = it.next()
                    val old    = column.renderWidth

                    column.width?.let { width ->
                        column.renderWidth += min(remainingWidth, width - column.renderWidth)
                    } ?: {
                        column.renderWidth += remainingWidth / numNull
                        --numNull
                    }()

                    remainingWidth -= column.renderWidth - old

                    if (column.renderWidth == column.maxWidth) {
                        it.remove()
                    }
                }
            }
        }
    }

//    override fun layout(table: Table<T, *>, columns: List<Column<T, *>>) {
//        val nullColumns      = mutableSetOf<Column<T, *>>()
//        var totalColumnWidth = columns.fold(0.0) { sum, column -> sum + (column.width ?: 0.0.also { nullColumns += column }) }
//        var remainingWidth   = table.width - totalColumnWidth
//
//        if (remainingWidth >= 0) {
//            nullColumns.iterator().let {
//                while (it.hasNext()) {
//                    val column = it.next()
//
//                    column.renderWidth = remainingWidth / nullColumns.size
//
//                    remainingWidth   -= column.renderWidth
//                    totalColumnWidth += column.renderWidth
//
//                    it.remove()
//                }
//            }
//        } else {
//            nullColumns.forEach { column ->
//                column.renderWidth = 0.0
//            }
//        }
//
//        remainingWidth = table.width - totalColumnWidth
//
//        if (remainingWidth != 0.0) {
//            columns.forEach { column ->
//                column.renderWidth += if (totalColumnWidth > 0) remainingWidth * column.renderWidth / totalColumnWidth else 0.0
//            }
//        }
//    }

    override fun widthChanged(table: Table<T, *>, columns: List<Table.Column>, index: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class ListModel<T>(private val list: List<T>): Model<T> {

    override val size get() = list.size

    override fun get     (index: Int             ) = list.getOrNull(index)
    override fun section (range: ClosedRange<Int>) = list.subList (range.start, range.endInclusive + 1)
    override fun contains(value: T               ) = list.contains(value                              )
    override fun iterator(                       ) = list.iterator(                                   )
}

open class Table<T, M: Model<T>>(private   val strand        : Strand,
                                 private   val model         : M,
                                 protected val selectionModel: SelectionModel<Int>? = null,
                                               block         : ColumnBuilder<T>.() -> Unit): View(), Selectable<Int> by ListSelectionManager(selectionModel, { model.size }) {

    interface Column {
        val text       : String
        val width      : Double?
        val minWidth   : Double
        val maxWidth   : Double?
        var renderWidth: Double
    }

    interface ColumnBuilder<T> {
        fun <R> column(text: String, width: Double? = null, minWidth: Double = 0.0, maxWidth: Double? = null, itemGenerator: ItemGenerator<R>, extractor: T.() -> R): Column
    }

    val numRows: Int get() = model.size

    var columnSizePolicy: ColumnSizePolicy<T> = ConstrainedSizePolicy()

    var behavior = null as TableBehavior<T>?
        set(new) {
            if (new == behavior) { return }

            field?.let {
                it.bodyDirty   = null
                it.headerDirty = null

                it.uninstall(this)
            }

            field = new?.also { behavior ->
                behavior.bodyDirty   = bodyDirty
                behavior.headerDirty = headerDirty

                columns.forEach {
                    it.behavior(behavior)
                }

                behavior.install(this)

                header.children.batch {
                    clear()

                    headerItemsToColumns.clear()

                    addAll(columns.map { column ->
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
//                        header.left  = header.parent.left
//                        header.right = header.parent.right
                    }

                    panel.top    = header.bottom
                    panel.left   = panel.parent.left
                    panel.right  = panel.parent.right
                    panel.bottom = panel.parent.bottom
                }
            }
        }

    private inner class ColumnBuilderImpl: ColumnBuilder<T> {
        override fun <R> column(text: String, width: Double?, minWidth: Double, maxWidth: Double?, itemGenerator: ItemGenerator<R>, extractor: (T) -> R) = InternalColumn(text, itemGenerator, width, minWidth, maxWidth, extractor).also { columns += it }
    }

    private inner class InternalColumn<R>(
            override val text         : String,
                     val itemGenerator: ItemGenerator<R>,
            override val width        : Double? = null,
            override val minWidth     : Double  = 0.0,
            override val maxWidth     : Double? = null,
                         extractor    : T.() -> R): Column {

        private inner class FieldModel<A>(private val model: M, private val extractor: T.() -> A): com.nectar.doodle.controls.list.Model<A> {
            override val size get() = model.size

            override fun get(index: Int) = model[index]?.let(extractor)

            override fun section(range: ClosedRange<Int>) = model.section(range).map(extractor)

            override fun contains(value: A) = value in model.map(extractor)

            override fun iterator() = model.map(extractor).iterator()
        }

        override var renderWidth = width ?: 0.0
            set(new) {
                field = max(minWidth, new).let {
                    if (maxWidth != null) {
                        min(maxWidth, it)
                    } else {
                        it
                    }
                }
            }

        val list = com.nectar.doodle.controls.list.List(strand, FieldModel(model, extractor), itemGenerator, selectionModel).apply {
            acceptsThemes = false
        }

        fun behavior(behavior: TableBehavior<T>?) {
            behavior?.let {
                list.behavior = object : ListBehavior<R> {
                    override val generator: ListBehavior.RowGenerator<R>
                        get() = object : ListBehavior.RowGenerator<R> {
                            override fun invoke(list: com.nectar.doodle.controls.list.List<R, *>, row: R, index: Int, current: View?) = behavior.cellGenerator.invoke(this@Table, row, index, itemGenerator, current)
                        }

                    override val positioner: ListBehavior.RowPositioner<R>
                        get() = object : ListBehavior.RowPositioner<R> {
                            override fun invoke(list: com.nectar.doodle.controls.list.List<R, *>, row: R, index: Int) = behavior.rowPositioner.invoke(this@Table, model[index]!!, index).run { Rectangle(0.0, y, list.width, height) }

                            override fun rowFor(list: com.nectar.doodle.controls.list.List<R, *>, y: Double) = behavior.rowPositioner.rowFor(this@Table, y)
                        }

                    override fun render(view: com.nectar.doodle.controls.list.List<R, *>, canvas: Canvas) {}
                }
            }
        }
    }


    private val columns = mutableListOf<InternalColumn<*>>()

    init {
        ColumnBuilderImpl().apply(block)

        columns += InternalColumn("", itemGenerator = object : ItemGenerator<String> {
            override fun invoke(item: String, previous: View?) = object : View() {}
        }) { "" } // FIXME: Use a more robust method to avoid any rendering of the cell contents
    }

    private val headerItemsToColumns = mutableMapOf<View, InternalColumn<*>>()

    private val header = object: Box() {
        init {
            layout = object : Layout() {
                override fun layout(positionable: Positionable) {
                    var x = 0.0
                    var totalWidth = 0.0

                    positionable.children.forEachIndexed { index, view ->
                        view.bounds = Rectangle(Point(x, 0.0), Size(columns[index].renderWidth, positionable.height))

                        x += view.width
                        totalWidth += view.width
                    }

                    positionable.width = totalWidth
                }
            }
        }

        override fun render(canvas: Canvas) {
            behavior?.renderHeader(this@Table, canvas)
        }
    }

    private val panel = ScrollPanel(object: Box() {
        init {
            children += columns.map { it.list }

            layout = object : Layout() {
                override fun layout(positionable: Positionable) {
                    var x          = 0.0
                    var height     = 0.0
                    var totalWidth = 0.0

                    positionable.children.forEachIndexed { index, view ->
                        view.bounds = Rectangle(Point(x, 0.0), Size(columns[index].renderWidth, view.height))

                        x          += view.width
                        height      = max(height, view.height)
                        totalWidth += view.width
                    }

                    positionable.size = Size(max(positionable.parent!!.width, totalWidth), max(positionable.parent!!.height, height))
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
    protected open val selectionChanged_: SetObserver<SelectionModel<Int>, Int> = { set,removed,added ->
        val adaptingSet: ObservableSet<Table<T, *>, Int> = AdaptingObservableSet(this, set)

        (selectionChanged as SetPool).forEach {
            it(adaptingSet, removed, added)
        }
    }

    init {
        children += listOf(header, panel)

        selectionModel?.let { it.changed += selectionChanged_ }
    }

    private val bodyDirty  : () -> Unit = { panel.content?.rerender() }
    private val headerDirty: () -> Unit = { header.rerender        () }

    operator fun get(index: Int) = model[index]

    val selectionChanged: Pool<SetObserver<Table<T, *>, Int>> = SetPool()

    override fun removedFromDisplay() {
        selectionModel?.let { it.changed -= selectionChanged_ }

        super.removedFromDisplay()
    }

    public override var insets
        get(   ) = super.insets
        set(new) { super.insets = new }

    override fun doLayout() {
        columnSizePolicy.layout(this, this.columns)

        super.doLayout()

        header.doLayout()
        (panel.content as? Box)?.doLayout()
    }

    companion object {
//        operator fun invoke(
//                strand        : Strand,
//                progression   : IntProgression,
//                selectionModel: SelectionModel<Int>? = null
////                fitContent    : Boolean              = true,
//                /*cacheLength   : Int                  = 10*/) = Table(strand, progression.toList())

        operator fun <T> invoke(
                       strand        : Strand,
                       values        : List<T>,
                       selectionModel: SelectionModel<Int>? = null,
                       block         : ColumnBuilder<T>.() -> Unit
//                selectionModel: SelectionModel<Int>? = null,
//                fitContent    : Boolean              = true,
                /*cacheLength   : Int                  = 10*/): Table<T, Model<T>> = Table(strand, ListModel(values), selectionModel, block)
    }
}