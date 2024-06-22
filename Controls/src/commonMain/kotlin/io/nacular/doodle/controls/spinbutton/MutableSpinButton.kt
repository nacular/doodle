package io.nacular.doodle.controls.spinbutton

import io.nacular.doodle.controls.EditOperation
import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.core.View
import io.nacular.doodle.utils.Editable

public interface MutableSpinButtonModel<T>: SpinButtonModel<T> {
    public override var value: T
}

/**
 * Defines the [EditOperation] for a [MutableSpinButton].
 */
public interface SpinButtonEditor<T> {
    /**
     * @param spinButton being edited
     * @param value within [spinButton] that is being edited
     * @param current View used to represent that value
     * @return an edit operation to manage changing [value]
     */
    public fun edit(spinButton: MutableSpinButton<T, *>, value: T, current: View): EditOperation<T>
}

/**
 * Creates a [SpinButtonEditor] from the given lambda [block].
 *
 * @param block to execute to create the [EditOperation]
 * @return an edit operation
 */
public inline fun <T> spinButtonEditor(crossinline block: (spinButton: MutableSpinButton<T, *>, value: T, current: View) -> EditOperation<T>): SpinButtonEditor<T> = object: SpinButtonEditor<T> {
    override fun edit(spinButton: MutableSpinButton<T, *>, value: T, current: View): EditOperation<T> = block(spinButton, value, current)
}

/**
 * Provides presentation and behavior customization for [MutableSpinButton].
 */
public abstract class MutableSpinButtonBehavior<T, M: MutableSpinButtonModel<T>>: SpinButtonBehavior<T, M>() {
    /**
     * Called whenever editing begins for the [MutableSpinButton]. This lets the behavior reconfigure
     * the SpinButton accordingly.
     *
     * @param spinButton being edited
     * @param value being edited
     * @return the edit operation
     */
    public abstract fun editingStarted(spinButton: MutableSpinButton<T, M>, value: T): EditOperation<T>?

    /**
     * Called whenever editing completes for the [MutableSpinButton]. This lets the behavior reconfigure
     * the SpinButton accordingly.
     *
     * @param spinButton that was being edited
     */
    public abstract fun editingEnded(spinButton: MutableSpinButton<T, M>)
}

/**
 * A spin button control that can be edited.
 * @see [SpinButton]
 *
 * @param model that holds the current value of the spin-button
 * @param itemVisualizer to visualize the values
 */
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