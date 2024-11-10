package io.nacular.doodle.controls.panels

import io.nacular.doodle.controls.panels.GridPanel.Companion.FitContent
import io.nacular.doodle.controls.panels.SizingPolicy.OverlappingView
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.Layout
import io.nacular.doodle.core.Positionable
import io.nacular.doodle.core.View
import io.nacular.doodle.core.behavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.constraints.Bounds
import io.nacular.doodle.layout.constraints.Constrainer
import io.nacular.doodle.layout.constraints.ConstraintDslContext
import io.nacular.doodle.layout.constraints.fill
import io.nacular.doodle.utils.observable
import kotlin.math.max
import kotlin.math.min
import kotlin.properties.ReadWriteProperty

/**
 * Determines how rows or columns are sized within a [GridPanel].
 */
public interface SizingPolicy {
    /**
     * Represents the dimensions of a View within a collection of cells in a [GridPanel].
     *
     * @param span indicating how many rows/columns the View occupies
     * @param size of the View (width or height)
     * @param idealSize of the View (width or height)
     */
    public class OverlappingView(public val span: Int, public val size: Double, public val idealSize: Double?)

    /**
     * Generates a map between row/column number and width/height.
     *
     * @param panelSize of the GridPanel (width/height)
     * @param spacing of the rows or columns in the panel
     * @param views a mapping of the list of overlapping View items to a row / column
     */
    public operator fun invoke(panelSize: Double, spacing: Double, views: Map<Int, List<OverlappingView>>): Map<Int, Double>
}

/**
 * Determines the spacing of a [GridPanel]'s rows or columns.
 */
public typealias SpacingPolicy = (panelSize: Double) -> Double

/**
 * A control that manages a generic list of `View`s and displays them within a grid layout.
 */
public open class GridPanel: View() {
    private class Location(val row: Int, val column: Int) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Location) return false

            if (row != other.row) return false
            if (column != other.column) return false

            return true
        }

        override fun hashCode(): Int {
            var result = row
            result = 31 * result + column
            return result
        }
    }

    private class Dimensions(var offset: Double = 0.0, var size: Double = 0.0) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Dimensions) return false

            if (offset != other.offset) return false
            if (size != other.size) return false

            return true
        }

        override fun hashCode(): Int {
            var result = offset.hashCode()
            result = 31 * result + size.hashCode()
            return result
        }
    }

    private val locations        = mutableMapOf<View, Set<Location>>()
    private val rowSpans         = mutableMapOf<View, Int>().withDefault { 1 }
    private val columnSpans      = mutableMapOf<View, Int>().withDefault { 1 }
    private var rowDimensions    = mapOf<Int, Dimensions>()
    private var columnDimensions = mapOf<Int, Dimensions>()

    /**
     * Controls how items are aligned within the grid cells. Defaults to [fill].
     */
    public var cellAlignment: ConstraintDslContext.(Bounds) -> Unit by layoutProperty(fill)

    /**
     * Determines the space between rows. Defaults = `0.0`
     */
    public var rowSpacing: SpacingPolicy by layoutProperty({ 0.0 })

    /**
     * Determines the space between columns. Defaults = `0.0`
     */
    public var columnSpacing: SpacingPolicy by layoutProperty({ 0.0 })

    /**
     * Controls how rows are sized within the panel. Defaults = [FitContent]
     */
    public var rowSizingPolicy: SizingPolicy by layoutProperty(FitContent)

    /**
     * Controls how columns are sized within the panel. Defaults = [FitContent]
     */
    public var columnSizingPolicy: SizingPolicy by layoutProperty(FitContent)

    /**
     * Controls how the panel behaves. The panel delegates rendering to its behavior when
     * specified.
     */
    public var behavior: Behavior<GridPanel>? by behavior()

    final override var layout: Layout? = GridLayout()

    public override var insets: Insets get() = super.insets; set(new) { super.insets = new; relayout() }

    /**
     * Adds a View to the panel at the specified grid cells.
     *
     * @param child being added
     * @param row in the panel to put the child
     * @param column in the panel to put the child
     * @param rowSpan number of rows the item will span in the panel
     * @param columnSpan number of columns the item will span in the panel
     */
    public fun add(child: View, row: Int = 0, column: Int = 0, rowSpan: Int = 1, columnSpan: Int = 1) {
        locations[child] = mutableSetOf<Location>().apply {
            repeat(rowSpan) { r ->
                repeat(columnSpan) { c ->
                    add(Location(row = max(0, row + r), column = max(0, column + c)))
                }
            }
        }

        if (rowSpan    > 1) rowSpans   [child] = rowSpan
        if (columnSpan > 1) columnSpans[child] = columnSpan

        children += child
    }

    /**
     * @param child to remove from the panel.
     */
    public fun remove(child: View) {
        locations   -= child
        rowSpans    -= child
        columnSpans -= child
        children    -= child
    }

    /**
     * Clears all children from the panel.
     */
    public fun clear() {
        locations.clear  ()
        rowSpans.clear   ()
        columnSpans.clear()
        children.clear   ()
    }

    override fun render(canvas: Canvas) {
        behavior?.render(this, canvas)
    }

    private inline fun <T> layoutProperty(initial: T, noinline onChange: GridPanel.(old: T, new: T) -> Unit = { _,_ -> }): ReadWriteProperty<GridPanel, T> = observable(initial) { old, new ->
        relayout()
        onChange(old, new)
    }

    private inner class GridLayout: Layout {
        private val constrainer = Constrainer()

        private var idealWidth  = null as Double?
        private var idealHeight = null as Double?

        override fun layout(views: Sequence<Positionable>, min: Size, current: Size, max: Size, insets: Insets): Size {
            val rowLanes = mutableMapOf<Int, MutableList<OverlappingView>>()
            val colLanes = mutableMapOf<Int, MutableList<OverlappingView>>()

            // Calculate row and column sizes
            children.forEach { child ->
                locations[child]?.forEach { location ->
                    val rowSpan   = rowSpans.getValue   (child)
                    val colSpan   = columnSpans.getValue(child)
                    val idealSize = child.idealSize

                    rowLanes.getOrPut(location.row   ) { mutableListOf() }.also { it += OverlappingView(rowSpan, child.size.height, idealSize.height) }
                    colLanes.getOrPut(location.column) { mutableListOf() }.also { it += OverlappingView(colSpan, child.size.width,  idealSize.width ) }
                }
            }

            val panelWidth        = current.width  - insets.run { left + right  }
            val panelHeight       = current.height - insets.run { top  + bottom }
            val verticalSpacing   = rowSpacing  (panelHeight)
            val horizontalSpacing = columnSpacing(panelWidth)

            rowDimensions    = rowSizingPolicy   (panelHeight, verticalSpacing,   rowLanes).mapValues { Dimensions(0.0, it.value) }
            columnDimensions = columnSizingPolicy(panelWidth,  horizontalSpacing, colLanes).mapValues { Dimensions(0.0, it.value) }

            var offset = 0.0
            rowDimensions.entries.sortedBy { it.key }.forEach {
                it.value.offset = offset
                offset += it.value.size + verticalSpacing
            }

            idealHeight = offset - if (rowDimensions.size > 1) verticalSpacing else 0.0

            offset = 0.0
            columnDimensions.entries.sortedBy { it.key }.forEach {
                it.value.offset = offset
                offset += it.value.size + horizontalSpacing
            }

            idealWidth = offset - if (columnDimensions.size > 1) horizontalSpacing else 0.0

            children.forEach {
                var x       = null as Double?
                var y       = null as Double?
                val widths  = mutableSetOf<Dimensions>()
                val heights = mutableSetOf<Dimensions>()

                locations[it]?.forEach {
                    val rowDim = rowDimensions.getValue   (it.row   )
                    val colDim = columnDimensions.getValue(it.column)

                    x = min(x ?: colDim.offset, colDim.offset)
                    y = min(y ?: rowDim.offset, rowDim.offset)
                    widths.add (colDim)
                    heights.add(rowDim)
                }

                it.suggestBounds(constrainer(
                    it.bounds,
                    within = Rectangle(
                        (x ?: 0.0) + insets.left,
                        (y ?: 0.0) + insets.top,
                        widths.sumOf  { it.size } + horizontalSpacing * (widths.size - 1),
                        heights.sumOf { it.size } + verticalSpacing *   (heights.size - 1)
                    ),
                    using     = cellAlignment
                ))
            }

            return Size(idealWidth!! + insets.run { left + right }, idealHeight!! + insets.run { top + bottom })
        }
    }

    public companion object {

        private class FitContentImpl: SizingPolicy {
            override fun invoke(panelSize: Double, spacing: Double, views: Map<Int, List<OverlappingView>>): Map<Int, Double> = views.mapValues { entry ->
                var size = 0.0

                entry.value.forEach {
                    size = max(size, (it.size - spacing * (it.span - 1)) / it.span)
                }

                size
            }
        }

        private class FitPanelImpl: SizingPolicy {
            override fun invoke(panelSize: Double, spacing: Double, views: Map<Int, List<OverlappingView>>): Map<Int, Double> = views.mapValues {
                max(0.0, (panelSize - spacing * (views.size - 1)) / views.size)
            }
        }

        /**
         * Computes row / column sizes, so they fit the [GridPanel]'s height / width (including insets)
         */
        public val FitPanel  : SizingPolicy = FitPanelImpl()

        /**
         * Computes row / column sizes, so they fit the largest View that overlaps them.
         */
        public val FitContent: SizingPolicy = FitContentImpl()
    }
}