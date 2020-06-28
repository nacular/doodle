package io.nacular.doodle.utils


/**
 * Created by Nicholas Eddy on 3/22/20.
 */
class LeastRecentlyUsedCache<K, V>(private val maxSize: Int): MutableMap<K, V> {
    private inner class Entry(var value: V, var key: K) {
        var left : Entry? = null
        var right: Entry? = null
    }

    private val hashMap = mutableMapOf<K, Entry>()
    private var start   = null as LeastRecentlyUsedCache<K, V>.Entry?
    private var end     = null as LeastRecentlyUsedCache<K, V>.Entry?

    override operator fun get(key: K): V? = hashMap[key]?.let {
        removeNode(it)
        addAtTop  (it)
        return it.value
    }

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = hashMap.entries.map {
        object: MutableMap.MutableEntry<K, V> {
            override var key   = it.key
            override var value = it.value.value
            override fun setValue(newValue: V): V {
                val result = it.value.value

                it.value.value = newValue

                return result
            }
        }
    }.toMutableSet()

    override val keys   get() = hashMap.keys
    override val size   get() = hashMap.size
    override val values get() = hashMap.values.map { it.value }.toMutableList()

    override fun containsKey(key: K) = hashMap.containsKey(key)

    override fun containsValue(value: V) = value in values

    override fun isEmpty() = hashMap.isEmpty()

    override fun clear() {
        hashMap.clear()
        start = null
        end   = null
    }

    override fun put(key: K, value: V): V? {
        var result = value

        when (val entry = hashMap[key]) {
            null -> {
                val newNode = Entry(key = key, value = value)

                // We have reached maximum size so need to make room for new element.
                if (hashMap.size >= maxSize) {
                    end?.let { end ->
                        hashMap.remove(end.key)
                        removeNode(end)
                        addAtTop(newNode)
                    }
                } else {
                    addAtTop(newNode)
                }

                if (end != null) {
                    hashMap[key] = newNode
                }
            }
            else -> {
                result = entry.value
                entry.value = value
                removeNode(entry)
                addAtTop(entry)
            }
        }

        return result
    }

    override fun putAll(from: Map<out K, V>) {
        from.forEach {
            this[it.key] = it.value
        }
    }

    override fun remove(key: K): V? = hashMap.remove(key)?.also {
        removeNode(it)
    }?.value


    private fun addAtTop(node: Entry) {
        node.left  = null
        node.right = start

        start?.let { it.left = node }

        start = node

        if (end == null) end = start
    }

    private fun removeNode(node: Entry) {
        when (val l = node.left) {
            null ->   start = node.right
            else -> l.right = node.right
        }
        when (val r = node.right) {
            null ->    end = node.left
            else -> r.left = node.left
        }
    }
}