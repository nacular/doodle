package com.nectar.doodle.controls.list

/**
 * Created by Nicholas Eddy on 3/19/18.
 */
class SingleItemSelectionModel: MultiSelectionModel() {
    override fun add(element: Int): Boolean {
        clear()
        return super.add(element)
    }

    override fun addAll(elements: Collection<Int>): Boolean {
        return if (elements.isEmpty()) false else add(elements.last())
    }
}