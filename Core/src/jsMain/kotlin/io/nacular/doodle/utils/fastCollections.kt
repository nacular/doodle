package io.nacular.doodle.utils

import kotlinx.browser.window
import kotlin.collections.Map
import kotlin.collections.MutableMap.MutableEntry

/**
 * Shape for [JS Map](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Map) type.
 */
private external class Map<K, V> {
    val size: Int
    fun set(key: K, value: V)
    fun get(key: K): V?
    fun delete(key: K)
    fun has(key: K): Boolean
    fun clear()
}

/**
 * Wrapper around [JS Map](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Map) instance.
 */
@Suppress("LocalVariableName", "UnsafeCastFromDynamic")
private class MapWrapper<K, V>: AbstractMutableMap<K, V>(), MutableIterable<MutableEntry<K, V>> {
    private val delegate = Map<Int, Pair<K, V>>()

    private abstract class AbstractEntrySet<E : Map.Entry<K, V>, K, V> : AbstractMutableSet<E>() {
        final override fun contains(element: E): Boolean = containsEntry(element)
        abstract fun containsEntry(element: Map.Entry<K, V>): Boolean
        final override fun remove(element: E): Boolean = removeEntry(element)
        abstract fun removeEntry(element: Map.Entry<K, V>): Boolean
    }

    private inner class EntrySet: AbstractEntrySet<MutableEntry<K, V>, K, V>() {

        override fun add(element: MutableEntry<K, V>): Boolean = throw UnsupportedOperationException("Add is not supported on entries")
        override fun clear() {
            this@MapWrapper.clear()
        }

        override fun containsEntry(element: Map.Entry<K, V>): Boolean = this@MapWrapper.containsEntry(element)

        override operator fun iterator(): MutableIterator<MutableEntry<K, V>> = this@MapWrapper.iterator()

        override fun removeEntry(element: Map.Entry<K, V>): Boolean {
            if (contains(element)) {
                this@MapWrapper.remove(element.key)
                return true
            }
            return false
        }

        override val size: Int get() = this@MapWrapper.size
    }

    private fun containsEntry(entry: Map.Entry<*, *>?): Boolean {
        // since entry comes from @UnsafeVariance parameters it can be virtually anything
        if (entry !is Map.Entry<*, *>) return false
        val key = entry.key
        val value = entry.value
        val ourValue = get(key)

        if (value != ourValue) {
            return false
        }

        // Perhaps it was null and we don't contain the key?
        if (ourValue == null && !containsKey(key)) {
            return false
        }

        return true
    }
    private fun createEntrySet(): MutableSet<MutableEntry<K, V>> = EntrySet()

    private var _entries: MutableSet<MutableEntry<K, V>>? = null
    override val entries: MutableSet<MutableEntry<K, V>>
        get() {
            if (_entries == null) {
                _entries = createEntrySet()
            }
            return _entries!!
        }

    override fun put(key: K, value: V): V? {
        val key_   = key.hashCode()
        val result = delegate.get(key_)
        delegate.set(key_, key to value)

        return if (result === undefined) null else result.second
    }

    override fun containsKey(key: K): Boolean = delegate.has(key.hashCode())
    override fun get(key: K): V? = delegate.get(key.hashCode())?.second

    override fun remove(key: K): V? {
        val old = this[key]
        delegate.delete(key.hashCode())
        return old
    }

    override fun clear() = delegate.clear()

    override val size: Int get() = delegate.size

    override fun iterator(): MutableIterator<MutableEntry<K, V>> = object: MutableIterator<MutableEntry<K, V>> {
        val iter        = delegate.asDynamic()[js("Symbol.iterator")]()
        var item        = iter.next()
        var moveForward = false

        override fun hasNext(): Boolean {
            if (moveForward) {
                item        = iter.next()
                moveForward = false
            }
            return !item.done
        }

        override fun next(): MutableEntry<K, V> = object: MutableEntry<K, V> {
            init {
                moveForward = true
            }

            override val key  : K get() = item.value[1].first
            override val value: V get() = item.value[1].second

            override fun setValue(newValue: V): V = this@MapWrapper.put(key, newValue)!!
        }

        override fun remove() {
            delegate.delete(item.value[0])
        }
    }
}

/**
 * HashSet based on [MapWrapper].
 */
private class FastHashSet<E>: AbstractMutableSet<E>(), MutableSet<E> {
    private val map = fastMutableMapOf<E, Any>()

    override val size: Int get() = map.size

    override fun add     (element: E) = map.put(element, this) == null
    override fun clear   (          ) = map.clear()
    override fun remove  (element: E) = map.remove(element) != null
    override fun isEmpty (          ) = map.isEmpty()
    override fun contains(element: E) = map.containsKey(element)
    override fun iterator(          ) = map.keys.iterator()
}

public actual fun <K, V> fastMutableMapOf(): MutableMap<K, V> = when {
    window.asDynamic()["Map"] != null -> MapWrapper()
    else                              -> mutableMapOf()
}

public actual fun <E> fastMutableSetOf(): MutableSet<E> = when {
    window.asDynamic()["Map"] != null -> FastHashSet()
    else                              -> mutableSetOf()
}