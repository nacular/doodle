/**
 * Created by Nicholas Eddy on 1/24/18.
 */
package io.nacular.doodle.utils

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

    private var previous = null as Node<T>?
    private val queue    = mutableListOf(root)

    override fun hasNext() = queue.isNotEmpty() || previous?.children?.none() == false

    override fun next(): T {
        if (!hasNext()) {
            throw NoSuchElementException("The tree has no more elements")
        }

        // This is a bit of a hack to delay processing the children of an item as long as possible.
        // Ideally, this would all be done w/ Sequences, but the performance of this is really bad for ~2-3 hundred items some reason
        previous?.let {
            queue.addAll(it.children)
        }

        return queue.removeAt(0).also {
            previous = it
//            queue.addAll(it.children)
        }.value
    }

//    private var history = sequenceOf(root)
//
//    override fun hasNext() = !history.none()
//
//    override fun next(): T {
//        if (!hasNext()) {
//            throw NoSuchElementException("The tree has no more elements")
//        }
//
//        val node = history.first()
//
//        history = history.drop(1) + node.children
//
//        return node.value
//    }
}