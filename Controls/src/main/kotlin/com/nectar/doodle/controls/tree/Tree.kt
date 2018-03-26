package com.nectar.doodle.controls.tree

import com.nectar.doodle.controls.SelectionModel
import com.nectar.doodle.controls.theme.ItemPositioner
import com.nectar.doodle.controls.theme.ItemUIGenerator
import com.nectar.doodle.controls.theme.TreeUI
import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.core.Layout
import com.nectar.doodle.core.Positionable
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.geometry.Rectangle
import kotlin.math.max


/**
 * Created by Nicholas Eddy on 3/23/18.
 */

private class Property<T>(var value: T)

typealias ExpansionObserver<T> = (source: Tree<T>, paths: Set<Path<Int>>) -> Unit

interface ExpansionObservers<T> {
    operator fun plusAssign (observer: ExpansionObserver<T>)
    operator fun minusAssign(observer: ExpansionObserver<T>)
}

private class ExpansionObserversImpl<T>(
        private val source: Tree<T>,
        private val mutableSet: MutableSet<ExpansionObserver<T>> = mutableSetOf()): Set<ExpansionObserver<T>> by mutableSet, ExpansionObservers<T> {
    override fun plusAssign(observer: ExpansionObserver<T>) {
        mutableSet += observer
    }

    override fun minusAssign(observer: ExpansionObserver<T>) {
        mutableSet -= observer
    }

    operator fun invoke(paths: Set<Path<Int>>) = mutableSet.forEach { it(source, paths) }
}


class Tree<T>(val model: Model<T>, val selectionModel: SelectionModel<Path<Int>>, private val fitContent: Boolean = true): Gizmo() {
    private val expandedPaths = mutableSetOf<Path<Int>>()

    var rootVisible = false

    public override var insets
        get(   ) = super.insets
        set(new) { super.insets = new }

    private var itemUIGenerator: ItemUIGenerator<T>? = null

    var renderer: TreeUI<T>? = null
        set(new) {
            if (new == renderer) { return }

            field = new?.also {
                itemUIGenerator = it.uiGenerator

                children.clear()

                val root = Path<Int>()

                when (rootVisible) {
                    true -> process        (root)
                    else -> processChildren(root)
                }

                layout = InternalLayout(it.positioner)
            }
        }

    val onExpanded : ExpansionObservers<T> by lazy { ExpansionObserversImpl(this) }
    val onCollapsed: ExpansionObservers<T> by lazy { ExpansionObserversImpl(this) }

    operator fun get(path: Path<Int>) = model[path]

    fun isLeaf(path: Path<Int>) = model.isLeaf(path)

    fun expanded(path: Path<Int>) = path in expandedPaths

    fun expand(index  : Int     ) = expand(setOf(index))
    fun expand(indices: Set<Int>) = expand(indices.asSequence().map { pathFromRow(it) }.filterNotNull().toSet())

    fun expand(path: Path<Int>) = expand(setOf(path))

    fun expand(paths: Set<Path<Int>>) {
        val pathSet = paths.filterTo(HashSet()) { it.depth > 0 }

        if (pathSet.isNotEmpty()) {
            expandedPaths +=  pathSet

            // TODO: Only process paths with fully expanded ancestors (including those in this given set)
//            pathSet.forEach { processChildren(it) }

            processChildren(Path())

            (onExpanded as ExpansionObserversImpl)(pathSet)
        }
    }

    override fun render(canvas: Canvas) {
        renderer?.render(this, canvas)
    }

    private fun processChildren(parent: Path<Int>, parentIndex: Int = rowFromPath(parent)): Int {
        var index = parentIndex + 1

        (0 until model.numChildren(parent)).forEach { index = process(parent.getDecendant(it), index) }

        return index
    }

    private fun process(path: Path<Int>, index: Int = rowFromPath(path)): Int {
        var result = index

        itemUIGenerator?.let {
            model[path]?.let { value ->
                it(this, value, path, index, path in selectionModel, false, path in expandedPaths).also {
                    when {
                        index > children.lastIndex -> children.add(it)
                        else                       -> children[index] = it
                    }
                }

                if (path.depth == 0 || path in expandedPaths) {
                    result += processChildren(path, index)
                }
            }
        }

        return result + 1
    }

    fun expandAll() {
        val pathsToExpand = HashSet<Path<Int>>()

        expandAllBelowPath(Path(), pathsToExpand)

        expand(pathsToExpand)
    }

    fun collapse(index  : Int     ) = collapse(setOf(index))
    fun collapse(indices: Set<Int>) = collapse(indices.asSequence().map { pathFromRow(it) }.filterNotNull().toSet())

    fun collapse(path: Path<Int>) = collapse(setOf(path))

    fun collapse(paths: Set<Path<Int>>) {
        val pathSet = paths.filterTo(HashSet()) { it.depth > 0 }

        if (pathSet.isNotEmpty()) {
            expandedPaths -= pathSet

            // TODO: Handle properly
//            pathSet.forEach { processChildren(it) }

            // FIXME: HACK!!
            children.clear()
            processChildren(Path())

            (onCollapsed as ExpansionObserversImpl)(pathSet)
        }
    }

    fun collapseAll() = collapse(expandedPaths)

    fun selected(row: Int) = pathFromRow(row)?.let { selected(it) } ?: false

    fun addSelection   (rows: Set<Int>) = addSelection   (rows.asSequence().map { pathFromRow(it) }.filterNotNull().toSet())
    fun setSelection   (rows: Set<Int>) = setSelection   (rows.asSequence().map { pathFromRow(it) }.filterNotNull().toSet())
    fun removeSelection(rows: Set<Int>) = removeSelection(rows.asSequence().map { pathFromRow(it) }.filterNotNull().toSet())

    fun selected(path: Path<Int>) = path in selectionModel

    fun addSelection(paths: Set<Path<Int>>) {
        selectionModel.addAll(paths)
    }

    fun setSelection(paths: Set<Path<Int>>) {
        selectionModel.replaceAll(paths)
    }

    fun removeSelection(paths: Set<Path<Int>>) {
        selectionModel.removeAll(paths)
    }

    fun clearSelection() = selectionModel.clear()

    val numRows = rowsBelow(Path()) + if(rootVisible) 1 else 0

    fun visible(row: Int) = pathFromRow(row)?.let { visible(it) } ?: false

    fun visible(path: Path<Int>): Boolean {
        var parent: Path<Int>? = path

        while (parent != null) {
            if (parent.depth == 0) {
                return rootVisible
            }

            if (parent.parent?.let { pathExpanded(it) } != true) {
                return false
            }

            parent = parent.parent
        }

        return false
    }

//    @Throws(ExpansionVetoException::class)
    fun makeVisible(path: Path<Int>) {
        var parent = path.parent

        while (parent != null) {
            expand(parent)

            parent = parent.parent
        }
    }

    private fun rowExpanded(index: Int) = pathFromRow(index)?.let { pathExpanded(it) } ?: false

    private fun pathExpanded(path: Path<Int>) = path.depth == 0 || path in expandedPaths

    private fun pathFromRow(index: Int): Path<Int>? {

        if (model.isEmpty()) {
            return null
        }

        val newIndex = index + if (!rootVisible) 1 else 0

        val path   = Path<Int>()
        return addRowsToPath(path, Property(newIndex))
    }

    private fun rowFromPath(path: Path<Int>): Int {
        var row       = -1
        var pathIndex = 0

        if (rootVisible) {
            row++
        }

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

                    currentPath = currentPath.getDecendant(i)
                    numChildren = model.numChildren(currentPath)
                    i           = -1
                } else {
                    row += rowsBelow(currentPath.getDecendant(i))
                }
                ++i
            }

            row = -1
        }

        return row
    }

    private fun rowsBelow(path: Path<Int>): Int {
        var numRows = 0

        if (pathExpanded(path) && visible(path)) {
            val numChildren = model.numChildren(path)

            (0 until numChildren).map { it.let{ path.getDecendant(it) } }.forEach { numRows += rowsBelow(it) + 1 }
        }

        return numRows
    }

    private fun expandAllBelowPath(path: Path<Int>, expandedPath: MutableSet<Path<Int>> = mutableSetOf()) {
        if (model.isLeaf(path)) {
            return
        }

        val numChildren = model.numChildren(path)

        for (i in 0 until numChildren) {
            path.getDecendant(i).let { child ->
                if (!model.isLeaf(child)) {
                    if (child !in expandedPaths) {
                        expandedPath.add(child)
                    }

                    expandAllBelowPath(child, expandedPath)
                }
            }
        }
    }

    private fun addRowsToPath(path: Path<Int>, index: Property<Int>): Path<Int> {
        if (index.value <= 0) {
            return path
        }

        var newPath     = path
        val numChildren = model.numChildren(path)

        for (i in 0 until numChildren) {
            index.value = index.value - 1

            if (index.value == 0) {
                break
            }

            newPath = path.getDecendant(i)

            if (pathExpanded(newPath)) {
                newPath = addRowsToPath(newPath, index)

                if (index.value == 0) {
                    break
                }
            }
        }

        return newPath
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
