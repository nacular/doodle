package io.nacular.doodle.controls.treecolumns

import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.utils.Path

/**
 * Created by Nicholas Eddy on 7/25/20.
 */
public abstract class TreeColumnsBehavior<T>: Behavior<TreeColumns<T, *>> {
    public interface CellGenerator<T> {
        public operator fun invoke(treeColumns: TreeColumns<T, *>, node: T, path: Path<Int>, row: Int, current: View? = null): View
    }

    public interface RowPositioner<T> {
        public fun rowBounds(treeColumns: TreeColumns<T, *>, columnWidth: Double, path: Path<Int>, row: T, index: Int, current: View? = null): Rectangle

        public fun row(of: TreeColumns<T, *>, path: Path<Int>, y: Double): Int

        public fun totalRowHeight(of: TreeColumns<T, *>, path: Path<Int>): Double
    }

    public abstract val generator : CellGenerator<T>
    public abstract val positioner: RowPositioner<T>

    public fun TreeColumns<T, *>.columnDirty(path: Path<Int>) { this.columnDirty(path) }

    public open fun renderColumnBody(treeColumns: TreeColumns<T, *>, path: Path<Int>, canvas: Canvas) {}
}