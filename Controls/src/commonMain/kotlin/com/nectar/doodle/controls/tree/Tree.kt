package com.nectar.doodle.controls.tree

import com.nectar.doodle.JvmName
import com.nectar.doodle.controls.SelectionModel
import com.nectar.doodle.controls.theme.basic.TreeUI
import com.nectar.doodle.controls.theme.basic.TreeUI.ItemPositioner
import com.nectar.doodle.controls.theme.basic.TreeUI.ItemUIGenerator
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.utils.ObservableSet
import com.nectar.doodle.utils.Path
import com.nectar.doodle.utils.SetObserver
import com.nectar.doodle.utils.SetPool
import kotlin.math.min


/**
 * Created by Nicholas Eddy on 3/23/18.
 */

private class Property<T>(var value: T)

typealias ExpansionObserver<T> = (source: Tree<T>, paths: Set<Path<Int>>) -> Unit

typealias ExpansionObservers<T> = SetPool<ExpansionObserver<T>>

private class ExpansionObserversImpl<T>(
        private val source: Tree<T>,
        mutableSet: MutableSet<ExpansionObserver<T>> = mutableSetOf()): SetPool<ExpansionObserver<T>>(mutableSet) {
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

class Tree<T>(private val model: Model<T>, private val selectionModel: SelectionModel<Path<Int>>? = null): View() {
    var rootVisible = false
        set(new) {
            if (field == new) { return }

            field = new

            // TODO: make more efficient?

            children.batch {
                clear()
                refreshAll()
            }
        }

    var numRows = 0
        private set

    public override var insets
        get(   ) = super.insets
        set(new) { super.insets = new }

    var renderer: TreeUI<T>? = null
        set(new) {
            if (new == renderer) { return }

            field = new?.also {
                itemPositioner  = it.positioner
                itemUIGenerator = it.uiGenerator

                children.batch {
                    clear()
                    refreshAll()
                }
            }
        }

    val expanded : ExpansionObservers<T> by lazy { ExpansionObserversImpl(this) }
    val collapsed: ExpansionObservers<T> by lazy { ExpansionObserversImpl(this) }

    private var itemPositioner : ItemPositioner<T>? = null
    private var itemUIGenerator: ItemUIGenerator<T>? = null

    private val expandedPaths = mutableSetOf<Path<Int>>()

    private val rowToPath = mutableMapOf<Int, Path<Int>>()
//    private val pathToRow = mutableMapOf<Path<Int>, Int>()

    private var firstVisibleRow =  0
    private var lastVisibleRow  = -1

    val selectionChanged by lazy { ObservableSet<Tree<T>, T>(this) }

    @Suppress("PrivatePropertyName")
    private val selectionChanged_: SetObserver<SelectionModel<Path<Int>>, Path<Int>> = { _,removed,added ->
        children.batch {
            (added + removed).forEach {
                update(this, it)
            }
        }
    }

    init {
        monitorsDisplayRect = true

        selectionModel?.let { it.changed += selectionChanged_ }

        updateNumRows()
    }

    override fun render(canvas: Canvas) {
        renderer?.render(this, canvas)
    }

    override fun handleDisplayRectEvent(old: Rectangle, new: Rectangle) {
        val oldFirst = firstVisibleRow
        val oldLast  = lastVisibleRow

        firstVisibleRow = when (val y = new.y) {
            old.y -> firstVisibleRow
            else  -> findRowAt(y, firstVisibleRow)
        }

        lastVisibleRow = when (val y = new.y + new.height) {
            old.y + old.height -> lastVisibleRow
            else               -> findRowAt(y, lastVisibleRow)
        }

//        println("display rect changed: $new")

//        println("numRows: $numRows, first: $firstVisibleRow, last: $lastVisibleRow")

        if (oldFirst > firstVisibleRow) {
            val end = min(oldFirst, lastVisibleRow)

            (firstVisibleRow until end).asSequence().mapNotNull { (rowToPath[it] ?: pathFromRow(it))?.run { it to this } }.forEach { (index, path) ->
                insert(children, path, index)
            }
        }

        if (oldLast < lastVisibleRow) {
            val start = when {
                oldLast > firstVisibleRow -> oldLast + 1
                else                      -> firstVisibleRow
            }

            (start .. lastVisibleRow).asSequence().mapNotNull { (rowToPath[it] ?: pathFromRow(it))?.run { it to this } }.forEach { (index, path) ->
                insert(children, path, index)
            }
        }
    }

    override fun removedFromDisplay() {
        selectionModel?.let { it.changed -= selectionChanged_ }

        super.removedFromDisplay()
    }

    operator fun get(path: Path<Int>): T? = model[path]

    operator fun get(row: Int): T? = rowToPath[row]?.let { model[it] }

    fun isLeaf(path: Path<Int>) = model.isLeaf(path)

    fun expanded(path: Path<Int>) = path in expandedPaths

    @JvmName("expandRows") fun expand(row : Int     ) = expand(setOf(row))
    @JvmName("expandRows") fun expand(rows: Set<Int>) = expand(rows.asSequence().map { rowToPath[it] }.filterNotNull().toSet())

    fun expand(path: Path<Int>) = expand(setOf(path))

    fun expand(paths: Set<Path<Int>>) {
        val pathList = paths.asSequence().filter { it.depth > 0 && !expanded(it) }.sortedWith(PathComparator.then(DepthComparator))

        var empty         = true
        val pathsToUpdate = mutableSetOf<Path<Int>>()

        children.batch {

            pathList.forEach {
                empty = false

                expandedPaths += it

                if (visible(it)) {
                    pathsToUpdate -= it

                    numRows += rowsBelow(it)

                    this@Tree.height += heightBelow(it)

                    update        (this, it)
                    insertChildren(this, it)

                    pathsToUpdate += ancestralSiblingsAfter(it)
                }
            }

            pathsToUpdate.forEach {
                updateRecursively(this, it)
            }

            updateNumRows()
        }

        expandedPaths.addAll(paths)

        if (!empty) {
            (expanded as ExpansionObserversImpl)(pathList.toSet())
        }
    }

    fun expandAll() {
        val pathsToExpand = HashSet<Path<Int>>()

        expandAllBelowPath(Path(), pathsToExpand)

        expand(pathsToExpand)
    }

    @JvmName("collapseRows") fun collapse(row : Int     ) = collapse(setOf(row))
    @JvmName("collapseRows") fun collapse(rows: Set<Int>) = collapse(rows.asSequence().map { rowToPath[it] }.filterNotNull().toSet())

    fun collapse(path : Path<Int>     ) = collapse(setOf(path))
    fun collapse(paths: Set<Path<Int>>) {
        val pathSet = paths.asSequence().filter { it.depth > 0 && expanded(it) }.sortedWith(PathComparator.thenDescending(DepthComparator)).toSet()
        var empty    = true

        children.batch {
            pathSet.firstOrNull { visible(it) }?.let {
                expandedPaths -= pathSet
                empty          = false

                update(this, it)

                ancestralSiblingsAfter(it).forEach {
                    updateRecursively(this, it)
                }

                updateNumRows()

                // FIXME: This should be handled better
                this@Tree.height = heightBelow(Path()) + insets.run { top + bottom }

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

            (collapsed as ExpansionObserversImpl)(pathSet)
        }
    }

    fun collapseAll() = collapse(expandedPaths)

    fun selected(row : Int      ) = rowToPath[row]?.let { selected(it) } ?: false
    fun selected(path: Path<Int>) = selectionModel?.contains(path) ?: false

    @JvmName("addSelectionRows")
    fun addSelection(rows : Set<Int>      ) = addSelection(rows.asSequence().map { rowToPath[it] }.filterNotNull().toSet())
    fun addSelection(paths: Set<Path<Int>>) {
        selectionModel?.addAll(paths)
    }

    @JvmName("setSelectionRows")
    fun setSelection(rows : Set<Int>      ) = setSelection(rows.asSequence().map { rowToPath[it] }.filterNotNull().toSet())
    fun setSelection(paths: Set<Path<Int>>) {
        selectionModel?.replaceAll(paths)
    }

    @JvmName("removeSelectionRows")
    fun removeSelection(rows : Set<Int>      ) = removeSelection(rows.asSequence().map { rowToPath[it] }.filterNotNull().toSet())
    fun removeSelection(paths: Set<Path<Int>>) {
        selectionModel?.removeAll(paths)
    }

    fun clearSelection() = selectionModel?.clear()

    fun visible(row: Int) = rowToPath[row]?.let { visible(it) } ?: false

    tailrec fun visible(path: Path<Int>): Boolean {
        return when {
            path.depth == 0 -> rootVisible
            path.depth == 1 -> true
            else            -> {
                val parent = path.parent

                when {
                    parent == null || !expanded(parent) -> false
                    else                                -> visible(parent)
                }
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

    private fun updateNumRows() {
        numRows = rowsBelow(Path()) + if(rootVisible) 1 else 0
    }

    private fun findRowAt(y: Double, nearbyRow: Int): Int {
        return min(numRows - 1, itemPositioner?.rowFor(this, y) ?: nearbyRow)
    }

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

    private fun refreshAll() {
        val root = Path<Int>()

        // FIXME: Move to better location; handle rootVisible case
        height = heightBelow(root) + insets.run { top + bottom }
    }

    private fun insertChildren(children: MutableList<View>, parent: Path<Int>, parentIndex: Int = rowFromPath(parent)): Int {
        var index = parentIndex + 1

        (0 until model.numChildren(parent)).forEach {
            val old = index
            index   = insert(children, parent + it, index)

            if (old == index) {
                return@forEach
            }
        }

        return index
    }

    private fun insert(children: MutableList<View>, path: Path<Int>, index: Int = rowFromPath(path)): Int {
        var result = index

        // Path index not found (could be invisible)
        if (index >= 0) {
            rowToPath[index] = path
            itemUIGenerator?.let {
                model[path]?.let { value ->
//                    pathToRow[path ] = index

                    val expanded = path in expandedPaths

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

                    ++result

                    if (path.depth == 0 || expanded) {
                        result = insertChildren(children, path, index)
                    }
                }
            }
        }

        return result
    }

    private fun updateChildren(children: MutableList<View>, parent: Path<Int>, parentIndex: Int = rowFromPath(parent)): Int {
        var index = parentIndex + 1

        (0 until model.numChildren(parent)).forEach {
            val old = index
            index   = updateRecursively(children, parent + it, index)

            if (old == index) {
                return@forEach
            }
        }

        return index
    }

    private fun update(children: MutableList<View>, path: Path<Int>, index: Int = rowFromPath(path)): Int {
        var result = index

        if (index >= 0) {
            rowToPath[index] = path
        }

        // Path index not found (could be invisible)
        if (index in firstVisibleRow .. lastVisibleRow) {
            itemUIGenerator?.let {
                model[path]?.let { value ->
//                    pathToRow[path ] = index

                    val i = index % children.size

                    it(this, value, path, index, children.getOrNull(i)).also {
                        children[i] = it

                        layout(it, value, path, index)
                    }

                    ++result
                }
            }
        }

        return result
    }

    private fun layout(view: View, node: T, path: Path<Int>, index: Int) {
        itemPositioner?.let {
            view.bounds = it(this, node, path, index)
        }
    }

    private fun updateRecursively(children: MutableList<View>, path: Path<Int>, index: Int = rowFromPath(path)): Int {
        var result = update(children, path, index)

        if (result >= 0 && expanded(path)) {
            result = updateChildren(children, path, index)
        }

        return result
    }

    private fun rowExpanded(index: Int) = pathFromRow(index)?.let { expanded(it) } ?: false

    private fun pathFromRow(index: Int): Path<Int>? {
        if (model.isEmpty()) {
            return null
        }

//        return rowToPath.getOrPut(index) {
//            addRowsToPath(Path(), Property(index + if (!rootVisible) 1 else 0)).also {
//                pathToRow[it] = index
//            }
//        }

        return addRowsToPath(Path(), index + if (!rootVisible) 1 else 0).first
    }

    // TODO: Have this return an Int?
    private fun rowFromPath(path: Path<Int>): Int /*= pathToRow.getOrPut(path)*/ {
        var row = if (rootVisible) 0 else -1
        var pathIndex = 0
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

        if (path.depth == 0 || (path in expandedPaths && visible(path))) {
            val numChildren = model.numChildren(path)

            (0 until numChildren).asSequence().map { path + it }.forEach { numRows += rowsBelow(it) + 1 }
        }

        return numRows
    }

    private fun heightBelow(path: Path<Int>): Double {
        // TODO: move this logic into ItemPositioner
        return rowsBelow(path) * (model[path]?.let { itemPositioner?.invoke(this, it, path, 0)?.height } ?: 0.0)
    }

    private fun expandAllBelowPath(path: Path<Int>, expandedPath: MutableSet<Path<Int>> = mutableSetOf()) {
        if (model.isLeaf(path)) {
            return
        }

        val numChildren = model.numChildren(path)

        (0 until numChildren).forEach {
            (path + it).let { child ->
                expandedPath += child

                if (!model.isLeaf(child)) {
                    if (child !in expandedPaths) {
                        expandedPath.add(child)
                    }

                    expandAllBelowPath(child, expandedPath)
                }
            }
        }
    }

    private fun addRowsToPath(path: Path<Int>, index: Int): Pair<Path<Int>, Int> {
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
                addRowsToPath(newPath, newIndex).also {
                    newPath  = it.first
                    newIndex = it.second
                }

                if (newIndex == 0) {
                    break
                }
            }
        }

        return newPath to newIndex
    }
}
