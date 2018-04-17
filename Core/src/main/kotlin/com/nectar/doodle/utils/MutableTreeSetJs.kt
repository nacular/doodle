package com.nectar.doodle.utils

class MutableTreeSetJs<E: Comparable<E>> constructor(elements: Collection<E>): TreeSetJs<E>(elements), MutableSet<E> {
    constructor(): this(emptyList<E>())

    override fun iterator(): MutableIterator<E> = BstIterator()

    override fun add(element: E): Boolean = super.add(element)

    override fun remove(element: E): Boolean = super.remove_(element)

    override fun addAll(elements: Collection<E>) = elements.all { add(it) }

    override fun removeAll(elements: Collection<E>) = elements.all { remove(it) }

    override fun retainAll(elements: Collection<E>): Boolean {
        val set    = elements.toSet()
        var result = false

        asSequence().filterNot { it in set }.forEach { result = remove(it) }

        return result
    }

    override fun clear() { root = null }
}