package io.nacular.doodle.controls.list

import io.nacular.doodle.controls.EditOperation
import io.nacular.doodle.controls.IndexedItem
import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.controls.ListModel
import io.nacular.doodle.controls.MutableListModel
import io.nacular.doodle.controls.SelectionModel
import io.nacular.doodle.controls.ViewVisualizer
import io.nacular.doodle.controls.mutableListModelOf
import io.nacular.doodle.core.View
import io.nacular.doodle.utils.Editable
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.PropertyObserversImpl
import io.nacular.doodle.utils.SortOrder
import io.nacular.doodle.utils.SortOrder.Ascending
import io.nacular.doodle.utils.SortOrder.Descending
import io.nacular.doodle.utils.observable


/**
 * Manages editing for a [MutableList].
 */
public interface ListEditor<T> {
    /**
     * Called to initiate editing of a [MutableList].
     *
     * @param list to be edited
     * @param item being edited within the list
     * @param index of the item to be edited
     * @param current View being used to display the item in the list
     * @return an edit operation
     */
    public fun edit(list: MutableList<T, *>, item: T, index: Int, current: View): EditOperation<T>
}

/**
 * Creates a [ListEditor] that calls [block] to perform its edit operation.
 *
 * @param block that performs edit operation
 */
public inline fun <T> listEditor(crossinline block: (list: MutableList<T, *>, item: T, index: Int, current: View) -> EditOperation<T>): ListEditor<T> = object: ListEditor<T> {
    override fun edit(list: MutableList<T, *>, item: T, index: Int, current: View): EditOperation<T> = block(list, item, index, current)
}

/**
 * A [DynamicList] component that renders a mutable list of items of type [T] using a [ListBehavior]. Items are obtained via
 * the [model] and selection is managed via the optional [selectionModel]. Large ("infinite") lists are supported
 * efficiently, since List recycles the Views generated to render its items.
 *
 * MutableList does not provide scrolling internally, so it should be embedded in a [ScrollPanel][io.nacular.doodle.controls.panels.ScrollPanel] or similar component if needed.
 *
 * @param model that holds the data for this List
 * @param itemVisualizer that maps [T] to [View] for each item in the List
 * @param selectionModel that manages the List's selection state
 * @param fitContent determines whether the List scales to fit it's items width and total height
 * @param scrollCache determining how many "hidden" items are rendered above and below the List's view-port. A value of 0 means
 * only visible items are rendered, but quick scrolling is more likely to show blank areas.
 */
public open class MutableList<T, M: MutableListModel<T>>(
        model         : M,
        itemVisualizer: ItemVisualizer<T, IndexedItem>? = null,
        selectionModel: SelectionModel<Int>?            = null,
        fitContent    : Boolean                         = true,
        scrollCache   : Int                             = 10): DynamicList<T, M>(model, itemVisualizer, selectionModel, fitContent, scrollCache), Editable {

    /**
     * Indicates whether the list is currently being edited.
     */
    public val editing: Boolean get() = editingItem != null

    /**
     * Controls whether and how the list can be edited. The list will not be editable without an editor specified.
     */
    public var editor: ListEditor<T>? = null

    /** Notifies changes to [sortOrder] */
    public val sortingChanged: PropertyObservers<MutableList<T, M>, SortOrder?> by lazy { PropertyObserversImpl<MutableList<T, M>, SortOrder?>(this) }

    /** current sorting for the list default is ```null```.  */
    public var sortOrder: SortOrder? by observable(null, sortingChanged as PropertyObserversImpl<MutableList<T, M>, SortOrder?>)
        private set

    private var editingItem = null as Int?
        set(new) {
            field = new?.also { selectionModel?.replaceAll(setOf(it)) }
        }

    private var editOperation = null as EditOperation<T>?

    /**
     * Sets the value at [index] in the underlying [model]. This will result in changes to the list as well if the
     * Model accepts the change.
     */
    public operator fun set(index: Int, value: T) {
        if (value == model.set(index, value).getOrNull()) {
            // This is the case that the "new" value is the same as what was there
            // so need to explicitly update since the model won't fire a change
            update(children, index)
        }
    }

    public fun add      (value : T                         ): Unit      = model.add      (value        )
    public fun add      (index : Int, values: T            ): Unit      = model.add      (index, values)
    public fun remove   (value : T                         ): Unit      = model.remove   (value        )
    public fun removeAt (index : Int                       ): Result<T> = model.removeAt (index        )
    public fun addAll   (values: Collection<T>             ): Unit      = model.addAll   (values       )
    public fun addAll   (index : Int, values: Collection<T>): Unit      = model.addAll   (index, values)
    public fun removeAll(values: Collection<T>             ): Unit      = model.removeAll(values       )
    public fun retainAll(values: Collection<T>             ): Unit      = model.retainAll(values       )
    public fun clear    (                                  ): Unit      = model.clear    (             )

    /**
     * Initiates editing of the list at the given [index]. This will cancel any other edit operation and
     * begin a new one if an [editor] is present.
     */
    public fun startEditing(index: Int) {
        cancelEditing()

        editor?.let {
            model[index].onSuccess { item ->
                val i = index % children.size

                editingItem    = index
                editOperation = it.edit(this, item, index, children[i]).also {
                    it()?.let { children[i] = it }

                    layout(children[i], item, index)
                }
            }
        }
    }

    /**
     * Indicates that editing is now complete. The current edit operation is terminated and its result is incorporated
     * into the list.
     */
    public override fun completeEditing() {
        editOperation?.let { operation ->
            editingItem?.let { index ->
                operation.complete().onSuccess {
                    cleanupEditing()
                    this[index] = it
                }
            }
        }
    }

    /**
     * Cancels the current edit operation and discards its result.
     */
    public override fun cancelEditing() {
        // FIXME: Cancel editing on selection/focus change
        cleanupEditing()?.let { update(children, it) }
    }

    /**
     * Sorts the list with the given comparator. This causes the underlying [model] to also be sorted.
     */
    public fun sort(with: Comparator<T>) {
        model.sortWith(with)

        sortOrder = Ascending
    }

    /**
     * Sorts the list (descending) with the given comparator. This causes the underlying [model] to also be sorted.
     */
    public fun sortDescending(with: Comparator<T>) {
        model.sortWithDescending(with)

        sortOrder = Descending
    }

    private fun cleanupEditing(): Int? {
        editOperation?.cancel()
        val result    = editingItem
        editOperation = null
        editingItem   = null
        return result
    }

    public companion object {
        public operator fun invoke(
                progression   : IntProgression,
                itemVisualizer: ItemVisualizer<Int, IndexedItem>,
                selectionModel: SelectionModel<Int>? = null,
                fitContent    : Boolean              = true,
                scrollCache   : Int                  = 10): MutableList<Int, MutableListModel<Int>> =
                MutableList(progression.toMutableList(), itemVisualizer, selectionModel, fitContent, scrollCache)

        public inline operator fun <reified T> invoke(
                values        : kotlin.collections.List<T>,
                itemVisualizer: ItemVisualizer<T, IndexedItem>,
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
                itemVisualizer: ItemVisualizer<T, IndexedItem>? = null,
                selectionModel: SelectionModel<Int>?           = null,
                fitContent    : Boolean                        = true,
                scrollCache   : Int                            = 10): MutableList<T, M> =
                MutableList(model, itemVisualizer, selectionModel, fitContent, scrollCache)

    }
}