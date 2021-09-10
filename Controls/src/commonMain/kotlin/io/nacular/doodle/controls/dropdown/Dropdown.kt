package io.nacular.doodle.controls.dropdown

import io.nacular.doodle.controls.IndexedIem
import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.controls.ListModel
import io.nacular.doodle.controls.list.List
import io.nacular.doodle.controls.list.ListBehavior
import io.nacular.doodle.controls.spinner.Model
import io.nacular.doodle.controls.spinner.Spinner
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.View
import io.nacular.doodle.core.behavior
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.utils.ChangeObservers
import io.nacular.doodle.utils.ChangeObserversImpl
import io.nacular.doodle.utils.ObservableList

public interface DropdownBehavior<T, M: ListModel<T>>: Behavior<Dropdown<T, M>> {
    public val Dropdown<T, M>.list    : List<T, M>           get() = _list
    public val Dropdown<T, M>.children: ObservableList<View> get() = this._children
    public var Dropdown<T, M>.insets  : Insets               get() = this._insets; set(new) { _insets = new }
    public var Dropdown<T, M>.layout  : Layout?              get() = this._layout; set(new) { _layout = new }
}

public class Dropdown<T, M: ListModel<T>>(
        protected open val model              : M,
        public         val comboItemVisualizer: ItemVisualizer<T?, Unit>?      = null,
        public         val listItemVisualizer : ItemVisualizer<T, IndexedIem>? = null,
): View() {
    // Expose container APIs for behavior
    internal val _list     get() = list
    internal val _children get() = children
    internal var _insets   get() = insets; set(new) { insets = new }
    internal var _layout   get() = layout; set(new) { layout = new }

    private val list = List(model)

    public val isEmpty: Boolean get() = model.isEmpty

    public var selection: Int? = if (isEmpty) null else 0
        set(new) {
            new.takeIf { it != field && (it == null || (it > 0 && it < model.size)) }?.let {
                field = it

                changed_()
            }
        }

    public val value: T? get() = selection?.let { model[it] }

    @Suppress("PrivatePropertyName")
    private val changed_ by lazy { ChangeObserversImpl(this) }

    public val changed: ChangeObservers<Dropdown<T, M>> = changed_

    public var behavior: DropdownBehavior<T, M>? by behavior()
}