package com.nectar.doodle.controls.tree

/**
 * Created by Nicholas Eddy on 3/23/18.
 */
interface Model<T> {
    operator fun get(path: Path<Int>): T?

    fun isEmpty   (): Boolean
    fun isNotEmpty() = !isEmpty()

    fun children(parent: Path<Int>): Iterator<T>

    fun isLeaf      (node  : Path<Int>            ): Boolean
    fun child       (of    : Path<Int>, index: Int): T?
    fun numChildren (of    : Path<Int>            ): Int
    fun indexOfChild(parent: Path<Int>, child: T  ): Int
}

class TreeNode<T>(val value: T, val children: List<TreeNode<T>> = emptyList())

class SimpleModel<T>(private val root: TreeNode<T>): Model<T> {

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

    override fun child(of: Path<Int>, index: Int) = node(of)?.children?.get(index)?.value

    override fun numChildren(of: Path<Int>) = node(of)?.children?.size ?: -1

    override fun indexOfChild(parent: Path<Int>, child: T) = node(parent)?.children?.map { it.value }?.indexOf(child) ?: -1
}
