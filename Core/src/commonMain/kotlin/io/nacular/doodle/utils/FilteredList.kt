package io.nacular.doodle.utils

/**
 * Provides a filtered view of an underlying [ObservableList]. Changes to this list affect the underlying list,
 * but their effects may not be visible once applied due to [filter].
 *
 * @property source list underlying this one
 * @param filter to apply to [source]
 */
public class FilteredList<E>(public val source: ObservableList<E>, filter: ((E) -> Boolean)? = null): ObservableList<E> by source {
    private val changed_ = SetPool<ListObserver<ObservableList<E>, E>>()
    public override val changed: Pool<ListObserver<ObservableList<E>, E>> = changed_

    /**
     * The filter applied to [source]
     */
    public var filter: ((E) -> Boolean)? = null
        set(new) {
            if (field != new) {
                field = new
                refilter()
            }
        }

    private val indexToSource = mutableListOf<Int>()

    init {
        this.filter = filter

        updateIndexes()

        source.changed += { _, removed, added, moved ->
            val mappedMoved   = mutableMapOf<Int, Pair<Int, E>>()
            val mappedRemoved = handleRemoves(removed)
            val mappedAdded   = handleAdds   (added  )

            val movedIndexes = mutableMapOf<Int, Int>()

            indexToSource.forEachIndexed { i, sourceIndex ->
                moved[sourceIndex]?.let {
                    movedIndexes[i] = it.first

                    indexToSource[i] = it.first
                }
            }

            indexToSource.sort()

            movedIndexes.forEach {
                val sourceIndex = indexFromSource(it.value)!!
                mappedMoved[it.key] = sourceIndex to source[it.value]
            }

            // FIXME: Prune redundant moves

            if (mappedRemoved.isNotEmpty() || mappedAdded.isNotEmpty() || mappedMoved.isNotEmpty()) {
                changed_.forEach { it(this, mappedRemoved, mappedAdded, mappedMoved) }
            }
        }
    }

    override val size: Int get() = indexToSource.size

    override fun get(index: Int): E = source[indexToSource[index]]

    override fun add(element: E): Boolean {
        val index = indexToSource.size

        return when {
            index == 0 || index >= source.size - 1 ->             source.add(element           )
            else                                   -> true.also { source.add(index + 1, element) }
        }
    }

    override fun move(element: E, to: Int): Boolean = source.move(element, indexToSource[to])

    override fun removeAt(index: Int): E = source.removeAt(indexToSource[index])

    override fun set(index: Int, element: E): E = source.set(indexToSource[index], element)

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> {
        val result = mutableListOf<E>()
        return indexToSource.subList(indexToSource[fromIndex], indexToSource[toIndex]).mapTo(result) { source[it] }
    }

    override fun equals(other: Any?): Boolean {
        if (other !is List<*> || other.size != this.size) return false

        indexToSource.forEachIndexed { index, e ->
            if (other[index] != source[e]) return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = source.hashCode()
        result = 31 * result + indexToSource.hashCode()
        return result
    }

    override fun <T> batch(block: MutableList<E>.() -> T): T = source.batch {
        block(FilteredList(ObservableList.wrap(this), filter))
    }

    public override operator fun plusAssign(element: E) {
        add(element)
    }

    public override operator fun minusAssign(element: E) {
        remove(element)
    }

    override fun notifyChanged(index: Int) {
        source.notifyChanged(indexToSource[index])
    }

    override fun clear() {
        source.batch {
            removeAll(indexToSource.map { source[it] })
        }
    }

    override fun lastIndexOf(element: E): Int = indexFromSource(source.lastIndexOf(element)) ?: -1

    override fun add(index: Int, element: E) {
        source.add(indexToSource[index], element)
    }

    override fun indexOf(element: E): Int = indexFromSource(source.indexOf(element)) ?: -1

    override fun contains(element: E): Boolean = source.indexOf(element) in indexToSource

    override fun iterator(): MutableIterator<E> = when (filter) {
        null -> source.iterator()
        else -> {
            object: MutableIterator<E> {
                private var index = 0

                override fun next   () = source[indexToSource[index++]]
                override fun hasNext() = index < indexToSource.size
                override fun remove () = source.removeAt(indexToSource[index]).let {}
            }
        }
    }

    override fun toString(): String = "${indexToSource.map { source[it] }}"

    private fun refilter() {
        val oldIndexes: MutableList<Int> = mutableListOf(*indexToSource.toTypedArray())

        indexToSource.clear()

        updateIndexes()

        diffLists(oldIndexes, indexToSource)?.let { diffs ->
            changed_.forEach {
                it(this,
                   diffs.removed.mapValues { source[it.value] },
                   diffs.added.mapValues   { source[it.value] },
                   diffs.moved.mapValues   { it.value.first to source[it.value.second] }
                )
            }
        }
    }

    private fun updateIndexes() {
        var i = 0

        source.forEachIndexed { index, item ->
            item.takeIf { filter?.invoke(item) ?: true }?.let {
                when {
                    i < size -> indexToSource[i] = index
                    else     -> indexToSource.add(index)
                }
                i++
            }
        }
    }

    private fun handleRemoves(removed: Map<Int, E>): Map<Int, E> {
        val mappedRemoved = mutableMapOf<Int, E>()
        val removedKeys = removed.keys.sorted()
        var j = removedKeys.size - 1
        var i = size - 1

        while(j >= 0 && i >= 0) {
            when {
                indexToSource[i] > removedKeys[j] -> {
                    indexToSource[i] -= j + 1
                    --i
                }
                else                               -> {
                    if (indexToSource[i] == removedKeys[j]) {
                        indexToSource.removeAt(i)
                        mappedRemoved[i] = removed[removedKeys[j]]!!
                        --i
                    }

                    --j
                }
            }
        }

        return mappedRemoved
    }

    private fun handleAdds(added: Map<Int, E>): Map<Int, E> {
        val mappedAdded = mutableMapOf<Int, E>()
        val addedKeys = added.keys.sorted()
        var j = added.size - 1

        addedKeys.forEach { addedIndex ->
            if (this.filter?.invoke(added[addedIndex]!!) != false) {
                indexToSource.addOrAppend(insertionIndex(addedIndex), addedIndex)
            }
        }

        if (j >= 0) {
            (size - 1 downTo 0).forEach { i ->
                val addedIndex    = addedKeys    [j]
                val localIndex    = indexToSource[i]
                val previousIndex = indexToSource.getOrNull(i-1) ?: -1

                when {
                    localIndex > addedIndex || (localIndex == addedIndex &&
                            (previousIndex == addedIndex || this.filter?.invoke(added[addedIndex]!!) == false)) -> indexToSource[i] += 1
                    else                    -> {
                        if (localIndex == addedIndex && previousIndex < addedIndex) {
                            mappedAdded[i] = added[indexToSource[i]]!!
                        }

                        if (--j < 0) {
                            return mappedAdded
                        }
                    }
                }
            }
        }

        return mappedAdded
    }

    private fun insertionIndex(indexFromSource: Int): Int {
        val result = indexToSource.binarySearch(indexFromSource, 0, size)

        return when {
            result < 0 -> -result - 1
            else       -> result
        }

    }

    private fun indexFromSource(index: Int): Int? {
        val result = indexToSource.binarySearch(index, 0, size)

        return when {
            result < 0 -> null
            else       -> result
        }
    }
}
