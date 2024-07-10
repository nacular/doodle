package io.nacular.doodle.controls.date

import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.Positionable
import io.nacular.doodle.core.View
import io.nacular.doodle.core.behavior
import io.nacular.doodle.core.renderProperty
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Size
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.DayOfWeek.MONDAY
import kotlinx.datetime.DayOfWeek.SUNDAY

/**
 * Determines how a [DaysOfTheWeekPanel] renders.
 */
public interface DaysOfTheWeekPanelBehavior: Behavior<DaysOfTheWeekPanel> {
    /**
     * Returns the visualizer to use for the given panel.
     *
     * @param of the panel in question
     */
    public fun itemVisualizer(of: DaysOfTheWeekPanel): ItemVisualizer<DayOfWeek, Unit>?
}

/**
 * Panel used to render the days of the week in a horizontal layout. This panel combines well with
 * [MonthPanel] to create calendar components.
 *
 * @param weekStart indicates which day should be used as the start of the week
 * @param itemVisualizer that maps [DayOfWeek] to [View] for each item in the panel
 */
public class DaysOfTheWeekPanel(weekStart: DayOfWeek = SUNDAY, public val itemVisualizer: ItemVisualizer<DayOfWeek, Unit>? = null): View() {
    private inner class DaysLayout: Layout {
        override fun layout(views: Sequence<Positionable>, min: Size, current: Size, max: Size): Size {
            val columnWidth = current.width / numColumns
            var col         = shiftDay(weekStart) % numColumns
            val size        = Size(columnWidth, current.height)

            views.forEach {
                it.updateBounds(
                    x   = columnWidth * col,
                    y   = 0.0,
                    min = size,
                    max = size
                )

                col = (col + 1) % numColumns
            }

            return current
        }
    }

    private val numColumns = DayOfWeek.entries.size

    /**
     * Indicates which day should be used as the start of the week.
     */
    public var weekStart: DayOfWeek by renderProperty(weekStart) { _,_ ->
        relayout()
    }

    /**
     * Behavior used to render the panel itself.
     */
    public var behavior: DaysOfTheWeekPanelBehavior? by behavior(afterChange = { _,_ ->
        update()
    })

    init {
        update()

        layout = DaysLayout()
    }

    private fun shiftDay(weekStart: DayOfWeek) = (SUNDAY.ordinal - (weekStart.ordinal + MONDAY.ordinal) + 1)  % (SUNDAY.ordinal + 1)

    override fun render(canvas: Canvas) {
        behavior?.render(this, canvas)
    }

    private fun update() {
        children.clear()

        val visualizer = behavior?.itemVisualizer(this) ?: itemVisualizer ?: return

        children += DayOfWeek.entries.map { visualizer(it, null, Unit) }
    }
}