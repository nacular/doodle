package io.nacular.doodle.controls.tree

import io.nacular.doodle.utils.Path
import io.nacular.doodle.utils.Pool
import io.nacular.doodle.utils.SetPool

/**
 * Created by Nicholas Eddy on 3/23/18.
 */
public interface TreeModel<T> {
    public operator fun get(path: Path<Int>): T?

    public fun isEmpty   (): Boolean
    public fun isNotEmpty(): Boolean = !isEmpty()

    public fun children(parent: Path<Int>): Iterator<T>

    public fun isLeaf      (node  : Path<Int>            ): Boolean
    public fun child       (of    : Path<Int>, path : Int): T?
    public fun numChildren (of    : Path<Int>            ): Int
    public fun indexOfChild(parent: Path<Int>, child: T  ): Int
}

public typealias ModelObserver<T> = (source: MutableTreeModel<T>, removed: Map<Path<Int>, T>, added: Map<Path<Int>, T>, moved: Map<Path<Int>, Pair<Path<Int>, T>>) -> Unit

public interface MutableTreeModel<T>: TreeModel<T> {
    public operator fun set(path: Path<Int>, value: T): T?

    public fun add        (path : Path<Int>, values: T            )
    public fun removeAt   (path : Path<Int>                       ): T?
    public fun addAll     (path : Path<Int>, values: Collection<T>)
    public fun removeAllAt(paths: Collection<Path<Int>>           )

    public fun clear()

    public val changed: Pool<ModelObserver<T>>
}

public open class TreeNode<T>(public open val value: T, public open val children: List<TreeNode<T>> = emptyList()) {
    public operator fun get(index: Int): TreeNode<T> = children[index]
}

public class MutableTreeNode<T>(override var value: T, override var children: List<MutableTreeNode<T>> = emptyList()): TreeNode<T>(value, children)

public open class SimpleTreeModel<T, N: TreeNode<T>>(protected val root: N): TreeModel<T> {

    override fun get(path: Path<Int>): T? = node(path)?.value

    private fun node(path: Path<Int>): TreeNode<T>? {
        var node = root as TreeNode<T>?

        path.forEach {
            node = node?.children?.getOrNull(it)

            if (node == null) return@forEach
        }

        return node
    }

    override fun isEmpty(): Boolean = false

    override fun children(parent: Path<Int>): Iterator<T> = (node(parent)?.children?.asSequence() ?: emptySequence()).map { it.value }.iterator()

    override fun isLeaf(node: Path<Int>): Boolean = node(node)?.children?.isEmpty() ?: true

    override fun child(of: Path<Int>, path: Int): T? = node(of)?.children?.get(path)?.value

    override fun numChildren(of: Path<Int>): Int = node(of)?.children?.size ?: -1

    override fun indexOfChild(parent: Path<Int>, child: T): Int = node(parent)?.children?.map { it.value }?.indexOf(child) ?: -1
}


public class SimpleMutableTreeModel<T>(root: MutableTreeNode<T>): SimpleTreeModel<T, MutableTreeNode<T>>(root), MutableTreeModel<T> {
    override operator fun set(path: Path<Int>, value: T): T? {
        var node = root as MutableTreeNode?

        path.forEach {
            node = node?.children?.getOrNull(it)
        }

        val previous = node?.value

        if (previous != value) {
            node?.value = value

            (changed as SetPool).forEach {
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

    override val changed: Pool<ModelObserver<T>> = SetPool<ModelObserver<T>>()
}
