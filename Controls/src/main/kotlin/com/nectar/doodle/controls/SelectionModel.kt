package com.nectar.doodle.controls

import com.nectar.doodle.utils.ObservableSet
import com.nectar.doodle.utils.Pool
import com.nectar.doodle.utils.SetObserver

/**
 * Created by Nicholas Eddy on 3/19/18.
 */
interface SelectionModel<T>: Iterable<T> {
    val size: Int

    val first: T?
    val last : T?

    fun add        (item  : T           ): Boolean
    fun clear      (                    )
    fun addAll     (items: Collection<T>): Boolean
    fun remove     (item  : T           ): Boolean
    fun isEmpty    (                    ): Boolean
    fun contains   (item  : T           ): Boolean
    fun removeAll  (items: Collection<T>): Boolean
    fun retainAll  (items: Collection<T>): Boolean
    fun replaceAll (items: Collection<T>): Boolean
    fun containsAll(items: Collection<T>): Boolean

    val changed: Pool<SetObserver<SelectionModel<T>, T>>
}

open class MultiSelectionModel<T>: SelectionModel<T> {

    private val set           = mutableSetOf<T>()
    private val observableSet by lazy { ObservableSet(this as SelectionModel<T>, set) }

    override val size  get() = observableSet.size
    override val first get() = set.firstOrNull()
    override val last  get() = set.lastOrNull()

    override fun add        (item  : T           ) = observableSet.add        (item )

    override fun clear      (                    ) = observableSet.clear      (     )
    override fun addAll     (items: Collection<T>) = observableSet.addAll     (items)
    override fun remove     (item  : T           ) = observableSet.remove     (item )
    override fun isEmpty    (                    ) = observableSet.isEmpty    (     )
    override fun iterator   (                    ) = observableSet.iterator   (     )
    override fun contains   (item  : T           ) = observableSet.contains   (item )
    override fun removeAll  (items: Collection<T>) = observableSet.removeAll  (items)
    override fun retainAll  (items: Collection<T>) = observableSet.retainAll  (items)
    override fun containsAll(items: Collection<T>) = observableSet.containsAll(items)
    override fun replaceAll (items: Collection<T>) = observableSet.replaceAll (items)

    override val changed = observableSet.changed
}

class SingleItemSelectionModel<T>: MultiSelectionModel<T>() {
    override fun add(item: T): Boolean {
        clear()
        return super.add(item)
    }

    override fun addAll(items: Collection<T>): Boolean {
        return if (items.isEmpty()) false else add(items.last())
    }
}