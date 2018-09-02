package com.nectar.doodle.controls.tree

/**
 * Created by Nicholas Eddy on 3/27/18.
 */

class NodeBuilder<T>(var value: T, val children: MutableList<NodeBuilder<T>> = mutableListOf()) {
    fun treeNode(): TreeNode<T> = TreeNode(value, children.map { it.treeNode() })

    fun node(value: T, block: NodeBuilder<T>.() -> Unit = {}): NodeBuilder<T> {
        val node = NodeBuilder(value).also { block(it) }

        children.add(node)

        return node
    }
}

fun <T> node(value: T, block: NodeBuilder<T>.() -> Unit = {}) = NodeBuilder(value).also { block(it) }.treeNode()