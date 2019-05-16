package com.nectar.doodle.controls.table

import com.nectar.doodle.controls.ItemGenerator
import com.nectar.doodle.controls.ModelObserver
import com.nectar.doodle.controls.MutableListModel
import com.nectar.doodle.controls.SelectionModel
import com.nectar.doodle.controls.list.ListBehavior
import com.nectar.doodle.controls.panels.ScrollPanel
import com.nectar.doodle.controls.theme.TreeBehavior
import com.nectar.doodle.controls.tree.Tree
import com.nectar.doodle.controls.tree.TreeLike
import com.nectar.doodle.controls.tree.TreeModel
import com.nectar.doodle.core.Box
import com.nectar.doodle.core.Layout
import com.nectar.doodle.core.Positionable
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.AffineTransform
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.layout.constant
import com.nectar.doodle.layout.constrain
import com.nectar.doodle.scheduler.Strand
import com.nectar.doodle.utils.AdaptingObservableSet
import com.nectar.doodle.utils.ObservableSet
import com.nectar.doodle.utils.Path
import com.nectar.doodle.utils.Pool
import com.nectar.doodle.utils.SetObserver
import com.nectar.doodle.utils.SetPool
import kotlin.math.max
import kotlin.math.min

/**
 * Created by Nicholas Eddy on 5/5/19.
 */

typealias ExpansionObserver<T>  = (source: TreeTable<T, *>, paths: Set<Path<Int>>) -> Unit
typealias ExpansionObservers<T> = SetPool<ExpansionObserver<T>>


private class TreePathIterator(private val tree: TreeLike): Iterator<Path<Int>> {
    private var index = 0

    override fun hasNext() = index < tree.numRows

    override fun next() = tree.pathFromRow(index)!!
}

private class TreeModelIterator<T>(private val model: TreeModel<T>, private val iterator: TreePathIterator): Iterator<T> {
    override fun hasNext() = iterator.hasNext()

    override fun next() = model[iterator.next()]!!
}

fun <T, R> Iterator<T>.map(mapper: (T) -> R) = object: Iterator<R> {
    override fun hasNext() = this@map.hasNext()

    override fun next() = mapper(this@map.next())
}

fun <T, R> TreeModel<T>.map(mapper: (T) -> R) = object: TreeModel<R> {
    override fun get(path: Path<Int>): R? = this@map[path]?.let(mapper)

    override fun isEmpty() = this@map.isEmpty()

    override fun children(parent: Path<Int>) = this@map.children(parent).map(mapper)

    override fun isLeaf(node: Path<Int>) = this@map.isLeaf(node)

    override fun child(of: Path<Int>, path: Int) = this@map.child(of, path)?.let(mapper)

    override fun numChildren(of: Path<Int>) = this@map.numChildren(of)

    override fun indexOfChild(parent: Path<Int>, child: R) = children(parent).asSequence().indexOf(child)
}

class TreeTable<T, M: TreeModel<T>>(
        private   val strand        : Strand,
                      model         : M,
        protected val selectionModel: SelectionModel<Path<Int>>? = null,
                      block         : ColumnBuilder<T>.() -> Unit): View(), TreeLike {
    override val rootVisible get() = tree.rootVisible

    override fun visible(row : Int      ) = tree.visible(row )
    override fun visible(path: Path<Int>) = tree.visible(path)

    override fun isLeaf  (path: Path<Int>) = tree.isLeaf  (path)
    override fun expanded(path: Path<Int>) = tree.expanded(path)
    override fun collapse(path: Path<Int>) = tree.collapse(path)
    override fun expand  (path: Path<Int>) = tree.expand  (path)

    override fun selectAll      (                      ) = tree.selectAll      (      )
    override fun selected       (item  : Path<Int>     ) = tree.selected       (item  )
    override fun addSelection   (items : Set<Path<Int>>) = tree.addSelection   (items )
    override fun setSelection   (items : Set<Path<Int>>) = tree.setSelection   (items )
    override fun removeSelection(items : Set<Path<Int>>) = tree.removeSelection(items )
    override fun toggleSelection(items : Set<Path<Int>>) = tree.toggleSelection(items )
    override fun clearSelection (                      ) = tree.clearSelection (      )
    override fun next           (after : Path<Int>     ) = tree.next           (after )
    override fun previous       (before: Path<Int>     ) = tree.previous       (before)

    override val selection       get() = tree.selection
    override val lastSelection   get() = tree.lastSelection
    override val firstSelection  get() = tree.firstSelection
    override val selectionAnchor get() = tree.selectionAnchor

    val expanded : ExpansionObservers<T> by lazy { ExpansionObserversImpl(this) }
    val collapsed: ExpansionObservers<T> by lazy { ExpansionObserversImpl(this) }

    private lateinit var tree: Tree<*, TreeModel<*>>

    private class ExpansionObserversImpl<T>(private val source: TreeTable<T, *>, mutableSet: MutableSet<ExpansionObserver<T>> = mutableSetOf()): SetPool<ExpansionObserver<T>>(mutableSet) {
        operator fun invoke(paths: Set<Path<Int>>) = delegate.forEach { it(source, paths) }
    }

    private inner class ColumnBuilderImpl: ColumnBuilder<T> {
        override fun <R> column(header       : View?,
                width        : Double?,
                minWidth     : Double,
                maxWidth     : Double?,
                itemGenerator: ItemGenerator<R>,
                extractor    : (T) -> R
        ): Column<T> {
            return if (!::tree.isInitialized) {
                InternalTreeColumn(header, itemGenerator, width, minWidth, maxWidth, extractor).also {
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
                InternalListColumn(header, itemGenerator, width, minWidth, maxWidth, extractor).also { internalColumns += it }
            }
        }
    }

    private abstract inner class InternalColumn<R>(
            override val header         : View?,
                     val itemGenerator  : ItemGenerator<R>,
                         preferredWidth : Double? = null,
                         minWidth       : Double  = 0.0,
                         maxWidth       : Double? = null): Column<T>, ColumnSizePolicy.Column {

        override var preferredWidth = preferredWidth
            set(new) {
                field = new

                field?.let {
                    resizingCol = columns.indexOf(this)
                    columnSizePolicy.widthChanged(this@TreeTable.width, internalColumns, columns.indexOf(this), it)
                    this@TreeTable.doLayout()
                    resizingCol = null
                }
            }

        override var width = preferredWidth ?: minWidth
            set(new) {
                field = max(minWidth, new).let {
                    maxWidth?.let { maxWidth -> min(maxWidth, it) } ?: it
                }
            }

        override var minWidth = minWidth
            protected set(new) {
                field = maxWidth?.let { max(new, it) } ?: new
            }

        override var maxWidth = maxWidth
            protected set(new) {
                field = new?.let { min(new, minWidth) }
            }

        /** FIXME: Refactor and join w/ impl in [[Table]] */
        override fun moveBy(x: Double) {
            val myIndex    = this@TreeTable.columns.indexOf(this)
            val header     = this@TreeTable.header.children[myIndex]
            val translateX = view.transform.translateX
            val delta      = min(max(x, 0 - (header.x + translateX)), this@TreeTable.width - width - (header.x + translateX))

            this@TreeTable.header.children[myIndex].transform *= AffineTransform.Identity.translate(delta)
            view.transform *= AffineTransform.Identity.translate(delta)

            internalColumns.dropLast(1).forEachIndexed { index, column ->
                // FIXME: Support fixed columns instead?

                if (column != this && index > 0) {
                    val targetBounds = this@TreeTable.header.children[index].bounds

                    if (index < myIndex && header.x + translateX < targetBounds.x + targetBounds.width / 2) {
                        this@TreeTable.header.children[index].transform = AffineTransform.Identity.translate(width)
                        column.view.transform = AffineTransform.Identity.translate(width)
                    } else if (index > myIndex && header.x + translateX + header.width > targetBounds.x + targetBounds.width / 2) {
                        this@TreeTable.header.children[index].transform = AffineTransform.Identity.translate(-width)
                        column.view.transform = AffineTransform.Identity.translate(-width)
                    } else {
                        this@TreeTable.header.children[index].transform = AffineTransform.Identity
                        column.view.transform = AffineTransform.Identity
                    }
                }
            }
        }

        override fun resetPosition() {
            var moved      = false
            val myIndex    = this@TreeTable.columns.indexOf(this)
            val myOffset   = this@TreeTable.header.children[myIndex].run { x + transform.translateX }
            var myNewIndex = if (myOffset >= internalColumns.last().view.x ) internalColumns.size - 2 else myIndex

            internalColumns.forEachIndexed { index, column ->
                if (!moved && myOffset < column.view.run { x + transform.translateX }) {
                    myNewIndex = index - if (myIndex < index) 1 else 0
                    moved = true
                }

                this@TreeTable.header.children.getOrNull(index)?.transform = AffineTransform.Identity
                column.view.transform = AffineTransform.Identity
            }

            if (myIndex == myNewIndex) {
                return
            }

            this@TreeTable.header.children.batch {
                if (myNewIndex < size) {
                    add(myNewIndex, removeAt(myIndex))
                } else {
                    add(removeAt(myIndex))
                }
            }

            (panel.content as Box).children.batch {
                if (myNewIndex < size) {
                    add(myNewIndex, removeAt(myIndex))
                } else {
                    add(removeAt(myIndex))
                }
            }

            internalColumns.add(myNewIndex, internalColumns.removeAt(myIndex))

            doLayout()
        }
        abstract val view: View

        abstract fun behavior(behavior: TreeTableBehavior<T>?)
    }

    private inner class InternalTreeColumn<R>(
            header        : View?,
            itemGenerator : ItemGenerator<R>,
            preferredWidth: Double?        = null,
            minWidth      : Double         = 0.0,
            maxWidth      : Double?        = null,
            extractor     : T.() -> R): InternalColumn<R>(header, itemGenerator, preferredWidth, minWidth, maxWidth) {

        override val view = Tree(model.map(extractor), selectionModel).apply {
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

        override fun behavior(behavior: TreeTableBehavior<T>?) {
            behavior?.let {
                view.behavior = object: TreeBehavior<R> {
                    override val generator get() = object: TreeBehavior.RowGenerator<R> {
                        override fun invoke(tree: Tree<R, *>, node: R, path: Path<Int>, index: Int, current: View?) = it.treeCellGenerator.invoke(this@TreeTable, node, path, index, itemGenerator)
                    }

                    override val positioner get() = object: TreeBehavior.RowPositioner<R> {
                        override fun rowBounds(tree: Tree<R, *>, node: R, path: Path<Int>, index: Int, current: View?) = it.rowPositioner.invoke(this@TreeTable, path, model[path]!!, index)

                        override fun contentBounds(tree: Tree<R, *>, node: R, path: Path<Int>, index: Int, current: View?) = rowBounds(tree, node, path, index, current) // FIXME

                        override fun row(of: Tree<R, *>, atY: Double) = it.rowPositioner.rowFor(this@TreeTable, atY)
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

    /**
     * Returns all rows below the given [[Path]]; even if path is collapsed/invisible
     */
    private fun rowsBelow(path: Path<Int>): List<Path<Int>> = (0 until model.numChildren(path)).map { path + it }.flatMap { listOf(it) + if (expanded(it)) rowsBelow(it) else emptyList() }

    private inner class InternalListColumn<R>(
            header        : View?,
            itemGenerator : ItemGenerator<R>,
            preferredWidth: Double? = null,
            minWidth      : Double  = 0.0,
            maxWidth      : Double? = null,
            extractor     : T.() -> R): InternalColumn<R>(header, itemGenerator, preferredWidth, minWidth, maxWidth) {

        private inner class FieldModel<A>(private val model: M, private val extractor: T.() -> A): MutableListModel<A> {
            init {
                this@TreeTable.expanded += { _: TreeTable<*,*>, paths: Set<Path<Int>> ->
                    val added = paths.flatMap { rowsBelow(it) }

                    changed.forEach {
                        it(this, emptyMap(), added.associate { rowFromPath(it)!! to extractor(model[it]!!) }, emptyMap())
                    }
                }

                this@TreeTable.collapsed += { _: TreeTable<*,*>, paths: Set<Path<Int>> ->
                    val removed = paths.flatMap {
                        var index = rowFromPath(it)!!

                        rowsBelow(it).map { index++ to it }
                    }

                    changed.forEach {
                        it(this, removed.associate { (index, path) -> index to extractor(model[path]!!) }, emptyMap(), emptyMap())
                    }
                }
            }

            override fun set(index: Int, value: A): A? = null

            override fun add(value: A) {}

            override fun add(index: Int, values: A) {}

            override fun remove(value: A) {}

            override fun removeAt(index: Int): A? = null

            override fun addAll(values: Collection<A>) {}

            override fun addAll(index: Int, values: Collection<A>) {}

            override fun removeAll(values: Collection<A>) {}

            override fun retainAll(values: Collection<A>) {}

            override fun removeAllAt(indexes: Collection<Int>) {}

            override fun clear() {}

            override val changed = SetPool<ModelObserver<A>>()

            override val size get() = numRows

            override fun get(index: Int) = pathFromRow(index)?.let { model[it]?.let(extractor) }

            override fun section(range: ClosedRange<Int>) = iterator().asSequence().toList().subList(range.start, range.endInclusive + 1)

            override fun contains(value: A) = value in iterator().asSequence()

            // TODO: Re-use
            override fun iterator() = TreeModelIterator(model.map(extractor), TreePathIterator(this@TreeTable))
        }

        override val view = com.nectar.doodle.controls.list.MutableList(strand, FieldModel(model, extractor), itemGenerator, cacheLength = 0).apply {
            acceptsThemes = false
        }

        override fun behavior(behavior: TreeTableBehavior<T>?) {
            behavior?.let {
                view.behavior = object: ListBehavior<R> {
                    override val generator get() = object: ListBehavior.RowGenerator<R> {
                        override fun invoke(list: com.nectar.doodle.controls.list.List<R, *>, row: R, index: Int, current: View?) = it.cellGenerator.invoke(this@TreeTable, row, pathFromRow(index)!!, index, itemGenerator, current)
                    }

                    override val positioner get() = object : ListBehavior.RowPositioner<R> {
                        override fun invoke(list: com.nectar.doodle.controls.list.List<R, *>, row: R, index: Int) = it.rowPositioner.invoke(this@TreeTable, pathFromRow(index)!!, model[pathFromRow(index)!!]!!, index).run { Rectangle(0.0, y, list.width, height) }

                        override fun rowFor(list: com.nectar.doodle.controls.list.List<R, *>, y: Double) = it.rowPositioner.rowFor(this@TreeTable, y)
                    }

                    override fun render(view: com.nectar.doodle.controls.list.List<R, *>, canvas: Canvas) {}
                }
            }
        }
    }

    var model = model
        private set

    override val numRows: Int get() = tree.numRows

    operator fun get(path: Path<Int>): T? = model[path]

    operator fun get(row: Int): T? = pathFromRow(row)?.let { model[it] }

    override fun pathFromRow(index: Int) = tree.pathFromRow(index)

    override fun rowFromPath(path: Path<Int>) = tree.rowFromPath(path)

    var columnSizePolicy: ColumnSizePolicy<T> = ConstrainedSizePolicy()
        set(new) {
            field = new

            doLayout()
        }

    var behavior = null as TreeTableBehavior<T>?
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

    val columns: List<Column<T>> get() = internalColumns.dropLast(1)

    val selectionChanged: Pool<SetObserver<TreeTable<T, *>, Path<Int>>> = SetPool()

    private val internalColumns = mutableListOf<InternalColumn<*>>()

    init {
        ColumnBuilderImpl().apply(block)

        internalColumns += InternalListColumn(null, itemGenerator = object : ItemGenerator<String> {
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

                    positionable.width = totalWidth + internalColumns[internalColumns.size - 1].width
                }
            }
        }

        override fun render(canvas: Canvas) {
            behavior?.renderHeader(this@TreeTable, canvas)
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

                    positionable.height = max(positionable.parent!!.height, height)

                    positionable.children.forEach {
                        it.height = positionable.height
                    }
                }
            }
        }

        override fun render(canvas: Canvas) {
            behavior?.renderBody(this@TreeTable, canvas)
        }
    }.apply {
        // FIXME: Use two scroll-panels instead since async scrolling makes this look bad
        boundsChanged += { _,old,new ->
            if (old.x != new.x) {
                header.x = new.x
            }
        }
    }).apply {
        scrollsHorizontally = false
    }

    @Suppress("PrivatePropertyName")
    protected open val selectionChanged_: SetObserver<SelectionModel<Path<Int>>, Path<Int>> = { set,removed,added ->
        val adaptingSet: ObservableSet<TreeTable<T, *>, Path<Int>> = AdaptingObservableSet(this, set)

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

        // Needed b/c width of header isn't constrained
        header.doLayout()
        (panel.content as? Box)?.doLayout() // FIXME
    }
}