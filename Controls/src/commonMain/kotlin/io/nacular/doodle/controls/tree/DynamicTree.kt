package io.nacular.doodle.controls.tree

import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.controls.SelectionModel
import io.nacular.doodle.controls.ExpandableItem
import io.nacular.doodle.utils.Path
import io.nacular.doodle.utils.Pool
import io.nacular.doodle.utils.SetPool

/**
 * Created by Nicholas Eddy on 9/29/19.
 */

public typealias ItemsObserver<T> = (source: Tree<T, *>, removed: Map<Path<Int>, T>, added: Map<Path<Int>, T>, moved: Map<Path<Int>, Pair<Path<Int>, T>>) -> Unit

public open class DynamicTree<T, M: DynamicTreeModel<T>>(model: M, itemVisualizer: ItemVisualizer<T, ExpandableItem>? = null, selectionModel: SelectionModel<Path<Int>>? = null): Tree<T, M>(model, itemVisualizer = itemVisualizer, selectionModel = selectionModel) {
    private val modelChanged: ModelObserver<T> = { _,removed,added,moved ->
        var trueRemoved = removed.filterKeys { it !in added   }
        var trueAdded   = added.filterKeys   { it !in removed }

        // Handle selection move
        itemsRemoved(trueRemoved)
        itemsAdded  (trueAdded  )

        if (trueRemoved.isNotEmpty() || trueAdded.isNotEmpty() || moved.isNotEmpty()) {
            refreshAll()
        }

        trueAdded   = trueAdded.filterKeys   { rowFromPath(it)?.let { it <= lastVisibleRow } ?: false }
        trueRemoved = trueRemoved.filterKeys { rowFromPath(it)?.let { it <= lastVisibleRow } ?: false }

        if (trueRemoved.size > trueAdded.size) {
            if (children.size == lastVisibleRow - 1) {
                children.batch {
                    for (it in 0..trueRemoved.size - trueAdded.size) {
                        if (size > 0) {
                            removeAt(size - 1)
                        }
                    }
                }
            }
        }

        if (trueRemoved.isNotEmpty() || trueAdded.isNotEmpty() || moved.isNotEmpty()) {
            // FIXME: Make this more efficient
            (firstVisibleRow..lastVisibleRow).forEach { update(children, pathFromRow(it)!!) }
        } else {
            // These are the edited rows
            added.keys.filter { it in removed }.forEach { update(children, it) }
        }

        (itemsChanged as SetPool).forEach { it(this, removed, added, moved) }
    }

    public val itemsChanged: Pool<ItemsObserver<T>> = SetPool()

    init {
        model.changed += modelChanged
    }

    private fun itemsAdded(values: Map<Path<Int>, T>) {
//        if (selectionModel != null && values.isNotEmpty()) {
//            val updatedSelection = mutableSetOf<Path<Int>>()
//
//            for (selectionItem in selectionModel) {
//                var delta = 0
//
//                for (path in values.keys) {
//                    if (selectionItem >= path) {
//                        ++delta
//                    }
//                }
//
//                updatedSelection.add(selectionItem + delta)
//            }
//
//            setSelection(updatedSelection)
//        }
    }

    private fun itemsRemoved(values: Map<Path<Int>, T>) {
//        if (selectionModel != null && values.isNotEmpty()) {
//
//            val updatedSelection = mutableSetOf<Path<Int>>()
//
//            for (selectionItem in selectionModel) {
//                var delta = 0
//
//                for (path in values.keys) {
//                    if (selectionItem > path) {
//                        delta--
//                    }
//                }
//
//                if (delta != 0) {
//                    updatedSelection.add(selectionItem + delta)
//                }
//            }
//
//            removeSelection(values.keys)
//
//            setSelection(updatedSelection)
//        }
    }
}
