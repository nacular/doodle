package com.nectar.doodle.utils

/**
 * Created by Nicholas Eddy on 4/11/18.
 */

actual open class TreeSet<E> actual constructor(comparator: Comparator<E>, elements: Collection<E>): Set<E> {
    actual constructor(comparator: Comparator<E>): this(comparator, emptyList<E>())

    protected val treeSet = java.util.TreeSet(comparator).also { it.addAll(elements) }

    actual override val size get   (                       ) = treeSet.size
    actual override fun isEmpty    (                       ) = treeSet.isEmpty()
    actual override fun contains   (element: E             ) = treeSet.contains(element)
    actual override fun containsAll(elements: Collection<E>) = treeSet.containsAll(elements)

    actual override fun iterator(): Iterator<E> = treeSet.iterator()

    actual companion object {
        actual operator fun <T: Comparable<T>> invoke(): TreeSet<T> = TreeSet(Comparator { a, b -> a.compareTo(b) })

        actual operator fun <T: Comparable<T>> invoke(elements: Collection<T>): TreeSet<T> = TreeSet(Comparator { a, b -> a.compareTo(b) }, elements)
    }
}

actual class MutableTreeSet<E> actual constructor(comparator: Comparator<E>, elements: Collection<E>): com.nectar.doodle.utils.TreeSet<E>(comparator, elements), MutableSet<E> {
    actual constructor(comparator: Comparator<E>): this(comparator, emptyList<E>())

    actual override fun iterator(): MutableIterator<E> = treeSet.iterator()

    actual override fun add      (element:  E            ) = treeSet.add      (element )
    actual override fun remove   (element:  E            ) = treeSet.remove   (element )
    actual override fun addAll   (elements: Collection<E>) = treeSet.addAll   (elements)
    actual override fun removeAll(elements: Collection<E>) = treeSet.removeAll(elements)
    actual override fun retainAll(elements: Collection<E>) = treeSet.retainAll(elements)

    actual override fun clear() = treeSet.clear()

    actual companion object {
        actual operator fun <T : Comparable<T>> invoke(): MutableTreeSet<T> = MutableTreeSet(Comparator { a, b -> a.compareTo(b) })

        actual operator fun <T : Comparable<T>> invoke(elements: Collection<T>): MutableTreeSet<T> = MutableTreeSet(Comparator { a, b -> a.compareTo(b) }, elements)
    }
}