package io.nacular.doodle.controls.spinner

import io.nacular.doodle.controls.EditOperation
import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.core.View
import io.nacular.doodle.utils.Editable


public interface MutableModel<T>: Model<T> {
    public override var value: T
}

public interface SpinnerEditor<T> {
    public fun edit(spinner: MutableSpinner<T, *>, value: T, current: View): EditOperation<T>
}

public inline fun <T> spinnerEditor(crossinline block: (spinner: MutableSpinner<T, *>, value: T, current: View) -> EditOperation<T>): SpinnerEditor<T> = object: SpinnerEditor<T> {
    override fun edit(spinner: MutableSpinner<T, *>, value: T, current: View): EditOperation<T> = block(spinner, value, current)
}

public abstract class MutableSpinnerBehavior<T, M: MutableModel<T>>: SpinnerBehavior<T, M>() {
    /**
     * Called whenever editing begins for the MutableSpinner. This lets the behavior reconfigure
     * the Spinner accordingly.
     *
     * @param spinner being edited
     * @return the edit operation
     */
    public abstract fun editingStarted(spinner: MutableSpinner<T, M>): EditOperation<T>?

    /**
     * Called whenever editing completes for the MutableSpinner. This lets the behavior reconfigure
     * the Spinner accordingly.
     *
     * @param spinner that was being edited
     */
    public abstract fun editingEnded(spinner: MutableSpinner<T, M>)
}


public class MutableSpinner<T, M: MutableModel<T>>(model: M, itemVisualizer: ItemVisualizer<T, Spinner<T, M>>? = null): Spinner<T, M>(model, itemVisualizer), Editable {
    override var value: T
        get(   ) = super.value
        set(new) { model.value = new }

    private var editOperation = null as EditOperation<T>?

    public val editing: Boolean get() = editOperation != null

    public var editor: SpinnerEditor<T>? = null

    public fun startEditing() {
        cancelEditing()

        editor?.let {
            editOperation = (behavior as? MutableSpinnerBehavior<T,M>)?.editingStarted(this)
        }
    }

    public override fun completeEditing() {
        editOperation?.let { operation ->
            val result = operation.complete() ?: return

            cleanupEditing()

            model.value = result
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
            (behavior as? MutableSpinnerBehavior<T, M>)?.editingEnded(this)
        }
    }

    public companion object {
        public operator fun <T> invoke(values: List<T>, itemVisualizer: ItemVisualizer<T, Any>?   = null): MutableSpinner<T, MutableListModel<T>> = MutableSpinner(MutableListModel(values), itemVisualizer)
    }
}