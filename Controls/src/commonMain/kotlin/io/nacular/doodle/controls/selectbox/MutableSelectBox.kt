package io.nacular.doodle.controls.selectbox

import io.nacular.doodle.controls.EditOperation
import io.nacular.doodle.controls.IndexedItem
import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.controls.ModelObserver
import io.nacular.doodle.controls.MutableListModel
import io.nacular.doodle.core.View
import io.nacular.doodle.utils.Editable

/**
 * Defines the [EditOperation] for a [MutableSelectBox].
 */
public interface SelectBoxEditor<T> {
    /**
     * @param selectBox being edited
     * @param value within [selectBox] that is being edited
     * @param current View used to represent that value
     * @return an edit operation to manage changing [value]
     */
    public fun edit(selectBox: MutableSelectBox<T, *>, value: T, current: View): EditOperation<T>
}

/**
 * Creates a [SelectBoxEditor] from the given lambda [block].
 *
 * @param block to execute to create the [EditOperation]
 * @return an edit operation
 */
public inline fun <T> selectBoxEditor(crossinline block: (selectBox: MutableSelectBox<T, *>, value: T, current: View) -> EditOperation<T>): SelectBoxEditor<T> = object: SelectBoxEditor<T> {
    override fun edit(selectBox: MutableSelectBox<T, *>, value: T, current: View): EditOperation<T> = block(selectBox, value, current)
}

/**
 * Provides presentation and behavior customization for [MutableSelectBox].
 */
public abstract class MutableSelectBoxBehavior<T, M: MutableListModel<T>>: SelectBoxBehavior<T, M> {
    /**
     * Called whenever editing begins for the MutableDropdown. This lets the behavior reconfigure
     * the Dropdown accordingly.
     *
     * @param selectBox being edited
     * @param value being edited
     * @return the edit operation
     */
    public abstract fun editingStarted(selectBox: MutableSelectBox<T, M>, value: T): EditOperation<T>?

    /**
     * Called whenever editing completes for the MutableDropdown. This lets the behavior reconfigure
     * the Dropdown accordingly.
     *
     * @param selectBox that was being edited
     */
    public abstract fun editingEnded(selectBox: MutableSelectBox<T, M>)
}

/**
 * A selectbox control that can be edited.
 * @see [SelectBox]
 *
 * @param model used to represent the underlying choices
 * @param boxItemVisualizer to render the selected item within the select box
 * @param listItemVisualizer to render each item within the list of choices
 */
public class MutableSelectBox<T, M: MutableListModel<T>>(
        model             : M,
        boxItemVisualizer : ItemVisualizer<T, IndexedItem>? = null,
        listItemVisualizer: ItemVisualizer<T, IndexedItem>? = boxItemVisualizer,
): SelectBox<T, M>(model, boxItemVisualizer, listItemVisualizer), Editable {
    public fun set(value: T) {
        model[selection] = value
    }

    /** Indicates whether there is an ongoing edit operation */
    public val editing: Boolean get() = editOperation != null

    /** Controls how editing is done */
    public var editor: SelectBoxEditor<T>? = null

    private var editOperation = null as EditOperation<T>?

    private val modelChanged: ModelObserver<T> = { _,_ ->
        behavior?.changed(this)
    }

    init {
        this.model.changed += modelChanged
    }

    /**
     * Initiate editing of the control. This will notify the `Behavior` that editing is starting. The behavior
     * can then use the `editor` to control the process.
     *
     * NOTE: this has no effect if no `editor` or `behavior` are installed.
     */
    public fun startEditing() {
        cancelEditing()

        value.getOrNull()?.let { value ->
            editor?.let {
                editOperation = (behavior as? MutableSelectBoxBehavior<T, M>)?.editingStarted(this, value)
            }
        }
    }

    /**
     * Requests that the existing edit operation be completed. This will try to fetch a valid
     * result and update the underlying value if possible.
     */
    public override fun completeEditing() {
        editOperation?.let { operation ->
            operation.complete().onSuccess {
                cleanupEditing()
                set(it)
            }
        }
    }

    /**
     * Requests that the existing edit operation be canceled. This will discard the current
     * process and retain the underlying value before editing started.
     */
    public override fun cancelEditing() {
        // FIXME: Cancel editing on selection/focus change
        cleanupEditing()
    }

    private fun cleanupEditing() {
        if (editing) {
            editOperation?.cancel()
            editOperation = null
            (behavior as? MutableSelectBoxBehavior<T, M>)?.editingEnded(this)
        }
    }
}
