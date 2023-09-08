package io.nacular.doodle.controls

import io.nacular.doodle.utils.ObservableList
import io.nacular.doodle.utils.Pool
import io.nacular.doodle.utils.SetPool
import io.nacular.doodle.utils.diff.Differences
import io.nacular.doodle.utils.sortWith
import io.nacular.doodle.utils.sortWithDescending

/**
 * Holds data in a list-like structure for use with controls like [List][io.nacular.doodle.controls.list.List].
 */
public interface ListModel<T>: Iterable<T> {
    /** Number of elements in the model */
    public val size: Int

    /**
     * Indicates whether the model has any contents
     */
    public val isEmpty: Boolean get() = size == 0

    /**
     * @param index to retrieve
     * @return item found at the index or `null` if index invalid
     */
    public operator fun get(index: Int): Result<T>

    /**
     * @param range of data to extract
     * @return list of items in the range
     */
    public fun section (range: ClosedRange<Int>): List<T> // FIXME: This should return List<Result<T>> for consistency

    /**
     * @param value to check
     * @return `true` if the item is in the model
     */
    public fun contains(value: T): Boolean
}

public typealias ModelObserver<T> = (source: DynamicListModel<T>, Differences<T>) -> Unit

/**
 * A model that can change over time. These models do not directly expose mutators,
 * but they admit to being mutable.
 */
public interface DynamicListModel<T>: ListModel<T> {
    /** Notifies of changes to the model */
    public val changed: Pool<ModelObserver<T>>
}

/**
 * A [ListModel] model that lets callers modify it.
 */
public interface MutableListModel<T>: DynamicListModel<T> {

    /**
     * Set the value at a specific index.
     *
     * @param index to update
     * @param value to set
     */
    public operator fun set(index: Int, value: T): Result<T>

    /**
     * Forces the model to notify listeners that [index] changed.
     * This is useful when the model has mutable items that change
     * their internal state in some way that observers need to know
     * about. Such changes cannot be detected by the model directly,
     * since it relies on reference comparisons to detect changes.
     *
     * @param index that was changed
     */
    public fun notifyChanged(index: Int)

    /**
     * Adds a new value to the end of the model.
     *
     * @param value to add
     */
    public fun add(value: T)

    /**
     * Inserts a new value to the model at an index.
     *
     * @param index where the new value should go
     * @param value to add
     */
    public fun add(index: Int, value: T)

    /**
     * Removes [value] from the model.
     *
     * @param value to remove
     */
    public fun remove(value: T)

    /**
     * Removes the value at [index] from the model.
     *
     * @param index to remove
     * @return the value removed or an error if none exists
     */
    public fun removeAt(index: Int): Result<T>

    /**
     * Adds [values] to the end of the model.
     *
     * @param values to add
     */
    public fun addAll(values: Collection<T>)

    /**
     * Inserts [values] at [index] in the model.
     *
     * @param index where the new values should go
     * @param values to add
     */
    public fun addAll(index: Int, values: Collection<T>)

    /**
     * Removes [values] from the model.
     *
     * @param values to remove
     */
    public fun removeAll(values: Collection<T>)

    /**
     * Keeps only the items in the model that are contained within [values].
     *
     * @param values to keep
     */
    public fun retainAll(values: Collection<T>)

    /**
     * Removes all values at the given [indexes] from the model.
     *
     * @param indexes to remove
     */
    public fun removeAllAt(indexes: Collection<Int>)

    /**
     * Replaces all values in the model with new items.
     *
     * @param values the model will end up with
     */
    public fun replaceAll(values: Collection<T>)

    /**
     * Removes all items from the model.
     */
    public fun clear()

    /**
     * Sorts the contents of the model ascending.
     *
     * @param comparator used for sort
     */
    public fun sortWith(comparator: Comparator<in T>)

    /**
     * Sorts the contents of the model descending.
     *
     * @param comparator used for sort
     */
    public fun sortWithDescending(comparator: Comparator<in T>)

    /**
     * Sorts the contents of the model ascending.
     *
     * @param selector used for sort
     */
    public fun <R: Comparable<R>> sortBy(selector: (T) -> R?)

    /**
     * Sorts the contents of the model descending.
     *
     * @param selector used for sort
     */
    public fun <R: Comparable<R>> sortByDescending(selector: (T) -> R?)
}

public fun <T: Comparable<T>> MutableListModel<T>.sort() {
    sortWith(naturalOrder())
}

public fun <T: Comparable<T>> MutableListModel<T>.sortDescending() {
    sortWithDescending(naturalOrder())
}

/**
 * [ListModel] based directly on a [List].
 */
public open class SimpleListModel<T>(private val list: List<T>): ListModel<T> {

    override val size: Int get() = list.size

    override fun get     (index: Int             ): Result<T>   = runCatching { list[index] }
    override fun section (range: ClosedRange<Int>): List<T>     = list.subList (range.start, range.endInclusive + 1)
    override fun contains(value: T               ): Boolean     = list.contains(value                              )
    override fun iterator(                       ): Iterator<T> = list.iterator(                                   )
}

/**
 * [MutableListModel] based directly on an [ObservableList].
 */
public open class SimpleMutableListModel<T>(private val list: ObservableList<T>): SimpleListModel<T>(list), MutableListModel<T> {
    init {
        list.changed += { _,diffs ->
            (changed as SetPool).forEach {
                it(this, diffs)
            }
        }
    }

    override fun set(index: Int, value: T): Result<T> = runCatching { list.set(index, value) }

    override fun add(value: T            ): Unit      = list.add(value).run {}
    override fun add(index: Int, value: T): Unit      = list.add(index, value)

    override fun notifyChanged(index: Int): Unit = list.notifyChanged(index)

    override fun remove     (value  : T                         ): Unit      = list.remove   (value        ).run {}
    override fun removeAt   (index  : Int                       ): Result<T> = runCatching { list.removeAt(index) }
    override fun addAll     (values : Collection<T>             ): Unit      = list.addAll   (values       ).run {}
    override fun addAll     (index  : Int, values: Collection<T>): Unit      = list.addAll   (index, values).run {}
    override fun removeAll  (values : Collection<T>             ): Unit      = list.removeAll(values       ).run {}
    override fun retainAll  (values : Collection<T>             ): Unit      = list.retainAll(values       ).run {}
    override fun removeAllAt(indexes: Collection<Int>           ): Unit      = list.batch { indexes.sortedDescending().forEach { list.removeAt(it) } }
    override fun replaceAll (values : Collection<T>             ): Unit      = list.replaceAll(values).run {}

    override fun clear(): Unit = list.clear()

    override val changed: Pool<ModelObserver<T>> = SetPool()

    override fun sortWith(comparator: Comparator<in T>) {
        list.sortWith(comparator)
    }

    override fun sortWithDescending(comparator: Comparator<in T>) {
        list.sortWithDescending(comparator)
    }

    override fun <R: Comparable<R>> sortBy(selector: (T) -> R?) {
        list.sortBy(selector)
    }

    override fun <R: Comparable<R>> sortByDescending(selector: (T) -> R?) {
        list.sortByDescending(selector)
    }

    public companion object {
        public operator fun <T> invoke(list: List<T> = emptyList()): SimpleMutableListModel<T> = SimpleMutableListModel(ObservableList(list))
    }
}

/**
 * Creates a [MutableListModel] from the given set of [elements].
 */
public fun <T> mutableListModelOf(vararg elements: T): MutableListModel<T> = SimpleMutableListModel(mutableListOf(*elements))