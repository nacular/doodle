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
import io.nacular.doodle.layout.Constraints
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.utils.ChangeObservers
import io.nacular.doodle.utils.ChangeObserversImpl
import io.nacular.doodle.utils.ObservableList
import kotlin.properties.Delegates

/**
 * Provides presentation and behavior customization for [Dropdown].
 */
public interface DropdownBehavior<T, M: ListModel<T>>: Behavior<Dropdown<T, M>> {
    public val Dropdown<T, M>.model   : M                    get() = _model
    public var Dropdown<T, M>.list    : List<T, M>?          get() = list;    set(new) { list = new }
    public val Dropdown<T, M>.children: ObservableList<View> get() = _children
    public var Dropdown<T, M>.insets  : Insets               get() = _insets; set(new) { _insets = new }
    public var Dropdown<T, M>.layout  : Layout?              get() = _layout; set(new) { _layout = new }

    /**
     * Called whenever the Dropdown's value or selection changes. This is an explicit API to ensure that
     * behaviors receive the notification before listeners to [Dropdown.changed].
     *
     * @param dropdown with change
     */
    public fun changed(dropdown: Dropdown<T, M>) {}

    /**
     * Called whenever [Dropdown.boxCellAlignment] or [Dropdown.listCellAlignment] change.
     *
     * @param dropdown with change
     */
    public fun alignmentChanged(dropdown: Dropdown<T, M>) {}
}

/**
 * Controls used to select an item within a list.
 *
 * @property model used to represent the underlying choices
 * @property boxItemVisualizer to render the selected item within the drop-down box
 * @property listItemVisualizer to render each item within the list of choices
 */
@Suppress("PropertyName")
public open class Dropdown<T, M: ListModel<T>>(
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
    public open val value: T get() = model[selection] as T // FIXME: Change ListModel so it just returns T and throws

    /**
     * Defines how the contents within the drop-down box should be aligned.
     */
    public var boxCellAlignment: (Constraints.() -> Unit)? by Delegates.observable(null) { _,_,_ ->
        behavior?.alignmentChanged(this)
    }

    /**
     * Defines how the contents of each choice be aligned.
     */
    public var listCellAlignment: (Constraints.() -> Unit)? by Delegates.observable(null) { _,_,_ ->
        behavior?.alignmentChanged(this)
    }

    @Suppress("PrivatePropertyName")
    private val changed_ by lazy { ChangeObserversImpl(this) }

    /**
     * Broadcasts changes Dropdown
     */
    public val changed: ChangeObservers<Dropdown<T, M>> = changed_

    public var behavior: DropdownBehavior<T, M>? by behavior()

    override fun render(canvas: Canvas) {
        behavior?.render(this, canvas)
    }

    public companion object {
        public operator fun invoke(
                progression       : IntProgression,
                boxItemVisualizer : ItemVisualizer<Int, IndexedItem>? = null,
                listItemVisualizer: ItemVisualizer<Int, IndexedItem>? = boxItemVisualizer
        ): Dropdown<Int, ListModel<Int>> = Dropdown(IntProgressionModel(progression), boxItemVisualizer, listItemVisualizer)

        public operator fun <T> invoke(
                values            : kotlin.collections.List<T>,
                boxItemVisualizer : ItemVisualizer<T, IndexedItem>? = null,
                listItemVisualizer: ItemVisualizer<T, IndexedItem>? = boxItemVisualizer
        ): Dropdown<T, ListModel<T>> = Dropdown(SimpleListModel(values), boxItemVisualizer, listItemVisualizer)
    }
}