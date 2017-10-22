package com.zinoti.jaz.utils

/**
 * Created by Nicholas Eddy on 10/21/17.
 */

typealias PropertyObserver<T> = (old: T, new: T) -> Unit

interface PropertyObservers<T> {
    operator fun plus (observer: PropertyObserver<T>): PropertyObservers<T>
    operator fun minus(observer: PropertyObserver<T>): PropertyObservers<T>
}

class PropertyObserversImpl<T>(private val mutableSet: MutableSet<PropertyObserver<T>>): MutableSet<PropertyObserver<T>> by mutableSet, PropertyObservers<T> {
    override fun plus(observer: (old: T, new: T) -> Unit): PropertyObservers<T> {
        mutableSet + observer

        return this
    }

    override fun minus(observer: (old: T, new: T) -> Unit): PropertyObservers<T> {
        mutableSet - observer

        return this
    }
}


class ObservableList<E>(private val l: MutableList<E>): MutableList<E> by l {

    private val onChange_ = PropertyObserversImpl<List<E>>(mutableSetOf())
    val onChange: PropertyObservers<List<E>> = onChange_

    override fun add(element: E): Boolean = execute { l.add(element) }

    override fun remove(element: E): Boolean = execute { l.remove(element) }

    override fun addAll(elements: Collection<E>): Boolean = execute { l.addAll(elements) }

    override fun addAll(index: Int, elements: Collection<E>): Boolean = execute { l.addAll(index, elements) }

    override fun removeAll(elements: Collection<E>): Boolean = execute { l.removeAll(elements) }
    override fun retainAll(elements: Collection<E>): Boolean = execute { l.retainAll(elements) }
    override fun clear() = execute { l.clear() }

    override operator fun set(index: Int, element: E): E = execute { l.set(index, element) }

    override fun add(index: Int, element: E) = execute { l.add(index, element) }

    override fun removeAt(index: Int): E = execute { l.removeAt(index) }

    private fun <T> execute(block: () -> T): T {
        if (onChange_.isEmpty()) {
            return block()
        } else {
            val old = ArrayList(l)

            return block().also {
                if (old != this) {
                    onChange_.forEach {
                        it(this, old)
                    }
                }
            }
        }
    }
}
