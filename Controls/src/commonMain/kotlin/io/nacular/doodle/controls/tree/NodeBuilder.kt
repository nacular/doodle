package io.nacular.doodle.controls.tree

/**
 * Created by Nicholas Eddy on 3/27/18.
 */

public class NodeBuilder<T>(public var value: T, public val children: MutableList<NodeBuilder<T>> = mutableListOf()) {
    public fun treeNode(): TreeNode<T> = TreeNode(value, children.map { it.treeNode() })

    public fun node(value: T, block: NodeBuilder<T>.() -> Unit = {}): NodeBuilder<T> {
        val node = NodeBuilder(value).also { block(it) }

        children.add(node)

        return node
    }
}

public class MutableNodeBuilder<T>(public var value: T, public val children: MutableList<MutableNodeBuilder<T>> = mutableListOf()) {
    public fun mutableTreeNode(): MutableTreeNode<T> = MutableTreeNode(value, children.map { it.mutableTreeNode() }.toMutableList())

    public fun node(value: T, block: MutableNodeBuilder<T>.() -> Unit = {}): MutableNodeBuilder<T> {
        val node = MutableNodeBuilder(value).also { block(it) }

        children.add(node)

        return node
    }
}

public fun <T> rootNode(value: T, block: NodeBuilder<T>.() -> Unit = {}): TreeNode<T> = NodeBuilder(value).also { block(it) }.treeNode()

public fun <T> mutableRootNode(value: T, block: MutableNodeBuilder<T>.() -> Unit = {}): MutableTreeNode<T> = MutableNodeBuilder(value).also { block(it) }.mutableTreeNode()