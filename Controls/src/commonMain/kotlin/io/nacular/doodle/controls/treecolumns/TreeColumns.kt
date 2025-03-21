package io.nacular.doodle.controls.treecolumns

import io.nacular.doodle.controls.IndexedItem
import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.controls.Selectable
import io.nacular.doodle.controls.SelectionModel
import io.nacular.doodle.controls.SimpleMutableListModel
import io.nacular.doodle.controls.list.List
import io.nacular.doodle.controls.list.ListBehavior
import io.nacular.doodle.controls.list.MutableList
import io.nacular.doodle.controls.panels.ScrollPanel
import io.nacular.doodle.controls.tree.TreeModel
import io.nacular.doodle.core.Layout.Companion.simpleLayout
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.utils.Direction.East
import io.nacular.doodle.utils.Path
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.Resizer
import io.nacular.doodle.utils.SetObservers
import io.nacular.doodle.utils.SetPool
import kotlin.math.max

/**
 * Created by Nicholas Eddy on 6/1/19.
 */
private interface Focusable {
    val hasFocus    : Boolean
    val focusChanged: PropertyObservers<View, Boolean>
}

// TODO: Use filtered SelectionModel to avoid selecting hidden items?
public open class TreeColumns<T, M: TreeModel<T>>(
        protected val model         : M,
        public    val itemVisualizer: ItemVisualizer<T, IndexedItem>? = null,
                      selectionModel: SelectionModel<Path<Int>>?      = null): View(), Selectable<Path<Int>>, Focusable {

    protected class FilteringSelectionModel(delegate: SelectionModel<Path<Int>>): SelectionModel<Path<Int>> by delegate {
        public var root: Path<Int>? = null
        public val children: kotlin.collections.MutableList<Int> = mutableListOf()

        init {
            // FIXME: There are still bugs around toggling
            delegate.changed += { _, removed, added ->
                root?.let { root ->
                    removed.forEach {
                        // Toggle leaf
                        when {
                            root == it.parent -> {
                                children -= it.bottom!!
                            }
                            it == root || it ancestorOf root -> {
                                children.clear()
                                this.root = null
                            }
                        }
                    }
                }

                val r = root

                added.forEach {
                    when {
                        r != null && it != r && it.parent == r.parent -> {
                            children += listOf(it.bottom!!, r.bottom!!)
                            root = it.parent
                        }
                        else -> {
                            children.clear()

                            root = it
                        }
                    }
                }
            }
        }
    }

    private val columns = mutableListOf<Column<T>>()

    protected val selectionModel: FilteringSelectionModel? = selectionModel?.let { FilteringSelectionModel(it) }

    private class LocalSelectionModel(var root: Path<Int>, private val delegate: SelectionModel<Path<Int>>): SelectionModel<Int> { //MultiSelectionModel<Int>() {

        private val changeObserver: (SelectionModel<Path<Int>>, Set<Path<Int>>, Set<Path<Int>>) -> Unit = { _, removed, added ->
            (changed as SetPool).forEach {
                it(
                    this,
                    removed.filter { root ancestorOf it }.mapNotNull { it.nonOverlappingStem(root).firstOrNull() }.toSet(),
                    added.filter   { root ancestorOf it }.mapNotNull { it.nonOverlappingStem(root).firstOrNull() }.toSet()
                )
            }
        }

        private var delegateHandle = null as SelectionModel<Path<Int>>?

        fun enable() {
            if (delegateHandle == null) {
                delegateHandle = delegate
                delegate.changed += changeObserver
            }
        }

        fun disable() {
            delegateHandle?.let {
                it.changed -= changeObserver
                delegateHandle = null
            }
        }

        override fun add   (item : Int            ) = delegateHandle?.add      (root + item) ?: false
        override fun addAll(items: Collection<Int>) = delegateHandle?.addAll   (items.map { root + it }) ?: false
        override fun remove(item : Int            ) = delegateHandle?.remove   (root + item) ?: false
        override fun clear (                      ) { delegateHandle?.removeAll(delegate.iterator().asSequence().filter { root ancestorOf it }.toList()) }

        override fun removeAll  (items: Collection<Int>) = delegateHandle?.removeAll  (items.map { root + it }) ?: false
        override fun retainAll  (items: Collection<Int>) = delegateHandle?.retainAll  (items.map { root + it }) ?: false
        override fun containsAll(items: Collection<Int>) = delegateHandle?.containsAll(items.map { root + it }) ?: false
        override fun replaceAll (items: Collection<Int>) = delegateHandle?.replaceAll (items.map { root + it }) ?: false
        override fun toggle     (items: Collection<Int>) = delegateHandle?.toggle     (items.map { root + it }) ?: false

        private fun overlappingIndex(path: Path<Int>?): Int? {
            return when {
                path != null && root ancestorOf path -> path.nonOverlappingStem(root).firstOrNull()
                else                                 -> null
            }
        }

        override val first  get() = overlappingIndex(delegateHandle?.first)
        override val last   get() = overlappingIndex(delegateHandle?.last )
        override val anchor get() = overlappingIndex(delegateHandle?.anchor)
        override val size   get() = delegateHandle?.iterator()?.asSequence()?.fold(0) { size, path ->
            size + (overlappingIndex(path)?.let { 1 } ?: 0)
        } ?: 0

        override val isEmpty get() = size == 0

        override fun contains(item: Int) = delegateHandle?.iterator()?.asSequence()?.find { overlappingIndex(it) == item } != null

        // FIXME: This is pretty inefficient
        override val changed: SetObservers<SelectionModel<Int>, Int> = SetPool()

        override fun iterator() = delegateHandle?.iterator()?.asSequence()?.mapNotNull { overlappingIndex(it) }?.iterator() ?: emptyList<Int>().iterator()
    }

    private class CustomMutableList<T>(
            model              : SimpleMutableListModel<T>,
            itemGenerator      : ItemVisualizer<T, IndexedItem>? = null,
        val localSelectionModel: LocalSelectionModel?            = null,
    ): MutableList<T, SimpleMutableListModel<T>>(model, itemGenerator, localSelectionModel) {
        public override val model = super.model

        fun enable() {
            localSelectionModel?.enable()
        }

        fun disable() {
            localSelectionModel?.disable()
        }

        override fun addedToDisplay() {
            super.addedToDisplay()
            enable()
        }

        override fun removedFromDisplay() {
            super.removedFromDisplay()
            disable()
        }
    }

    public var behavior: TreeColumnsBehavior<T>? = null
        set(new) {
            if (new == behavior) {
                return
            }

            field?.uninstall(this)

            field = new

            new?.also { behavior ->
                behavior.install(this)

                columns.forEach {
                    installBehavior(it, behavior)
                }
            }
        }

    private class Column<T>(var path: Path<Int>, val list: CustomMutableList<T>)

    private fun installBehavior(column: Column<T>, behavior: TreeColumnsBehavior<T>) {
        column.list.behavior = object: ListBehavior<T> {
            override val generator = object: ListBehavior.ItemGenerator<T> {
                override fun invoke(list: List<T, *>, item: T, index: Int, current: View?) = behavior.generator(
                    this@TreeColumns,
                    item,
                    column.path + index,
                    index,
                    current
                )
            }

            override val positioner = object: ListBehavior.ItemPositioner<T> {
                override fun itemBounds(of: List<T, *>, item: T, index: Int, view: View?) = behavior.positioner.rowBounds(
                    this@TreeColumns,
                    of.width,
                    column.path + index,
                    item,
                    index,
                    view
                )

                override fun item(of: List<T, *>, at: Point) = behavior.positioner.row(this@TreeColumns, column.path, at)

                override fun minimumSize(of: List<T, *>) = behavior.positioner.minimumSize(this@TreeColumns, column.path)
            }

            override fun render(view: List<T, *>, canvas: Canvas) {
                behavior.renderColumnBody(this@TreeColumns, column.path, canvas)
            }
        }
    }

    public override var insets: Insets
        get(   ) = super.insets
        set(new) { super.insets = new }

    init {
        this.selectionModel?.changed?.plusAssign { _,_,_ ->
            val rootDepth = this.selectionModel.root?.depth ?: 0

            this.selectionModel.root?.let { root ->
                var path = Path<Int>()

                root.forEachIndexed { index, element ->
                    path += element
                    val columnIndex = index + 1

                    if (columnIndex < columns.size) {
                        val column = columns[columnIndex]

                        if (column.path != path) {
                            column.apply {
                                list.disable()
                                this.path = path
                                list.localSelectionModel?.root = path
                                list.model.replaceAll(model.children(path).asSequence().toList())
                                list.enable()
                            }
                        }
                    } else {
                        columns += createColumn(path)
                    }
                }
            }

            children.batch {
                when {
                    rootDepth < size - 1 -> {
                        repeat((children.size - 1) - rootDepth) {
                            columns.removeAt(columns.size - 1)
                            // remove from children
                            removeAt(size - 1)
                        }
                    }
                    else                      -> {
                        (size until columns.size).forEach {
                            // add to children
                            this += createScrollPanel(columns[it].list)
                        }
                    }
                }
            }
        }

        columns += createColumn(Path())

        children.addAll(columns.map { createScrollPanel(it.list) })

        layout = simpleLayout { views,_,current,_,_ ->
            val y     = insets.top
            var x     = insets.left
            val h     = current.height - insets.run { top + bottom }
            var width = 0.0

            views.forEach {
                it.updateBounds(Rectangle(x, y, it.bounds.width, h).also {
                    x     += it.width
                    width += it.width
                })
            }

            if (width > 0) {
                this@TreeColumns.suggestWidth(max(current.width, width + insets.run { left + right }))
            }

            current
        }
    }

    public fun visible(path: Path<Int>): Boolean? = path.parent?.let { selected(it) }

    public fun isLeaf(path: Path<Int>): Boolean = model.isLeaf(path)

    public fun children(parent: Path<Int>): Iterator<T> = model.children(parent)

    public fun child       (of    : Path<Int>, path : Int): Result<T> = model.child       (of,     path )
    public fun numChildren (of    : Path<Int>            ): Int       = model.numChildren (of           )
    public fun indexOfChild(parent: Path<Int>, child: T  ): Int       = model.indexOfChild(parent, child)

    override fun selected(item: Path<Int>): Boolean = selectionModel?.contains(item) ?: false

    public fun enclosedBySelection(item: Path<Int>): Boolean = selectionModel?.root?.let {
        item == it || item ancestorOf it || item in selectionModel
    } ?: false

    override fun selectAll() {
        selectionModel?.root?.let { root ->
            repeat(model.numChildren(root)) {
                selectionModel.add(root + it)
            }
        }
    }

    override fun addSelection(items: Set<Path<Int>>) { selectionModel?.addAll(items) }

    override fun setSelection(items: Set<Path<Int>>) { selectionModel?.replaceAll(items) }

    override fun removeSelection(items: Set<Path<Int>>) { selectionModel?.removeAll(items) }

    override fun toggleSelection(items: Set<Path<Int>>) { selectionModel?.toggle(items) }

    override fun clearSelection() { selectionModel?.clear() }

    override fun next(after: Path<Int>): Path<Int>? = after.parent?.let { parent ->
        val numChildren = model.numChildren(parent)

        after.bottom?.let { bottom ->
            when {
                bottom < numChildren - 1 -> parent + (bottom + 1)
                else                     -> null
            }
        }
    }

    override fun previous(before: Path<Int>): Path<Int>? = before.parent?.let { parent ->
        before.bottom?.let { bottom ->
            when {
                bottom > 0 -> parent + (bottom - 1)
                else       -> null
            }
        }
    }

    override val firstSelection : Path<Int>?     get() = selectionModel?.first
    override val lastSelection  : Path<Int>?     get() = selectionModel?.last
    override val selectionAnchor: Path<Int>?     get() = selectionModel?.anchor
    override val selection      : Set<Path<Int>> get() = selectionModel?.toSet() ?: emptySet()
    override val firstSelectable: Path<Int>?     get() = columns.lastOrNull()?.run { path + (list.lastSelectable  ?: 0) }
    override val lastSelectable : Path<Int>?     get() = columns.lastOrNull()?.run { path + (list.firstSelectable ?: 0) }

    internal fun columnDirty(path: Path<Int>) {
        // FIXME: IMPLEMENT
    }

    private fun createScrollPanel(view: View) = ScrollPanel(view).apply {
        contentWidthConstraints  = { it eq width - verticalScrollBarWidth }
        contentHeightConstraints = { it eq height                         }

        // FIXME: REMOVE
        suggestWidth(200.0)
        Resizer(this).apply {
            movable = false
            directions = setOf(East)
        }
    }

    private fun createColumn(node: Path<Int>): Column<T> = Column(
        node,
        CustomMutableList(
            SimpleMutableListModel(model.children(node).asSequence().toList()),
            itemVisualizer,
            selectionModel?.let { LocalSelectionModel(node, it) },
        ).apply {
            acceptsThemes = false
        }
    ).also {
        behavior?.let { behavior ->
            installBehavior(it, behavior)
        }
    }
}