package com.nectar.doodle.controls.list

/**
 * Created by Nicholas Eddy on 3/19/18.
 */
class SingleItemSelectionModel: MultiSelectionModel() {
    override fun add(index: Int): Boolean {
        clear()
        return super.add(index)
    }

    override fun addAll(indices: Collection<Int>): Boolean {
        return if (indices.isEmpty()) false else add(indices.last())
    }
}