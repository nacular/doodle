package com.nectar.doodle.controls

import com.nectar.doodle.controls.buttons.CheckBox
import com.nectar.doodle.controls.panels.TabbedPanel
import com.nectar.doodle.controls.text.Label
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.TextMetrics
import com.nectar.doodle.text.StyledText

/**
 * Provides a mapping between an item and a View to represent it.
 */
interface ItemVisualizer<T> {
    /**
     * Called every time the [TabbedPanel]
     *
     * @param item being represented
     * @param previous View that represented an item
     * @return a View to represent this item
     */
    operator fun invoke(item: T, previous: View? = null): View
}

interface SelectableItemVisualizer<T> {
    operator fun invoke(item: T, previous: View? = null, isSelected: () -> Boolean = { false }): View
}

interface IndexedItemVisualizer<T> {
    operator fun invoke(item: T, index: Int, previous: View? = null, isSelected: () -> Boolean = { false }): View
}

open class TextItemVisualizer(private val textMetrics: TextMetrics): ItemVisualizer<String> {
    override fun invoke(item: String, previous: View?) = when (previous) {
        is Label -> previous.apply { text = item }
        else     -> Label(textMetrics, StyledText(item))
    }
}

open class BooleanItemVisualizer: ItemVisualizer<Boolean> {
    override fun invoke(item: Boolean, previous: View?): CheckBox = when (previous) {
        is CheckBox -> previous.apply   { enabled = true;  selected = item; enabled = false; }
        else        -> CheckBox().apply { enabled = false; selected = item                   }
    }
}

fun <T> toString(delegate: ItemVisualizer<String>) = object: ItemVisualizer<T> {
    override fun invoke(item: T, previous: View?) = delegate(item.toString(), previous)
}

fun <T> ignoreIndex(delegate: ItemVisualizer<T>) = object: IndexedItemVisualizer<T> {
    override fun invoke(item: T, index: Int, previous: View?, isSelected: () -> Boolean) = delegate(item, previous)
}

fun <T> ignoreSelection(delegate: ItemVisualizer<T>) = object: SelectableItemVisualizer<T> {
    override fun invoke(item: T, previous: View?, isSelected: () -> Boolean) = delegate(item, previous)
}

fun <T> ignoreIndex(delegate: SelectableItemVisualizer<T>) = object: IndexedItemVisualizer<T> {
    override fun invoke(item: T, index: Int, previous: View?, isSelected: () -> Boolean) = delegate(item, previous, isSelected)
}