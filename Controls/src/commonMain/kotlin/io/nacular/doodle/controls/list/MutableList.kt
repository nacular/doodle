package io.nacular.doodle.controls.list

import io.nacular.doodle.controls.EditOperation
import io.nacular.doodle.controls.IndexedItemVisualizer
import io.nacular.doodle.controls.MutableListModel
import io.nacular.doodle.controls.SelectableItemVisualizer
import io.nacular.doodle.controls.SelectionModel
import io.nacular.doodle.controls.ignoreIndex
import io.nacular.doodle.controls.mutableListModelOf
import io.nacular.doodle.core.View
import io.nacular.doodle.utils.ObservableProperty
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.PropertyObserversImpl
import io.nacular.doodle.utils.SortOrder
import io.nacular.doodle.utils.SortOrder.Ascending
import io.nacular.doodle.utils.SortOrder.Descending


interface ListEditor<T> {
    fun edit(list: MutableList<T, *>, row: T, index: Int, current: View): EditOperation<T>
}

open class MutableList<T, M: MutableListModel<T>>(
        model         : M,
        itemGenerator : IndexedItemVisualizer<T>? = null,
        selectionModel: SelectionModel<Int>?      = null,
        fitContent    : Boolean                   = true,
        scrollCache   : Int                       = 10): DynamicList<T, M>(model, itemGenerator, selectionModel, fitContent, scrollCache) {

    val editing get() = editingRow != null

    var editor = null as ListEditor<T>?

    /** Notifies changes to [sortOrder] */
    val sortingChanged: PropertyObservers<MutableList<T, M>, SortOrder?> by lazy { PropertyObserversImpl<MutableList<T, M>, SortOrder?>(this) }

    /** current sorting for the list default is ```null```.  */
    var sortOrder: SortOrder? by ObservableProperty(null, { this }, sortingChanged as PropertyObserversImpl<MutableList<T, M>, SortOrder?>)
        private set

    private var editingRow = null as Int?
        set(new) {
            field = new?.also { selectionModel?.replaceAll(setOf(it)) }
        }

    private var editOperation = null as EditOperation<T>?

    operator fun set(index: Int, value: T) {
        if (value == model.set(index, value)) {
            // This is the case that the "new" value is the same as what was there
            // so need to explicitly update since the model won't fire a change
            update(children, index)
        }
    }

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

                this[index] = result
            }
        }
    }

    // FIXME: Cancel editing on selection/focus change
    fun cancelEditing() {
        cleanupEditing()?.let { update(children, it) }
    }

    fun sort(with: Comparator<T>) {
        model.sortWith(with)

        sortOrder = Ascending
    }

    fun sortDescending(with: Comparator<T>) {
        model.sortWithDescending(with)

        sortOrder = Descending
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
                itemGenerator : IndexedItemVisualizer<Int>,
                selectionModel: SelectionModel<Int>? = null,
                fitContent    : Boolean              = true,
                scrollCache   : Int                  = 10) =
                MutableList(progression.toMutableList(), itemGenerator, selectionModel, fitContent, scrollCache)

        operator fun invoke(
                progression   : IntProgression,
                itemGenerator : SelectableItemVisualizer<Int>,
                selectionModel: SelectionModel<Int>? = null,
                fitContent    : Boolean              = true,
                scrollCache   : Int                  = 10) =
                MutableList(progression.toMutableList(), itemGenerator, selectionModel, fitContent, scrollCache)

        inline operator fun <reified T> invoke(
                values        : kotlin.collections.List<T>,
                itemGenerator : IndexedItemVisualizer<T>,
                selectionModel: SelectionModel<Int>? = null,
                fitContent    : Boolean              = true,
                scrollCache   : Int                  = 10): MutableList<T, MutableListModel<T>> =
                MutableList(mutableListModelOf(*values.toTypedArray()), itemGenerator, selectionModel, fitContent, scrollCache)

        inline operator fun <reified T> invoke(
                values        : kotlin.collections.List<T>,
                itemGenerator : SelectableItemVisualizer<T>,
                selectionModel: SelectionModel<Int>? = null,
                fitContent    : Boolean              = true,
                scrollCache   : Int                  = 10): MutableList<T, MutableListModel<T>> =
                MutableList(mutableListModelOf(*values.toTypedArray()), itemGenerator, selectionModel, fitContent, scrollCache)


        operator fun  <T, M: MutableListModel<T>>invoke(model     : M,
                itemGenerator : SelectableItemVisualizer<T>?   = null,
                selectionModel: SelectionModel<Int>? = null,
                fitContent    : Boolean              = true,
                scrollCache   : Int                  = 10) = MutableList(model, itemGenerator?.let { ignoreIndex(it) }, selectionModel, fitContent, scrollCache)

    }
}