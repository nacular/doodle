package io.nacular.doodle.controls.treecolumns

import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.utils.Path

/**
 * Created by Nicholas Eddy on 7/25/20.
 */
abstract class TreeColumnsBehavior<T>: Behavior<TreeColumns<T, *>> {
    interface CellGenerator<T> {
        operator fun invoke(treeColumns: TreeColumns<T, *>, node: T, path: Path<Int>, row: Int, current: View? = null): View
    }

    interface RowPositioner<T> {
        fun rowBounds(treeColumns: TreeColumns<T, *>, columnWidth: Double, path: Path<Int>, row: T, index: Int, current: View? = null): Rectangle

        fun row(of: TreeColumns<T, *>, path: Path<Int>, y: Double): Int

        fun totalRowHeight(of: TreeColumns<T, *>, path: Path<Int>): Double
    }

    abstract val generator : CellGenerator<T>
    abstract val positioner: RowPositioner<T>

    fun TreeColumns<T, *>.columnDirty(path: Path<Int>) { this.columnDirty(path) }

    open fun renderColumnBody(treeColumns: TreeColumns<T, *>, path: Path<Int>, canvas: Canvas) {}
}