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
     * @return the edit operation
     */
    public abstract fun editingStarted(dropdown: MutableDropdown<T, M>): EditOperation<T>?

    /**
     * Called whenever editing completes for the MutableDropdown. This lets the behavior reconfigure
     * the Dropdown accordingly.
     *
     * @param dropdown that was being edited
     */
    public abstract fun editingEnded(dropdown: MutableDropdown<T, M>)
}


public class MutableDropdown<T, M: MutableListModel<T>>(
        model             : M,
        boxItemVisualizer : ItemVisualizer<T, IndexedItem>? = null,
        listItemVisualizer: ItemVisualizer<T, IndexedItem>? = boxItemVisualizer,
): Dropdown<T, M>(model, boxItemVisualizer, listItemVisualizer), Editable {
    public override var value: T
        get(   ) = super.value
        set(new) { model[selection] = new }

    private var editOperation = null as EditOperation<T>?

    public val editing: Boolean get() = editOperation != null

    public var editor: DropdownEditor<T>? = null

    private val modelChanged: ModelObserver<T> = { _,_,_,_ ->
        behavior?.changed(this)
    }

    init {
        this.model.changed += modelChanged
    }

    public fun startEditing() {
        cancelEditing()

        editor?.let {
            editOperation = (behavior as? MutableDropdownBehavior<T, M>)?.editingStarted(this)
        }
    }

    public override fun completeEditing() {
        editOperation?.let { operation ->
            val result = operation.complete() ?: return

            cleanupEditing()

            value = result
        }
    }

    // FIXME: Cancel editing on selection/focus change
    public override fun cancelEditing() {
        cleanupEditing()
    }

    private fun cleanupEditing() {
        if (editing) {
            editOperation?.cancel()
            editOperation = null
            (behavior as? MutableDropdownBehavior<T, M>)?.editingEnded(this)
        }
    }

    public companion object {
//        public operator fun <T> invoke(values: List<T>, itemVisualizer: ItemVisualizer<T, Any>?   = null): MutableSpinner<T, io.nacular.doodle.controls.spinner.MutableListModel<T>> = MutableSpinner(io.nacular.doodle.controls.spinner.MutableListModel(values), itemVisualizer)
    }
}
