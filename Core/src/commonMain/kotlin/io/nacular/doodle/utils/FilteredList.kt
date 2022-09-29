package io.nacular.doodle.utils

import io.nacular.doodle.utils.diff.Delete
import io.nacular.doodle.utils.diff.Difference
import io.nacular.doodle.utils.diff.Differences
import io.nacular.doodle.utils.diff.Equal
import io.nacular.doodle.utils.diff.Insert
import io.nacular.doodle.utils.diff.compare

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

        source.changed += { _, diffs ->
            var filteredDiffs = diffs

            this.filter?.let { filter->
                val changes = mutableListOf<Difference<E>>()

                var index = 0

                diffs.forEach { difference ->
                    difference.items.filter { filter(it) }.takeIf { it.isNotEmpty() }?.let { items ->
                        when (difference) {
                            is Equal  -> {
                                changes.lastOrNull()?.takeIf { it is Equal }?.let {
                                    changes[changes.size - 1] = Equal(it.items + items)
                                } ?: changes.plusAssign(Equal(items))
                            }
                            is Delete -> {
                                changes.lastOrNull()?.takeIf { it is Delete }?.let {
                                    changes[changes.size - 1] = Delete(it.items + items)
                                } ?: changes.plusAssign(Delete(items))
                            }
                            is Insert -> {
                                changes.lastOrNull()?.takeIf { it is Insert }?.let {
                                    changes[changes.size - 1] = Insert(it.items + items)
                                } ?: changes.plusAssign(Insert(items))
                            }
                        }
                    }

                    when (difference) {
                        is Delete -> {
                            handleRemoves(List(difference.items.size) { i -> index + i })
                        }
                        is Insert -> {
                            difference.items.forEachIndexed { itemIndex, item ->
                                if (difference.origin(of = item) == null && this.filter?.invoke(item) != false) {
                                    indexToSource.addOrAppend(insertionIndex(index + itemIndex), index + itemIndex)
                                }

                                ++index
                            }
                        }
                        else -> index += difference.items.size
                    }
                }

                filteredDiffs = Differences(changes)
            }

            changed_.forEach { it(this, filteredDiffs) }
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

        compare(oldIndexes, indexToSource).let { diffs ->
            changed_.forEach {
                val changes = mutableListOf<Difference<E>>()

                diffs.forEach { difference ->
                    changes += when (difference) {
                        is Equal  -> Equal (difference.items.map { source[it] })
                        is Delete -> Delete(difference.items.map { source[it] })
                        is Insert -> Insert(difference.items.map { source[it] })
                    }
                }

                it(this, Differences(changes))
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

    private fun handleRemoves(removed: List<Int>) {
        var j = removed.size - 1
        var i = size - 1

        while(j >= 0 && i >= 0) {
            when {
                indexToSource[i] > removed[j] -> {
                    indexToSource[i] -= j + 1
                    --i
                }
                else                               -> {
                    if (indexToSource[i] == removed[j]) {
                        indexToSource.removeAt(i)
                        --i
                    }

                    --j
                }
            }
        }
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
