package io.nacular.doodle.controls.tree

import io.nacular.doodle.utils.Path
import io.nacular.doodle.utils.Pool
import io.nacular.doodle.utils.SetPool
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success

/**
 * Created by Nicholas Eddy on 3/23/18.
 */
public interface TreeModel<T> {
    public operator fun get(path: Path<Int>): Result<T>

    public fun isEmpty   (): Boolean
    public fun isNotEmpty(): Boolean = !isEmpty()

    public fun children(parent: Path<Int>): Iterator<T>

    public fun isLeaf      (node  : Path<Int>            ): Boolean
    public fun child       (of    : Path<Int>, path : Int): Result<T>
    public fun numChildren (of    : Path<Int>            ): Int
    public fun indexOfChild(parent: Path<Int>, child: T  ): Int
}

public typealias ModelObserver<T> = (source: DynamicTreeModel<T>, removed: Map<Path<Int>, T>, added: Map<Path<Int>, T>, moved: Map<Path<Int>, Pair<Path<Int>, T>>) -> Unit

public interface DynamicTreeModel<T>: TreeModel<T> {
    public val changed: Pool<ModelObserver<T>>
}

public interface MutableTreeModel<T>: DynamicTreeModel<T> {
    public operator fun set(path: Path<Int>, value: T): Result<T>

    public fun add        (path : Path<Int>, values: T            )
    public fun removeAt   (path : Path<Int>                       ): Result<T>
    public fun addAll     (path : Path<Int>, values: Collection<T>)
    public fun removeAllAt(paths: Collection<Path<Int>>           )

    public fun clear()
}

public open class TreeNode<T>(public open val value: T, public open val children: List<TreeNode<T>> = emptyList()) {
    public operator fun get(index: Int): TreeNode<T> = children[index]
}

public class MutableTreeNode<T>(override var value: T, override var children: List<MutableTreeNode<T>> = emptyList()): TreeNode<T>(value, children)

public open class SimpleTreeModel<T, N: TreeNode<T>>(protected val root: N): TreeModel<T> {

    override fun get(path: Path<Int>): Result<T> = node(path).map { it.value }

    private fun node(path: Path<Int>): Result<TreeNode<T>> {
        var node = success(root) as Result<TreeNode<T>>

        path.forEach { index ->
            node = node.mapCatching { it.children[index] }

//            node = node.onSuccess { it.children.getOrNull(index)?.let { success(it) } ?: failure(IllegalArgumentException()) }

            if (node.isFailure) return@forEach
        }

        return node
    }

    override fun isEmpty(): Boolean = false

    override fun children(parent: Path<Int>): Iterator<T> = (node(parent).getOrNull()?.children?.asSequence() ?: emptySequence()).map { it.value }.iterator()

    override fun isLeaf(node: Path<Int>): Boolean = node(node).getOrNull()?.children?.isEmpty() ?: true

    override fun child(of: Path<Int>, path: Int): Result<T> = node(of).mapCatching { it.children[path].value }

    override fun numChildren(of: Path<Int>): Int = node(of).getOrNull()?.children?.size ?: -1

    override fun indexOfChild(parent: Path<Int>, child: T): Int = node(parent).getOrNull()?.children?.map { it.value }?.indexOf(child) ?: -1
}

public class SimpleMutableTreeModel<T>(root: MutableTreeNode<T>): SimpleTreeModel<T, MutableTreeNode<T>>(root), MutableTreeModel<T> {
    override operator fun set(path: Path<Int>, value: T): Result<T> {
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

        return previous?.let { success(it) } ?: failure(IllegalArgumentException())
    }

    override fun add        (path : Path<Int>, values: T            ) {}
    override fun removeAt   (path : Path<Int>                       ): Result<T> = failure(UnsupportedOperationException())
    override fun addAll     (path : Path<Int>, values: Collection<T>) {}
    override fun removeAllAt(paths: Collection<Path<Int>>           ) {}

    override fun clear() {}

    override val changed: Pool<ModelObserver<T>> = SetPool<ModelObserver<T>>()
}
