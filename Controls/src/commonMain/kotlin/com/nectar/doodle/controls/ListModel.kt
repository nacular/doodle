package com.nectar.doodle.controls

import com.nectar.doodle.utils.ObservableList
import com.nectar.doodle.utils.Pool
import com.nectar.doodle.utils.SetPool
import com.nectar.doodle.utils.sortWith
import com.nectar.doodle.utils.sortWithDescending

/**
 * Created by Nicholas Eddy on 3/19/18.
 */

interface ListModel<T>: Iterable<T> {
    val size: Int
    val isEmpty get() = size == 0

    operator fun get(index: Int): T?

    fun section (range: ClosedRange<Int>): List<T>
    fun contains(value: T               ): Boolean
}

typealias ModelObserver<T> = (source: DynamicListModel<T>, removed: Map<Int, T>, added: Map<Int, T>, moved: Map<Int, Pair<Int, T>>) -> Unit

interface DynamicListModel<T>: ListModel<T> {
    val changed: Pool<ModelObserver<T>>
}


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

open class SimpleMutableListModel<T>(list: MutableList<T> = mutableListOf()): SimpleListModel<T>(list), MutableListModel<T> {

    private val list by lazy { ObservableList(list) }

    init {
        this.list.changed += { _,removed,added,moved ->
            changed.forEach {
                it(this, removed, added, moved)
            }
        }
    }

    override fun set(index: Int, value: T) = list.set(index, value)
    override fun add(value: T            ) = list.add(value).run { Unit }
    override fun add(index: Int, value: T) = list.add(index, value)

    override fun remove     (value  : T                         ) = list.remove(value).run { Unit }
    override fun removeAt   (index  : Int                       ) = list.removeAt(index)
    override fun addAll     (values : Collection<T>             ) = list.addAll(values).run { Unit }
    override fun addAll     (index  : Int, values: Collection<T>) = list.addAll(index, values).run { Unit }
    override fun removeAll  (values : Collection<T>             ) = list.removeAll(values).run { Unit }
    override fun retainAll  (values : Collection<T>             ) = list.retainAll(values).run { Unit }
    override fun removeAllAt(indexes: Collection<Int>           ) = list.batch { indexes.sortedDescending().forEach { list.removeAt(it) } }

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
}

fun <T> mutableListModelOf(vararg elements: T): MutableListModel<T> = SimpleMutableListModel(mutableListOf(*elements))