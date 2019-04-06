package com.nectar.doodle.controls

import com.nectar.doodle.utils.ObservableSet
import com.nectar.doodle.utils.Pool
import com.nectar.doodle.utils.SetObserver

/**
 * Created by Nicholas Eddy on 3/19/18.
 */
interface SelectionModel<T>: Iterable<T> {
    val first  : T?
    val last   : T?
    val anchor : T?
    val size   : Int
    val isEmpty: Boolean

    fun add        (item : T            ): Boolean
    fun clear      (                    )
    fun addAll     (items: Collection<T>): Boolean
    fun remove     (item : T            ): Boolean
    fun contains   (item : T            ): Boolean
    fun removeAll  (items: Collection<T>): Boolean
    fun retainAll  (items: Collection<T>): Boolean
    fun replaceAll (items: Collection<T>): Boolean
    fun containsAll(items: Collection<T>): Boolean
    fun toggle     (items: Collection<T>): Boolean

    val changed: Pool<SetObserver<SelectionModel<T>, T>>
}

open class MultiSelectionModel<T>: SelectionModel<T> {

    private val set = LinkedHashSet<T>()
    private var anchor_: T? = null
    protected val observableSet by lazy { ObservableSet(this as SelectionModel<T>, set) }

    init {
        observableSet.changed += { set, removed, added ->
            if (set.size <= 1 || (anchor_ in removed && anchor_ !in added)) {
                anchor_ = null
            }
        }
    }

    override val size   get() = observableSet.size
    override val first  get() = observableSet.firstOrNull()
    override val last   get() = observableSet.lastOrNull ()
    override val anchor get() = anchor_ ?: first

    override val isEmpty get() = observableSet.isEmpty()

    override fun add        (item  : T           ) =                                               observableSet.add        (item )
    override fun clear      (                    ) =                                               observableSet.clear      (     )
    override fun addAll     (items: Collection<T>) =                                               observableSet.addAll     (items)
    override fun remove     (item  : T           ): Boolean { moveAnchorToDeleteSite(item); return observableSet.remove     (item ) }
    override fun iterator   (                    ) =                                               observableSet.iterator   (     )
    override fun contains   (item  : T           ) =                                               observableSet.contains   (item )
    override fun removeAll  (items: Collection<T>) =                                               observableSet.removeAll  (items)
    override fun retainAll  (items: Collection<T>) =                                               observableSet.retainAll  (items)
    override fun containsAll(items: Collection<T>) =                                               observableSet.containsAll(items)
    override fun replaceAll (items: Collection<T>) =                                               observableSet.replaceAll (items)
    override fun toggle     (items: Collection<T>): Boolean {
        var result = false

        observableSet.batch {
            items.forEach {
                result = remove(it)
                if (!result) {
                    result = add(it)
                }
            }
        }

        return result
    }

    override val changed = observableSet.changed

    private fun moveAnchorToDeleteSite(item: T) {
        if (anchor_ == item) {
            anchor_ = null
        } else {
            observableSet.indexOf(item).let {
                if (it >= 0) {
                    anchor_ = set.elementAtOrNull(it + 1)
                }
            }
        }
    }
}

class SingleItemSelectionModel<T>: MultiSelectionModel<T>() {
    override fun add(item: T): Boolean {
        var result = false

        observableSet.batch {
            clear()
            result = add(item)
        }

        return result
    }

    override fun addAll(items: Collection<T>): Boolean {
        if (observableSet.firstOrNull() in items) {
            return false
        }

        return items.lastOrNull()?.let { add(it) } ?: false
    }

    override fun replaceAll(items: Collection<T>) = items.lastOrNull()?.let { super.replaceAll(listOf(it)) } ?: false

    override fun toggle(items: Collection<T>): Boolean {
        var result = false

        observableSet.batch {
            items.forEach {
                result = remove(it)
                if (!result) {
                    clear()
                    result = add(it)
                }
            }
        }

        return result
    }
}