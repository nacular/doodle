package io.nacular.doodle.controls.theme

import io.nacular.doodle.controls.tree.Tree
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.View
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.utils.Path

/**
 * Created by Nicholas Eddy on 3/23/18.
 */
public interface TreeBehavior<T>: Behavior<Tree<T, *>> {
    public interface RowGenerator<T> {
        public operator fun invoke(tree: Tree<T, *>, node: T, path: Path<Int>, index: Int, current: View? = null): View
    }

    public abstract class RowPositioner<T> {
        public fun Tree<T, *>.rowsBelow(path: Path<Int>): Int = this.rowsBelow(path)

        public abstract fun rowBounds    (tree: Tree<T, *>, node: T, path: Path<Int>, index: Int, current: View? = null): Rectangle
        public abstract fun contentBounds(tree: Tree<T, *>, node: T, path: Path<Int>, index: Int, current: View? = null): Rectangle

        public abstract fun row(of: Tree<T, *>, atY: Double): Int
        public abstract fun height(of: Tree<T, *>, below: Path<Int>): Double
    }

    public val generator : RowGenerator<T>
    public val positioner: RowPositioner<T>
}