package io.nacular.doodle.controls.date

import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.PositionableContainer
import io.nacular.doodle.core.View
import io.nacular.doodle.core.behavior
import io.nacular.doodle.core.renderProperty
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Rectangle
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.DayOfWeek.MONDAY
import kotlinx.datetime.DayOfWeek.SUNDAY
import kotlinx.datetime.DayOfWeek.values

/**
 * Panel used to render the days of the week in a horizontal layout. This panel combines well with
 * [MonthPanel] to create calendar components.
 *
 * @param weekStart indicates which day should be used as the start of the week
 * @param itemVisualizer that maps [DayOfWeek] to [View] for each item in the panel
 */
public class DaysOfTheWeekPanel(weekStart: DayOfWeek = SUNDAY, itemVisualizer: ItemVisualizer<DayOfWeek, Unit>): View() {
    private val weekStart by renderProperty(weekStart)

    private inner class DaysLayout: Layout {
        override fun layout(container: PositionableContainer) {
            val columnWidth = container.width / numColumns
            var col         = shiftDay(weekStart) % numColumns

            container.children.forEach {
                it.bounds = Rectangle(
                        x      = columnWidth * col,
                        y      = 0.0,
                        width  = columnWidth,
                        height = container.height
                )

                col = (col + 1) % numColumns
            }
        }
    }

    private val numColumns = values().size

    /**
     * Behavior used to render the panel itself.
     */
    public var behavior: Behavior<DaysOfTheWeekPanel>? by behavior()

    init {
        children += values().map { itemVisualizer(it, null, Unit) }

        layout = DaysLayout()
    }

    private fun shiftDay(weekStart: DayOfWeek) = (SUNDAY.ordinal - (weekStart.ordinal + MONDAY.ordinal) + 1)  % (SUNDAY.ordinal + 1)

    override fun render(canvas: Canvas) {
        behavior?.render(this, canvas)
    }
}