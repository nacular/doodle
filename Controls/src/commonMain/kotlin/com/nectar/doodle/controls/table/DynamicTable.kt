package com.nectar.doodle.controls.table

import com.nectar.doodle.controls.ModelObserver
import com.nectar.doodle.controls.MutableListModel
import com.nectar.doodle.controls.SelectionModel

/**
 * Created by Nicholas Eddy on 9/29/19.
 */
open class DynamicTable<T, M: MutableListModel<T>>(
        model         : M,
        selectionModel: SelectionModel<Int>? = null,
        block         : ColumnFactory<T>.() -> Unit): Table<T, M>(model, selectionModel, block) {

    private val editors = mutableMapOf<Column<*>, ((T) -> T)?>()

    private val modelChanged: ModelObserver<T> = { _,removed,added,_ ->
        var trueRemoved = removed.filterKeys { it !in added   }
        var trueAdded   = added.filterKeys   { it !in removed }

        itemsRemoved(trueRemoved)
        itemsAdded  (trueAdded  )

        val oldHeight = height

//        if (trueRemoved.isNotEmpty() || trueAdded.isNotEmpty()) {
//            updateVisibleHeight()
//        }
//
//        trueAdded   = trueAdded.filterKeys   { it <= lastVisibleRow }
//        trueRemoved = trueRemoved.filterKeys { it <= lastVisibleRow }
//
//        if (trueRemoved.size > trueAdded.size && height < oldHeight) {
//            children.batch {
//                for (it in 0 until trueRemoved.size - trueAdded.size) {
//                    removeAt(0)
//                }
//            }
//        }
//
//        if (trueRemoved.isNotEmpty() || trueAdded.isNotEmpty()) {
//            // FIXME: Make this more efficient
//            (firstVisibleRow..lastVisibleRow).forEach { update(children, it) }
//        } else {
//            // These are the edited rows
//            added.keys.filter { it in removed }.forEach { update(children, it) }
//        }
    }

    init {
        model.changed += modelChanged
    }

    override fun removedFromDisplay() {
        model.changed -= modelChanged

        super.removedFromDisplay()
    }

    private fun itemsAdded(values: Map<Int, T>) {
        if (selectionModel != null && values.isNotEmpty()) {
            val updatedSelection = mutableSetOf<Int>()

            for (selectionItem in selectionModel) {
                var delta = 0

                for (index in values.keys) {
                    if (selectionItem >= index) {
                        ++delta
                    }
                }

                updatedSelection.add(selectionItem + delta)
            }

            setSelection(updatedSelection)
        }
    }

    private fun itemsRemoved(values: Map<Int, T>) {
        if (selectionModel != null && values.isNotEmpty()) {

            val updatedSelection = mutableSetOf<Int>()

            for (selectionItem in selectionModel) {
                var delta = 0

                for (index in values.keys) {
                    if (selectionItem > index) {
                        delta--
                    }
                }

                if (delta != 0) {
                    updatedSelection.add(selectionItem + delta)
                }
            }

            removeSelection(values.keys)

            setSelection(updatedSelection)
        }
    }
}