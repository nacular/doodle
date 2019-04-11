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

    data class Column<T, R>(val name: String, val extractor: (T) -> R)

    private inner class FieldModel<A>(private val model: M, private val extractor: (T) -> A): com.nectar.doodle.controls.list.Model<A> {
        override val size get() = model.size

        override fun get(index: Int) = model[index]?.let(extractor)

        override fun section(range: ClosedRange<Int>) = model.section(range).map(extractor)

        override fun contains(value: A) = value in model.map(extractor)

        override fun iterator() = model.map(extractor).iterator()
    }

    val numRows: Int get() = model.size

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

                    addAll(columns.keys.map {
                        behavior.headerCellGenerator(this@Table, it)
                    })
                }

                behavior.headerPositioner.invoke(this@Table).apply {
                    header.height = height
                }

                layout = constrain(header, panel) { header, panel ->
                    behavior.headerPositioner.invoke(this@Table).apply {
                        header.top   = header.parent.top + y
                        header.left  = header.parent.left
                        header.right = header.parent.right
                    }

                    panel.top    = header.bottom
                    panel.left   = header.left
                    panel.right  = header.right
                    panel.bottom = panel.parent.bottom
                }
            }
        }

    private val columns = listOf(column, *columns).associate {
        it to com.nectar.doodle.controls.list.List(strand, FieldModel(model, it.extractor), selectionModel).apply {
            acceptsThemes = false
        }
    }

    private val header = Box().apply {
        layout = object: Layout() {
            override fun layout(positionable: Positionable) {
                var x     = 0.0
                val width = positionable.width / positionable.children.size

                positionable.children.forEach {
                    it.bounds = Rectangle(Point(x, 0.0), Size(width, positionable.height))

                    x += it.width
                }
            }
        }
    }

    private val panel = ScrollPanel(object: Box() {
        init {
            children += this@Table.columns.values

            layout = object : Layout() {
                override fun layout(positionable: Positionable) {
                    var x      = 0.0
                    val width  = positionable.width / positionable.children.size
                    var height = 0.0

                    positionable.children.forEach {
                        it.bounds = Rectangle(Point(x, 0.0), Size(width, it.height))

                        x      += it.width
                        height  = max(height, it.height)
                    }

                    positionable.height = height
                }
            }
        }

        override fun render(canvas: Canvas) {
            behavior?.render(this@Table, canvas)
        }
    }).apply { scrollsHorizontally = false }

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