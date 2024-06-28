package io.nacular.doodle.controls.dropdown

import io.nacular.doodle.controls.IndexedItem
import io.nacular.doodle.controls.IntProgressionModel
import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.controls.ListModel
import io.nacular.doodle.controls.SimpleListModel
import io.nacular.doodle.controls.list.List
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
import kotlin.properties.Delegates

/**
 * Provides presentation and behavior customization for [SelectBox].
 */
public interface SelectBoxBehavior<T, M: ListModel<T>>: Behavior<SelectBox<T, M>> {
    public val SelectBox<T, M>.model   : M                    get() = _model
    public var SelectBox<T, M>.list    : List<T, M>?          get() = list;    set(new) { list = new }
    public val SelectBox<T, M>.children: ObservableList<View> get() = _children
    public var SelectBox<T, M>.insets  : Insets               get() = _insets; set(new) { _insets = new }
    public var SelectBox<T, M>.layout  : Layout?              get() = _layout; set(new) { _layout = new }

    /**
     * Called whenever the SelectBox's value or selection changes. This is an explicit API to ensure that
     * behaviors receive the notification before listeners to [SelectBox.changed].
     *
     * @param dropdown with change
     */
    public fun changed(dropdown: SelectBox<T, M>) {}

    /**
     * Called whenever [SelectBox.boxCellAlignment] or [SelectBox.listCellAlignment] change.
     *
     * @param dropdown with change
     */
    public fun alignmentChanged(dropdown: SelectBox<T, M>) {}
}

/**
 * Control used to select an item within a hidden list.
 *
 * @property model used to represent the underlying choices
 * @property boxItemVisualizer to render the selected item within the select box
 * @property listItemVisualizer to render each item within the list of choices
 */
@Suppress("PropertyName")
public open class SelectBox<T, M: ListModel<T>>(
        protected open val model             : M,
        public         val boxItemVisualizer : ItemVisualizer<T, IndexedItem>? = null,
        public         val listItemVisualizer: ItemVisualizer<T, IndexedItem>? = boxItemVisualizer,
): View() {
    // Expose container APIs for behavior
    internal val _model    get() = model
    internal val _children get() = children
    internal var _insets   get() = insets; set(new) { insets = new }
    internal var _layout   get() = layout; set(new) { layout = new }

    internal var list: List<T, M>? = null

    /**
     * Indicates whether the option list is empty
     */
    public val isEmpty: Boolean get() = model.isEmpty

    /**
     * Index of currently selected value
     */
    public var selection: Int = 0
        set(new) {
            new.takeIf { it != field && it >= 0 && it < model.size }?.let { selection ->
                field = selection
                behavior?.changed(this)
                changed_()
            }
        }

    /**
     * Currently selected value
     */
    public open val value: Result<T> get() = model[selection]

    /**
     * Defines how the contents within the drop-down box should be aligned.
     */
    public var boxCellAlignment: (ConstraintDslContext.(Bounds) -> Unit)? by Delegates.observable(null) { _,_,_ ->
        behavior?.alignmentChanged(this)
    }

    /**
     * Defines how the contents of each choice be aligned.
     */
    public var listCellAlignment: (ConstraintDslContext.(Bounds) -> Unit)? by Delegates.observable(null) { _,_,_ ->
        behavior?.alignmentChanged(this)
    }

    @Suppress("PrivatePropertyName")
    private val changed_ by lazy { ChangeObserversImpl(this) }

    /**
     * Broadcasts changes SelectBox
     */
    public val changed: ChangeObservers<SelectBox<T, M>> = changed_

    /** Controls the SelectBox's look and behavior. */
    public var behavior: SelectBoxBehavior<T, M>? by behavior()

    override fun render(canvas: Canvas) {
        behavior?.render(this, canvas)
    }

    public companion object {
        public operator fun invoke(
                progression       : IntProgression,
                boxItemVisualizer : ItemVisualizer<Int, IndexedItem>? = null,
                listItemVisualizer: ItemVisualizer<Int, IndexedItem>? = boxItemVisualizer
        ): SelectBox<Int, ListModel<Int>> = SelectBox(IntProgressionModel(progression), boxItemVisualizer, listItemVisualizer)

        public operator fun <T> invoke(
                values            : kotlin.collections.List<T>,
                boxItemVisualizer : ItemVisualizer<T, IndexedItem>? = null,
                listItemVisualizer: ItemVisualizer<T, IndexedItem>? = boxItemVisualizer
        ): SelectBox<T, ListModel<T>> = SelectBox(SimpleListModel(values), boxItemVisualizer, listItemVisualizer)
    }
}