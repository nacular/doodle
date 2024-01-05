package io.nacular.doodle.utils

/**
 * Very basic Set that creates a copy whenever its contents are modified, and returns an [Iterator]
 * that points to the latest snapshot of the set. As a result, the iterator does not support mutations.
 * This allows it to be used in cases where [ConcurrentModificationException]s are a consideration.
 *
 * NOTE: This works best in cases where the set size is small and the number of iterations is
 * much larger than the number of edits.
 */
internal class CopyOnWriteSet<T>(delegate: MutableSet<T>): MutableSet<T> {
    private var current: MutableSet<T> = delegate

    override val size: Int get() = current.size

    override fun isEmpty    (                       ): Boolean = current.isEmpty()
    override fun containsAll(elements: Collection<T>): Boolean = current.containsAll(elements)
    override fun contains   (element : T            ): Boolean = current.contains(element)
    override fun add        (element : T            ): Boolean = copy { add      (element ) }
    override fun addAll     (elements: Collection<T>): Boolean = copy { addAll   (elements) }
    override fun remove     (element : T            ): Boolean = copy { remove   (element ) }
    override fun removeAll  (elements: Collection<T>): Boolean = copy { removeAll(elements) }
    override fun retainAll  (elements: Collection<T>): Boolean = copy { retainAll(elements) }
    override fun clear      (                       )          = copy { clear    (        ) }

    override fun iterator(): MutableIterator<T> = object: MutableIterator<T> {
        private val underlyingIterator = current.iterator()

        override fun hasNext(): Boolean = underlyingIterator.hasNext()
        override fun next   (): T       = underlyingIterator.next()

        override fun remove() {
            throw UnsupportedOperationException()
        }
    }

    private fun <R> copy(then: MutableSet<T>.() -> R): R {
        current = current.toMutableSet()
        return then(current)
    }
}