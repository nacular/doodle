package io.nacular.doodle.utils

/**
 * Created by Nicholas Eddy on 7/22/21.
 */
/** @suppress */
public actual open class SetPool<T> private actual constructor(private val delegate: MutableSet<T>): Pool<T>, Set<T> {
    public actual constructor(): this(CopyOnWriteSet(mutableSetOf()))

    actual override fun plusAssign (item: T) { delegate += item }
    actual override fun minusAssign(item: T) { delegate -= item }

    actual override val size: Int get() = delegate.size

    actual override fun isEmpty    (                       ): Boolean     = delegate.isEmpty()
    actual override fun iterator   (                       ): Iterator<T> = delegate.iterator()
    actual override fun contains   (element : T            ): Boolean     = delegate.contains(element)
    actual override fun containsAll(elements: Collection<T>): Boolean     = delegate.containsAll(elements)
}

/** @suppress */
public actual open class ObservableSetPool<T> actual constructor(private val delegate: SetPool<T>): ObservablePool<T>, Set<T> /*by delegate*/ {
    actual override val changed: Pool<PoolObserver<T>> by lazy { SetPool() }

    actual override fun plusAssign (item: T) {
        val notify = item !in delegate
        delegate += item

        if (notify) {
            (changed as SetPool<PoolObserver<T>>).forEach { it.added(this, item) }
        }
    }
    actual override fun minusAssign(item: T) {
        val notify = item in delegate
        delegate -= item
        if (notify){
            (changed as SetPool<PoolObserver<T>>).forEach { it.removed(this, item) }
        }
    }

    actual override val size: Int get() = delegate.size

    actual override fun isEmpty    (                       ): Boolean     = delegate.isEmpty()
    actual override fun iterator   (                       ): Iterator<T> = delegate.iterator()
    actual override fun contains   (element : T            ): Boolean     = delegate.contains(element)
    actual override fun containsAll(elements: Collection<T>): Boolean     = delegate.containsAll(elements)
}