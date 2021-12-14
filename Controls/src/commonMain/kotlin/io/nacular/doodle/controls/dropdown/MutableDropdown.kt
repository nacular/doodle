package io.nacular.doodle.controls.dropdown

import io.nacular.doodle.controls.EditOperation
import io.nacular.doodle.controls.IndexedItem
import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.controls.ModelObserver
import io.nacular.doodle.controls.MutableListModel
import io.nacular.doodle.controls.spinner.Model
import io.nacular.doodle.core.View
import io.nacular.doodle.utils.Editable

public interface DropdownEditor<T> {
    public fun edit(dropdown: MutableDropdown<T, *>, value: T, current: View): EditOperation<T>
}

public inline fun <T> dropdownEditor(crossinline block: (dropdown: MutableDropdown<T, *>, value: T, current: View) -> EditOperation<T>): DropdownEditor<T> = object: DropdownEditor<T> {
    override fun edit(dropdown: MutableDropdown<T, *>, value: T, current: View): EditOperation<T> = block(dropdown, value, current)
}

public abstract class MutableDropdownBehavior<T, M: MutableListModel<T>>: DropdownBehavior<T, M> {
    /**
     * Called whenever editing begins for the MutableDropdown. This lets the behavior reconfigure
     * the Dropdown accordingly.
     *
     * @param dropdown being edited
     * @param value being edited
     * @return the edit operation
     */
    public abstract fun editingStarted(dropdown: MutableDropdown<T, M>, value: T): EditOperation<T>?

    /**
     * Called whenever editing completes for the MutableDropdown. This lets the behavior reconfigure
     * the Dropdown accordingly.
     *
     * @param dropdown that was being edited
     */
    public abstract fun editingEnded(dropdown: MutableDropdown<T, M>)
}

/**
 * A dropdown control that can be edited.
 * @see [Dropdown]
 *
 * @param model used to represent the underlying choices
 * @param boxItemVisualizer to render the selected item within the drop-down box
 * @param listItemVisualizer to render each item within the list of choices
 */
public class MutableDropdown<T, M: MutableListModel<T>>(
        model             : M,
        boxItemVisualizer : ItemVisualizer<T, IndexedItem>? = null,
        listItemVisualizer: ItemVisualizer<T, IndexedItem>? = boxItemVisualizer,
): Dropdown<T, M>(model, boxItemVisualizer, listItemVisualizer), Editable {
    public fun set(value: T) {
        model[selection] = value
    }

    /** Indicates whether there is an ongoing edit operation */
    public val editing: Boolean get() = editOperation != null

    /** Controls how editing is done */
    public var editor: DropdownEditor<T>? = null

    private var editOperation = null as EditOperation<T>?

    private val modelChanged: ModelObserver<T> = { _,_,_,_ ->
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
                editOperation = (behavior as? MutableDropdownBehavior<T, M>)?.editingStarted(this, value)
            }
        }
    }

    /**
     * Requests that the existing edit operation be completed. This will try to fetch a valid
     * result and update the underlying value if possible.
     */
    public override fun completeEditing() {
        editOperation?.let { operation ->
            val result = operation.complete() ?: return

            cleanupEditing()

            set(result)
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
            (behavior as? MutableDropdownBehavior<T, M>)?.editingEnded(this)
        }
    }
}
