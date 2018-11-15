package com.nectar.doodle.controls.list

import com.nectar.doodle.utils.ObservableList
import com.nectar.doodle.utils.Pool
import com.nectar.doodle.utils.SetPool

/**
 * Created by Nicholas Eddy on 3/19/18.
 */

interface Model<T>: Iterable<T> {
    val size: Int

    operator fun get(index: Int): T?

    fun isEmpty (                       ) = size == 0
    fun section (range: ClosedRange<Int>): kotlin.collections.List<T>
    fun contains(value: T               ): Boolean
}

typealias ModelObserver<T> = (source: MutableModel<T>, removed: Map<Int, T>, added: Map<Int, T>, moved: Map<Int, Pair<Int, T>>) -> Unit


interface MutableModel<T>: Model<T> {

    operator fun set(index: Int, value: T): T

    fun add        (value  : T                         )
    fun add        (index  : Int, values: T            )
    fun remove     (value  : T                         )
    fun removeAt   (index  : Int                       ): T?
    fun addAll     (values : Collection<T>             )
    fun addAll     (index  : Int, values: Collection<T>)
    fun removeAll  (values : Collection<T>             )
    fun retainAll  (values : Collection<T>             )
    fun removeAllAt(indexes: Collection<Int>           )

    fun clear()

    val changed: Pool<ModelObserver<T>>
}

open class ListModel<T>(private val list: kotlin.collections.List<T>): Model<T> {

    override val size get() = list.size

    override fun get     (index: Int             ) = list.getOrNull(index)
    override fun section (range: ClosedRange<Int>) = list.subList (range.start, range.endInclusive + 1)
    override fun contains(value: T               ) = list.contains(value                              )
    override fun iterator(                       ) = list.iterator(                                   )
}

open class MutableListModel<T>(list: kotlin.collections.MutableList<T> = mutableListOf()): ListModel<T>(list), MutableModel<T> {

    private val list by lazy { ObservableList(this, list) }

    init {
        this.list.changed += { _,removed,added,moved ->
            changed.forEach {
                it(this, removed, added, moved)
            }
        }
    }

    override fun set(index: Int, value: T ) = list.set(index, value)
    override fun add(value: T             ) = list.add(value).run { Unit }
    override fun add(index: Int, values: T) = list.add(index, values)

    override fun remove     (value  : T                         ) = list.remove(value).run { Unit }
    override fun removeAt   (index  : Int                       ) = list.removeAt(index)
    override fun addAll     (values : Collection<T>             ) = list.addAll(values).run { Unit }
    override fun addAll     (index  : Int, values: Collection<T>) = list.addAll(index, values).run { Unit }
    override fun removeAll  (values : Collection<T>             ) = list.removeAll(values).run { Unit }
    override fun retainAll  (values : Collection<T>             ) = list.retainAll(values).run { Unit }
    override fun removeAllAt(indexes: Collection<Int>           ) = list.batch { indexes.forEach { list.removeAt(it) } }

    override fun clear() = list.clear()

    override val changed = SetPool<ModelObserver<T>>()
}