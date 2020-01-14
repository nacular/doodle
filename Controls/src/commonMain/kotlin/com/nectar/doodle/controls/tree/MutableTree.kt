package com.nectar.doodle.controls.tree

import com.nectar.doodle.controls.EditOperation
import com.nectar.doodle.controls.SelectionModel
import com.nectar.doodle.core.View
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.utils.Path

/**
 * Created by Nicholas Eddy on 12/13/18.
 */

interface TreeEditor<T> {
    fun edit(tree: MutableTree<T, *>, node: T, path: Path<Int>, contentBounds: Rectangle, current: View): EditOperation<T>
}

class MutableTree<T, M: MutableTreeModel<T>>(model: M, selectionModel: SelectionModel<Path<Int>>? = null): DynamicTree<T, M>(model, selectionModel) {
    init {
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

    operator fun set(path: Path<Int>, value: T) { model[path] = value }

    operator fun set(row: Int, value: T) { pathFromRow(row)?.let { updateModel(it, value) } }

    private var editingPath = null as Path<Int>?
        set(new) {
            field       = new?.also { selectionModel?.replaceAll(setOf(it)) }
            editingRect = field?.let { path ->
                this[path]?.let { node ->
                    val row = rowFromPath(path)!!

                    rowPositioner?.rowBounds(this, node, path, row)
                }
            }

            preEditValue = new?.let { model[it] }
        }

    private var preEditValue = null as T?

    private var editingRect   = null as Rectangle?
    private var editOperation = null as EditOperation<T>?

    fun add     (path: Path<Int>, values: T            ) = model.add     (path, values)
    fun removeAt(path: Path<Int>                       ) = model.removeAt(path        )
    fun addAll  (path: Path<Int>, values: Collection<T>) = model.addAll  (path, values)
    fun clear   (                                      ) = model.clear   (            )

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

        cancelEditing()

        editor?.let {
            model[path]?.let { item ->
                rowFromPath(path)?.let { row ->
                    val i = row % children.size

                    editingPath   = path
                    editOperation = it.edit(this, item, path, rowPositioner?.contentBounds(this, item, path, i, children[i]) ?: Rectangle.Empty, children[i]).also {
                        it()?.let { children[i] = it }

                        layout(children[i], item, path, i)
                    }
                }
            }
        }
    }

    fun completeEditing() {
        editOperation?.let { operation ->
            editingPath?.let { path ->
                val result = operation.complete() ?: return

                cleanupEditing()

                updateModel(path, result)
            }
        }
    }

    fun cancelEditing() {
        preEditValue?.let { oldValue -> cleanupEditing()?.let { path -> updateModel(path, oldValue) } }
    }

    private fun updateModel(path: Path<Int>, value: T) {
        if (value == model.set(path, value)) {
            // This is the case that the "new" value is the same as what was there
            // so need to explicitly update since the model won't fire a change
            update(children, path)
        }
    }

    private fun cleanupEditing(): Path<Int>? {
        editOperation?.cancel()
        val result    = editingPath
        editOperation = null
        editingPath   = null

        return result
    }
}
