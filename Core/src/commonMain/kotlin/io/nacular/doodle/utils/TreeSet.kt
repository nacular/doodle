package io.nacular.doodle.utils

/**
 * Created by Nicholas Eddy on 4/11/18.
 */
expect class TreeSet<E>: Set<E> {
    constructor(comparator: Comparator<E>)
    constructor(comparator: Comparator<E>, elements: Collection<E>)

    // From Set
    override val size: Int
    override fun isEmpty(): Boolean
    override fun contains(element: E): Boolean
    override fun containsAll(elements: Collection<E>): Boolean
    override fun iterator(): Iterator<E>

    companion object {
        operator fun <T: Comparable<T>> invoke(): TreeSet<T>
        operator fun <T: Comparable<T>> invoke(elements: Collection<T>): TreeSet<T>
    }
}

expect class MutableTreeSet<E>: MutableSet<E> {
    constructor(comparator: Comparator<E>)
    constructor(comparator: Comparator<E>, elements: Collection<E>)

    // From Set
    override val size: Int
    override fun isEmpty(): Boolean
    override fun contains(element: E): Boolean
    override fun containsAll(elements: Collection<E>): Boolean

    // From MutableSet
    override fun iterator(): MutableIterator<E>
    override fun add(element: E): Boolean
    override fun remove(element: E): Boolean
    override fun addAll(elements: Collection<E>): Boolean
    override fun removeAll(elements: Collection<E>): Boolean
    override fun retainAll(elements: Collection<E>): Boolean
    override fun clear()

    companion object {
        operator fun <T: Comparable<T>> invoke(): MutableTreeSet<T>
        operator fun <T: Comparable<T>> invoke(elements: Collection<T>): MutableTreeSet<T>
    }
}