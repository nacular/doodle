package com.nectar.doodle.controls.tree

import com.nectar.doodle.controls.SelectionModel
import com.nectar.doodle.utils.Path

/**
 * Created by Nicholas Eddy on 9/29/19.
 */
open class DynamicTree<T, M: MutableTreeModel<T>>(model: M, selectionModel: SelectionModel<Path<Int>>? = null): Tree<T, M>(model, selectionModel) {
    private val modelChanged: ModelObserver<T> = { _,removed,added,_ ->
        var trueRemoved = removed.filterKeys { it !in added   }
        var trueAdded   = added.filterKeys   { it !in removed }

        itemsRemoved(trueRemoved)
        itemsAdded  (trueAdded  )

        if (trueRemoved.isNotEmpty() || trueAdded.isNotEmpty()) {
            refreshAll()
        }

        trueAdded   = trueAdded.filterKeys   { rowFromPath(it)?.let { it <= lastVisibleRow } ?: false }
        trueRemoved = trueRemoved.filterKeys { rowFromPath(it)?.let { it <= lastVisibleRow } ?: false }

        if (trueRemoved.size > trueAdded.size) {
            if (children.size == lastVisibleRow - 1) {
                children.batch {
                    for (it in 0..trueRemoved.size - trueAdded.size) {
                        children.removeAt(0)
                    }
                }
            }
        }

        if (trueRemoved.isNotEmpty() || trueAdded.isNotEmpty()) {
            // FIXME: Make this more efficient
            (firstVisibleRow..lastVisibleRow).forEach { update(children, pathFromRow(it)!!) }
        } else {
            // These are the edited rows
            added.keys.filter { it in removed }.forEach { update(children, it) }
        }
    }

    init {
        model.changed += modelChanged
    }

    override fun removedFromDisplay() {
        model.changed -= modelChanged

        super.removedFromDisplay()
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
