package io.nacular.doodle.utils

import java.util.concurrent.CopyOnWriteArraySet

/**
 * Created by Nicholas Eddy on 7/22/21.
 */
public actual open class SetPool<T> actual constructor(protected actual val delegate: MutableSet<T>): Pool<T>, Set<T> by delegate {
    public actual constructor(): this(CopyOnWriteArraySet())

    override fun plusAssign (item: T) { delegate += item }
    override fun minusAssign(item: T) { delegate -= item }
}

public actual class ChangeObserversImpl<S> actual constructor(private val source: S, mutableSet: MutableSet<ChangeObserver<S>>): SetPool<ChangeObserver<S>>(mutableSet) {
    public actual constructor(source: S): this(source, CopyOnWriteArraySet())

    public actual operator fun invoke(): Unit = delegate.forEach { it(source) }
}

public actual class PropertyObserversImpl<S, T> actual constructor(private val source: S, mutableSet: MutableSet<PropertyObserver<S, T>>): SetPool<PropertyObserver<S, T>>(mutableSet) {
    public actual constructor(source: S): this(source, CopyOnWriteArraySet())

    public actual operator fun invoke(old: T, new: T): Unit = delegate.forEach { it(source, old, new) }
}