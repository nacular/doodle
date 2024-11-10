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

        /**
         * Indicates where to place a row in the Tree.
         *
         * @param of the tree where the row is
         * @param node data in question
         * @param path of the row
         * @param index of the row
         * @return row bounds relative to the tree
         */
        public abstract fun rowBounds(of: Tree<T, *>, node: T, path: Path<Int>, index: Int): Rectangle

        /**
         * Specifies the bounds of content within a Tree's row.
         *
         * @param of the tree where the row is
         * @param node data in question
         * @param path of the row
         * @param index of the row
         * @return content bounds relative to the row
         */
        public abstract fun contentBounds(of: Tree<T, *>, node: T, path: Path<Int>, index: Int): Rectangle

        /**
         * Indicates which row would be located at the given y offset.
         *
         * @param of the tree where the row is
         * @param at position
         * @return index of the row
         */
        public abstract fun row(of: Tree<T, *>, at: Point): Int

        /**
         * Indicates the minimum size needed to display content under [below].
         *
         * @param of the tree where the row is
         * @param below this given node
         * @return size of contents below node
         */
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
