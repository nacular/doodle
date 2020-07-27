package io.nacular.doodle.controls

import io.nacular.doodle.utils.ObservableList
import io.nacular.doodle.utils.Pool
import io.nacular.doodle.utils.SetPool
import io.nacular.doodle.utils.sortWith
import io.nacular.doodle.utils.sortWithDescending

/**
 * Holds data in a list-like structure for use with controls like [List][io.nacular.doodle.controls.list.List].
 */
interface ListModel<T>: Iterable<T> {
    /** Number of elements in the model */
    val size: Int

    val isEmpty get() = size == 0

    /**
     * @param index to retrieve
     * @return item found at the index or `null` if index invalid
     */
    operator fun get(index: Int): T?

    /**
     * @param range of data to extract
     * @return list of items in the range
     */
    fun section (range: ClosedRange<Int>): List<T>

    /**
     * @param value to check
     * @return `true` if the item is in the model
     */
    fun contains(value: T): Boolean
}

typealias ModelObserver<T> = (source: DynamicListModel<T>, removed: Map<Int, T>, added: Map<Int, T>, moved: Map<Int, Pair<Int, T>>) -> Unit

/**
 * A model that can change over time. These models do not directly expose mutators,
 * but they admit to being mutable.
 */
interface DynamicListModel<T>: ListModel<T> {
    /** Notifies of changes to the model */
    val changed: Pool<ModelObserver<T>>
}

/**
 * A model that lets callers modify it.
 */
interface MutableListModel<T>: DynamicListModel<T> {

    operator fun set(index: Int, value: T): T?

    fun add        (value  : T                         )
    fun add        (index  : Int, value: T             )
    fun remove     (value  : T                         )
    fun removeAt   (index  : Int                       ): T?
    fun addAll     (values : Collection<T>             )
    fun addAll     (index  : Int, values: Collection<T>)
    fun removeAll  (values : Collection<T>             )
    fun retainAll  (values : Collection<T>             )
    fun removeAllAt(indexes: Collection<Int>           )
    fun replaceAll (values : Collection<T>             )

    fun clear()

    fun sortWith          (comparator: Comparator<in T>)
    fun sortWithDescending(comparator: Comparator<in T>)

    fun <R: Comparable<R>> sortBy          (selector: (T) -> R?)
    fun <R: Comparable<R>> sortByDescending(selector: (T) -> R?)
}

fun <T: Comparable<T>> MutableListModel<T>.sort() {
    sortWith(naturalOrder())
}

fun <T: Comparable<T>> MutableListModel<T>.sortDescending() {
    sortWithDescending(naturalOrder())
}

open class SimpleListModel<T>(private val list: List<T>): ListModel<T> {

    override val size get() = list.size

    override fun get     (index: Int             ) = list.getOrNull(index)
    override fun section (range: ClosedRange<Int>) = list.subList (range.start, range.endInclusive + 1)
    override fun contains(value: T               ) = list.contains(value                              )
    override fun iterator(                       ) = list.iterator(                                   )
}

open class SimpleMutableListModel<T> private constructor(private val list: ObservableList<T>): SimpleListModel<T>(list), MutableListModel<T> {
    init {
        list.changed += { _,removed,added,moved ->
            changed.forEach {
                it(this, removed, added, moved)
            }
        }
    }

    override fun set(index: Int, value: T) = list.set(index, value)
    override fun add(value: T            ) = list.add(value).run { Unit }
    override fun add(index: Int, value: T) = list.add(index, value)

    override fun remove     (value  : T                         ) = list.remove   (value        ).run { Unit }
    override fun removeAt   (index  : Int                       ) = list.removeAt (index        )
    override fun addAll     (values : Collection<T>             ) = list.addAll   (values       ).run { Unit }
    override fun addAll     (index  : Int, values: Collection<T>) = list.addAll   (index, values).run { Unit }
    override fun removeAll  (values : Collection<T>             ) = list.removeAll(values       ).run { Unit }
    override fun retainAll  (values : Collection<T>             ) = list.retainAll(values       ).run { Unit }
    override fun removeAllAt(indexes: Collection<Int>           ) = list.batch { indexes.sortedDescending().forEach { list.removeAt(it) } }
    override fun replaceAll (values : Collection<T>             ) = list.replaceAll(values).run { Unit }

    override fun clear() = list.clear()

    override val changed = SetPool<ModelObserver<T>>()

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

    companion object {
        operator fun <T> invoke(list: List<T> = emptyList()): SimpleMutableListModel<T> = SimpleMutableListModel(ObservableList(list))
    }
}

fun <T> mutableListModelOf(vararg elements: T): MutableListModel<T> = SimpleMutableListModel(mutableListOf(*elements))