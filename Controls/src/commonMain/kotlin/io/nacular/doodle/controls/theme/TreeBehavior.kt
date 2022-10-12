package io.nacular.doodle.controls.theme

import io.nacular.doodle.controls.theme.TreeBehavior.RowGenerator
import io.nacular.doodle.controls.tree.Tree
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.View
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
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

        public abstract fun row(of: Tree<T, *>, at: Point): Int
        public abstract fun minimumSize(of: Tree<T, *>, below: Path<Int>): Size
    }

    public val generator : RowGenerator<T>
    public val positioner: RowPositioner<T>
}

/**
 * Creates an [RowGenerator] from the given lambda.
 *
 * @param block that will serve as the visualizer's [RowGenerator.invoke].
 */
public inline fun <T> rowGenerator(crossinline block: (tree: Tree<T, *>, node: T, path: Path<Int>, index: Int, current: View?) -> View): RowGenerator<T> = object: RowGenerator<T> {
    override fun invoke(tree: Tree<T, *>, node: T, path: Path<Int>, index: Int, current: View?) = block(tree, node, path, index, current)
}
