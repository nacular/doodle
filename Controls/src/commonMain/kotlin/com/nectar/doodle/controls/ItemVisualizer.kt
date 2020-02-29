package com.nectar.doodle.controls

import com.nectar.doodle.controls.buttons.CheckBox
import com.nectar.doodle.controls.text.Label
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.TextMetrics
import com.nectar.doodle.text.StyledText

/**
 * Created by Nicholas Eddy on 4/24/19.
 */
interface ItemVisualizer<T> {
    operator fun invoke(item: T, previous: View? = null, isSelected: () -> Boolean = { false }): View
}

interface IndexedItemVisualizer<T> {
    operator fun invoke(item: T, index: Int, previous: View? = null, isSelected: () -> Boolean = { false }): View
}

open class TextItemVisualizer(private val textMetrics: TextMetrics): ItemVisualizer<String> {
    override fun invoke(item: String, previous: View?, isSelected: () -> Boolean) = when (previous) {
        is Label -> { previous.text = item; previous }
        else     -> Label(textMetrics, StyledText(item))
    }
}

class HighlightingTextItemVisualizer(textMetrics: TextMetrics, private val color: Color): TextItemVisualizer(textMetrics) {
    override fun invoke(item: String, previous: View?, isSelected: () -> Boolean) = super.invoke(item, previous, isSelected).apply {
        foregroundColor = if (isSelected()) color else null
    }
}

class FastTextItemVisualizer(private val textMetrics: TextMetrics): ItemVisualizer<String> {
    override fun invoke(item: String, previous: View?, isSelected: () -> Boolean) = when (previous) {
        is Label -> { previous.text = item; previous }
        else     -> Label(textMetrics, StyledText(item)).apply { fitText = emptySet() }
    }
}

open class BooleanItemVisualizer: ItemVisualizer<Boolean> {
    override fun invoke(item: Boolean, previous: View?, isSelected: () -> Boolean): CheckBox = when (previous) {
        is CheckBox ->                  { previous.selected = item; previous.enabled = false; previous }
        else        -> CheckBox().apply { this.selected     = item;          enabled = false;          }
    }
}

class ToStringItemVisualizer<T>(private val delegate: ItemVisualizer<String>): ItemVisualizer<T> {
    override fun invoke(item: T, previous: View?, isSelected: () -> Boolean) = delegate(item.toString(), previous, isSelected)
}

fun <T> passThrough(delegate: ItemVisualizer<T>) = object: IndexedItemVisualizer<T> {
    override fun invoke(item: T, index: Int, previous: View?, isSelected: () -> Boolean) = delegate(item, previous, isSelected)
}