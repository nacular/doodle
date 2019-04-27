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
        operator fun <A> invoke(table: Table<T, *>, cell: A, row: Int, current: View? = null): View
    }

    interface RowPositioner<T> {
        operator fun invoke(table: Table<T, *>, row: T, index: Int): Rectangle

        fun rowFor(table: Table<T, *>, y: Double): Int
    }

    interface HeaderPositioner<T> {
        operator fun invoke(table: Table<T, *>): HeaderGeometry
    }

    interface HeaderCellGenerator<T> {
        operator fun invoke(table: Table<T, *>, column: Column<T, *>): View
    }

    data class HeaderGeometry(val y: Double, val height: Double)

    val cellGenerator      : CellGenerator<T>
    val rowPositioner      : RowPositioner<T>
    val headerPositioner   : HeaderPositioner<T>
    val headerCellGenerator: HeaderCellGenerator<T>
}

interface ColumnSizePolicy<T> {
    fun layout(table: Table<T, *>, columns: List<Column<T, *>>)

    fun widthChanged(table: Table<T, *>, columns: List<Column<T, *>>, index: Int)
}

class ConstrainedSizePolicy<T>: ColumnSizePolicy<T> {
    override fun layout(table: Table<T, *>, columns: List<Column<T, *>>) {
        var numNull          = columns.filter { it.width == null }.size
        val totalColumnWidth = columns.fold(0.0) { sum, column -> sum + column.renderWidth }
        var remainingWidth   = table.width - totalColumnWidth

        if (remainingWidth > 0) {
            val sortedColumns = columns.sortedWith(compareByDescending<Column<T, *>>{ it.width }.thenByDescending { it.maxWidth }).toMutableList()

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
            val sortedColumns = columns.sortedWith(compareBy<Column<T, *>>{ it.width }.thenBy { it.maxWidth }).toMutableList()

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

    override fun widthChanged(table: Table<T, *>, columns: List<Table.Column<T, *>>, index: Int) {
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

class Table<T, M: Model<T>>(private   val strand        : Strand,
                            private   val model         : M,
                            protected val selectionModel: SelectionModel<Int>? = null,
                                          column        : Column<T, Any>,
                            vararg        columns       : Column<T, Any>): View(), Selectable<Int> by ListSelectionManager(selectionModel, { model.size }) {

    class Column<T, R>(val text: String, val width: Double? = null, val minWidth: Double = 0.0, val maxWidth: Double? = null, val extractor: (T) -> R) {
        internal var renderWidth = width ?: 0.0
            set(new) {
                field = max(minWidth, new).let {
                    if (maxWidth != null) {
                        min(maxWidth, it)
                    } else {
                        it
                    }
                }
            }
    }

    private inner class FieldModel<A>(private val model: M, private val extractor: (T) -> A): com.nectar.doodle.controls.list.Model<A> {
        override val size get() = model.size

        override fun get(index: Int) = model[index]?.let(extractor)

        override fun section(range: ClosedRange<Int>) = model.section(range).map(extractor)

        override fun contains(value: A) = value in model.map(extractor)

        override fun iterator() = model.map(extractor).iterator()
    }

    val numRows: Int get() = model.size

    val columnSizePolicy = ConstrainedSizePolicy<T>()

    public override var insets
        get(   ) = super.insets
        set(new) { super.insets = new }

    var behavior = null as TableBehavior<T>?
        set(new) {
            if (new == behavior) { return }

            field?.uninstall(this)

            field = new?.also { behavior ->
                columns.values.forEach {
                    it.behavior = object: ListBehavior<Any> {
                        override val generator: ListBehavior.ItemGenerator<Any> get() = object: ListBehavior.ItemGenerator<Any> {
                            override fun invoke(list: com.nectar.doodle.controls.list.List<Any, *>, row: Any, index: Int, current: View?) = behavior.cellGenerator.invoke(this@Table, row, index, current)
                        }

                        override val positioner: ListBehavior.ItemPositioner<Any> get() = object: ListBehavior.ItemPositioner<Any> {
                            override fun invoke(list: com.nectar.doodle.controls.list.List<Any, *>, row: Any, index: Int) = behavior.rowPositioner.invoke(this@Table, model[index]!!, index).run { Rectangle(0.0, y, list.width, height) }

                            override fun rowFor(list: com.nectar.doodle.controls.list.List<Any, *>, y: Double) = behavior.rowPositioner.rowFor(this@Table, y)
                        }

                        override fun render(view: com.nectar.doodle.controls.list.List<Any, *>, canvas: Canvas) {}
                    }
                }

                behavior.install(this)

                header.children.batch {
                    clear()

                    headerItemsToColumns.clear()

                    addAll(columns.keys.map { column ->
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

//    private class CustomList<T, M: com.nectar.doodle.controls.list.Model<T>>(strand: Strand, model: M, selectionModel: SelectionModel<Int>?, val column: Column<*, T>): com.nectar.doodle.controls.list.List<T, M>(strand, model, selectionModel) {
//        init {
//            acceptsThemes = false
//        }
//    }

    override fun doLayout() {
        columnSizePolicy.layout(this, this.columns.keys.toList())

        super.doLayout()

        header.doLayout()
        (panel.content as? Box)?.doLayout()
    }

    private val headerItemsToColumns = mutableMapOf<View, Column<T, *>>()

    private var numNullWidths = 0

    private val fillerColumn = Column<T, String>("") { "" } // FIXME: Use a more robust method to avoid any rendering of the cell contents

    private val columns = listOf(column, *columns, fillerColumn).associate {
        if (it.width == null) {
            ++numNullWidths
        }

        it to com.nectar.doodle.controls.list.List(strand, FieldModel(model, it.extractor), selectionModel).apply {
            acceptsThemes = false
        }
    }

    private val header = Box().apply {
        layout = object: Layout() {
            override fun layout(positionable: Positionable) {
                var x          = 0.0
                var totalWidth = 0.0

                positionable.children.forEachIndexed { index, view ->
                    view.bounds = Rectangle(Point(x, 0.0), Size(this@Table.columns.keys.toList()[index].renderWidth, positionable.height))

                    x += view.width
                    totalWidth += view.width
                }

                positionable.width = totalWidth
            }
        }
    }

    private val panel = ScrollPanel(object: Box() {
        init {
            children += this@Table.columns.values

            layout = object : Layout() {
                override fun layout(positionable: Positionable) {
                    var x          = 0.0
                    var height     = 0.0
                    var totalWidth = 0.0

                    positionable.children.forEachIndexed { index, view ->
                        view.bounds = Rectangle(Point(x, 0.0), Size(this@Table.columns.keys.toList()[index].renderWidth, view.height))

                        x          += view.width
                        height      = max(height, view.height)
                        totalWidth += view.width
                    }

                    positionable.size = Size(max(positionable.parent!!.width, totalWidth), max(positionable.parent!!.height, height))
                }
            }
        }

        override fun render(canvas: Canvas) {
            behavior?.render(this@Table, canvas)
        }
    }.apply {
        // FIXME: Use two scroll-panels instead since async scrolling makes this look bad
        boundsChanged += { _,old,new ->
            if (old.x != new.x) {
                header.x = new.x
            }
        }
    })

    init {
        children += listOf(header, panel)
    }

    companion object {
//        operator fun invoke(
//                strand        : Strand,
//                progression   : IntProgression
////                selectionModel: SelectionModel<Int>? = null,
////                fitContent    : Boolean              = true,
//                /*cacheLength   : Int                  = 10*/) =
//                Table(strand, progression.toList())

        operator fun <T> invoke(
                       strand        : Strand,
                       values        : List<T>,
                       selectionModel: SelectionModel<Int>? = null,
                       column        : Column<T, Any>,
                vararg columns       : Column<T, Any>
//                selectionModel: SelectionModel<Int>? = null,
//                fitContent    : Boolean              = true,
                /*cacheLength   : Int                  = 10*/): Table<T, Model<T>> = Table(strand, ListModel(values), selectionModel, column, *columns)
    }
}