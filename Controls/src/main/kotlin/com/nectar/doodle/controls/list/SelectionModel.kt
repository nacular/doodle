package com.nectar.doodle.controls.list

import com.nectar.doodle.utils.ObservableSet
import com.nectar.doodle.utils.SetObservers

/**
 * Created by Nicholas Eddy on 3/19/18.
 */
interface SelectionModel: Iterable<Int> {
    val size: Int

    fun add        (index  : Int            ): Boolean
    fun clear      (                        )
    fun addAll     (indices: Collection<Int>): Boolean
    fun remove     (index  : Int            ): Boolean
    fun isEmpty    (                        ): Boolean
    fun contains   (index  : Int            ): Boolean
    fun removeAll  (indices: Collection<Int>): Boolean
    fun retainAll  (indices: Collection<Int>): Boolean
    fun replaceAll (indices: Collection<Int>): Boolean
    fun containsAll(indices: Collection<Int>): Boolean

    val onChanged: SetObservers<SelectionModel, Int>
}

open class MultiSelectionModel: SelectionModel {

    private val set           = mutableSetOf<Int>()
    private val observableSet = ObservableSet(this as SelectionModel, set)

    override val size get() = observableSet.size

    override fun add        (index  : Int            ) = observableSet.add        (index  )

    override fun clear      (                        ) = observableSet.clear      (       )
    override fun addAll     (indices: Collection<Int>) = observableSet.addAll     (indices)
    override fun remove     (index  : Int            ) = observableSet.remove     (index  )
    override fun isEmpty    (                        ) = observableSet.isEmpty    (       )
    override fun iterator   (                        ) = observableSet.iterator   (       )
    override fun contains   (index  : Int            ) = observableSet.contains   (index  )
    override fun removeAll  (indices: Collection<Int>) = observableSet.removeAll  (indices)
    override fun retainAll  (indices: Collection<Int>) = observableSet.retainAll  (indices)
    override fun containsAll(indices: Collection<Int>) = observableSet.containsAll(indices)
    override fun replaceAll (indices: Collection<Int>) = observableSet.replaceAll (indices)

    override val onChanged = observableSet.onChange
}