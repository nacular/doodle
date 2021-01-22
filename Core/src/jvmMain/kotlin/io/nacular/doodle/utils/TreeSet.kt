package io.nacular.doodle.utils

/**
 * Created by Nicholas Eddy on 4/11/18.
 */

public actual open class TreeSet<E> actual constructor(comparator: Comparator<E>, elements: Collection<E>): Set<E> {
    public actual constructor(comparator: Comparator<E>): this(comparator, emptyList<E>())

    protected val treeSet: java.util.TreeSet<E> = java.util.TreeSet(comparator).also { it.addAll(elements) }

    actual override val size: Int get(                       ) = treeSet.size
    actual override fun isEmpty      (                       ): Boolean = treeSet.isEmpty    (        )
    actual override fun contains     (element: E             ): Boolean = treeSet.contains   (element )
    actual override fun containsAll  (elements: Collection<E>): Boolean = treeSet.containsAll(elements)

    actual override fun iterator(): Iterator<E> = treeSet.iterator()

    public actual companion object {
        public actual operator fun <T: Comparable<T>> invoke(): TreeSet<T> = TreeSet(Comparator { a, b -> a.compareTo(b) })

        public actual operator fun <T: Comparable<T>> invoke(elements: Collection<T>): TreeSet<T> = TreeSet(Comparator { a, b -> a.compareTo(b) }, elements)
    }
}

public actual class MutableTreeSet<E> actual constructor(comparator: Comparator<E>, elements: Collection<E>): io.nacular.doodle.utils.TreeSet<E>(comparator, elements), MutableSet<E> {
    public actual constructor(comparator: Comparator<E>): this(comparator, emptyList<E>())

    actual override fun iterator(): MutableIterator<E> = treeSet.iterator()

    actual override fun add      (element:  E            ): Boolean = treeSet.add      (element )
    actual override fun remove   (element:  E            ): Boolean = treeSet.remove   (element )
    actual override fun addAll   (elements: Collection<E>): Boolean = treeSet.addAll   (elements)
    actual override fun removeAll(elements: Collection<E>): Boolean = treeSet.removeAll(elements)
    actual override fun retainAll(elements: Collection<E>): Boolean = treeSet.retainAll(elements)

    actual override fun clear(): Unit = treeSet.clear()

    public actual companion object {
        public actual operator fun <T : Comparable<T>> invoke(): MutableTreeSet<T> = MutableTreeSet(Comparator { a, b -> a.compareTo(b) })

        public actual operator fun <T : Comparable<T>> invoke(elements: Collection<T>): MutableTreeSet<T> = MutableTreeSet(Comparator { a, b -> a.compareTo(b) }, elements)
    }
}