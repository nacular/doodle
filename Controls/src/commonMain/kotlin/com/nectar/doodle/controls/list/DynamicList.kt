package com.nectar.doodle.controls.list

import com.nectar.doodle.controls.ItemVisualizer
import com.nectar.doodle.controls.ModelObserver
import com.nectar.doodle.controls.MutableListModel
import com.nectar.doodle.controls.SelectionModel
import com.nectar.doodle.controls.SimpleMutableListModel
import com.nectar.doodle.utils.size

open class DynamicList<T, M: MutableListModel<T>>(
        model         : M,
        itemGenerator : ItemVisualizer<T>?   = null,
        selectionModel: SelectionModel<Int>? = null,
        fitContent    : Boolean              = true,
        scrollCache   : Int                  = 10): List<T, M>(model, itemGenerator, selectionModel, fitContent, scrollCache) {

    private val modelChanged: ModelObserver<T> = { _,removed,added,_ ->
        var trueRemoved = removed.filterKeys { it !in added   }
        var trueAdded   = added.filterKeys   { it !in removed }

        itemsRemoved(trueRemoved)
        itemsAdded  (trueAdded  )

        val oldVisibleRange = firstVisibleRow..lastVisibleRow

        if (trueRemoved.isNotEmpty() || trueAdded.isNotEmpty()) {
            updateVisibleHeight()
        }

        trueAdded   = trueAdded.filterKeys   { it <= lastVisibleRow }
        trueRemoved = trueRemoved.filterKeys { it <= lastVisibleRow }

        if (trueRemoved.size > trueAdded.size && oldVisibleRange.size != (firstVisibleRow..lastVisibleRow).size) {
            children.batch {
                for (it in 0 until trueRemoved.size - trueAdded.size) {
                    removeAt(0)
                }
            }
        }

        if (trueRemoved.isNotEmpty() || trueAdded.isNotEmpty()) {
            // FIXME: Make this more efficient
            (firstVisibleRow..lastVisibleRow).forEach { update(children, it) }
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

    companion object {
        operator fun invoke(
                progression   : IntProgression,
                itemGenerator : ItemVisualizer<Int>,
                selectionModel: SelectionModel<Int>? = null,
                fitContent    : Boolean              = true,
                scrollCache   : Int                  = 10) =
                DynamicList(progression.toMutableList(), itemGenerator, selectionModel, fitContent, scrollCache)

        operator fun <T> invoke(
                values        : kotlin.collections.List<T>,
                itemGenerator : ItemVisualizer<T>,
                selectionModel: SelectionModel<Int>? = null,
                fitContent    : Boolean              = true,
                scrollCache   : Int                  = 10): DynamicList<T, SimpleMutableListModel<T>> =
                DynamicList(SimpleMutableListModel(values.toMutableList()), itemGenerator, selectionModel, fitContent, scrollCache)
    }
}