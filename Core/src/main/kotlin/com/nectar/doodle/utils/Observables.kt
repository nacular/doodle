package com.nectar.doodle.utils

import kotlin.properties.ObservableProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Created by Nicholas Eddy on 10/21/17.
 */


typealias SetObserver     <S, T> = (source: ObservableSet <S, T>, removed: Set<T>,      added: Set<T>                                    ) -> Unit
typealias ListObserver    <S, T> = (source: ObservableList<S, T>, removed: Map<Int, T>, added: Map<Int, T>, moved: Map<Int, Pair<Int, T>>) -> Unit
typealias ChangeObserver  <S>    = (source: S                                                                                            ) -> Unit
typealias PropertyObserver<S, T> = (source: S, old: T, new: T                                                                            ) -> Unit

interface ChangeObservers<out S> {
    operator fun plusAssign (observer: ChangeObserver<S>)
    operator fun minusAssign(observer: ChangeObserver<S>)
}

interface PropertyObservers<out S, out T> {
    operator fun plusAssign (observer: PropertyObserver<S, T>)
    operator fun minusAssign(observer: PropertyObserver<S, T>)
}

interface ListObservers<S, T> {
    operator fun plusAssign (observer: ListObserver<S, T>)
    operator fun minusAssign(observer: ListObserver<S, T>)
}

interface SetObservers<S, T> {
    operator fun plusAssign (observer: SetObserver<S, T>)
    operator fun minusAssign(observer: SetObserver<S, T>)
}

class ChangeObserversImpl<S>(private val source: S, private val mutableSet: MutableSet<ChangeObserver<S>> = mutableSetOf()): Set<ChangeObserver<S>> by mutableSet, ChangeObservers<S> {
    override fun plusAssign(observer: ChangeObserver<S>) {
        mutableSet += observer
    }

    override fun minusAssign(observer: ChangeObserver<S>) {
        mutableSet -= observer
    }

    operator fun invoke() = mutableSet.forEach { it(source) }
}

class PropertyObserversImpl<S, T>(private val source: S, private val mutableSet: MutableSet<PropertyObserver<S, T>> = mutableSetOf()): Set<PropertyObserver<S, T>> by mutableSet, PropertyObservers<S, T> {
    override fun plusAssign(observer: PropertyObserver<S, T>) {
        mutableSet += observer
    }

    override fun minusAssign(observer: PropertyObserver<S, T>) {
        mutableSet -= observer
    }

    operator fun invoke(old: T, new: T) = mutableSet.forEach { it(source, old, new) }
}

open class ObservableProperty<S, T>(initial: T, private val owner: () -> S, private val observers: Iterable<PropertyObserver<S, T>>): ObservableProperty<T>(initial) {
    override fun beforeChange(property: KProperty<*>, oldValue: T, newValue: T) = newValue != oldValue

    override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T) {
        super.afterChange(property, oldValue, newValue)

        if (oldValue != newValue) {
            observers.forEach { it(owner(), oldValue, newValue) }
        }
    }
}

class ListObserversImpl<S, T>(private val mutableSet: MutableSet<ListObserver<S, T>> = mutableSetOf()): Set<ListObserver<S, T>> by mutableSet, ListObservers<S, T> {
    override fun plusAssign(observer: ListObserver<S, T>) {
        mutableSet += observer
    }

    override fun minusAssign(observer: ListObserver<S, T>) {
        mutableSet -= observer
    }
}

// TODO: Change so only deltas are reported
class ObservableList<S, E>(val source: S, val list: MutableList<E> = mutableListOf()): MutableList<E> by list {

    private val onChange_ = ListObserversImpl<S, E>()
    val onChange: ListObservers<S, E> = onChange_

    fun move(element: E, to: Int): Boolean {
        val oldIndex = indexOf(element)

        if (to !in 0 until size || oldIndex < 0 || oldIndex == to) return false

        list.remove(element    )
        list.add   (to, element)

        onChange_.forEach {
            it(this, mapOf(), mapOf(), mapOf(to to (oldIndex to element)))
        }

        return true
    }

    override fun add(element: E) = list.add(element).ifTrue {
        onChange_.forEach {
            it(this, mapOf(), mapOf(list.size - 1 to element), mapOf())
        }
    }

    override fun remove(element: E): Boolean {
        val index = list.indexOf(element)

        return when {
            index < 0 -> false
            else      -> list.remove(element).ifTrue { onChange_.forEach { it(this, mapOf(index to element), mapOf(), mapOf()) } }
        }
    }

    override fun addAll(elements: Collection<E>) = batch { list.addAll(elements) }

    override fun addAll(index: Int, elements: Collection<E>) = batch { list.addAll(index, elements) }

    override fun removeAll(elements: Collection<E>) = batch { list.removeAll(elements) }
    override fun retainAll(elements: Collection<E>) = batch { list.retainAll(elements) }

    override fun clear() {
        val size    = list.size
        val oldList = ArrayList(list)

        list.clear()

        onChange_.forEach {
            it(this, (0 until size).associate { it to oldList[it] }, mapOf(), mapOf())
        }
    }

    override operator fun set(index: Int, element: E) = list.set(index, element).also { new ->
        if (new !== element) {
            onChange_.forEach {
                it(this, mapOf(index to new), mapOf(index to element), mapOf())
            }
        }
    }

    override fun add(index: Int, element: E) {
        list.add(index, element)

        onChange_.forEach {
            it(this, mapOf(), mapOf(index to element), mapOf())
        }
    }

    override fun removeAt(index: Int) = list.removeAt(index).also { removed ->
        onChange_.forEach {
            it(this, mapOf(index to removed), mapOf(), mapOf())
        }
    }

    private fun <T> batch(block: () -> T): T {
        return if (onChange_.isEmpty()) {
            block()
        } else {
            // TODO: Can this be optimized?
            val old = ArrayList(list)

            block().also {
                if (old != this) {
                    val removed = mutableMapOf<Int, E>()
                    val added   = mutableMapOf<Int, E>()
                    val moved   = mutableMapOf<Int, Pair<Int, E>>()

                    old.forEachIndexed { index, item ->
                        if (index >= this.size || this[index] != item) {
                            removed[index] = item
                        }
                    }

                    this.forEachIndexed { index, item ->
                        if (index >= old.size || old[index] != item) {
                            added[index] = item
                        }
                    }

                    onChange_.forEach {
                        it(this, removed, added, moved)
                    }
                }
            }
        }
    }
}

interface Pool<in T> {
    operator fun plusAssign (item: T)
    operator fun minusAssign(item: T)
}

class SetPool<T>(private val delegate: MutableSet<T> = mutableSetOf()): Pool<T>, Set<T> by delegate {
    override fun plusAssign (item: T) { delegate += item }
    override fun minusAssign(item: T) { delegate += item }
}

open class OverridableProperty<T>(initialValue: T, private val onChange: (property: KProperty<*>, oldValue: T, newValue: T) -> Unit): ObservableProperty<T>(initialValue) {
    override fun beforeChange(property: KProperty<*>, oldValue: T, newValue: T) = newValue != oldValue
    override fun afterChange (property: KProperty<*>, oldValue: T, newValue: T) = onChange(property, oldValue, newValue)
}

fun <T> observable(initialValue: T, onChange: (property: KProperty<*>, oldValue: T, newValue: T) -> Unit): ReadWriteProperty<Any?, T> = OverridableProperty(initialValue, onChange)

class SetObserversImpl<S, T>(private val mutableSet: MutableSet<SetObserver<S, T>> = mutableSetOf()): Set<SetObserver<S, T>> by mutableSet, SetObservers<S, T> {
    override fun plusAssign(observer: SetObserver<S, T>) {
        mutableSet += observer
    }

    override fun minusAssign(observer: SetObserver<S, T>) {
        mutableSet -= observer
    }
}

class ObservableSet<S, E>(val source: S, val set: MutableSet<E> = mutableSetOf()): MutableSet<E> by set {
    private val onChange_ = SetObserversImpl<S, E>()
    val onChange: SetObservers<S, E> = onChange_

    override fun add(element: E) = set.add(element).ifTrue {
        onChange_.forEach {
            it(this, emptySet(), setOf(element))
        }
    }

    override fun remove(element: E): Boolean {
        return set.remove(element).ifTrue { onChange_.forEach { it(this, setOf(element), emptySet()) } }
    }

    override fun addAll(elements: Collection<E>) = batch { set.addAll(elements) }

    override fun removeAll (elements: Collection<E>) = batch { set.removeAll(elements) }
    override fun retainAll (elements: Collection<E>) = batch { set.retainAll(elements) }
             fun replaceAll(elements: Collection<E>) = batch { set.run { clear(); addAll(elements) } }

    override fun clear() {
        val oldSet = HashSet(set)

        set.clear()

        onChange_.forEach {
            it(this, oldSet, emptySet())
        }
    }

    private fun <T> batch(block: () -> T): T {
        return if (onChange_.isEmpty()) {
            block()
        } else {
            // TODO: Can this be optimized?
            val old = HashSet(set)

            block().also {
                if (old != this) {
                    onChange_.forEach {
                        it(this, old.asSequence().filter { it !in set }.toSet(), set.asSequence().filter { it !in old }.toSet())
                    }
                }
            }
        }
    }
}
