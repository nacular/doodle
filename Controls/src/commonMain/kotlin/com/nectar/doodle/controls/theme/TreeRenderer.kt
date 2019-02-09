package com.nectar.doodle.controls.theme

import com.nectar.doodle.controls.tree.Tree
import com.nectar.doodle.core.View
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.theme.Renderer
import com.nectar.doodle.utils.Path

/**
 * Created by Nicholas Eddy on 3/23/18.
 */
interface TreeRenderer<T>: Renderer<Tree<T, *>> {
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