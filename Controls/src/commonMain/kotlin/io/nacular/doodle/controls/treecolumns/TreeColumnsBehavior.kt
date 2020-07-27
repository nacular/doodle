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
        operator fun invoke(treeColumns: TreeColumns<T, *>, columnWidth: Double, path: Path<Int>, row: T, index: Int): Rectangle

        fun row(of: TreeColumns<T, *>, y: Double): Int
    }

    abstract val generator : CellGenerator<T>
    abstract val positioner: RowPositioner<T>

    fun TreeColumns<T, *>.columnDirty(path: Path<Int>) { this.columnDirty(path) }

    open fun renderColumnBody(treeColumns: TreeColumns<T, *>, path: Path<Int>, canvas: Canvas) {}
}