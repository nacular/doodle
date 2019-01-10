package com.nectar.doodle.controls.tree

import com.nectar.doodle.controls.SelectionModel
import com.nectar.doodle.core.View
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.utils.Path

/**
 * Created by Nicholas Eddy on 12/13/18.
 */

interface EditOperation<T> {
    operator fun invoke(): View?
    fun complete(): T?
    fun cancel()
}

interface TreeEditor<T> {
    fun edit(tree: MutableTree<T, *>, node: T, path: Path<Int>, contentBounds: Rectangle, current: View): EditOperation<T>
}

class MutableTree<T, M: MutableModel<T>>(model: M, selectionModel: SelectionModel<Path<Int>>? = null): Tree<T, M>(model, selectionModel) {
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
        selectionChanged += { _,_,_ ->
            editingRect?.let {
                cancelEditing()
            }
        }

        expanded += { _,_ ->
            editingRect?.let {
                cancelEditing()
            }
        }

        collapsed += { _,_ ->
            editingPath?.let {
               cancelEditing()
            }
        }
    }

    val editing get() = editingPath != null

    var editor = null as TreeEditor<T>?

    private var editingPath = null as Path<Int>?
        set(new) {
            field = new
            editingRect = field?.let { path -> this[path]?.let { node -> itemPositioner?.rowBounds(this, node, path, rowFromPath(path)!!) } }
        }

    private var editingRect   = null as Rectangle?
    private var editOperation = null as EditOperation<T>?

    fun add     (path: Path<Int>, values: T            ) = model.add     (path, values)
    fun removeAt(path: Path<Int>                       ) = model.removeAt(path        )
    fun addAll  (path: Path<Int>, values: Collection<T>) = model.addAll  (path, values)
    fun clear   (                                      ) = model.clear   (            )

    override fun removedFromDisplay() {
        model.changed -= modelChanged

        super.removedFromDisplay()
    }

    override fun handleDisplayRectEvent(old: Rectangle, new: Rectangle) {
        super.handleDisplayRectEvent(old, new)

        editingRect?.let {
            if (it !in new) {
                cancelEditing()
            }
        }
    }

    fun startEditing(path: Path<Int>) {
        if (!visible(path)) {
            return
        }

        editor?.let {
            model[path]?.let { item ->
                val i = rowFromPath(path)!! % children.size

                editingPath   = path
                editOperation = it.edit(this, item, path, super.itemPositioner?.contentBounds(this, item, path, i) ?: Rectangle.Empty, children[i]).also {
                    it()?.let { children[i] = it }

                    layout(children[i], item, path, i)
                }
            }
        }
    }

    fun completeEditing() {
        editOperation?.let { operation ->
            editingPath?.let { path ->
                val result = operation.complete() ?: return

                cleanupEditing()

                if (result == model.set(path, result)) {
                    // This is the case that the "new" value is the same as what was there
                    // so need to explicitly update since the model won't fire a change
                    update(children, path)
                }
            }
        }
    }

    fun cancelEditing() {
        cleanupEditing()?.let { update(children, it) }
    }

    private fun cleanupEditing(): Path<Int>? {
        editOperation?.cancel()
        val result    = editingPath
        editOperation = null
        editingPath   = null
        return result
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
