package io.nacular.doodle.utils

import java.util.concurrent.CopyOnWriteArraySet

/**
 * Created by Nicholas Eddy on 7/22/21.
 */
/** @suppress */
public actual open class SetPool<T> actual constructor(private val delegate: MutableSet<T>): Pool<T>, Set<T> by delegate {
    public actual constructor(): this(CopyOnWriteArraySet())

    override fun plusAssign (item: T) { delegate += item }
    override fun minusAssign(item: T) { delegate -= item }
}

/** @suppress */
public actual open class ObservableSetPool<T> actual constructor(private val delegate: SetPool<T>): ObservablePool<T>, Set<T> by delegate {
    override val changed: Pool<PoolObserver<T>> by lazy { SetPool() }

    override fun plusAssign(item: T) {
        val notify = item !in delegate
        delegate += item

        if (notify) {
            (changed as SetPool<PoolObserver<T>>).forEach { it.added(this, item) }
        }
    }
    override fun minusAssign(item: T) {
        val notify = item in delegate
        delegate -= item
        if (notify){
            (changed as SetPool<PoolObserver<T>>).forEach { it.removed(this, item) }
        }
    }
}