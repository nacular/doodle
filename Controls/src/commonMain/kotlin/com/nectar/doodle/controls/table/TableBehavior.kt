package com.nectar.doodle.controls.table

import com.nectar.doodle.controls.ItemGenerator
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.theme.Behavior

/**
 * Created by Nicholas Eddy on 4/6/19.
 */
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
        operator fun invoke(table: Table<T, *>, column: Table.Column<T>): View
    }

    data class HeaderGeometry(val y: Double, val height: Double)

    val cellGenerator      : CellGenerator<T>
    val rowPositioner      : RowPositioner<T>
    val headerPositioner   : HeaderPositioner<T>
    val headerCellGenerator: HeaderCellGenerator<T>

    var headerDirty: (() -> Unit)?
    var bodyDirty  : (() -> Unit)?

    fun renderHeader(table: Table<T, *>, canvas: Canvas) {}
    fun renderBody  (table: Table<T, *>, canvas: Canvas) {}
}