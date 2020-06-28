@file:Suppress("NestedLambdaShadowedImplicitParameter")

package io.nacular.doodle.controls.tree

import io.nacular.doodle.JvmName
import io.nacular.doodle.controls.IndexedItemVisualizer
import io.nacular.doodle.controls.Selectable
import io.nacular.doodle.controls.SelectionModel
import io.nacular.doodle.controls.panels.ScrollPanel
import io.nacular.doodle.controls.theme.TreeBehavior
import io.nacular.doodle.controls.theme.TreeBehavior.RowGenerator
import io.nacular.doodle.controls.theme.TreeBehavior.RowPositioner
import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.PositionableContainer
import io.nacular.doodle.core.View
import io.nacular.doodle.core.mostRecentAncestor
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Rectangle.Companion.Empty
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.utils.Path
import io.nacular.doodle.utils.Pool
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.SetObserver
import io.nacular.doodle.utils.SetPool
import kotlin.math.max
import kotlin.math.min


/**
 * Created by Nicholas Eddy on 3/23/18.
 */

typealias ExpansionObserver<T>  = (source: Tree<T, *>, paths: Set<Path<Int>>) -> Unit
typealias ExpansionObservers<T> = SetPool<ExpansionObserver<T>>

interface TreeLike: Selectable<Path<Int>> {
    val numRows    : Int
    val hasFocus   : Boolean
    val rootVisible: Boolean

    val focusChanged: PropertyObservers<View, Boolean>

    fun visible(row: Int): Boolean
    fun visible(path: Path<Int>): Boolean

    fun isLeaf(path: Path<Int>): Boolean

    fun expanded(path: Path<Int>): Boolean

    fun pathFromRow(index: Int): Path<Int>?
    fun rowFromPath(path: Path<Int>): Int?

    fun selected       (row : Int     ) = pathFromRow(row)?.let { selected(it) } ?: false
    // FIXME: Hack to work around https://youtrack.jetbrains.com/issue/KT-32032
    fun addSelection   (rows: Set<Int>, jvmWorkaround: Int = 0) = addSelection   (rows.asSequence().map { pathFromRow(it) }.filterNotNull().toSet())
    fun toggleSelection(rows: Set<Int>, jvmWorkaround: Int = 0) = toggleSelection(rows.asSequence().map { pathFromRow(it) }.filterNotNull().toSet())
    fun setSelection   (rows: Set<Int>, jvmWorkaround: Int = 0) = setSelection   (rows.asSequence().map { pathFromRow(it) }.filterNotNull().toSet())
    fun removeSelection(rows: Set<Int>, jvmWorkaround: Int = 0) = removeSelection(rows.asSequence().map { pathFromRow(it) }.filterNotNull().toSet())
    fun collapse(path: Path<Int>)
    fun expand(path: Path<Int>)
    fun expandAll()
    fun collapseAll()
}

open class Tree<T, out M: TreeModel<T>>(
        protected open val model         : M,
                       val itemVisualizer: IndexedItemVisualizer<T>?  = null,
        protected      val selectionModel: SelectionModel<Path<Int>>? = null, // TODO: Use filtered SelectionModel to avoid selecting hidden items?
        private        val scrollCache   : Int                        = 10): View(), TreeLike {

    override var rootVisible = false
        set(new) {
            if (field == new) { return }

            field = new

            // TODO: make more efficient?

            children.batch {
                clear     ()
                rowToPath.clear()
                refreshAll()
            }
        }

    override var numRows = 0

    public override var insets
        get(   ) = super.insets
        set(new) { super.insets = new }

    var behavior: TreeBehavior<T>? = null
        set(new) {
            if (new == behavior) { return }

            field?.uninstall(this)

            field = new?.also {
                this.generator     = it.generator
                this.rowPositioner = it.positioner

                children.batch {
                    clear     ()
                    refreshAll()
                }

                it.install(this)
            }
        }

    val expanded        : ExpansionObservers<T>        by lazy { ExpansionObserversImpl(this) }
    val collapsed       : ExpansionObservers<T>        by lazy { ExpansionObserversImpl(this) }
    val selectionChanged: Pool<SetObserver<Path<Int>>> by lazy { SetPool<SetObserver<Path<Int>>>() }

    override val firstSelection  get() = selectionModel?.first
    override val lastSelection   get() = selectionModel?.last
    override val selectionAnchor get() = selectionModel?.anchor
    override val selection       get() = selectionModel?.toSet() ?: emptySet()

    private   var generator     = null as RowGenerator<T>?
    protected var rowPositioner = null as RowPositioner<T>?
    private   val expandedPaths = mutableSetOf<Path<Int>>()
    private   val rowToPath     = mutableMapOf<Int, Path<Int>>()
    private   var minVisibleY   = 0.0
    private   var maxVisibleY   = 0.0
    private   var minHeight     = 0.0
        set(new) {
            field = new

            minimumSize = Size(minimumSize.width, field)
            height      = field
        }

//    private val pathToRow = mutableMapOf<Path<Int>, Int>()

    protected var firstVisibleRow =  0
    protected var lastVisibleRow  = -1

    @Suppress("PrivatePropertyName")
    private val selectionChanged_: SetObserver<Path<Int>> = { set,removed,added ->
        (selectionChanged as SetPool).forEach {
            it(set, removed, added)
        }

        children.batch {
            (firstVisibleRow .. lastVisibleRow).asSequence().mapNotNull { pathFromRow(it) }.filter { it in removed || it in added }.forEach {
                update(this, it)
            }
        }
    }

    init {
        monitorsDisplayRect = true

        selectionModel?.let { it.changed += selectionChanged_ }

        updateNumRows()


        layout = object: Layout {
            override fun layout(container: PositionableContainer) {
                (firstVisibleRow .. lastVisibleRow).asSequence().mapNotNull { pathFromRow(it)?.run { it to this } }.forEach { (index, path) ->
                    model[path]?.let { value ->
                        layout(children[index % children.size], value, path, index)
                    }
                }
            }
        }
    }

    override var isFocusCycleRoot = true

    override fun render(canvas: Canvas) {
        behavior?.render(this, canvas)
    }

    override fun removedFromDisplay() {
        selectionModel?.let { it.changed -= selectionChanged_ }

        super.removedFromDisplay()
    }

    override fun handleDisplayRectEvent(old: Rectangle, new: Rectangle) {
        if (new.empty) {
            return
        }

        rowPositioner?.let { positioner ->
            if (maxVisibleY > new.bottom && minVisibleY < new.y) {
                return
            }

            val oldFirst = firstVisibleRow
            val oldLast  = lastVisibleRow

            firstVisibleRow = when (val y = new.y) {
                old.y -> firstVisibleRow
                else  -> max(0, findRowAt(y, firstVisibleRow) - scrollCache)
            }

            lastVisibleRow = when (val y = new.bottom) {
                old.bottom -> lastVisibleRow
                else       -> min(numRows, findRowAt(y, lastVisibleRow) + scrollCache)
            }

            val halfCacheLength = min(children.size, scrollCache) / 2

            pathFromRow(firstVisibleRow + halfCacheLength)?.let { path -> model[path]?.let { minVisibleY = positioner.rowBounds(this, it, path, firstVisibleRow + halfCacheLength).y      } }
            pathFromRow(lastVisibleRow  - halfCacheLength)?.let { path -> model[path]?.let { maxVisibleY = positioner.rowBounds(this, it, path, lastVisibleRow  - halfCacheLength).bottom } }

            children.batch {
                // FIXME: This is a bit of a hack to avoid inserting items into the child list at an index they won't be at as the list grows
                // this is b/c items are mapped to their % of the list size, so the list growing will lead to different mappings
                if (this.size <= lastVisibleRow - firstVisibleRow) {
                    repeat(lastVisibleRow - firstVisibleRow - children.size) {
                        add(object : View() {}.apply { visible = false })
                    }
                }

                if (oldFirst > firstVisibleRow) {
                    val end = min(oldFirst, lastVisibleRow)

                    (firstVisibleRow until end).asSequence().mapNotNull { pathFromRow(it)?.run { it to this } }.forEach { (index, path) ->
                        insert(this, path, index)
                    }
                }

                if (oldLast < lastVisibleRow) {
                    val start = when {
                        oldLast > firstVisibleRow -> oldLast + 1
                        else                      -> firstVisibleRow
                    }

                    (start .. lastVisibleRow).asSequence().mapNotNull { pathFromRow(it)?.run { it to this } }.forEach { (index, path) ->
                        insert(this, path, index)?.let {
                            if (it > lastVisibleRow) {
                                return@forEach
                            }
                        } ?: return@forEach
                    }
                }
            }
        }
    }

    operator fun get(path: Path<Int>): T? = model[path]

    operator fun get(row: Int): T? = pathFromRow(row)?.let { model[it] }

    override fun isLeaf(path: Path<Int>) = model.isLeaf(path)

    override fun expanded(path: Path<Int>) = path in expandedPaths

    @JvmName("expandRows") fun expand(row : Int     ) = expand(setOf(row))
    @JvmName("expandRows") fun expand(rows: Set<Int>) = expand(rows.asSequence().map { pathFromRow(it) }.filterNotNull().toSet())

    override fun expand(path: Path<Int>) = expand(setOf(path))

    fun expand(paths: Set<Path<Int>>) {
        val pathSet = paths.asSequence().filter { it.depth > 0 && !expanded(it) }.sortedWith(PathComparator.then(DepthComparator)).toSet()

        val pathsToUpdate = mutableSetOf<Path<Int>>()

        children.batch {
            pathSet.forEach {
                expandedPaths += it

                if (visible(it)) {
                    pathsToUpdate -= it

                    numRows   += rowsBelow  (it)
                    minHeight += heightBelow(it)

                    update        (this, it)
                    insertChildren(this, it)

                    pathsToUpdate += ancestralSiblingsAfter(it)
                }
            }

            pathsToUpdate.forEach {
                updateRecursively(this, it)
            }
        }

        if (maxVisibleY < displayRect.bottom) {
            // TODO: Can this be done better?  It feels a bit hacky
            handleDisplayRectEvent(Rectangle(0.0, minVisibleY, width, maxVisibleY - minVisibleY), displayRect)
        }

        expandedPaths.addAll(paths)

        if (pathSet.isNotEmpty()) {
            (expanded as ExpansionObserversImpl)(pathSet)
        }
    }

    override fun expandAll() {
        val pathsToExpand = HashSet<Path<Int>>()

        expandAllBelowPath(Path(), pathsToExpand)

        expand(pathsToExpand)
    }

    @JvmName("collapseRows") fun collapse(row : Int     ) = collapse(setOf(row))
    @JvmName("collapseRows") fun collapse(rows: Set<Int>) = collapse(rows.asSequence().map { pathFromRow(it) }.filterNotNull().toSet())

    override fun collapse(path : Path<Int>     ) = collapse(setOf(path))
    fun collapse(paths: Set<Path<Int>>) {
        var empty   = true
        val pathSet = paths.asSequence().filter { it.depth > 0 && expanded(it) }.sortedWith(PathComparator.thenDescending(DepthComparator)).toSet()

        children.batch {
            expandedPaths -= pathSet
            pathSet.firstOrNull { visible(it) }?.let {
                empty = false

                update(this, it)

                ancestralSiblingsAfter(it).forEach {
                    updateRecursively(this, it)
                }

                updateNumRows()

                // FIXME: This should be handled better
                minHeight = heightBelow(Path()) + insets.run { top + bottom }

                if (maxVisibleY < displayRect.bottom) {
                    // TODO: Can this be done better?  It feels a bit hacky
                    handleDisplayRectEvent(displayRect, Rectangle(0.0, minVisibleY, width, maxVisibleY - minVisibleY))
                }

                // Remove old children
                (numRows until size).forEach {
                    removeAt(numRows)
                    rowToPath.remove(it) //?.let { pathToRow.remove(it) }
                }
            }
        }

        if (!empty) {
            // Move selection up to first visible ancestor
            // TODO: Make this more efficient?  Add batch to SelectionModel?
            selectionModel?.forEach {
                if (!visible(it)) {
                    var parent = it.parent

                    while (parent != null && !visible(parent)) {
                        parent = parent.parent
                    }

                    removeSelection(setOf(it))
                    parent?.let { addSelection(setOf(it)) }
                }
            }
        }

        if (!empty) {
            (collapsed as ExpansionObserversImpl)(pathSet)
        }
    }

    override fun collapseAll() = collapse(expandedPaths)

    override fun selected(row: Int) = pathFromRow(row)?.let { selected(it) } ?: false
    override fun selected(item: Path<Int>) = selectionModel?.contains(item) ?: false

    override fun selectAll() {
        selectionModel?.addAll((0 .. numRows).mapNotNull {
            pathFromRow(it)
        }.toList())
    }

//    @JvmName("addSelectionRows")
//    override fun addSelection(rows : Set<Int>      ) = addSelection(rows.asSequence().map { pathFromRow(it) }.filterNotNull().toSet())
    override fun addSelection(items: Set<Path<Int>>) {
        selectionModel?.addAll(items.filter { visible(it) })

        scrollToSelection()
    }

//    @JvmName("toggleSelectionRows")
//    override fun toggleSelection(rows : Set<Int>      ) = toggleSelection(rows.asSequence().map { pathFromRow(it) }.filterNotNull().toSet())
    override fun toggleSelection(items: Set<Path<Int>>) {
        selectionModel?.toggle(items.filter { visible(it) })

        scrollToSelection()
    }

//    @JvmName("setSelectionRows")
//    override fun setSelection(rows : Set<Int>      ) = setSelection(rows.asSequence().map { pathFromRow(it) }.filterNotNull().toSet())
    override fun setSelection(items: Set<Path<Int>>) {
        selectionModel?.replaceAll(items.filter { visible(it) })

        scrollToSelection()
    }

//    @JvmName("removeSelectionRows")
//    override fun removeSelection(rows : Set<Int>      ) = removeSelection(rows.asSequence().map { pathFromRow(it) }.filterNotNull().toSet())
    override fun removeSelection(items: Set<Path<Int>>) {
        selectionModel?.removeAll(items)
    }

    override fun clearSelection() = selectionModel?.clear().let { Unit }

    override fun next(after: Path<Int>) = rowFromPath(after)?.let { it + 1 }?.let { pathFromRow(it) }

    override fun previous(before: Path<Int>) = rowFromPath(before)?.let { it - 1 }?.let { pathFromRow(it) }

    override fun visible(row: Int) = pathFromRow(row)?.let { visible(it) } ?: false

    override fun visible(path: Path<Int>): Boolean = when (path.depth) {
        0    -> rootVisible
        1    -> true
        else -> {
            val parent = path.parent

            when {
                parent == null || !expanded(parent) -> false
                else                                -> visible(parent)
            }
        }
    }

    fun makeVisible(path: Path<Int>) {
        var parent = path.parent

        while (parent != null) {
            expand(parent)

            parent = parent.parent
        }
    }

    protected fun update(children: MutableList<View>, path: Path<Int>, index: Int? = rowFromPath(path)): Int? {
        var result = null as Int?

        if (index != null) {
            result = index
            if (index >= 0) {
                rowToPath[index] = path
            }

            // Path index not found (could be invisible)
            if (index in firstVisibleRow..lastVisibleRow) {
                generator?.let {
                    model[path]?.let { value ->
                        // pathToRow[path ] = index

                        val i = index % children.size

                        it(this, value, path, index, children.getOrNull(i)).also {
                            children[i] = it

                            layout(it, value, path, index)
                        }

                        ++result
                    }
                }
            }
        }

        return result
    }

    protected fun layout(view: View, node: T, path: Path<Int>, index: Int) {
        rowPositioner?.let {
            view.bounds = it.rowBounds(this, node, path, index, view)

            minimumSize = Size(max(width, view.width), minHeight)
        }
    }

    private fun updateNumRows() {
        numRows = rowsBelow(Path()) + if(rootVisible) 1 else 0
    }

    private fun findRowAt(y: Double, nearbyRow: Int) = min(numRows - 1, rowPositioner?.row(this, y) ?: nearbyRow)

    private fun siblingsAfter(path: Path<Int>, parent: Path<Int>) = path.bottom?.let {
        (it + 1 until model.numChildren(parent)).map { parent + it }
    } ?: emptyList()

    private fun ancestralSiblingsAfter(path: Path<Int>): Set<Path<Int>> {
        var parent = path.parent
        var child  = path
        val result = mutableSetOf<Path<Int>>()

        while (parent != null) {
            result += siblingsAfter(child, parent)
            child  = parent
            parent = parent.parent
        }

        result += siblingsAfter(child, parent ?: Path())

        return result
    }

    protected fun refreshAll() {
        val root      = Path<Int>()
        val oldHeight = minHeight

        // FIXME: Move to better location; handle rootVisible case

        minHeight = heightBelow(root) + insets.run { top + bottom }

        if (oldHeight == minHeight) {
            // FIXME: This reset logic could be handled better
            minVisibleY     =  0.0
            maxVisibleY     =  0.0
            firstVisibleRow =  0
            lastVisibleRow  = -1
        }

        handleDisplayRectEvent(Empty, displayRect)

        updateNumRows()
    }

    private fun insertChildren(children: MutableList<View>, parent: Path<Int>, parentIndex: Int? = rowFromPath(parent)): Int? {
        var index: Int? = (parentIndex ?: -1) + 1

        (0 until model.numChildren(parent)).forEach {
            val old = index
            index   = insert(children, parent + it, index)

            if (old == index) {
                return@forEach
            }
        }

        return index
    }

    private fun insert(children: MutableList<View>, path: Path<Int>, index: Int? = rowFromPath(path)): Int? {
        if (index == null) {
            return null
        }

        // Path index not found (could be invisible)
        var result: Int? = index

        rowToPath[index] = path
        generator?.let {
            model[path]?.let { value ->
//                    pathToRow[path ] = index

                val expanded = expanded(path)

                if (children.size <= lastVisibleRow - firstVisibleRow) {
                    it(this, value, path, index).also {
                        when {
                            index > children.lastIndex -> children.add(it)
                            else                       -> children.add(index, it)
                        }

                        layout(it, value, path, index)
                    }
                } else {
                    update(children, path, index)
                }

                result = result?.let { it + 1 }

                if (path.depth == 0 || expanded) {
                    result = insertChildren(children, path, index)
                }
            }
        }

        return result
    }

    private fun updateChildren(children: MutableList<View>, parent: Path<Int>, parentIndex: Int? = rowFromPath(parent)): Int? {
        var index: Int? = (parentIndex ?: -1) + 1

        (0 until model.numChildren(parent)).forEach {
            val old = index
            index   = updateRecursively(children, parent + it, index)

            if (old == index) {
                return@forEach
            }
        }

        return index
    }

    private fun updateRecursively(children: MutableList<View>, path: Path<Int>, index: Int? = rowFromPath(path)): Int? {
        var result = update(children, path, index)

        if (result != null && expanded(path)) {
            result = updateChildren(children, path, index)
        }

        return result
    }

    private fun rowExpanded(index: Int) = pathFromRow(index)?.let { expanded(it) } ?: false

    override fun pathFromRow(index: Int): Path<Int>? {
        if (model.isEmpty()) {
            return null
        }

        return if (index < 0 && !rootVisible) null else {
            rowToPath.getOrElse(index) {
                addRowsToPath(Path(), index + if (!rootVisible) 1 else 0)?.first?.also {
                    rowToPath[index] = it
                }
            }
        }
    }

    override fun rowFromPath(path: Path<Int>): Int? /*= pathToRow.getOrPut(path)*/ {
        var row         = if (rootVisible) 0 else -1
        var pathIndex   = 0
        var currentPath = Path<Int>()
        var numChildren = model.numChildren(currentPath)

        if (pathIndex < path.depth) {
            var i = 0
            while (i < numChildren) {
                ++row

                if (i == path[pathIndex]) {
                    pathIndex++

                    if (pathIndex >= path.depth) {
                        return row
                    }

                    if (!rowExpanded(row)) {
                        break
                    }

                    currentPath += i
                    numChildren = model.numChildren(currentPath)
                    i = -1
                } else {
                    row += rowsBelow(currentPath + i)
                }
                ++i
            }

            row = -1
        }

        return row
    }

    private fun rowsBelow(path: Path<Int>): Int {
        var numRows = 0

        if (path.depth == 0 || (expanded(path) && visible(path))) {
            val numChildren = model.numChildren(path)

            (0 until numChildren).asSequence().map { path + it }.forEach { numRows += rowsBelow(it) + 1 }
        }

        return numRows
    }

    // TODO: move this logic into ItemPositioner
    private fun heightBelow(path: Path<Int>) = rowsBelow(path) * (model[path]?.let { rowPositioner?.rowBounds(this, it, path, 0)?.height } ?: 0.0)

    private fun expandAllBelowPath(path: Path<Int>, expandedPath: MutableSet<Path<Int>> = mutableSetOf()) {
        if (model.isLeaf(path)) {
            return
        }

        val numChildren = model.numChildren(path)

        (0 until numChildren).forEach {
            (path + it).let { child ->
                expandedPath += child

                if (!model.isLeaf(child)) {
                    if (!expanded(child)) {
                        expandedPath.add(child)
                    }

                    expandAllBelowPath(child, expandedPath)
                }
            }
        }
    }

    private fun addRowsToPath(path: Path<Int>, index: Int): Pair<Path<Int>, Int>? {
        if (index <= 0) {
            return path to index
        }

        var newIndex = index

        var newPath     = path
        val numChildren = model.numChildren(path)

        for(i in 0 until numChildren) {
            newPath = path + i

            --newIndex

            if (newIndex == 0) {
                break
            }

            if (expanded(newPath)) {
                addRowsToPath(newPath, newIndex)?.also {
                    newPath  = it.first
                    newIndex = it.second
                }

                if (newIndex == 0) {
                    break
                }
            }
        }

        return if (newIndex == 0) newPath to newIndex else null
    }

    fun scrollToSelection() {
        mostRecentAncestor { it is ScrollPanel }?.let { it as ScrollPanel }?.let { parent ->
            lastSelection?.let { lastSelection ->
                val item  = this[lastSelection]
                val index = rowFromPath(lastSelection)

                if (item != null && index != null) {
                    rowPositioner?.rowBounds(this, item, lastSelection, index)?.let {
                        parent.scrollVerticallyToVisible(it.y .. it.bottom)
                    }
                }
            }
        }
    }
}

private class ExpansionObserversImpl<T>(private val source: Tree<T, *>, mutableSet: MutableSet<ExpansionObserver<T>> = mutableSetOf()): SetPool<ExpansionObserver<T>>(mutableSet) {
    operator fun invoke(paths: Set<Path<Int>>) = delegate.forEach { it(source, paths) }
}

private object PathComparator: Comparator<Path<Int>> {
    override fun compare(a: Path<Int>, b: Path<Int>): Int {
        (0 until min(a.depth, b.depth)).forEach {
            (a[it] - b[it]).let {
                if (it != 0) {
                    return it
                }
            }
        }

        return 0
    }
}

private val DepthComparator = Comparator<Path<Int>> { a, b -> b.depth - a.depth }
