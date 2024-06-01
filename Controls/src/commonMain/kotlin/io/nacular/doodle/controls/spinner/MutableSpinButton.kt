package io.nacular.doodle.controls.spinner

import io.nacular.doodle.controls.EditOperation
import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.core.View
import io.nacular.doodle.utils.Editable

public interface MutableSpinButtonModel<T>: SpinButtonModel<T> {
    public override var value: T
}

public interface SpinButtonEditor<T> {
    public fun edit(spinner: MutableSpinButton<T, *>, value: T, current: View): EditOperation<T>
}

public inline fun <T> spinButtonEditor(crossinline block: (spinner: MutableSpinButton<T, *>, value: T, current: View) -> EditOperation<T>): SpinButtonEditor<T> = object: SpinButtonEditor<T> {
    override fun edit(spinner: MutableSpinButton<T, *>, value: T, current: View): EditOperation<T> = block(spinner, value, current)
}

public abstract class MutableSpinButtonBehavior<T, M: MutableSpinButtonModel<T>>: SpinButtonBehavior<T, M>() {
    /**
     * Called whenever editing begins for the [MutableSpinButton]. This lets the behavior reconfigure
     * the SpinButton accordingly.
     *
     * @param spinner being edited
     * @param value being edited
     * @return the edit operation
     */
    public abstract fun editingStarted(spinner: MutableSpinButton<T, M>, value: T): EditOperation<T>?

    /**
     * Called whenever editing completes for the [MutableSpinButton]. This lets the behavior reconfigure
     * the SpinButton accordingly.
     *
     * @param spinner that was being edited
     */
    public abstract fun editingEnded(spinner: MutableSpinButton<T, M>)
}

public class MutableSpinButton<T, M: MutableSpinButtonModel<T>>(model: M, itemVisualizer: ItemVisualizer<T, SpinButton<T, M>>? = null): SpinButton<T, M>(model, itemVisualizer), Editable {
    public fun set(value: T) {
        model.value = value
    }

    private var editOperation = null as EditOperation<T>?

    public val editing: Boolean get() = editOperation != null

    public var editor: SpinButtonEditor<T>? = null

    public fun startEditing() {
        cancelEditing()

        value.onSuccess { value ->
            editor?.let {
                editOperation = (behavior as? MutableSpinButtonBehavior<T,M>)?.editingStarted(this, value)
            }
        }
    }

    public override fun completeEditing() {
        editOperation?.let { operation ->
            operation.complete().onSuccess {
                cleanupEditing()
                model.value = it
            }
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
            (behavior as? MutableSpinButtonBehavior<T, M>)?.editingEnded(this)
        }
    }

    public companion object {
        public operator fun <T> invoke(
            values: List<T>,
            itemVisualizer: ItemVisualizer<T, Any>? = null
        ): MutableSpinButton<T, MutableListSpinButtonModel<T>> = MutableSpinButton(MutableListSpinButtonModel(values), itemVisualizer)
    }
}