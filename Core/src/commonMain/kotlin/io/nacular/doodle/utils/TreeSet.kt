package io.nacular.doodle.utils

import io.nacular.doodle.core.Internal

/**
 * Created by Nicholas Eddy on 4/11/18.
 */
/** @suppress */
@Internal
public expect class TreeSet<E>: Set<E> {
    public constructor(comparator: Comparator<E>                         )
    public constructor(comparator: Comparator<E>, elements: Collection<E>)

    // From Set
    override val size: Int
    override fun isEmpty    (                       ): Boolean
    override fun contains   (element : E            ): Boolean
    override fun containsAll(elements: Collection<E>): Boolean
    override fun iterator   (                       ): Iterator<E>

    public companion object {
        public operator fun <T: Comparable<T>> invoke(                       ): TreeSet<T>
        public operator fun <T: Comparable<T>> invoke(elements: Collection<T>): TreeSet<T>
    }
}

/** @suppress */
@Internal
public expect class MutableTreeSet<E>: MutableSet<E> {
    public constructor(comparator: Comparator<E>                         )
    public constructor(comparator: Comparator<E>, elements: Collection<E>)

    // From Set
    override val size: Int
    override fun isEmpty    (                       ): Boolean
    override fun contains   (element : E            ): Boolean
    override fun containsAll(elements: Collection<E>): Boolean

    // From MutableSet
    override fun iterator(): MutableIterator<E>

    override fun add      (element : E            ): Boolean
    override fun remove   (element : E            ): Boolean
    override fun addAll   (elements: Collection<E>): Boolean
    override fun removeAll(elements: Collection<E>): Boolean
    override fun retainAll(elements: Collection<E>): Boolean
    override fun clear    (                       )

    public companion object {
        public operator fun <T: Comparable<T>> invoke(                       ): MutableTreeSet<T>
        public operator fun <T: Comparable<T>> invoke(elements: Collection<T>): MutableTreeSet<T>
    }
}