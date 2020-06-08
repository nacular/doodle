package io.nacular.doodle.controls.tree

import io.nacular.doodle.utils.Path
import io.nacular.doodle.utils.Pool
import io.nacular.doodle.utils.SetPool

/**
 * Created by Nicholas Eddy on 3/23/18.
 */
interface TreeModel<T> {
    operator fun get(path: Path<Int>): T?

    fun isEmpty   (): Boolean
    fun isNotEmpty() = !isEmpty()

    fun children(parent: Path<Int>): Iterator<T>

    fun isLeaf      (node  : Path<Int>            ): Boolean
    fun child       (of    : Path<Int>, path: Int): T?
    fun numChildren (of    : Path<Int>            ): Int
    fun indexOfChild(parent: Path<Int>, child: T  ): Int
}

typealias ModelObserver<T> = (source: MutableTreeModel<T>, removed: Map<Path<Int>, T>, added: Map<Path<Int>, T>, moved: Map<Path<Int>, Pair<Path<Int>, T>>) -> Unit

interface MutableTreeModel<T>: TreeModel<T> {
    operator fun set(path: Path<Int>, value: T): T?

    fun add        (path : Path<Int>, values: T            )
    fun removeAt   (path : Path<Int>                       ): T?
    fun addAll     (path : Path<Int>, values: Collection<T>)
    fun removeAllAt(paths: Collection<Path<Int>>           )

    fun clear()

    val changed: Pool<ModelObserver<T>>
}

open class TreeNode<T>(open val value: T, open val children: List<TreeNode<T>> = emptyList()) {
    operator fun get(index: Int) = children[index]
}

class MutableTreeNode<T>(override var value: T, override var children: List<MutableTreeNode<T>> = emptyList()): TreeNode<T>(value, children)

open class SimpleTreeModel<T, N: TreeNode<T>>(protected val root: N): TreeModel<T> {

    override fun get(path: Path<Int>) = node(path)?.value

    private fun node(path: Path<Int>): TreeNode<T>? {
        var node = root as TreeNode<T>?

        path.forEach {
            node = node?.children?.getOrNull(it)

            if (node == null) return@forEach
        }

        return node
    }

    override fun isEmpty() = false

    override fun children(parent: Path<Int>) = (node(parent)?.children?.asSequence() ?: emptySequence()).map { it.value }.iterator()

    override fun isLeaf(node: Path<Int>) = node(node)?.children?.isEmpty() ?: false

    override fun child(of: Path<Int>, path: Int) = node(of)?.children?.get(path)?.value

    override fun numChildren(of: Path<Int>) = node(of)?.children?.size ?: -1

    override fun indexOfChild(parent: Path<Int>, child: T) = node(parent)?.children?.map { it.value }?.indexOf(child) ?: -1
}


class SimpleMutableTreeModel<T>(root: MutableTreeNode<T>): SimpleTreeModel<T, MutableTreeNode<T>>(root), MutableTreeModel<T> {
    override operator fun set(path: Path<Int>, value: T): T? {
        var node = root as MutableTreeNode?

        path.forEach {
            node = node?.children?.getOrNull(it)
        }

        val previous = node?.value

        if (previous != value) {
            node?.value = value

            changed.forEach {
                it(this, previous?.let { mapOf(path to it) } ?: emptyMap(), mapOf(path to value), emptyMap())
            }
        }

        return previous
    }

    override fun add        (path : Path<Int>, values: T            ) {}
    override fun removeAt   (path : Path<Int>                       ): T? { return null }
    override fun addAll     (path : Path<Int>, values: Collection<T>) {}
    override fun removeAllAt(paths: Collection<Path<Int>>           ) {}

    override fun clear() {}

    override val changed = SetPool<ModelObserver<T>>()
}
