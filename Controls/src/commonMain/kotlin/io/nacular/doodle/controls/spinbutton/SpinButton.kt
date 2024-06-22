package io.nacular.doodle.controls.spinbutton

import io.nacular.doodle.accessibility.SpinButtonRole
import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.View
import io.nacular.doodle.core.behavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.constraints.Bounds
import io.nacular.doodle.layout.constraints.ConstraintDslContext
import io.nacular.doodle.utils.ChangeObservers
import io.nacular.doodle.utils.ChangeObserversImpl
import io.nacular.doodle.utils.ObservableList
import io.nacular.doodle.utils.observable
import kotlin.properties.Delegates

/**
 * An iterator-like model that tracks the items a [SpinButton] can present.
 */
public interface SpinButtonModel<T> {
    /** Advances the model's [value] to the next item if it [hasNext]. */
    public fun next()

    /** Advances the model's [value] to the previous item if it [hasNext]. */
    public fun previous()

    /** Currently selected value of the model. */
    public val value: T

    /** `true` if a call to [next] will move [value] forward. */
    public val hasNext: Boolean

    /** `true` if a call to [next] will move [value] backward. */
    public val hasPrevious: Boolean

    /** Notifies of changes to the current [value]. */
    public val changed: ChangeObservers<SpinButtonModel<T>>
}

/**
 * Provides presentation and behavior customization for [SpinButton].
 */
public abstract class SpinButtonBehavior<T, M: SpinButtonModel<T>>: Behavior<SpinButton<T, M>> {
    /**
     * The underlying list of Views within a spin-button. This gives behaviors control over the View composition of spin buttons.
     */
    public val SpinButton<T, M>.children: ObservableList<View> get() = this._children

    /** The insets used by the spin-button's layout when positioning its children. */
    public var SpinButton<T, M>.insets: Insets get() = this._insets; set(new) { _insets = new }

    /** The [Layout] used by the spin-button's to position its children. */
    public var SpinButton<T, M>.layout: Layout? get() = this._layout; set(new) { _layout = new }

    /**
     * Called whenever the [SpinButton]'s selection changes. This is an explicit API to ensure that
     * behaviors receive the notification before listeners to [SpinButton.changed].
     *
     * @param spinButton that changed
     */
    public abstract fun changed(spinButton: SpinButton<T, M>)

    /**
     * Called whenever [SpinButton.cellAlignment] changes.
     *
     * @param spinButton that changed
     */
    @Suppress("UNUSED_PARAMETER")
    public fun alignmentChanged(spinButton: SpinButton<T, M>) {}
}

/**
 * Controls used to flip through items (one at a time) within a sequential collection.
 *
 * @property model that holds the current value of the spin-button
 */
@Suppress("PropertyName")
public open class SpinButton<T, M: SpinButtonModel<T>> internal constructor(
    public val  model         : M,
                itemVisualizer: ItemVisualizer<T, SpinButton<T, M>>? = null,
    private val role          : SpinButtonRole = SpinButtonRole()
): View(accessibilityRole = role) {

    /** Updates the spin-button's value to the next item in its [model], if one exists. */
    public fun next(): Unit = model.next()

    /** Updates the spin-button's value to the previous item in its [model], if one exists. */
    public fun previous(): Unit = model.previous()

    /** The button's current value. */
    public open val value: Result<T> get() = runCatching { model.value }

    /** `true` IFF the spin-button's [model] has a previous value. */
    public val hasPrevious: Boolean get() = model.hasPrevious

    /** `true` IFF the spin-button's [model] has a next value. */
    public val hasNext: Boolean get() = model.hasNext

    /** Manages how the spin-button appearances and responds to interactions. */
    public var behavior: SpinButtonBehavior<T, M>? by behavior()

    /** Determines how the spin-button renders it's [value]. */
    public var itemVisualizer: ItemVisualizer<T, SpinButton<T, M>>? = itemVisualizer
        protected set

    /** Defines how the contents within the spin-button should be aligned. */
    public var cellAlignment: (ConstraintDslContext.(Bounds) -> Unit)? by Delegates.observable(null) { _,_,_ ->
        behavior?.alignmentChanged(this)
    }

    /** Human-understandable text to represent the current value if the number is insufficient. */
    public var valueAccessibilityLabeler: ((Result<T>) -> String)? by observable(null) { _,new ->
        updateAccessibleValueText()
    }

    override fun render(canvas: Canvas) {
        behavior?.render(this, canvas)
    }

    // Expose container APIs for behavior
    internal val _children get() = children
    internal var _insets   get() = insets; set(new) { insets = new }
    internal var _layout   get() = layout; set(new) { layout = new }

    @Suppress("PrivatePropertyName")
    private val changed_ by lazy { ChangeObserversImpl(this) }

    /** Notifies of changes to the spin-button's [value]. */
    public val changed: ChangeObservers<SpinButton<T, M>> = changed_

    private val modelChanged: (SpinButtonModel<T>) -> Unit = {
        updateAccessibleValueText()

        changed_()
    }

    init {
        this.model.changed += modelChanged

        updateAccessibleValueText()
    }

    private fun updateAccessibleValueText() {
        role.valueText = (valueAccessibilityLabeler ?: { it.getOrNull()?.let { "$it" } ?: "" })(value)
    }

    public companion object {
        /**
         * Creates a new [SpinButton] that has the [Int] values in the given [progression].
         *
         * @param progression    to use for the underlying model
         * @param itemVisualizer to visualize the values
         */
        public operator fun invoke(progression: IntProgression, itemVisualizer: ItemVisualizer<Int, Any>? = null): SpinButton<Int, IntSpinButtonModel> = SpinButton(IntSpinButtonModel (progression), itemVisualizer)

        /**
         * Creates a new [SpinButton] that cycles through the given [values].
         *
         * @param values         to use for the underlying model
         * @param itemVisualizer to visualize the values
         */
        public operator fun <T> invoke(values: List<T>, itemVisualizer: ItemVisualizer<T, Any>? = null): SpinButton<T, ListSpinButtonModel<T, List<T>>> = SpinButton(ListSpinButtonModel(values), itemVisualizer)

        /**
         * Creates a new [SpinButton] with values contained in the given [model].
         *
         * @param model          to use with the spin-button
         * @param itemVisualizer to visualize the values
         */
        public operator fun <T, M: SpinButtonModel<T>> invoke(model: M, itemVisualizer: ItemVisualizer<T, SpinButton<T, M>>? = null): SpinButton<T, M> = SpinButton(model, itemVisualizer)
    }
}