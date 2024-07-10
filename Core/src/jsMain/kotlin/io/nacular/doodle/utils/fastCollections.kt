package io.nacular.doodle.utils

import kotlin.collections.Map.Entry
import kotlin.collections.MutableMap.MutableEntry

/**
 * Shape for [JS Map](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Map) type.
 */
private external class Map<K, V> {
    fun set   (key: K, value: V)
    fun get   (key: K          ): V?
    fun delete(key: K          )
    fun clear (                )
}

/**
 * Wrapper around [JS Map](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Map) instance.
 */
@Suppress("LocalVariableName", "UnsafeCastFromDynamic")
private class MapWrapper<K, V>: AbstractMutableMap<K, V>(), MutableIterable<MutableEntry<K, V>> {
    private class MutablePair<A, B>(var first: A, var second: B)

    private val delegate = Map<Int, Array<MutablePair<K, V>>>()

    private abstract class AbstractEntrySet<E : Entry<K, V>, K, V> : AbstractMutableSet<E>() {
        override fun contains     (element: E          ): Boolean = containsEntry(element)
        abstract fun containsEntry(element: Entry<K, V>): Boolean
        override fun remove       (element: E          ): Boolean = removeEntry(element)
        abstract fun removeEntry  (element: Entry<K, V>): Boolean
    }

    private inner class EntrySet: AbstractEntrySet<MutableEntry<K, V>, K, V>() {
        override val size: Int get() = this@MapWrapper.size

        override fun add          (element: MutableEntry<K, V>) = throw UnsupportedOperationException("Add is not supported on entries")
        override fun clear        (                           ) = this@MapWrapper.clear        (       )
        override fun iterator     (                           ) = this@MapWrapper.iterator     (       )
        override fun containsEntry(element: Entry<K, V>       ) = this@MapWrapper.containsEntry(element)
        override fun removeEntry  (element: Entry<K, V>       ): Boolean {
            if (contains(element)) {
                this@MapWrapper.remove(element.key)
                return true
            }
            return false
        }
    }

    private fun containsEntry(entry: Entry<*, *>?): Boolean {
        // since entry comes from @UnsafeVariance parameters it can be virtually anything
        if (entry !is Entry<*, *>) return false
        val key      = entry.key
        val value    = entry.value
        val ourValue = get(key)

        if (value != ourValue) {
            return false
        }

        // Perhaps it was null, and we don't contain the key?
        return !(ourValue == null && !containsKey(key))
    }
    private fun createEntrySet(): MutableSet<MutableEntry<K, V>> = EntrySet()

    private var _entries: MutableSet<MutableEntry<K, V>>? = null
    override val entries: MutableSet<MutableEntry<K, V>> get() {
        if (_entries == null) {
            _entries = createEntrySet()
        }
        return _entries!!
    }

    override fun put(key: K, value: V): V? {
        val hashCode = key.hashCode()

        return when (val bucket = delegate.get(hashCode)) {
            undefined -> null.also { delegate.set(hashCode, arrayOf(MutablePair(key, value))); ++size }
            else      -> {
                when (val existing = bucket.firstOrNull { it.first == key }) {
                    null -> { bucket.asDynamic().push(MutablePair(key, value)); ++size; null }
                    else -> {
                        val result = existing.second
                        existing.second = value
                        result
                    }
                }
            }
        }
    }

    override fun containsKey(key: K): Boolean = findPair(key) != null
    override fun get        (key: K): V?      = findPair(key)?.second

    override fun remove(key: K): V? {
        val hashCode = key.hashCode()
        val bucket   = delegate.get(hashCode)

        if (bucket != undefined) {
            bucket.forEachIndexed { index, existing ->
                if (existing.first == key) {
                    remove(bucket, index, hashCode)
                    return existing.second
                }
            }
        }

        return null
    }

    override fun clear() {
        delegate.clear()
        size = 0
    }

    override var size = 0; private set

    override fun iterator(): MutableIterator<MutableEntry<K, V>> = object: MutableIterator<MutableEntry<K, V>> {
        var key   = null as K?
        val iter  = delegate.asDynamic()[js("Symbol.iterator")]()
        var item  = iter.next()
        var index = 0

        override fun hasNext(): Boolean = when {
            item.done                                       -> false
            index >= item.value[1].length.unsafeCast<Int>() -> {
                item  = iter.next()
                index = 0
                !item.done as Boolean
            }
            else                                            -> true
        }

        override fun next(): MutableEntry<K, V> = object: MutableEntry<K, V> {
            private val myIndex = index
            override val key  : K = item.value[1][myIndex].unsafeCast<MutablePair<K, V>>().first
            override val value: V = item.value[1][myIndex].unsafeCast<MutablePair<K, V>>().second

            override fun setValue(newValue: V): V {
                val old = item.value[1][myIndex].unsafeCast<MutablePair<K, V>>().second
                item.value[1][myIndex].unsafeCast<MutablePair<K, V>>().second = newValue
                return old
            }
        }.also { key = it.key; ++index }

        override fun remove() {
            key?.let {
                remove(bucket = item.value[1], --index, key.hashCode())
            }
        }
    }

    private fun findPair(key: K): MutablePair<K, V>? = delegate.get(key.hashCode())?.firstOrNull { it.first == key }

    private fun remove(bucket: Array<MutablePair<K, V>>, index: Int, hashCode: Int) {
        val bucket_ = bucket.asDynamic()
        bucket_.splice(index, 1)
        if (bucket_.length == 0) {
            delegate.delete(hashCode)
        }
        --size
    }
}

/**
 * HashSet based on [MapWrapper].
 */
private class FastHashSet<E>: AbstractMutableSet<E>(), MutableSet<E> {
    private val map = fastMutableMapOf<E, Any?>()

    override val size: Int get() = map.size

    override fun add     (element: E) = map.put(element, element) == null
    override fun clear   (          ) = map.clear()
    override fun remove  (element: E) = map.remove(element) != null
    override fun isEmpty (          ) = map.isEmpty()
    override fun contains(element: E) = map.containsKey(element)
    override fun iterator(          ) = map.keys.iterator()
}

public actual fun <K, V> fastMutableMapOf(): MutableMap<K, V> = when {
    mapSupported -> MapWrapper  ()
    else         -> mutableMapOf()
}

public actual fun <E> fastSetOf(): Set<E> = when {
    mapSupported -> FastHashSet()
    else         -> setOf      ()
}

public actual fun <E> fastMutableSetOf(): MutableSet<E> = when {
    mapSupported -> FastHashSet ()
    else         -> mutableSetOf()
}

public actual fun <E> fastSetOf(vararg elements: E): Set<E> = when {
    mapSupported -> FastHashSet<E>().apply { elements.forEach { add(it) } }
    else         -> setOf(*elements)
}

public actual fun <E> fastMutableSetOf(vararg elements: E): MutableSet<E> = when {
    mapSupported -> FastHashSet<E>().apply { elements.forEach { add(it) } }
    else         -> mutableSetOf(*elements)
}

private val mapSupported by lazy { false } //window.asDynamic()["Map"] != null }