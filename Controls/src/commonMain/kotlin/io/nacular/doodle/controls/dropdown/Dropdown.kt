package io.nacular.doodle.controls.dropdown

import io.nacular.doodle.controls.IndexedItem
import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.controls.IntProgressionModel
import io.nacular.doodle.controls.ListModel
import io.nacular.doodle.controls.SimpleListModel
import io.nacular.doodle.controls.list.List
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.View
import io.nacular.doodle.core.behavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.utils.ChangeObservers
import io.nacular.doodle.utils.ChangeObserversImpl
import io.nacular.doodle.utils.ObservableList

public interface DropdownBehavior<T, M: ListModel<T>>: Behavior<Dropdown<T, M>> {
    public val Dropdown<T, M>.model   : M                    get() = _model
    public var Dropdown<T, M>.list    : List<T, M>?          get() = list;    set(new) { list = new }
    public val Dropdown<T, M>.children: ObservableList<View> get() = _children
    public var Dropdown<T, M>.insets  : Insets               get() = _insets; set(new) { _insets = new }
    public var Dropdown<T, M>.layout  : Layout?              get() = _layout; set(new) { _layout = new }

    public fun changed(dropdown: Dropdown<T, M>) {}
}

public open class Dropdown<T, M: ListModel<T>>(
        protected open val model             : M,
        public         val boxItemVisualizer : ItemVisualizer<T, IndexedItem>? = null,
        public         val listItemVisualizer: ItemVisualizer<T, IndexedItem>? = boxItemVisualizer,
): View() {
    public data class SelectedValue<T>(val value: T, val index: Int)

    // Expose container APIs for behavior
    internal val _model    get() = model
    internal val _children get() = children
    internal var _insets   get() = insets; set(new) { insets = new }
    internal var _layout   get() = layout; set(new) { layout = new }

    internal var list: List<T, M>? = null

    public val isEmpty: Boolean get() = model.isEmpty

    public var selection: Int = 0
        set(new) {
            new.takeIf { it != field && it >= 0 && it < model.size }?.let { selection ->
                field = selection
                value = model[selection]!!

                behavior?.changed(this)
                changed_()
            }
        }

    public var value: T = model[selection] as T // FIXME: Change ListModel so it just returns T and throws
        private set

    @Suppress("PrivatePropertyName")
    private val changed_ by lazy { ChangeObserversImpl(this) }

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