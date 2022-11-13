package io.nacular.doodle.controls.tree

import io.nacular.doodle.controls.EditOperation
import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.controls.SelectionModel
import io.nacular.doodle.controls.ExpandableItem
import io.nacular.doodle.core.View
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.utils.Editable
import io.nacular.doodle.utils.Path

/**
 * Created by Nicholas Eddy on 12/13/18.
 */

public interface TreeEditor<T> {
    public fun edit(tree: MutableTree<T, *>, node: T, path: Path<Int>, contentBounds: Rectangle, current: View): EditOperation<T>
}

public class MutableTree<T, M: MutableTreeModel<T>>(model         : M,
                                                    itemVisualizer: ItemVisualizer<T, ExpandableItem>? = null,
                                                    selectionModel: SelectionModel<Path<Int>>? = null
): DynamicTree<T, M>(model, itemVisualizer, selectionModel), Editable {
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

    public val editing: Boolean get() = editingPath != null

    public var editor: TreeEditor<T>? = null

    public operator fun set(path: Path<Int>, value: T) { model[path] = value }

    public operator fun set(row: Int, value: T) { pathFromRow(row)?.let { updateModel(it, value) } }

    private var editingPath = null as Path<Int>?
        set(new) {
            field       = new?.also { selectionModel?.replaceAll(setOf(it)) }
            editingRect = field?.let { path ->
                this[path].fold(onSuccess =  { node ->
                    val row = rowFromPath(path)!!

                    rowPositioner?.rowBounds(this, node, path, row)
                }, onFailure = { null })
            }

            preEditValue = new?.let { model[it] }
        }

    private var preEditValue = null as Result<T>?

    private var editingRect   = null as Rectangle?
    private var editOperation = null as EditOperation<T>?

    public fun add     (path: Path<Int>, values: T            ): Unit      = model.add     (path, values)
    public fun removeAt(path: Path<Int>                       ): Result<T> = model.removeAt(path        )
    public fun addAll  (path: Path<Int>, values: Collection<T>): Unit      = model.addAll  (path, values)
    public fun clear   (                                      ): Unit      = model.clear   (            )

    override fun handleDisplayRectEvent(old: Rectangle, new: Rectangle) {
        super.handleDisplayRectEvent(old, new)

        editingRect?.let {
            if (it !in new) {
                cancelEditing()
            }
        }
    }

    public fun startEditing(path: Path<Int>) {
        if (!visible(path)) {
            return
        }

        cancelEditing()

        editor?.let {
            model[path].onSuccess { item ->
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

    public override fun completeEditing() {
        editOperation?.let { operation ->
            editingPath?.let { path ->
                operation.complete().onSuccess {
                    cleanupEditing()
                    updateModel(path, it)
                }
            }
        }
    }

    public override fun cancelEditing() {
        preEditValue?.onSuccess { oldValue -> cleanupEditing()?.let { path -> updateModel(path, oldValue) } }
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
