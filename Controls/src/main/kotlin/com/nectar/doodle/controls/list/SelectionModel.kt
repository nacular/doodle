package com.nectar.doodle.controls.list

import com.nectar.doodle.utils.ObservableSet
import com.nectar.doodle.utils.SetObservers

/**
 * Created by Nicholas Eddy on 3/19/18.
 */
interface SelectionModel: Iterable<Int> {
    val size: Int

    fun add        (element : Int            ): Boolean
    fun clear      (                         )
    fun addAll     (elements: Collection<Int>): Boolean
    fun remove     (element : Int            ): Boolean
    fun isEmpty    (                         ): Boolean
    fun contains   (element : Int            ): Boolean
    fun removeAll  (elements: Collection<Int>): Boolean
    fun retainAll  (elements: Collection<Int>): Boolean
    fun replaceAll (elements: Collection<Int>): Boolean
    fun containsAll(elements: Collection<Int>): Boolean

    val onChanged: SetObservers<SelectionModel, Int>
}

open class MultiSelectionModel(private val set: MutableSet<Int>): SelectionModel {
    private val observableSet = ObservableSet(this as SelectionModel, set)

    override val size get() = observableSet.size

    override fun add        (element : Int            ) = observableSet.add        (element )

    override fun clear      (                         ) = observableSet.clear      (        )
    override fun addAll     (elements: Collection<Int>) = observableSet.addAll     (elements)
    override fun remove     (element : Int            ) = observableSet.remove     (element )
    override fun isEmpty    (                         ) = observableSet.isEmpty    (        )
    override fun iterator   (                         ) = observableSet.iterator   (        )
    override fun contains   (element : Int            ) = observableSet.contains   (element )
    override fun removeAll  (elements: Collection<Int>) = observableSet.removeAll  (elements)
    override fun retainAll  (elements: Collection<Int>) = observableSet.retainAll  (elements)
    override fun containsAll(elements: Collection<Int>) = observableSet.containsAll(elements)
    override fun replaceAll (elements: Collection<Int>): Boolean {
        set.clear()

        return observableSet.addAll(elements)
    }

    override val onChanged = observableSet.onChange
}