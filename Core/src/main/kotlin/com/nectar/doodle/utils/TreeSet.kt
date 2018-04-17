package com.nectar.doodle.utils

/**
 * Created by Nicholas Eddy on 4/11/18.
 */
expect class TreeSet<E: Comparable<E>>: Set<E> {
    constructor()
    constructor(elements: Collection<E>)

    // From Set
    override val size: Int
    override fun isEmpty(): Boolean
    override fun contains(element: E): Boolean
    override fun containsAll(elements: Collection<E>): Boolean
    override fun iterator(): Iterator<E>
}

expect class MutableTreeSet<E: Comparable<E>>: MutableSet<E> {
    constructor()
    constructor(elements: Collection<E>)

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
}