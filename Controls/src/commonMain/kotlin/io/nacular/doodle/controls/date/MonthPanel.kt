package io.nacular.doodle.controls.date

import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.controls.Selectable
import io.nacular.doodle.controls.SelectionModel
import io.nacular.doodle.controls.panels.ScrollPanel
import io.nacular.doodle.controls.text.Label
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.Positionable
import io.nacular.doodle.core.View
import io.nacular.doodle.core.behavior
import io.nacular.doodle.core.renderProperty
import io.nacular.doodle.core.scrollTo
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.constraints.Bounds
import io.nacular.doodle.layout.constraints.Constrainer
import io.nacular.doodle.layout.constraints.ConstraintDslContext
import io.nacular.doodle.layout.constraints.fill
import io.nacular.doodle.utils.ChangeObservers
import io.nacular.doodle.utils.SetObserver
import io.nacular.doodle.utils.SetObservers
import io.nacular.doodle.utils.SetPool
import io.nacular.doodle.utils.observable
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.DayOfWeek.SUNDAY
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.math.ceil

/**
 * Determines how a [MonthPanel] renders
 */
public interface MonthPanelBehavior: Behavior<MonthPanel> {
    /**
     * Returns the visualizer to use for the given panel.
     *
     * @param of the panel in question
     */
    public fun itemVisualizer(of: MonthPanel): ItemVisualizer<LocalDate, MonthPanel>?
}

/**
 * A panel that displays the days of a month in a particular year.
 *
 * @param date whose month will be displayed
 * @param itemVisualizer that maps [LocalDate] to [View] for each item in the panel. The default simply uses a [Label] to show each day's number.
 * @param selectionModel that manages the panel's selection state
 * @param weekStart indicates which day should be used as the start of the week
 */
public open class MonthPanel(
                date          : LocalDate,
    public  val itemVisualizer: ItemVisualizer<LocalDate, MonthPanel>? = null,
    private val selectionModel: SelectionModel<LocalDate>? = null,
                weekStart     : DayOfWeek = SUNDAY
): View(), Selectable<LocalDate> {
    private inner class MonthLayout: Layout {
        private val constrainer = Constrainer()

        override fun layout(views: Sequence<Positionable>, min: Size, current: Size, max: Size, insets: Insets): Size {
            var row         = 0
            var col         = if (showAdjacentMonths) 0 else shiftDay(weekStart, startDate.dayOfWeek) % numColumns
            val rowHeight   = current.height / numRows
            val columnWidth = current.width  / numColumns

            views.forEach {
                val within = Rectangle(
                    x      = columnWidth * col,
                    y      = rowHeight   * row,
                    width  = columnWidth,
                    height = rowHeight
                )

                col = (col + 1) % numColumns

                if (col == 0) row++

                it.updateBounds(when (cellAlignment) {
                    fill -> within
                    else -> constrainer(it.bounds, within = within, using = cellAlignment)
                })
            }

            return current
        }
    }

    private var numRows    = 0
    private val numColumns = DayOfWeek.values().size

    /**
     * Indicates which day should be used as the start of the week.
     */
    public var weekStart: DayOfWeek by renderProperty(weekStart) { _,_ -> update() }

    /**
     * The first date in the panel (i.e. day 1 of the month shown).
     */
    public var startDate: LocalDate = date.firstDayOfMonth; private set(new) {
        if (new != field) {
            field = new
            numDays = startDate.numDaysOfMonth
            update  ()
            rerender()

            (monthChanged as SetPool).forEach {
                it(this)
            }
        }
    }

    /**
     * The last date in the panel (i.e. the final day of the month shown)
     */
    public val endDate: LocalDate get() = startDate + DatePeriod(days = numDays - 1)

    /**
     * Sets the date the panel uses to display the month.
     *
     * @param date whose month the panel displays
     */
    public fun setDate(date: LocalDate) {
        startDate = date.firstDayOfMonth
    }

    /**
     * Indicates whether days from the previous and subsequent months are displayed by
     * the panel.
     *
     * Default: `false`
     */
    public var showAdjacentMonths: Boolean by renderProperty(false) { _,_ -> update() }

    /**
     * Defines how the View representing each day should be aligned within its cell.
     * The default is to have the View fill the cell.
     */
    public var cellAlignment: (ConstraintDslContext.(Bounds) -> Unit) by observable(fill) { _,_ ->
        relayout()
    }

    private var numDays = startDate.numDaysOfMonth

    private var previousDays = 0

    /**
     * Behavior used to render the panel itself.
     */
    public var behavior: MonthPanelBehavior? by behavior(afterChange = { _,_ ->
        update()
    })

    protected fun update() {
        val visualizer = behavior?.itemVisualizer(this) ?: itemVisualizer ?: return

        numRows = when {
            showAdjacentMonths -> 6
            else               -> ceil((shiftDay(weekStart, startDate.dayOfWeek) + numDays).toDouble() / numColumns).toInt()
        }

        var sequence: Sequence<Int> = (0 until numDays).asSequence()

        // Handle adjacent months
        if (showAdjacentMonths) {
            previousDays  = shiftDay(weekStart, startDate.dayOfWeek)
            val previousMonthDays = (-previousDays until 0).asSequence()

            sequence = previousMonthDays + sequence

            sequence += (numDays .. numDays + (numRows * numColumns - sequence.count())).asSequence()
        }

        var numDays = 0

        children.batch {
            sequence.forEachIndexed { index, day ->
                when {
                    index >= this.size -> this        += visualizer(startDate + DatePeriod(days = day), null,        this@MonthPanel)
                    else               -> this[index]  = visualizer(startDate + DatePeriod(days = day), this[index], this@MonthPanel)
                }
                ++numDays
            }

            while (this.size > numDays) {
                this.removeAt(this.size - 1)
            }
        }

        // Layout should not have changed if not showing adjacent months
        if (!showAdjacentMonths) {
            relayout()
        }
    }

    private fun shiftDay(weekStart: DayOfWeek, dayOfWeek: DayOfWeek): Int {
        val offset = (SUNDAY.ordinal - (weekStart.ordinal + DayOfWeek.MONDAY.ordinal) + 1)  % (SUNDAY.ordinal + 1)

        return (dayOfWeek.ordinal + offset) % (SUNDAY.ordinal + 1)
    }

    @Suppress("PrivatePropertyName")
    private val selectionChanged_: SetObserver<SelectionModel<LocalDate>, LocalDate> = { _,removed,added ->
        val filteredAdded   = mutableSetOf<LocalDate>()
        val filteredRemoved = mutableSetOf<LocalDate>()

        repeat(numDays) {
            when (val d = startDate + DatePeriod(days = it)) {
                in added   -> filteredAdded   += d
                in removed -> filteredRemoved += d
            }
        }

        if (filteredAdded.isNotEmpty() || filteredRemoved.isNotEmpty()) {
            scrollToSelection()

            (selectionChanged as SetPool).forEach {
                it(this, filteredRemoved, filteredAdded)
            }

            // FIXME: Only refresh the changed values?
            update()
        }
    }

    /**
     * Notifies of changes to the panel's selection.
     */
    public val selectionChanged: SetObservers<MonthPanel, LocalDate> = SetPool()

    /**
     * Notifies of changes to the month the panel shows
     */
    public val monthChanged: ChangeObservers<MonthPanel> = SetPool()

    init {
        layout = MonthLayout()
    }

    /**
     * Scrolls to the given day of the month @see [LocalDate.dayOfMonth] if the MonthPanel is within a [ScrollPanel].
     *
     * @param day of the month to scroll to
     */
    public fun scrollTo(day: Int) {
        if (day in 1 .. numDays) {
            scrollTo(children[day - 1 + previousDays].bounds)
        }
    }

    /**
     * Scrolls the last selected item into view if the MonthPanel is within a [ScrollPanel].
     */
    public fun scrollToSelection() {
        selectionAnchor?.let { scrollTo(it.dayOfMonth) }
    }

    override fun render(canvas: Canvas) {
        behavior?.render(this, canvas)
    }

    override fun addedToDisplay() {
        selectionModel?.let { it.changed += selectionChanged_ }

        update()

        super.addedToDisplay()
    }

    override fun removedFromDisplay() {
        selectionModel?.let { it.changed -= selectionChanged_ }

        super.removedFromDisplay()
    }

    override fun selected       (item : LocalDate     ): Boolean = item.sameMonth(startDate) && selectionModel?.contains  (item ) ?: false
    override fun selectAll      (                     )          { selectionModel?.addAll    ((firstSelectable .. lastSelectable).toList()) }
    override fun addSelection   (items: Set<LocalDate>)          { selectionModel?.addAll    (items.filter { it.sameMonth(startDate) }) }
    override fun setSelection   (items: Set<LocalDate>)          { selectionModel?.replaceAll(items.filter { it.sameMonth(startDate) }) }
    override fun removeSelection(items: Set<LocalDate>)          { selectionModel?.removeAll (items.filter { it.sameMonth(startDate) }) }
    override fun toggleSelection(items: Set<LocalDate>)          { selectionModel?.toggle    (items.filter { it.sameMonth(startDate) }) }
    override fun clearSelection (                     )          { selectionModel?.removeAll ((firstSelectable .. lastSelectable).toList()) }

    override fun next    (after : LocalDate): LocalDate? = (after  + DatePeriod(days = 1)).takeIf { it <= lastSelectable  }
    override fun previous(before: LocalDate): LocalDate? = (before - DatePeriod(days = 1)).takeIf { it >= firstSelectable }

    override val firstSelection : LocalDate?     get() = selectionModel?.asSequence()?.filter { it.sameMonth(startDate) }?.sortedBy { it.dayOfMonth }?.firstOrNull()
    override val lastSelection  : LocalDate?     get() = selectionModel?.asSequence()?.filter { it.sameMonth(startDate) }?.sortedBy { it.dayOfMonth }?.lastOrNull ()
    override val selectionAnchor: LocalDate?     get() = selectionModel?.anchor?.takeIf { it.sameMonth(startDate) }
    override val selection      : Set<LocalDate> get() = selectionModel?.filterTo(mutableSetOf()) { it.sameMonth(startDate) } ?: emptySet()

    override val firstSelectable: LocalDate get() = startDate
    override val lastSelectable : LocalDate get() = endDate

    private fun LocalDate.sameMonth(other: LocalDate) = this.year == other.year && this.month == other.month

    private fun ClosedRange<LocalDate>.toList(): List<LocalDate> {
        var d = start
        val result = mutableListOf<LocalDate>()

        while (d <= endInclusive) {
            result += d
            d += DatePeriod(days = 1)
        }

        return result
    }
}