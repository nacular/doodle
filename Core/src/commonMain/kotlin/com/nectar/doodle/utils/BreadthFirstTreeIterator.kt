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

    private var history = sequenceOf(root)

    override fun hasNext() = !history.none()

    override fun next(): T {
        if (!hasNext()) {
            throw NoSuchElementException("The tree has no more elements")
        }

        val node = history.first()

        history = history.drop(1) + node.children

        return node.value
    }
}