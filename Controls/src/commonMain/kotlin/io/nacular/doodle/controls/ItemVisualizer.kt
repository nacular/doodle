package io.nacular.doodle.controls

import io.nacular.doodle.controls.buttons.CheckBox
import io.nacular.doodle.controls.text.Label
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.text.StyledText

/**
 * Provides a mapping between an item and a View to represent it.
 */
interface ItemVisualizer<T> {
    /**
     * Called whenever an item needs to be translated to a View.
     *
     * @param item being represented
     * @param previous View that represented an item
     * @return a View to represent this item
     */
    operator fun invoke(item: T, previous: View? = null): View
}

/**
 * Visualizer for items that can be selected, like those in a [List][io.nacular.doodle.controls.list.List].
 */
interface SelectableItemVisualizer<T> {
    /**
     * Called whenever an item needs to be translated to a View.
     *
     * @param item being represented
     * @param previous View that represented an item
     * @param isSelected indicates whether the item is currently selected
     * @return a View to represent this item
     */
    operator fun invoke(item: T, previous: View? = null, isSelected: () -> Boolean = { false }): View
}

/**
 * Visualizer for items that have an index and can be selected, like those in a [List][io.nacular.doodle.controls.list.List].
 */
interface IndexedItemVisualizer<T> {
    /**
     * Called whenever an item needs to be translated to a View.
     *
     * @param item being represented
     * @param index of the item
     * @param previous View that represented an item
     * @param isSelected indicates whether the item is currently selected
     * @return a View to represent this item
     */
    operator fun invoke(item: T, index: Int, previous: View? = null, isSelected: () -> Boolean = { false }): View
}

/**
 * Visualizes Strings using [Label]s.
 */
open class TextItemVisualizer(private val textMetrics: TextMetrics): ItemVisualizer<String> {
    override fun invoke(item: String, previous: View?): Label = when (previous) {
        is Label -> previous.apply { text = item }
        else     -> Label(textMetrics, StyledText(item))
    }
}

/**
 * Visualizes Booleans using [CheckBox]s.
 */
open class BooleanItemVisualizer: ItemVisualizer<Boolean> {
    override fun invoke(item: Boolean, previous: View?): CheckBox = when (previous) {
        is CheckBox -> previous.apply   { enabled = true;  selected = item; enabled = false; }
        else        -> CheckBox().apply { enabled = false; selected = item                   }
    }
}

/**
 * Visualizes the item's `toString()` using the delegate.
 *
 * @param delegate to visualize the item's `toString()`
 */
fun <T> toString(delegate: ItemVisualizer<String>) = object: ItemVisualizer<T> {
    override fun invoke(item: T, previous: View?) = delegate(item.toString(), previous)
}

/**
 * Visualizes the item's `toString()` using the delegate.
 *
 * @param delegate to visualize the item's `toString()`
 */
fun <T> toString(delegate: IndexedItemVisualizer<String>) = object: IndexedItemVisualizer<T> {
    override fun invoke(item: T, index: Int, previous: View?, isSelected: () -> Boolean) = delegate(item.toString(), index, previous, isSelected)
}

/**
 * Visualizes the item's `toString()` using the delegate.
 *
 * @param delegate to visualize the item's `toString()`
 */
fun <T> toString(delegate: SelectableItemVisualizer<String>) = object: SelectableItemVisualizer<T> {
    override fun invoke(item: T, previous: View?, isSelected: () -> Boolean) = delegate(item.toString(), previous, isSelected)
}

/**
 * Helper for using an [ItemVisualizer] in place of an [IndexedItemVisualizer].
 *
 * @param delegate used for visualization
 */
fun <T> ignoreIndex(delegate: ItemVisualizer<T>) = object: IndexedItemVisualizer<T> {
    override fun invoke(item: T, index: Int, previous: View?, isSelected: () -> Boolean) = delegate(item, previous)
}

/**
 * Helper for using an [ItemVisualizer] in place of an [SelectableItemVisualizer].
 *
 * @param delegate used for visualization
 */
fun <T> ignoreSelection(delegate: ItemVisualizer<T>) = object: SelectableItemVisualizer<T> {
    override fun invoke(item: T, previous: View?, isSelected: () -> Boolean) = delegate(item, previous)
}

/**
 * Helper for using an [SelectableItemVisualizer] in place of an [IndexedItemVisualizer].
 *
 * @param delegate used for visualization
 */
fun <T> ignoreIndex(delegate: SelectableItemVisualizer<T>) = object: IndexedItemVisualizer<T> {
    override fun invoke(item: T, index: Int, previous: View?, isSelected: () -> Boolean) = delegate(item, previous, isSelected)
}