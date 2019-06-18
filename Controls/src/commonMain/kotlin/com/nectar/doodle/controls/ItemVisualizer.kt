package com.nectar.doodle.controls

import com.nectar.doodle.controls.buttons.CheckBox
import com.nectar.doodle.controls.text.Label
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.TextMetrics
import com.nectar.doodle.text.StyledText

/**
 * Created by Nicholas Eddy on 4/24/19.
 */
interface ItemVisualizer<T> {
    operator fun invoke(item: T, previous: View? = null): View
}

class TextItemGenerator(private val textMetrics: TextMetrics): ItemVisualizer<String> {
    override fun invoke(item: String, previous: View?) = when (previous) {
        is Label -> { previous.text = item; previous }
        else     -> Label(textMetrics, StyledText(item))
    }
}

class BooleanItemGenerator: ItemVisualizer<Boolean> {
    override fun invoke(item: Boolean, previous: View?) = when (previous) {
        is CheckBox ->                  { previous.selected = item; previous.enabled = false; previous }
        else        -> CheckBox().apply {          selected = item;          enabled = false;          }
    }
}

class ToStringItemGenerator<T>(textMetrics : TextMetrics): ItemVisualizer<T> {
    override fun invoke(item: T, previous: View?) = delegate(item.toString(), previous)

    private val delegate = TextItemGenerator(textMetrics)
}
