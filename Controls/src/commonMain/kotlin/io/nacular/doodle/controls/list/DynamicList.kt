package io.nacular.doodle.controls.list

import io.nacular.doodle.controls.DynamicListModel
import io.nacular.doodle.controls.IndexedItem
import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.controls.ModelObserver
import io.nacular.doodle.controls.SelectionModel
import io.nacular.doodle.controls.ViewVisualizer
import io.nacular.doodle.controls.mutableListModelOf
import io.nacular.doodle.core.View
import io.nacular.doodle.utils.Dimension
import io.nacular.doodle.utils.Dimension.Height
import io.nacular.doodle.utils.Dimension.Width
import io.nacular.doodle.utils.Pool
import io.nacular.doodle.utils.SetPool
import io.nacular.doodle.utils.diff.Delete
import io.nacular.doodle.utils.diff.Differences
import io.nacular.doodle.utils.diff.Insert
import io.nacular.doodle.utils.size

public typealias ItemsObserver<T> = (source: List<T, *>, differences: Differences<T>) -> Unit

/**
 * A [List] component that renders a potentially mutable list of items of type [T] using a [ListBehavior]. Items are obtained via
 * the [model] and selection is managed via the optional [selectionModel]. Large ("infinite") lists are supported
 * efficiently, since List recycles the Views generated to render its rows.
 *
 * Note that this class does not provide methods to change its underlying model. See [MutableList] if this behavior is desirable.
 *
 * DynamicList does not provide scrolling internally, so it should be embedded in a [ScrollPanel][io.nacular.doodle.controls.panels.ScrollPanel] or similar component if needed.
 *
 * @param model that holds the data for this List
 * @param itemVisualizer that maps [T] to [View] for each item in the List
 * @param selectionModel that manages the List's selection state
 * @param fitContent determines whether the List scales to fit it's rows width and total height
 * @param scrollCache determining how many "hidden" rows are rendered above and below the List's view-port. A value of 0 means
 * only visible rows are rendered, but quick scrolling is more likely to show blank areas.
 */
public open class DynamicList<T, M: DynamicListModel<T>>(
        model         : M,
        itemVisualizer: ItemVisualizer<T, IndexedItem>? = null,
        selectionModel: SelectionModel<Int>?            = null,
        fitContent    : Set<Dimension>                  = setOf(Width, Height),
        scrollCache   : Int                             = 0): List<T, M>(model, itemVisualizer, selectionModel, fitContent, scrollCache) {

    private val modelChanged: ModelObserver<T> = { _,diffs ->
        val removed = mutableListOf<Int>()
        val added   = mutableListOf<Int>()
        val moved   = mutableListOf<Int>()

        var index = 0

        diffs.computeMoves().forEach { diff ->
            when (diff) {
                is Insert -> {
                    diff.items.forEach {
                        when {
                            diff.origin(of = it) == null -> added += index
                            else                         -> moved += index
                        }
                        ++index
                    }
                }
                is Delete -> {
                    diff.items.forEach {
                        if (diff.destination(of = it) == null) {
                            removed += index
                        }
                    }
                }
                else -> { index += diff.items.size }
            }
        }

        // FIXME: Handle Selection updates
//        itemsChanged(added = trueAdded, removed = trueRemoved, moved = moved)

        val oldVisibleRange = firstVisibleItem..lastVisibleItem

        if (removed.isNotEmpty() || added.isNotEmpty()) {
            updateVisibleHeight()
        }

        if (removed.size > added.size && oldVisibleRange.size != (firstVisibleItem..lastVisibleItem).size) {
            val numToRemove = oldVisibleRange.size - (firstVisibleItem..lastVisibleItem).size
            children.batch {
                for (it in 0 until numToRemove) {
                    if (size > 0) {
                        removeAt(size - 1)
                    }
                }
            }
        }

        if (removed.isNotEmpty() || added.isNotEmpty() || moved.isNotEmpty()) {
            // FIXME: Make this more efficient
            (firstVisibleItem..lastVisibleItem).forEach { update(children, it) }
        } else {
            // These are the edited rows
            added.forEach { update(children, it) }
        }

        (itemsChanged as SetPool).forEach { it(this, diffs) }
    }

    /**
     * Notifies of changes to the list
     */
    public val itemsChanged: Pool<ItemsObserver<T>> = SetPool()

    init {
        model.changed += modelChanged
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

    public companion object {
        public operator fun invoke(
            progression   : IntProgression,
            itemVisualizer: ItemVisualizer<Int, IndexedItem>,
            selectionModel: SelectionModel<Int>? = null,
            fitContent    : Set<Dimension>       = setOf(Width, Height),
            scrollCache   : Int                  = 0): DynamicList<Int, DynamicListModel<Int>> =
            DynamicList(progression.toMutableList(), itemVisualizer, selectionModel, fitContent, scrollCache)

        public inline operator fun <reified T> invoke(
            values        : kotlin.collections.List<T>,
            itemVisualizer: ItemVisualizer<T, IndexedItem>,
            selectionModel: SelectionModel<Int>? = null,
            fitContent    : Set<Dimension>       = setOf(Width, Height),
            scrollCache   : Int                  = 0): DynamicList<T, DynamicListModel<T>> =
            DynamicList(mutableListModelOf(*values.toTypedArray()), itemVisualizer, selectionModel, fitContent, scrollCache)

        public operator fun invoke(
            values        : kotlin.collections.List<View>,
            selectionModel: SelectionModel<Int>? = null,
            fitContent    : Set<Dimension>       = setOf(Width, Height),
            scrollCache   : Int                  = 0): DynamicList<View, DynamicListModel<View>> =
            DynamicList(mutableListModelOf(*values.toTypedArray()), ViewVisualizer, selectionModel, fitContent, scrollCache)

        public operator fun <T, M: DynamicListModel<T>> invoke(
            model         : M,
            itemVisualizer: ItemVisualizer<T, IndexedItem>? = null,
            selectionModel: SelectionModel<Int>?            = null,
            fitContent    : Set<Dimension>                  = setOf(Width, Height),
            scrollCache   : Int                             = 0): DynamicList<T, M> =
            DynamicList(model, itemVisualizer, selectionModel, fitContent, scrollCache)
    }
}