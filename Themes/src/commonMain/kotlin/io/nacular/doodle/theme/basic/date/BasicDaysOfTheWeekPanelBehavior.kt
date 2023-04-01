package io.nacular.doodle.theme.basic.date

import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.controls.date.DaysOfTheWeekPanel
import io.nacular.doodle.controls.date.DaysOfTheWeekPanelBehavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Paint
import kotlinx.datetime.DayOfWeek

/**
 * Created by Nicholas Eddy on 2/21/22.
 */
public class BasicDaysOfTheWeekPanelBehavior(private val background: Paint, private val defaultVisualizer: ItemVisualizer<DayOfWeek, Unit>?): DaysOfTheWeekPanelBehavior {
    override fun itemVisualizer(of: DaysOfTheWeekPanel): ItemVisualizer<DayOfWeek, Unit>? = of.itemVisualizer ?: defaultVisualizer

    override fun render(view: DaysOfTheWeekPanel, canvas: Canvas) {
        canvas.rect(view.bounds.atOrigin, fill = background)
    }
}