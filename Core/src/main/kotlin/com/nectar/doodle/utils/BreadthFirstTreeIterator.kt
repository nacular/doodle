/**
 * Created by Nicholas Eddy on 1/24/18.
 */
package com.nectar.doodle.utils

/**
 * A node within a tree.
 */
interface Node<T> {
    val value   : T
    val children: List<Node<T>>
}

/**
 * Iterator that traverses the nodes of a tree in breadth-first order.
 */
class BreadthFirstTreeIterator<out T>(root: Node<T>): Iterator<T> {

    private val history = ArrayList<Node<T>>()

    init {
        history.add(root)
    }

    override fun hasNext() = history.isNotEmpty()

    override fun next(): T {
        if (!hasNext()) {
            throw NoSuchElementException("The tree has no more elements")
        }

        val node = history.removeAt(history.lastIndex)

        history.addAll(node.children.reversed())

        return node.value
    }
}