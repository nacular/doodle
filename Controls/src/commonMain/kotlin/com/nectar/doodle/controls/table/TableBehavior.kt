package com.nectar.doodle.controls.table

import com.nectar.doodle.controls.ItemGenerator
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.theme.Behavior
import com.nectar.doodle.utils.Cancelable
import com.nectar.doodle.utils.NoOpCancelable
import com.nectar.doodle.utils.Path

/**
 * Created by Nicholas Eddy on 4/6/19.
 */
data class HeaderGeometry(val y: Double, val height: Double)


interface TableBehavior<T>: Behavior<Table<T, *>> {
    interface CellGenerator<T> {
        operator fun <A> invoke(table: Table<T, *>, cell: A, row: Int, itemGenerator: ItemGenerator<A>, current: View? = null): View
    }

    interface RowPositioner<T> {
        operator fun invoke(table: Table<T, *>, row: T, index: Int): Rectangle

        fun rowFor(table: Table<T, *>, y: Double): Int
    }

    interface HeaderPositioner<T> {
        operator fun invoke(table: Table<T, *>): HeaderGeometry
    }

    interface HeaderCellGenerator<T> {
        operator fun <A> invoke(table: Table<T, *>, column: Column<A>): View
    }

    val cellGenerator      : CellGenerator<T>
    val rowPositioner      : RowPositioner<T>
    val headerPositioner   : HeaderPositioner<T>
    val headerCellGenerator: HeaderCellGenerator<T>

    var headerDirty: (() -> Unit)?
    var bodyDirty  : (() -> Unit)?

    fun moveColumn(block: (Float) -> Unit): Cancelable = NoOpCancelable.also { block(1f) }

    fun renderHeader    (table: Table<T, *>,                    canvas: Canvas) {}
    fun renderBody      (table: Table<T, *>,                    canvas: Canvas) {}
    fun <A> renderColumnBody(table: Table<T, *>, column: Column<A>, canvas: Canvas) {}
}


interface TreeTableBehavior<T>: Behavior<TreeTable<T, *>> {
    interface TreeCellGenerator<T> {
        operator fun <A> invoke(table: TreeTable<T, *>, cell: A, path: Path<Int>, row: Int, itemGenerator: ItemGenerator<A>, current: View? = null): View
    }

    interface CellGenerator<T> {
        operator fun <A> invoke(table: TreeTable<T, *>, cell: A, path: Path<Int>, row: Int, itemGenerator: ItemGenerator<A>, current: View? = null): View
    }

    interface RowPositioner<T> {
        operator fun invoke(table: TreeTable<T, *>, path: Path<Int>, row: T, index: Int): Rectangle

        fun rowFor(table: TreeTable<T, *>, y: Double): Int
    }

    interface HeaderPositioner<T> {
        operator fun invoke(table: TreeTable<T, *>): HeaderGeometry
    }

    interface HeaderCellGenerator<T> {
        operator fun invoke(table: TreeTable<T, *>, column: Column<T>): View
    }

    val treeCellGenerator  : TreeCellGenerator<T>
    val cellGenerator      : CellGenerator<T>
    val rowPositioner      : RowPositioner<T>
    val headerPositioner   : HeaderPositioner<T>
    val headerCellGenerator: HeaderCellGenerator<T>

    var headerDirty: (() -> Unit)?
    var bodyDirty  : (() -> Unit)?

    fun renderHeader(table: TreeTable<T, *>, canvas: Canvas) {}
    fun renderBody  (table: TreeTable<T, *>, canvas: Canvas) {}
}