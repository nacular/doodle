package io.nacular.doodle.controls.table

import io.nacular.doodle.controls.IndexedItem
import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.utils.Completable
import io.nacular.doodle.utils.NoOpCompletable
import io.nacular.doodle.utils.Path

/**
 * Indicates the y-offset and height of a table's header.
 */
public class MetaRowGeometry(public val insetTop: Double, public val insetBottom: Double, public val height: Double)

public interface AbstractTableBehavior<T: View>: Behavior<T> {
    /**
     * Provides the [MetaRowGeometry] for a given Table
     */
    public interface MetaRowPositioner<T> {
        /**
         * @param table
         * @return the location of [table]'s header
         */
        public operator fun invoke(table: T): MetaRowGeometry
    }

    /**
     * Creates the Views used for a Table's column header.
     */
    public interface HeaderCellGenerator<T> {
        /**
         * @param table in question
         * @param column in [table]
         * @return the header for [column]
         */
        public operator fun <A> invoke(table: T, column: Column<A>): View
    }

    /**
     * Creates the Views used for a Table's column footer.
     */
    public interface FooterCellGenerator<T> {
        /**
         * @param table in question
         * @param column in [table]
         * @return the footer for [column]
         */
        public operator fun <A> invoke(table: T, column: Column<A>): View
    }

    /**
     * Controls what goes into the unused space after the last column of a Table.
     * This virtual column is visible when the real columns do not fill the entire
     * width of the Table.
     */
    public interface OverflowColumnConfig<T> {
        /**
         * @param table in question
         * @return a View (or not) for the virtual column header
         */
        public fun header(table: T): View? = null

        /**
         * @param table in question
         * @return a View (or not) for the virtual column body
         */
        public fun body  (table: T): View? = null
    }

    public val headerPositioner    : MetaRowPositioner<T>
    public val footerPositioner    : MetaRowPositioner<T>
    public val headerCellGenerator : HeaderCellGenerator<T>
    public val footerCellGenerator : FooterCellGenerator<T>
    public val overflowColumnConfig: OverflowColumnConfig<T>?

    /**
     * Called whenever a column needs to automatically move to a location.
     * This happens (for example) if a column is being reordered. The given
     * [block] controls the progress and timing of this move, allowing for custom
     * animation.
     *
     * Calling block with progress from 0 to 1 results in moving the column
     * along the path from start to end.
     *
     * @param table in question
     * @param distance over which the column will move
     * @param block controlling move animation
     * @return a completable operation the Table will monitor
     */
    public fun moveColumn(table: T, distance: Double, block: (progress: Float) -> Unit): Completable = NoOpCompletable.also { block(1f) }

    /**
     * Called whenever a column begins moving in [table].
     *
     * @param table where column is
     * @param column being moved
     */
    public fun <A> columnMoveStart(table: T, column: Column<A>) {}

    /**
     * Called each time a column's position changes in [table].
     *
     * @param table where column is
     * @param column that was moved
     */
    public fun <A> columnMoved(table: T, column: Column<A>) {}

    /**
     * Called whenever movement of a column (started when [moveColumn] was called) ends.
     *
     * @param table where column is
     * @param column that was moved
     */
    public fun <A> columnMoveEnd(table: T, column: Column<A>) {}

    /**
     * Used to render the Table's header background.
     *
     * @param table in question
     * @param canvas of [table]'s header
     */
    public fun renderHeader(table: T, canvas: Canvas) {}

    /**
     * Used to render the Table's footer background.
     *
     * @param table in question
     * @param canvas of [table]'s footer
     */
    public fun renderFooter(table: T, canvas: Canvas) {}

    /**
     * Used to render the Table's body background.
     *
     * @param table in question
     * @param canvas of [table]'s body
     */
    public fun renderBody(table: T, canvas: Canvas) {}

    /**
     * Used to render individual backgrounds for the Table's columns.
     * This draws **above** the Table's body; and is useful for cases
     * where columns need distinct visuals (i.e. when being dragged).
     *
     * @param table in question
     * @param column in [table]
     * @param canvas of the column
     */
    public fun <A> renderColumnBody(table: T, column: Column<A>, canvas: Canvas) {}
}

/**
 * Controls the look and feel for a [Table].
 */
public abstract class TableBehavior<T>: AbstractTableBehavior<Table<T, *>> {
    /**
     * Responsible for creating a [View] to represent each cell in the Table.
     */
    public interface CellGenerator<T> {
        /**
         * Called whenever a new cell is needed by [table].
         *
         * @param table that needs the cell
         * @param column in [table] where the cell is
         * @param cell data
         * @param row in [table] where the cell is
         * @param itemGenerator that can be used to visualize the cell contents
         * @param current [View] being used to represent the cell
         * @return a View representing the cell (or [current] if it can be reused).
         */
        public operator fun <A> invoke(
                table        : Table<T, *>,
                column       : Column<A>,
                cell         : A,
                row          : Int,
                itemGenerator: ItemVisualizer<A, IndexedItem>,
                current      : View? = null
        ): View
    }

    /**
     * Determines the location and height of rows in the Table.
     */
    public interface RowPositioner<T> {
        /**
         * Indicates where to place a row in the Table.
         *
         * @param of the table where the row is
         * @param row data in question
         * @param index of the row
         * @return row bounds relative to the table
         */
        public fun rowBounds(of: Table<T, *>, row: T, index: Int): Rectangle

        /**
         * Indicates which row would be located at the given y offset.
         *
         * @param of the table where the row is
         * @param at position
         * @return index of the row
         */
        public fun row(of: Table<T, *>, at: Point): Int

        /**
         * Indicates the minimum size with all rows in the given Table.
         *
         * @param of the table where the row is
         * @return minimum size of rows
         */
        public fun minimumSize(of: Table<T, *>): Size
    }

    public abstract val cellGenerator: CellGenerator<T>
    public abstract val rowPositioner: RowPositioner<T>

    /**
     * Requests that the Table repaint its body. This will result in a call to [renderBody].
     */
    protected fun Table<T, *>.bodyDirty(): Unit = bodyDirty()

    /**
     * Requests that the Table repaint its header. This will result in a call to [renderHeader].
     */
    protected fun Table<T, *>.headerDirty(): Unit = headerDirty()

    /**
     * Requests that the Table repaint its footer. This will result in a call to [renderFooter].
     */
    protected fun Table<T, *>.footerDirty(): Unit = footerDirty()

    /**
     * Requests that the Table repaint a column. This will result in a call to [renderColumnBody].
     */
    protected fun Table<T, *>.columnDirty(column: Column<*>): Unit = columnDirty(column)
}

/**
 * Controls the look and feel for a [TreeTable].
 */
public abstract class TreeTableBehavior<T>: AbstractTableBehavior<TreeTable<T, *>> {
    /**
     * Responsible for creating a [View] to represent each cell in the first column of the TreeTable.
     */
    public interface TreeCellGenerator<T> {
        /**
         * Called whenever [table] needs a new cell in the first column.
         *
         * @param table that needs the cell
         * @param column in [table] where the cell is
         * @param cell data
         * @param path to the cell
         * @param row in [table] where the cell is
         * @param itemGenerator that can be used to visualize the cell contents
         * @param current [View] being used to represent the cell
         * @return a View representing the cell (or [current] if it can be reused).
         */
        public operator fun <A> invoke(
                table        : TreeTable<T, *>,
                column       : Column<A>,
                cell         : A,
                path         : Path<Int>,
                row          : Int,
                itemGenerator: ItemVisualizer<A, IndexedItem>,
                current      : View? = null
        ): View
    }

    /**
     * Responsible for creating a [View] to represent each cell in the TreeTable.
     */
    public interface CellGenerator<T> {
        /**
         * Called whenever [table] needs a new cell is in columns to the right of the first.
         *
         * @param table that needs the cell
         * @param column in [table] where the cell is
         * @param cell data
         * @param path to the cell
         * @param row in [table] where the cell is
         * @param itemGenerator that can be used to visualize the cell contents
         * @param current [View] being used to represent the cell
         * @return a View representing the cell (or [current] if it can be reused).
         */
        public operator fun <A> invoke(
                table        : TreeTable<T, *>,
                column       : Column<A>,
                cell         : A,
                path         : Path<Int>,
                row          : Int,
                itemGenerator: ItemVisualizer<A, IndexedItem>,
                current      : View? = null
        ): View
    }

    /**
     * Determines the location and height of rows in the TreeTable.
     */
    public abstract class RowPositioner<T> {
        public fun TreeTable<T, *>.rowsBelow(path: Path<Int>): Int = this.rowsBelow(path)

        /**
         * Indicates where to place a row in the TreeTable.
         *
         * @param of the table where the row is
         * @param path of the row
         * @param row data in question
         * @param index of the row
         * @return row bounds relative to the table
         */
        public abstract fun rowBounds(of: TreeTable<T, *>, path: Path<Int>, row: T, index: Int): Rectangle

        /**
         * Indicates which row would be located at the given y offset.
         *
         * @param of the table where the row is
         * @param at position
         * @return index of the row
         */
        public abstract fun row(of: TreeTable<T, *>, at: Point): Int

        /**
         * Indicates the total height of the path's descendant rows (recursively).
         *
         * @param of the table where the path is
         * @param below the given path
         * @return size of rows
         */
        public abstract fun size(of: TreeTable<T, *>, below: Path<Int>): Size
    }

    public abstract val treeCellGenerator: TreeCellGenerator<T>
    public abstract val cellGenerator    : CellGenerator<T>
    public abstract val rowPositioner    : RowPositioner<T>

    /**
     * Requests that the TreeTable repaint its body. This will result in a call to [renderBody].
     */
    protected fun TreeTable<T, *>.bodyDirty(): Unit = bodyDirty()

    /**
     * Requests that the TreeTable repaint its header. This will result in a call to [renderHeader].
     */
    protected fun TreeTable<T, *>.headerDirty(): Unit = headerDirty()

    /**
     * Requests that the TreeTable repaint its footer. This will result in a call to [renderFooter].
     */
    protected fun TreeTable<T, *>.footerDirty(): Unit = footerDirty()

    /**
     * Requests that the TreeTable repaint a column. This will result in a call to [renderColumnBody].
     */
    protected fun TreeTable<T, *>.columnDirty(column: Column<*>): Unit = columnDirty(column)
}