package io.nacular.doodle.controls.list

import io.nacular.doodle.controls.EditOperation
import io.nacular.doodle.controls.IndexedIem
import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.controls.ListModel
import io.nacular.doodle.controls.MutableListModel
import io.nacular.doodle.controls.SelectionModel
import io.nacular.doodle.controls.ViewVisualizer
import io.nacular.doodle.controls.mutableListModelOf
import io.nacular.doodle.core.View
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.PropertyObserversImpl
import io.nacular.doodle.utils.SortOrder
import io.nacular.doodle.utils.SortOrder.Ascending
import io.nacular.doodle.utils.SortOrder.Descending
import io.nacular.doodle.utils.observable


public interface ListEditor<T> {
    public fun edit(list: MutableList<T, *>, row: T, index: Int, current: View): EditOperation<T>
}

public inline fun <T> listEditor(crossinline block: (list: MutableList<T, *>, row: T, index: Int, current: View) -> EditOperation<T>): ListEditor<T> = object: ListEditor<T> {
    public override fun edit(list: MutableList<T, *>, row: T, index: Int, current: View): EditOperation<T> = block(list, row, index, current)
}

public open class MutableList<T, M: MutableListModel<T>>(
        model         : M,
        itemVisualizer: ItemVisualizer<T, IndexedIem>? = null,
        selectionModel: SelectionModel<Int>?           = null,
        fitContent    : Boolean                        = true,
        scrollCache   : Int                            = 10): DynamicList<T, M>(model, itemVisualizer, selectionModel, fitContent, scrollCache) {

    public val editing: Boolean get() = editingRow != null

    public var editor: ListEditor<T>? = null

    /** Notifies changes to [sortOrder] */
    public val sortingChanged: PropertyObservers<MutableList<T, M>, SortOrder?> by lazy { PropertyObserversImpl<MutableList<T, M>, SortOrder?>(this) }

    /** current sorting for the list default is ```null```.  */
    public var sortOrder: SortOrder? by observable(null, sortingChanged as PropertyObserversImpl<MutableList<T, M>, SortOrder?>)
        private set

    private var editingRow = null as Int?
        set(new) {
            field = new?.also { selectionModel?.replaceAll(setOf(it)) }
        }

    private var editOperation = null as EditOperation<T>?

    public operator fun set(index: Int, value: T) {
        if (value == model.set(index, value)) {
            // This is the case that the "new" value is the same as what was there
            // so need to explicitly update since the model won't fire a change
            update(children, index)
        }
    }

    public fun add      (value : T                         ): Unit = model.add      (value        )
    public fun add      (index : Int, values: T            ): Unit = model.add      (index, values)
    public fun remove   (value : T                         ): Unit = model.remove   (value        )
    public fun removeAt (index : Int                       ): T?   = model.removeAt (index        )
    public fun addAll   (values: Collection<T>             ): Unit = model.addAll   (values       )
    public fun addAll   (index : Int, values: Collection<T>): Unit = model.addAll   (index, values)
    public fun removeAll(values: Collection<T>             ): Unit = model.removeAll(values       )
    public fun retainAll(values: Collection<T>             ): Unit = model.retainAll(values       )
    public fun clear    (                                  ): Unit = model.clear()

    public fun startEditing(index: Int) {
        cancelEditing()

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

    public fun completeEditing() {
        editOperation?.let { operation ->
            editingRow?.let { index ->
                val result = operation.complete() ?: return

                cleanupEditing()

                this[index] = result
            }
        }
    }

    // FIXME: Cancel editing on selection/focus change
    public fun cancelEditing() {
        cleanupEditing()?.let { update(children, it) }
    }

    public fun sort(with: Comparator<T>) {
        model.sortWith(with)

        sortOrder = Ascending
    }

    public fun sortDescending(with: Comparator<T>) {
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

    public companion object {
        public operator fun invoke(
                progression   : IntProgression,
                itemVisualizer: ItemVisualizer<Int, IndexedIem>,
                selectionModel: SelectionModel<Int>? = null,
                fitContent    : Boolean              = true,
                scrollCache   : Int                  = 10): MutableList<Int, MutableListModel<Int>> =
                MutableList(progression.toMutableList(), itemVisualizer, selectionModel, fitContent, scrollCache)

        public inline operator fun <reified T> invoke(
                values        : kotlin.collections.List<T>,
                itemVisualizer: ItemVisualizer<T, IndexedIem>,
                selectionModel: SelectionModel<Int>? = null,
                fitContent    : Boolean              = true,
                scrollCache   : Int                  = 10): MutableList<T, MutableListModel<T>> =
                MutableList(mutableListModelOf(*values.toTypedArray()), itemVisualizer, selectionModel, fitContent, scrollCache)

        public operator fun invoke(
                values        : kotlin.collections.List<View>,
                selectionModel: SelectionModel<Int>? = null,
                fitContent    : Boolean              = true,
                scrollCache   : Int                  = 10): List<View, ListModel<View>> =
                MutableList(mutableListModelOf(*values.toTypedArray()), ViewVisualizer, selectionModel, fitContent, scrollCache)

        public operator fun  <T, M: MutableListModel<T>>invoke(
                model         : M,
                itemVisualizer: ItemVisualizer<T, IndexedIem>? = null,
                selectionModel: SelectionModel<Int>?           = null,
                fitContent    : Boolean                        = true,
                scrollCache   : Int                            = 10): MutableList<T, M> =
                MutableList(model, itemVisualizer, selectionModel, fitContent, scrollCache)

    }
}