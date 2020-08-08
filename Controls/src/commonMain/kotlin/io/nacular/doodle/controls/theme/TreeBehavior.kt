package io.nacular.doodle.controls.theme

import io.nacular.doodle.controls.tree.Tree
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.View
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.utils.Path

/**
 * Created by Nicholas Eddy on 3/23/18.
 */
interface TreeBehavior<T>: Behavior<Tree<T, *>> {
    interface RowGenerator<T> {
        operator fun invoke(tree: Tree<T, *>, node: T, path: Path<Int>, index: Int, current: View? = null): View
    }

    abstract class RowPositioner<T> {
        fun Tree<T, *>.rowsBelow(path: Path<Int>): Int = this.rowsBelow(path)

        abstract fun rowBounds    (tree: Tree<T, *>, node: T, path: Path<Int>, index: Int, current: View? = null): Rectangle
        abstract fun contentBounds(tree: Tree<T, *>, node: T, path: Path<Int>, index: Int, current: View? = null): Rectangle

        abstract fun row(of: Tree<T, *>, atY: Double): Int
        abstract fun height(of: Tree<T, *>, below: Path<Int>): Double
    }

    val generator : RowGenerator<T>
    val positioner: RowPositioner<T>
}