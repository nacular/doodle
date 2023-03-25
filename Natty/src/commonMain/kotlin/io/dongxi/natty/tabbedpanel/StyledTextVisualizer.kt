package io.dongxi.natty.tabbedpanel

import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.controls.text.Label
import io.nacular.doodle.core.View
import io.nacular.doodle.text.StyledText
import io.nacular.doodle.utils.Dimension


open class StyledTextVisualizer(private val fitText: Set<Dimension>? = null) : ItemVisualizer<StyledText, Any> {
    override fun invoke(item: StyledText, previous: View?, context: Any): Label = when (previous) {
        is Label -> previous.apply { text = item.text; this@StyledTextVisualizer.fitText?.let { fitText = it } }
        else -> Label(item).apply {
            this@StyledTextVisualizer.fitText?.let { fitText = it }
        }
    }
}
