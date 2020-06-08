package io.nacular.doodle.controls.theme

import io.nacular.doodle.controls.tree.Tree
import io.nacular.doodle.core.View
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.utils.Path

/**
 * Created by Nicholas Eddy on 3/23/18.
 */
interface TreeBehavior<T>: Behavior<Tree<T, *>> {
    interface RowGenerator<T> {
        operator fun invoke(tree: Tree<T, *>, node: T, path: Path<Int>, index: Int, current: View? = null): View
    }

    interface RowPositioner<T> {
        fun rowBounds    (tree: Tree<T, *>, node: T, path: Path<Int>, index: Int, current: View? = null): Rectangle
        fun contentBounds(tree: Tree<T, *>, node: T, path: Path<Int>, index: Int, current: View? = null): Rectangle

        fun row(of: Tree<T, *>, atY: Double): Int
    }

    val generator : RowGenerator<T>
    val positioner: RowPositioner<T>
}