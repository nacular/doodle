package com.nectar.doodle.controls.theme

import com.nectar.doodle.controls.tree.Tree
import com.nectar.doodle.core.View
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.theme.Renderer
import com.nectar.doodle.utils.Path

/**
 * Created by Nicholas Eddy on 3/23/18.
 */

interface TreeRenderer<T>: Renderer<Tree<T>> {
    interface ItemUIGenerator<T> {
        operator fun invoke(tree: Tree<T>, node: T, path: Path<Int>, index: Int, current: View? = null): View
    }

    interface ItemPositioner<T> {
        operator fun invoke(tree: Tree<T>, node: T, path: Path<Int>, index: Int): Rectangle

        fun rowFor(tree: Tree<T>, y: Double): Int
    }

    val positioner : ItemPositioner<T>
    val uiGenerator: ItemUIGenerator<T>
}