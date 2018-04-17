package com.nectar.doodle.utils

import java.util.TreeSet

/**
 * Created by Nicholas Eddy on 4/11/18.
 */
//actual typealias TreeSet<E>        = java.util.TreeSet<E>
//actual typealias MutableTreeSet<E> = java.util.TreeSet<E>

actual open class TreeSet<E: Comparable<E>> actual constructor(elements: Collection<E>): Set<E> {
    actual constructor(): this(emptyList<E>())

    protected val treeSet = TreeSet(elements)

    actual override val size get   (                       ) = treeSet.size
    actual override fun isEmpty    (                       ) = treeSet.isEmpty()
    actual override fun contains   (element: E             ) = treeSet.contains(element)
    actual override fun containsAll(elements: Collection<E>) = treeSet.containsAll(elements)

    actual override fun iterator(): Iterator<E> = treeSet.iterator()
}

actual class MutableTreeSet<E: Comparable<E>> actual constructor(elements: Collection<E>): com.nectar.doodle.utils.TreeSet<E>(elements), MutableSet<E> {
    actual constructor(): this(emptyList<E>())

    actual override fun iterator(): MutableIterator<E> = treeSet.iterator()

    actual override fun add      (element:  E            ) = treeSet.add      (element )
    actual override fun remove   (element:  E            ) = treeSet.remove   (element )
    actual override fun addAll   (elements: Collection<E>) = treeSet.addAll   (elements)
    actual override fun removeAll(elements: Collection<E>) = treeSet.removeAll(elements)
    actual override fun retainAll(elements: Collection<E>) = treeSet.retainAll(elements)

    actual override fun clear() = treeSet.clear()
}