package com.nectar.doodle.controls.table

import com.nectar.doodle.controls.ItemGenerator
import com.nectar.doodle.controls.ListModel
import com.nectar.doodle.controls.ListSelectionManager
import com.nectar.doodle.controls.Selectable
import com.nectar.doodle.controls.SelectionModel
import com.nectar.doodle.controls.SimpleListModel
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
import com.nectar.doodle.utils.AdaptingObservableSet
import com.nectar.doodle.utils.ObservableSet
import com.nectar.doodle.utils.Pool
import com.nectar.doodle.utils.SetObserver
import com.nectar.doodle.utils.SetPool
import kotlin.math.max
import kotlin.math.min

open class Table<T, M: ListModel<T>>(
        private   val strand        : Strand,
                      model         : M,
        protected val selectionModel: SelectionModel<Int>? = null,
                      block         : ColumnBuilder<T>.() -> Unit): View(), Selectable<Int> by ListSelectionManager(selectionModel, { model.size }) {

    interface Column<T> {
        val header        : View?
        val width         : Double
        val minWidth      : Double
        val maxWidth      : Double?
        var preferredWidth: Double?
//        val comparator  : Comparator<T>?
    }

    interface ColumnBuilder<T> {
        fun <R> column(header       : View?,
                       width        : Double? = null,
                       minWidth     : Double = 0.0,
                       maxWidth     : Double? = null,
                       itemGenerator: ItemGenerator<R>,
                       comparator   : Comparator<T>? = null,
                       extractor    : T.() -> R): Column<T>
    }

    var model = model
        private set

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

                internalColumns.forEach {
                    it.behavior(behavior)
                }

                behavior.install(this)

                header.children.batch {
                    clear()

                    headerItemsToColumns.clear()

                    addAll(internalColumns.map { column ->
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
        override fun <R> column(header       : View?,
                                width        : Double?,
                                minWidth     : Double,
                                maxWidth     : Double?,
                                itemGenerator: ItemGenerator<R>,
                                comparator   : Comparator<T>?,
                                extractor    : (T) -> R
        ) = InternalColumn(header, itemGenerator, width, minWidth, maxWidth, comparator, extractor).also { internalColumns += it }
    }

    private inner class InternalColumn<R>(
            override val header         : View?,
                     val itemGenerator  : ItemGenerator<R>,
                         preferredWidth : Double?        = null,
            override val minWidth       : Double         = 0.0,
            override val maxWidth       : Double?        = null,
                     val comparator     : Comparator<T>? = null,
                         extractor      : T.() -> R): Column<T>, ColumnSizePolicy.Column {

        private inner class FieldModel<A>(private val model: M, private val extractor: T.() -> A): ListModel<A> {
            override val size get() = model.size

            override fun get(index: Int) = model[index]?.let(extractor)

            override fun section(range: ClosedRange<Int>) = model.section(range).map(extractor)

            override fun contains(value: A) = value in model.map(extractor)

            override fun iterator() = model.map(extractor).iterator()
        }

        override var preferredWidth = preferredWidth
            set(new) {
                field = new

                field?.let {
                    resizingCol = columns.indexOf(this)
                    columnSizePolicy.widthChanged(this@Table, internalColumns, columns.indexOf(this), it)
                    this@Table.doLayout()
                    resizingCol = null
                }
            }

        override var width = preferredWidth ?: minWidth
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

    val columns: List<Column<T>> get() = internalColumns

    private val internalColumns = mutableListOf<InternalColumn<*>>()

    init {
        ColumnBuilderImpl().apply(block)

        internalColumns += InternalColumn(null, itemGenerator = object : ItemGenerator<String> {
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
                        view.bounds = Rectangle(Point(x, 0.0), Size(internalColumns[index].width, positionable.height))

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
            children += internalColumns.map { it.list }

            layout = object : Layout() {
                override fun layout(positionable: Positionable) {
                    var x          = 0.0
                    var height     = 0.0
                    var totalWidth = 0.0

                    positionable.children.forEachIndexed { index, view ->
                        view.bounds = Rectangle(Point(x, 0.0), Size(internalColumns[index].width, view.height))

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

    private var resizingCol: Int? = null

    override fun doLayout() {
        resizingCol = resizingCol ?: 0
        width = columnSizePolicy.layout(this, this.internalColumns, resizingCol ?: 0)
        resizingCol = null

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
                       block         : ColumnBuilder<T>.() -> Unit): Table<T, ListModel<T>> = Table(strand, SimpleListModel(values), selectionModel, block)
    }
}