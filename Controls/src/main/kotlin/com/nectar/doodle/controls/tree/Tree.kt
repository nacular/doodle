package com.nectar.doodle.controls.tree

import com.nectar.doodle.JvmName
import com.nectar.doodle.controls.SelectionModel
import com.nectar.doodle.controls.theme.TreeUI
import com.nectar.doodle.controls.theme.TreeUI.ItemPositioner
import com.nectar.doodle.controls.theme.TreeUI.ItemUIGenerator
import com.nectar.doodle.controls.tree.Tree.Direction.Down
import com.nectar.doodle.controls.tree.Tree.Direction.Up
import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.core.Layout
import com.nectar.doodle.core.Positionable
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.event.DisplayRectEvent
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.utils.SetPool
import kotlin.math.max
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

        return b.depth - a.depth
    }
}


class Tree<T>(private val model: Model<T>, private val selectionModel: SelectionModel<Path<Int>>? = null, private val fitContent: Boolean = true): Gizmo() {
    var rootVisible = false
        set(new) {
            if (field == new) { return }

            field = new

            // TODO: make more efficient?

            children.batch {
                clear()
                insertAll(this)
            }
        }

    val numRows get() = rowsBelow(Path()) + if(rootVisible) 1 else 0

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
                    insertAll(this)
                }

//                children.clear()
//                insertAll(children)

                layout = InternalLayout(it.positioner)
            }
        }

    val expanded : ExpansionObservers<T> by lazy { ExpansionObserversImpl(this) }
    val collapsed: ExpansionObservers<T> by lazy { ExpansionObserversImpl(this) }

    private var itemPositioner : ItemPositioner<T>? = null
    private var itemUIGenerator: ItemUIGenerator<T>? = null

    private val expandedPaths = mutableSetOf<Path<Int>>()

//    private val rowToPath = mutableMapOf<Int, Path<Int>>()
//    private val pathToRow = mutableMapOf<Path<Int>, Int>()

    private var firstVisibleRow = 0
    private var lastVisibleRow  = 0
//        set(new) {
//            if (new == field) return
//
//            field = new
//
//            pathFromRow(field)?.let {
//                insert(children, it, field)
//            }
//        }

    init {
        monitorsDisplayRect = true
    }

    override fun handleDisplayRectEvent(event: DisplayRectEvent) {
        event.apply {
            firstVisibleRow = new.y.let { when {
                it > old.y -> findRowAt(it, firstVisibleRow, Down)
                it < old.y -> findRowAt(it, firstVisibleRow, Up  )
                else       -> firstVisibleRow
            }}

            lastVisibleRow = (new.y + new.height).let { when {
                it > old.y + old.height -> findRowAt(it, lastVisibleRow, Down)
                it < old.y + old.height -> findRowAt(it, lastVisibleRow, Up  )
                else                    -> lastVisibleRow
            }}
        }

        println("display rect changed: ${event.new}")

        println("first: $firstVisibleRow, last: $lastVisibleRow")
    }

    private enum class Direction {
        Up, Down
    }

    private fun findRowAt(y: Double, nearbyRow: Int, direction: Direction): Int {
        var index = nearbyRow

        itemPositioner?.let { positioner ->
            while (true) {
                pathFromRow(index)?.let { path ->
                    val bounds = positioner(this, this[path]!!, path, index, selected(index), false, path in expandedPaths).let {
                        it.at(y = it.y + insets.top)
                    }

                    when (direction) {
                        Up   -> if (index <= 0           || y >= bounds.y                ) return index else --index
                        else -> if (index >= numRows - 1 || y <= bounds.y + bounds.height) return index else ++index
                    }
                }
            }
        }

        return index
    }

    operator fun get(path: Path<Int>): T? = model[path]

    operator fun get(row: Int): T? = pathFromRow(row)?.let { model[it] }

    fun isLeaf(path: Path<Int>) = model.isLeaf(path)

    fun expanded(path: Path<Int>) = path in expandedPaths

    @JvmName("expandRows") fun expand(row : Int     ) = expand(setOf(row))
    @JvmName("expandRows") fun expand(rows: Set<Int>) = expand(rows.asSequence().map { pathFromRow(it) }.filterNotNull().toSet())

    fun expand(path: Path<Int>) = expand(setOf(path))

    fun expand(paths: Set<Path<Int>>) {
        val pathList = paths.asSequence().filter { it.depth > 0 && !expanded(it) }.sortedWith(PathComparator)

        var empty         = true
        val pathsToUpdate = mutableSetOf<Path<Int>>()

        children.batch {

            pathList.forEach {
                empty = false

                expandedPaths += it

                if (visible(it)) {
                    pathsToUpdate -= it

                    update        (this, it)
                    insertChildren(this, it)

                    var parent = it.parent
                    var child  = it

                    while (parent != null) {
                        pathsToUpdate += siblingsAfter(child, parent)
                        child  = parent
                        parent = parent.parent
                    }

                    pathsToUpdate += siblingsAfter(child, parent ?: Path())
                }
            }

//            println("pathsToUpdate: $pathsToUpdate")

            pathsToUpdate.forEach {
                updateRecursively(this, it)
            }
        }

        expandedPaths.addAll(paths)

        if (!empty) {
            (expanded as ExpansionObserversImpl)(pathList.toSet())
        }
    }

    private fun siblingsAfter(path: Path<Int>, parent: Path<Int>) = path.bottom?.let {
        (it + 1 until model.numChildren(parent)).map { parent + it }
    } ?: emptyList()

    override fun render(canvas: Canvas) {
        renderer?.render(this, canvas)
    }

    fun expandAll() {
        val pathsToExpand = HashSet<Path<Int>>()

        expandAllBelowPath(Path(), pathsToExpand)

        expand(pathsToExpand)
    }

    @JvmName("collapseRows") fun collapse(row : Int     ) = collapse(setOf(row))
    @JvmName("collapseRows") fun collapse(rows: Set<Int>) = collapse(rows.asSequence().map { pathFromRow(it) }.filterNotNull().toSet())

    fun collapse(path: Path<Int>) = collapse(setOf(path))

    fun collapse(paths: Set<Path<Int>>) {
        val pathList = paths.filterTo(mutableListOf()) { it.depth > 0 && expanded(it) }.apply { sortBy { it.bottom } }

        if (pathList.isNotEmpty()) {
            expandedPaths -= pathList

            children.batch {
                // TODO: Only insert paths with fully expanded ancestors (including those in this given set)
                pathList.first { visible(it) }.let {
                    val index   = update(this, it)
                    val numRows = numRows

                    (index until numRows).asSequence().mapNotNull { pathFromRow(it) }.forEach {
                        updateRecursively(this, it)
                    }

                    // Remove old children
                    (numRows until size).forEach {
                        removeAt(numRows)
//                        rowToPath.remove(it)?.let { pathToRow.remove(it) }
                    }
                }
            }

            (collapsed as ExpansionObserversImpl)(pathList.toSet())
        }
    }

    fun collapseAll() = collapse(expandedPaths)

    fun selected(row: Int) = pathFromRow(row)?.let { selected(it) } ?: false

    @JvmName("addSelectionRows"   ) fun addSelection   (rows: Set<Int>) = addSelection   (rows.asSequence().map { pathFromRow(it) }.filterNotNull().toSet())
    @JvmName("setSelectionRows"   ) fun setSelection   (rows: Set<Int>) = setSelection   (rows.asSequence().map { pathFromRow(it) }.filterNotNull().toSet())
    @JvmName("removeSelectionRows") fun removeSelection(rows: Set<Int>) = removeSelection(rows.asSequence().map { pathFromRow(it) }.filterNotNull().toSet())

    fun selected(path: Path<Int>) = selectionModel?.contains(path) ?: false

    fun addSelection(paths: Set<Path<Int>>) {
        selectionModel?.addAll(paths)
    }

    fun setSelection(paths: Set<Path<Int>>) {
        selectionModel?.replaceAll(paths)
    }

    fun removeSelection(paths: Set<Path<Int>>) {
        selectionModel?.removeAll(paths)
    }

    fun clearSelection() = selectionModel?.clear()

    fun visible(row: Int) = pathFromRow(row)?.let { visible(it) } ?: false

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

    private fun insertAll(children: MutableList<Gizmo>) {
        val root = Path<Int>()

        when (rootVisible) {
            true -> insert        (children, root)
            else -> insertChildren(children, root)
        }
    }

    private fun insertChildren(children: MutableList<Gizmo>, parent: Path<Int>, parentIndex: Int = rowFromPath(parent)): Int {
        var index = parentIndex + 1

        (0 until model.numChildren(parent)).forEach { index = insert(children, parent + it, index) }

        return index
    }

    private fun insert(children: MutableList<Gizmo>, path: Path<Int>, index: Int = rowFromPath(path)): Int {
        var result = index

        // Path index not found (could be invisible)
        if (index >= 0 /*index in firstVisibleRow .. lastVisibleRow*/) {
            itemUIGenerator?.let {
                model[path]?.let { value ->
//                    rowToPath[index] = path
//                    pathToRow[path ] = index

                    val expanded = path in expandedPaths

                    it(this, value, path, index, selected(path), false, expanded).also {
                        when {
                            index > children.lastIndex -> children.add(it)
                            else                       -> children.add(index /*- firstVisibleRow*/, it)
                        }
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

    private fun updateChildren(children: MutableList<Gizmo>, parent: Path<Int>, parentIndex: Int = rowFromPath(parent)): Int {
        var index = parentIndex + 1

        (0 until model.numChildren(parent)).forEach { index = updateRecursively(children, parent + it, index) }

        return index
    }

    private fun update(children: MutableList<Gizmo>, path: Path<Int>, index: Int = rowFromPath(path)): Int {
        var result = index

        // Path index not found (could be invisible)
        if (index >= 0) {
            itemUIGenerator?.let {
                model[path]?.let { value ->
//                    rowToPath[index] = path
//                    pathToRow[path ] = index

                    it(this, value, path, index, selected(path), false, path in expandedPaths).also {
                        children[index /*- firstVisibleRow*/] = it
                    }

                    ++result
                }
            }
        }

        return result
    }

    private fun updateRecursively(children: MutableList<Gizmo>, path: Path<Int>, index: Int = rowFromPath(path)): Int {
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

    private inner class InternalLayout(private val positioner: ItemPositioner<T>): Layout() {
        override fun layout(positionable: Positionable) {
            val insets = positionable.insets
            var y      = insets.top

            positionable.children.asSequence().filter { it.visible }.forEachIndexed { index, child ->
                this@Tree.pathFromRow(index)?.let { path ->
                    val bounds = positioner(this@Tree, this@Tree[path]!!, path, index, this@Tree.selected(index), child.hasFocus, path in expandedPaths)

                    child.bounds = Rectangle(insets.left, y, max(0.0, this@Tree.width - insets.run { left + right }), bounds.height)

                    y += child.height
                }
            }

            if (this@Tree.fitContent) {
                this@Tree.height = y + insets.bottom
            }
        }
    }
}
