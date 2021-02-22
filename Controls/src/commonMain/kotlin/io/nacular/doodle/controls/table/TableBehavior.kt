package io.nacular.doodle.controls.table

import io.nacular.doodle.controls.IndexedIem
import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.utils.Completable
import io.nacular.doodle.utils.NoOpCompletable
import io.nacular.doodle.utils.Path

/**
 * Created by Nicholas Eddy on 4/6/19.
 */
public class HeaderGeometry(public val y: Double, public val height: Double)

public abstract class TableBehavior<T>: Behavior<Table<T, *>> {
    public fun Table<T, *>.bodyDirty  (                 ): Unit = bodyDirty  (      )
    public fun Table<T, *>.headerDirty(                 ): Unit = headerDirty(      )
    public fun Table<T, *>.columnDirty(column: Column<*>): Unit = columnDirty(column)

    public interface CellGenerator<T> {
        public operator fun <A> invoke(
                table        : Table<T, *>,
                column       : Column<A>,
                cell         : A,
                row          : Int,
                itemGenerator: ItemVisualizer<A, IndexedIem>,
                current      : View? = null
        ): View
    }

    public interface RowPositioner<T> {
        public fun rowBounds(of: Table<T, *>, row: T, index: Int): Rectangle

        public fun row(of: Table<T, *>, y: Double): Int

        public fun totalRowHeight(of: Table<T, *>): Double
    }

    public interface HeaderPositioner<T> {
        public operator fun invoke(table: Table<T, *>): HeaderGeometry
    }

    public interface HeaderCellGenerator<T> {
        public operator fun <A> invoke(table: Table<T, *>, column: Column<A>): View
    }

    public interface OverflowColumnConfig<T> {
        public fun header(table: Table<T, *>): View? = null
        public fun body  (table: Table<T, *>): View? = null
    }

    public abstract val cellGenerator       : CellGenerator<T>
    public abstract val rowPositioner       : RowPositioner<T>
    public abstract val headerPositioner    : HeaderPositioner<T>
    public abstract val headerCellGenerator : HeaderCellGenerator<T>
    public abstract val overflowColumnConfig: OverflowColumnConfig<T>?

    public open fun moveColumn(table: Table<T, *>, block: (Float) -> Unit): Completable = NoOpCompletable.also { block(1f) }

    public open fun <A> columnMoveStart(table: Table<T, *>, column: Column<A>) {}
    public open fun <A> columnMoved    (table: Table<T, *>, column: Column<A>) {}
    public open fun <A> columnMoveEnd  (table: Table<T, *>, column: Column<A>) {}

    public open fun renderHeader        (table: Table<T, *>,                    canvas: Canvas) {}
    public open fun renderBody          (table: Table<T, *>,                    canvas: Canvas) {}
    public open fun <A> renderColumnBody(table: Table<T, *>, column: Column<A>, canvas: Canvas) {}
}

public interface TreeTableBehavior<T>: Behavior<TreeTable<T, *>> {
    public interface TreeCellGenerator<T> {
        public operator fun <A> invoke(table: TreeTable<T, *>, column: Column<A>, cell: A, path: Path<Int>, row: Int, itemGenerator: ItemVisualizer<A, IndexedIem>, current: View? = null): View
    }

    public interface CellGenerator<T> {
        public operator fun <A> invoke(table: TreeTable<T, *>, column: Column<A>, cell: A, path: Path<Int>, row: Int, itemGenerator: ItemVisualizer<A, IndexedIem>, current: View? = null): View
    }

    public abstract class RowPositioner<T> {
        public fun TreeTable<T, *>.rowsBelow(path: Path<Int>): Int = this.rowsBelow(path)

        public abstract fun rowBounds(of: TreeTable<T, *>, path: Path<Int>, row: T, index: Int): Rectangle

        public abstract fun rowFor(of: TreeTable<T, *>, y: Double): Int

        public abstract fun height(of: TreeTable<T, *>, below: Path<Int>): Double
    }

    public interface HeaderPositioner<T> {
        public operator fun invoke(table: TreeTable<T, *>): HeaderGeometry
    }

    public interface HeaderCellGenerator<T> {
        public operator fun <A> invoke(table: TreeTable<T, *>, column: Column<A>): View
    }

    public interface OverflowColumnConfig<T> {
        public fun header(table: TreeTable<T, *>): View? = null
        public fun body  (table: TreeTable<T, *>): View? = null
    }

    public val treeCellGenerator   : TreeCellGenerator<T>
    public val cellGenerator       : CellGenerator<T>
    public val rowPositioner       : RowPositioner<T>
    public val headerPositioner    : HeaderPositioner<T>
    public val headerCellGenerator : HeaderCellGenerator<T>
    public val overflowColumnConfig: OverflowColumnConfig<T>?

    public var bodyDirty  : ((         ) -> Unit)?
    public var headerDirty: ((         ) -> Unit)?
    public var columnDirty: ((Column<*>) -> Unit)?

    public fun moveColumn(table: TreeTable<T, *>, block: (Float) -> Unit): Completable = NoOpCompletable.also { block(1f) }

    public fun <A> columnMoveStart(table: TreeTable<T, *>, column: Column<A>) {}
    public fun <A> columnMoved    (table: TreeTable<T, *>, column: Column<A>) {}
    public fun <A> columnMoveEnd  (table: TreeTable<T, *>, column: Column<A>) {}

    public fun renderHeader        (table: TreeTable<T, *>, canvas: Canvas) {}
    public fun renderBody          (table: TreeTable<T, *>, canvas: Canvas) {}
    public fun <A> renderColumnBody(table: TreeTable<T, *>, column: Column<A>, canvas: Canvas) {}
}