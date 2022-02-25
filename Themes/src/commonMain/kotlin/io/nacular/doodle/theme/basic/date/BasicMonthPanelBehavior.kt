package io.nacular.doodle.theme.basic.date

import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.controls.date.MonthPanel
import io.nacular.doodle.controls.date.MonthPanelBehavior
import io.nacular.doodle.controls.itemVisualizer
import io.nacular.doodle.controls.text.Label
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Paint
import kotlinx.datetime.LocalDate

/**
 * Created by Nicholas Eddy on 2/21/22.
 */
public class BasicMonthPanelBehavior(
    private val background: Paint,
    private val defaultVisualizer: ItemVisualizer<LocalDate, MonthPanel> = itemVisualizer { day, previous, panel ->
        val text = "${day.dayOfMonth}"

        when (previous) {
            is Label -> previous.apply    { fitText = emptySet(); this.text = text }
            else     -> Label(text).apply { fitText = emptySet() }
        }.also {
            it.enabled = day.month == panel.startDate.month
        }
    }
): MonthPanelBehavior {
    override fun install(view: MonthPanel) {
        super.install(view)
        view.rerender()
    }

    override fun itemVisualizer(of: MonthPanel): ItemVisualizer<LocalDate, MonthPanel> = of.itemVisualizer ?: defaultVisualizer

    override fun render(view: MonthPanel, canvas: Canvas) {
        canvas.rect(view.bounds.atOrigin, fill = background)
    }
}