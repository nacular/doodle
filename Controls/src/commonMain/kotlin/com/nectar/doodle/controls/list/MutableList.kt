package com.nectar.doodle.controls.list

import com.nectar.doodle.controls.EditOperation
import com.nectar.doodle.controls.ItemVisualizer
import com.nectar.doodle.controls.MutableListModel
import com.nectar.doodle.controls.SelectionModel
import com.nectar.doodle.controls.SimpleMutableListModel
import com.nectar.doodle.core.View


interface ListEditor<T> {
    fun edit(list: MutableList<T, *>, row: T, index: Int, current: View): EditOperation<T>
}

open class MutableList<T, M: MutableListModel<T>>(
        model         : M,
        itemGenerator : ItemVisualizer<T>?    = null,
        selectionModel: SelectionModel<Int>? = null,
        fitContent    : Boolean              = true,
        scrollCache   : Int                  = 10): DynamicList<T, M>(model, itemGenerator, selectionModel, fitContent, scrollCache) {

    val editing get() = editingRow != null

    var editor = null as ListEditor<T>?

    private var editingRow = null as Int?
        set(new) {
            field = new?.also { selectionModel?.replaceAll(setOf(it)) }
        }

    private var editOperation = null as EditOperation<T>?

    fun add      (value : T                         ) = model.add      (value        )
    fun add      (index : Int, values: T            ) = model.add      (index, values)
    fun remove   (value : T                         ) = model.remove   (value        )
    fun removeAt (index : Int                       ) = model.removeAt (index        )
    fun addAll   (values: Collection<T>             ) = model.addAll   (values       )
    fun addAll   (index : Int, values: Collection<T>) = model.addAll   (index, values)
    fun removeAll(values: Collection<T>             ) = model.removeAll(values       )
    fun retainAll(values: Collection<T>             ) = model.retainAll(values       )
    fun clear    (                                  ) = model.clear()

    fun startEditing(index: Int) {
        editor?.let {
            model[index]?.let { row ->
                val i = index % children.size

                editingRow    = index
                editOperation = it.edit(this, row, index, children[i]).also {
                    it()?.let { children[i] = it }

                    layout(children[i], row, index)
                }
            }
        }
    }

    fun completeEditing() {
        editOperation?.let { operation ->
            editingRow?.let { index ->
                val result = operation.complete() ?: return

                cleanupEditing()

                if (result == model.set(index, result)) {
                    // This is the case that the "new" value is the same as what was there
                    // so need to explicitly update since the model won't fire a change
                    update(children, index)
                }
            }
        }
    }

    fun cancelEditing() {
        cleanupEditing()?.let { update(children, it) }
    }

    private fun cleanupEditing(): Int? {
        editOperation?.cancel()
        val result    = editingRow
        editOperation = null
        editingRow    = null
        return result
    }

    companion object {
        operator fun invoke(
                progression   : IntProgression,
                itemGenerator : ItemVisualizer<Int>,
                selectionModel: SelectionModel<Int>? = null,
                fitContent    : Boolean              = true,
                scrollCache   : Int                  = 10) =
                MutableList(progression.toMutableList(), itemGenerator, selectionModel, fitContent, scrollCache)

        operator fun <T> invoke(
                values        : kotlin.collections.List<T>,
                itemGenerator : ItemVisualizer<T>,
                selectionModel: SelectionModel<Int>? = null,
                fitContent    : Boolean              = true,
                scrollCache   : Int                  = 10): MutableList<T, SimpleMutableListModel<T>> =
                MutableList(SimpleMutableListModel(values.toMutableList()), itemGenerator, selectionModel, fitContent, scrollCache)
    }
}