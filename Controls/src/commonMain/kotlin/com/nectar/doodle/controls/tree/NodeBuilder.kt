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

class MutableNodeBuilder<T>(var value: T, val children: MutableList<MutableNodeBuilder<T>> = mutableListOf()) {
    fun mutableTreeNode(): MutableTreeNode<T> = MutableTreeNode(value, children.map { it.mutableTreeNode() })

    fun node(value: T, block: MutableNodeBuilder<T>.() -> Unit = {}): MutableNodeBuilder<T> {
        val node = MutableNodeBuilder(value).also { block(it) }

        children.add(node)

        return node
    }
}

fun <T> rootNode(value: T, block: NodeBuilder<T>.() -> Unit = {}) = NodeBuilder(value).also { block(it) }.treeNode()

fun <T> mutableRootNode(value: T, block: MutableNodeBuilder<T>.() -> Unit = {}) = MutableNodeBuilder(value).also { block(it) }.mutableTreeNode()