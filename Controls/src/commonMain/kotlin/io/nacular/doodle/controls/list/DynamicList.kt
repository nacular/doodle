package io.nacular.doodle.controls.list

import io.nacular.doodle.controls.DynamicListModel
import io.nacular.doodle.controls.IndexedIem
import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.controls.ListModel
import io.nacular.doodle.controls.ModelObserver
import io.nacular.doodle.controls.MutableListModel
import io.nacular.doodle.controls.SelectionModel
import io.nacular.doodle.controls.ViewVisualizer
import io.nacular.doodle.controls.mutableListModelOf
import io.nacular.doodle.core.View
import io.nacular.doodle.utils.Pool
import io.nacular.doodle.utils.SetPool
import io.nacular.doodle.utils.size

typealias ItemsObserver<T> = (source: List<T, *>, removed: Map<Int, T>, added: Map<Int, T>, moved: Map<Int, Pair<Int, T>>) -> Unit

open class DynamicList<T, M: DynamicListModel<T>>(
        model         : M,
        itemVisualizer: ItemVisualizer<T, IndexedIem>? = null,
        selectionModel: SelectionModel<Int>?           = null,
        fitContent    : Boolean                        = true,
        scrollCache   : Int                            = 10): List<T, M>(model, itemVisualizer, selectionModel, fitContent, scrollCache) {

    private val modelChanged: ModelObserver<T> = { _,removed,added,moved ->
        var trueRemoved = removed.filterKeys { it !in added   }
        var trueAdded   = added.filterKeys   { it !in removed }

        itemsChanged(added = trueAdded, removed = trueRemoved, moved = moved)

        val oldVisibleRange = firstVisibleRow..lastVisibleRow

        trueRemoved = trueRemoved.filterKeys { it <= lastVisibleRow }

        if (trueRemoved.isNotEmpty() || trueAdded.isNotEmpty()) {
            updateVisibleHeight()
        }

        trueAdded = trueAdded.filterKeys   { it <= lastVisibleRow }

        if (trueRemoved.size > trueAdded.size && oldVisibleRange.size != (firstVisibleRow..lastVisibleRow).size) {
            val numToRemove = oldVisibleRange.size - (firstVisibleRow..lastVisibleRow).size
            children.batch {
                for (it in 0 until numToRemove) {
                    if (size > 0) {
                        removeAt(size - 1)
                    }
                }
            }
        }

        if (trueRemoved.isNotEmpty() || trueAdded.isNotEmpty() || moved.isNotEmpty()) {
            // FIXME: Make this more efficient
            (firstVisibleRow..lastVisibleRow).forEach { update(children, it) }
        } else {
            // These are the edited rows
            added.keys.filter { it in removed }.forEach { update(children, it) }
        }

        (itemsChanged as SetPool).forEach { it(this, removed, added, moved) }
    }

    val itemsChanged: Pool<ItemsObserver<T>> = SetPool()

    init {
        model.changed += modelChanged
    }

    override fun removedFromDisplay() {
        model.changed -= modelChanged

        super.removedFromDisplay()
    }

    private fun itemsChanged(added: Map<Int, T>, removed: Map<Int, T>, moved: Map<Int, Pair<Int, T>>) {
        if (selectionModel != null) {

            val effectiveAdded  : Map<Int, T> = added   + moved.values.associate { it.first to it.second }
            val effectiveRemoved: Map<Int, T> = removed + moved.mapValues { it.value.second }

            val updatedSelection = mutableSetOf<Int>()

            for (selectionItem in selectionModel) {
                var delta = 0

                for (index in effectiveAdded.keys) {
                    if (selectionItem >= index) {
                        ++delta
                    }
                }

                for (index in effectiveRemoved.keys) {
                    if (selectionItem > index) {
                        delta--
                    }
                }

                if (delta != 0) {
                    updatedSelection.add(selectionItem + delta)
                }
            }

            removeSelection(removed.keys)

            if (updatedSelection.isNotEmpty()) {
                setSelection(updatedSelection)
            }
        }
    }

    companion object {
        operator fun invoke(
                progression   : IntProgression,
                itemVisualizer: ItemVisualizer<Int, IndexedIem>,
                selectionModel: SelectionModel<Int>? = null,
                fitContent    : Boolean              = true,
                scrollCache   : Int                  = 10) =
                DynamicList(progression.toMutableList(), itemVisualizer, selectionModel, fitContent, scrollCache)

        inline operator fun <reified T> invoke(
                values        : kotlin.collections.List<T>,
                itemVisualizer: ItemVisualizer<T, IndexedIem>,
                selectionModel: SelectionModel<Int>? = null,
                fitContent    : Boolean              = true,
                scrollCache   : Int                  = 10): DynamicList<T, MutableListModel<T>> =
                DynamicList(mutableListModelOf(*values.toTypedArray()), itemVisualizer, selectionModel, fitContent, scrollCache)

        operator fun invoke(
                values        : kotlin.collections.List<View>,
                selectionModel: SelectionModel<Int>? = null,
                fitContent    : Boolean              = true,
                scrollCache   : Int                  = 10): List<View, ListModel<View>> =
                DynamicList(mutableListModelOf(*values.toTypedArray()), ViewVisualizer, selectionModel, fitContent, scrollCache)

        operator fun <T, M: DynamicListModel<T>> invoke(
                model         : M,
                itemVisualizer: ItemVisualizer<T, IndexedIem>? = null,
                selectionModel: SelectionModel<Int>?           = null,
                fitContent    : Boolean                        = true,
                scrollCache   : Int                            = 10) =
                DynamicList(model, itemVisualizer, selectionModel, fitContent, scrollCache)

    }
}