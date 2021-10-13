package io.nacular.doodle.controls

import io.nacular.doodle.utils.ObservableList
import io.nacular.doodle.utils.Pool
import io.nacular.doodle.utils.SetPool
import io.nacular.doodle.utils.sortWith
import io.nacular.doodle.utils.sortWithDescending

/**
 * Holds data in a list-like structure for use with controls like [List][io.nacular.doodle.controls.list.List].
 */
public interface ListModel<T>: Iterable<T> {
    /** Number of elements in the model */
    public val size: Int

    public val isEmpty: Boolean get() = size == 0

    /**
     * @param index to retrieve
     * @return item found at the index or `null` if index invalid
     */
    public operator fun get(index: Int): T?

    /**
     * @param range of data to extract
     * @return list of items in the range
     */
    public fun section (range: ClosedRange<Int>): List<T>

    /**
     * @param value to check
     * @return `true` if the item is in the model
     */
    public fun contains(value: T): Boolean
}

public typealias ModelObserver<T> = (source: DynamicListModel<T>, removed: Map<Int, T>, added: Map<Int, T>, moved: Map<Int, Pair<Int, T>>) -> Unit

/**
 * A model that can change over time. These models do not directly expose mutators,
 * but they admit to being mutable.
 */
public interface DynamicListModel<T>: ListModel<T> {
    /** Notifies of changes to the model */
    public val changed: Pool<ModelObserver<T>>
}

/**
 * A model that lets callers modify it.
 */
public interface MutableListModel<T>: DynamicListModel<T> {

    public operator fun set(index: Int, value: T): T?

    public fun notifyChanged(index: Int)

    public fun add        (value  : T                         )
    public fun add        (index  : Int, value: T             )
    public fun remove     (value  : T                         )
    public fun removeAt   (index  : Int                       ): T?
    public fun addAll     (values : Collection<T>             )
    public fun addAll     (index  : Int, values: Collection<T>)
    public fun removeAll  (values : Collection<T>             )
    public fun retainAll  (values : Collection<T>             )
    public fun removeAllAt(indexes: Collection<Int>           )
    public fun replaceAll (values : Collection<T>             )

    public fun clear()

    public fun sortWith          (comparator: Comparator<in T>)
    public fun sortWithDescending(comparator: Comparator<in T>)

    public fun <R: Comparable<R>> sortBy          (selector: (T) -> R?)
    public fun <R: Comparable<R>> sortByDescending(selector: (T) -> R?)
}

public fun <T: Comparable<T>> MutableListModel<T>.sort() {
    sortWith(naturalOrder())
}

public fun <T: Comparable<T>> MutableListModel<T>.sortDescending() {
    sortWithDescending(naturalOrder())
}

public open class SimpleListModel<T>(private val list: List<T>): ListModel<T> {

    override val size: Int get() = list.size

    override fun get     (index: Int             ): T?          = list.getOrNull(index)
    override fun section (range: ClosedRange<Int>): List<T>     = list.subList (range.start, range.endInclusive + 1)
    override fun contains(value: T               ): Boolean     = list.contains(value                              )
    override fun iterator(                       ): Iterator<T> = list.iterator(                                   )
}

public open class SimpleMutableListModel<T>(private val list: ObservableList<T>): SimpleListModel<T>(list), MutableListModel<T> {
    init {
        list.changed += { _,removed,added,moved ->
            (changed as SetPool).forEach {
                it(this, removed, added, moved)
            }
        }
    }

    override fun set(index: Int, value: T): T    = list.set(index, value)
    override fun add(value: T            ): Unit = list.add(value).run { Unit }
    override fun add(index: Int, value: T): Unit = list.add(index, value)

    override fun notifyChanged(index: Int): Unit = list.notifyChanged(index)

    override fun remove     (value  : T                         ): Unit = list.remove   (value        ).run { Unit }
    override fun removeAt   (index  : Int                       ): T    = list.removeAt (index        )
    override fun addAll     (values : Collection<T>             ): Unit = list.addAll   (values       ).run { Unit }
    override fun addAll     (index  : Int, values: Collection<T>): Unit = list.addAll   (index, values).run { Unit }
    override fun removeAll  (values : Collection<T>             ): Unit = list.removeAll(values       ).run { Unit }
    override fun retainAll  (values : Collection<T>             ): Unit = list.retainAll(values       ).run { Unit }
    override fun removeAllAt(indexes: Collection<Int>           ): Unit = list.batch { indexes.sortedDescending().forEach { list.removeAt(it) } }
    override fun replaceAll (values : Collection<T>             ): Unit = list.replaceAll(values).run { Unit }

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

public fun <T> mutableListModelOf(vararg elements: T): MutableListModel<T> = SimpleMutableListModel(mutableListOf(*elements))