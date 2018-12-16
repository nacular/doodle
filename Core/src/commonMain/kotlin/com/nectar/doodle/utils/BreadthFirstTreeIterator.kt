/**
 * Created by Nicholas Eddy on 1/24/18.
 */
package com.nectar.doodle.utils

/**
 * A node within a tree.
 */
interface Node<T> {
    val value   : T
    val children: Sequence<Node<T>>
}

/**
 * Iterator that traverses the nodes of a tree in breadth-first order.
 */
class BreadthFirstTreeIterator<out T>(root: Node<T>): Iterator<T> {

    private val queue = mutableListOf(root)

    override fun hasNext() = queue.isNotEmpty()

    override fun next(): T {
        if (!hasNext()) {
            throw NoSuchElementException("The tree has no more elements")
        }

        return queue.removeAt(0).also {
            queue.addAll(it.children)
        }.value
    }
}