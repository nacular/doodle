package io.nacular.doodle.controls.list

import io.nacular.doodle.controls.DynamicListModel
import io.nacular.doodle.controls.IndexedItem
import io.nacular.doodle.controls.IntProgressionModel
import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.controls.ListModel
import io.nacular.doodle.controls.MutableListModel
import io.nacular.doodle.controls.SelectionModel
import io.nacular.doodle.controls.SimpleListModel
import io.nacular.doodle.controls.ViewVisualizer
import io.nacular.doodle.controls.mutableListModelOf
import io.nacular.doodle.core.View
import io.nacular.doodle.utils.Dimension
import io.nacular.doodle.utils.Dimension.*
import kotlin.math.max

/**
 * [List] that is intended to display contents vertically with a configurable number of columns.
 *
 * @param model that holds the data for this List
 * @param itemVisualizer that maps [T] to [View] for each item in the List
 * @param selectionModel that manages the List's selection state
 * @param fitContent determines whether the List scales to fit its rows width and total height
 * @param numColumns to organize the items into
 * @param scrollCache determining how many "hidden" rows are rendered above and below the List's view-port. A value of 0 means
 * @property numColumns to organize the items into
 */
public class VerticalList<T, out M: ListModel<T>>(
   model         : M,
   itemVisualizer: ItemVisualizer<T, IndexedItem>? = null,
   selectionModel: SelectionModel<Int>?            = null,
   fitContent    : Set<Dimension>                  = setOf(Width, Height),
   numColumns    : Int                             = 1,
   scrollCache   : Int                             = 0
): List<T, M>(model, itemVisualizer, selectionModel, fitContent, scrollCache) {
    public val numColumns: Int = max(1, numColumns)

    public companion object {
        public operator fun invoke(
            progression    : IntProgression,
            itemVisualizer : ItemVisualizer<Int, IndexedItem>,
            selectionModel : SelectionModel<Int>? = null,
            fitContent     : Set<Dimension>       = setOf(Width, Height),
            numColumns     : Int                  = 1,
            scrollCache    : Int                  = 0): VerticalList<Int, ListModel<Int>> =
            VerticalList<Int, ListModel<Int>>(IntProgressionModel(progression), itemVisualizer, selectionModel, fitContent, numColumns, scrollCache)

        public inline operator fun <T> invoke(
            values        : kotlin.collections.List<T>,
            itemVisualizer: ItemVisualizer<T, IndexedItem>,
            selectionModel: SelectionModel<Int>? = null,
            fitContent    : Set<Dimension>       = setOf(Width, Height),
            numColumns    : Int                  = 1,
            scrollCache   : Int                  = 0): VerticalList<T, ListModel<T>> =
            VerticalList<T, ListModel<T>>(SimpleListModel(values), itemVisualizer, selectionModel, fitContent, numColumns, scrollCache)

        public inline operator fun invoke(
            values        : kotlin.collections.List<View>,
            selectionModel: SelectionModel<Int>? = null,
            fitContent    : Set<Dimension>       = setOf(Width, Height),
            numColumns    : Int                  = 1,
            scrollCache   : Int                  = 0): VerticalList<View, ListModel<View>> =
            VerticalList<View, ListModel<View>>(SimpleListModel(values), ViewVisualizer, selectionModel, fitContent, numColumns, scrollCache)

        public inline operator fun <T, M: ListModel<T>>invoke(
            model         : M,
            itemGenerator : ItemVisualizer<T, IndexedItem>? = null,
            selectionModel: SelectionModel<Int>?            = null,
            fitContent    : Set<Dimension>                  = setOf(Width, Height),
            numColumns    : Int                             = 1,
            scrollCache   : Int                             = 0): VerticalList<T, M> =
            VerticalList(model, itemGenerator, selectionModel, fitContent, numColumns, scrollCache)
    }
}

/**
 * [List] that is intended to display contents horizontally with a configurable number of rows.
 *
 * @param model that holds the data for this List
 * @param itemVisualizer that maps [T] to [View] for each item in the List
 * @param selectionModel that manages the List's selection state
 * @param fitContent determines whether the List scales to fit its rows width and total height
 * @param numRows to organize the items into
 * @param scrollCache determining how many "hidden" rows are rendered above and below the List's view-port. A value of 0 means
 * @property numRows to organize the items into
 */
public class HorizontalList<T, out M: ListModel<T>>(
    model         : M,
    itemVisualizer: ItemVisualizer<T, IndexedItem>? = null,
    selectionModel: SelectionModel<Int>?            = null,
    fitContent    : Set<Dimension>                  = setOf(Width, Height),
    numRows       : Int                             = 1,
    scrollCache   : Int                             = 0
): List<T, M>(model, itemVisualizer, selectionModel, fitContent, scrollCache) {
    public val numRows: Int = max(1, numRows)

    public companion object {
        public operator fun invoke(
            progression    : IntProgression,
            itemVisualizer : ItemVisualizer<Int, IndexedItem>,
            selectionModel : SelectionModel<Int>? = null,
            fitContent    : Set<Dimension>        = setOf(Width, Height),
            numRows        : Int                  = 1,
            scrollCache    : Int                  = 0): HorizontalList<Int, ListModel<Int>> =
            HorizontalList<Int, ListModel<Int>>(IntProgressionModel(progression), itemVisualizer, selectionModel, fitContent, numRows, scrollCache)

        public inline operator fun <T> invoke(
            values        : kotlin.collections.List<T>,
            itemVisualizer: ItemVisualizer<T, IndexedItem>,
            selectionModel: SelectionModel<Int>? = null,
            fitContent    : Set<Dimension>       = setOf(Width, Height),
            numRows       : Int                  = 1,
            scrollCache   : Int                  = 0): HorizontalList<T, ListModel<T>> =
            HorizontalList<T, ListModel<T>>(SimpleListModel(values), itemVisualizer, selectionModel, fitContent, numRows, scrollCache)

        public inline operator fun invoke(
            values        : kotlin.collections.List<View>,
            selectionModel: SelectionModel<Int>? = null,
            fitContent    : Set<Dimension>       = setOf(Width, Height),
            numRows       : Int                  = 1,
            scrollCache   : Int                  = 0): HorizontalList<View, ListModel<View>> =
            HorizontalList<View, ListModel<View>>(SimpleListModel(values), ViewVisualizer, selectionModel, fitContent, numRows, scrollCache)

        public inline operator fun <T, M: ListModel<T>>invoke(
            model         : M,
            itemGenerator : ItemVisualizer<T, IndexedItem>? = null,
            selectionModel: SelectionModel<Int>?            = null,
            fitContent    : Set<Dimension>                  = setOf(Width, Height),
            numRows       : Int                             = 1,
            scrollCache   : Int                             = 0): HorizontalList<T, M> =
            HorizontalList(model, itemGenerator, selectionModel, fitContent, numRows, scrollCache)
    }
}

/**
 * [DynamicList] that is intended to display contents vertically with a configurable number of columns.
 *
 * @param model that holds the data for this List
 * @param itemVisualizer that maps [T] to [View] for each item in the List
 * @param selectionModel that manages the List's selection state
 * @param fitContent determines whether the List scales to fit its rows width and total height
 * @param numColumns to organize the items into
 * @param scrollCache determining how many "hidden" rows are rendered above and below the List's view-port. A value of 0 means
 * @property numColumns to organize the items into
 */
public class VerticalDynamicList<T, M: DynamicListModel<T>>(
    model         : M,
    itemVisualizer: ItemVisualizer<T, IndexedItem>? = null,
    selectionModel: SelectionModel<Int>?            = null,
    fitContent    : Set<Dimension>                  = setOf(Width, Height),
    numColumns    : Int                             = 1,
    scrollCache   : Int                             = 0
): DynamicList<T, M>(model, itemVisualizer, selectionModel, fitContent, scrollCache) {
    public val numColumns: Int = max(1, numColumns)

    public companion object {
        public inline operator fun invoke(
            progression   : IntProgression,
            itemVisualizer: ItemVisualizer<Int, IndexedItem>,
            selectionModel: SelectionModel<Int>? = null,
            fitContent    : Set<Dimension>       = setOf(Width, Height),
            scrollCache   : Int                  = 0): VerticalDynamicList<Int, DynamicListModel<Int>> =
            VerticalDynamicList(progression.toMutableList(), itemVisualizer, selectionModel, fitContent, scrollCache)

        public inline operator fun <reified T> invoke(
            values        : kotlin.collections.List<T>,
            itemVisualizer: ItemVisualizer<T, IndexedItem>,
            selectionModel: SelectionModel<Int>? = null,
            fitContent    : Set<Dimension>       = setOf(Width, Height),
            scrollCache   : Int                  = 0): VerticalDynamicList<T, DynamicListModel<T>> =
            VerticalDynamicList(mutableListModelOf(*values.toTypedArray()), itemVisualizer, selectionModel, fitContent, scrollCache)

        public inline operator fun invoke(
            values        : kotlin.collections.List<View>,
            selectionModel: SelectionModel<Int>? = null,
            fitContent    : Set<Dimension>       = setOf(Width, Height),
            scrollCache   : Int                  = 0): VerticalDynamicList<View, DynamicListModel<View>> =
            VerticalDynamicList(mutableListModelOf(*values.toTypedArray()), ViewVisualizer, selectionModel, fitContent, scrollCache)

        public inline operator fun <T, M: DynamicListModel<T>> invoke(
            model         : M,
            itemVisualizer: ItemVisualizer<T, IndexedItem>? = null,
            selectionModel: SelectionModel<Int>?            = null,
            fitContent    : Set<Dimension>                  = setOf(Width, Height),
            scrollCache   : Int                             = 0): VerticalDynamicList<T, M> =
            VerticalDynamicList(model, itemVisualizer, selectionModel, fitContent, scrollCache)
    }
}

/**
 * [DynamicList] that is intended to display contents horizontally with a configurable number of rows.
 *
 * @param model that holds the data for this List
 * @param itemVisualizer that maps [T] to [View] for each item in the List
 * @param selectionModel that manages the List's selection state
 * @param fitContent determines whether the List scales to fit its rows width and total height
 * @param numRows to organize the items into
 * @param scrollCache determining how many "hidden" rows are rendered above and below the List's view-port. A value of 0 means
 * @property numRows to organize the items into
 */
public class HorizontalDynamicList<T, M: DynamicListModel<T>>(
    model         : M,
    itemVisualizer: ItemVisualizer<T, IndexedItem>? = null,
    selectionModel: SelectionModel<Int>?            = null,
    fitContent    : Set<Dimension>                  = setOf(Width, Height),
    numRows       : Int                             = 1,
    scrollCache   : Int                             = 0
): DynamicList<T, M>(model, itemVisualizer, selectionModel, fitContent, scrollCache) {
    public val numRows: Int = max(1, numRows)

    public companion object {
        public operator fun invoke(
            progression    : IntProgression,
            itemVisualizer : ItemVisualizer<Int, IndexedItem>,
            selectionModel : SelectionModel<Int>? = null,
            fitContent    : Set<Dimension>        = setOf(Width, Height),
            numRows        : Int                  = 1,
            scrollCache    : Int                  = 0): HorizontalList<Int, ListModel<Int>> =
            HorizontalList<Int, ListModel<Int>>(IntProgressionModel(progression), itemVisualizer, selectionModel, fitContent, numRows, scrollCache)

        public inline operator fun <T> invoke(
            values        : kotlin.collections.List<T>,
            itemVisualizer: ItemVisualizer<T, IndexedItem>,
            selectionModel: SelectionModel<Int>? = null,
            fitContent    : Set<Dimension>       = setOf(Width, Height),
            numRows       : Int                  = 1,
            scrollCache   : Int                  = 0): HorizontalList<T, ListModel<T>> =
            HorizontalList<T, ListModel<T>>(SimpleListModel(values), itemVisualizer, selectionModel, fitContent, numRows, scrollCache)

        public inline operator fun invoke(
            values        : kotlin.collections.List<View>,
            selectionModel: SelectionModel<Int>? = null,
            fitContent    : Set<Dimension>       = setOf(Width, Height),
            numRows       : Int                  = 1,
            scrollCache   : Int                  = 0): HorizontalList<View, ListModel<View>> =
            HorizontalList<View, ListModel<View>>(SimpleListModel(values), ViewVisualizer, selectionModel, fitContent, numRows, scrollCache)

        public inline operator fun <T, M: ListModel<T>>invoke(
            model         : M,
            itemGenerator : ItemVisualizer<T, IndexedItem>? = null,
            selectionModel: SelectionModel<Int>?            = null,
            fitContent    : Set<Dimension>                  = setOf(Width, Height),
            numRows       : Int                             = 1,
            scrollCache   : Int                             = 0): HorizontalList<T, M> =
            HorizontalList(model, itemGenerator, selectionModel, fitContent, numRows, scrollCache)
    }
}

/**
 * [MutableList] that is intended to display contents vertically with a configurable number of columns.
 *
 * @param model that holds the data for this List
 * @param itemVisualizer that maps [T] to [View] for each item in the List
 * @param selectionModel that manages the List's selection state
 * @param fitContent determines whether the List scales to fit its rows width and total height
 * @param numColumns to organize the items into
 * @param scrollCache determining how many "hidden" rows are rendered above and below the List's view-port. A value of 0 means
 * @property numColumns to organize the items into
 */
public class VerticalMutableList<T, M: MutableListModel<T>>(
    model         : M,
    itemVisualizer: ItemVisualizer<T, IndexedItem>? = null,
    selectionModel: SelectionModel<Int>?            = null,
    fitContent    : Set<Dimension>                  = setOf(Width, Height),
    numColumns    : Int                             = 1,
    scrollCache   : Int                             = 0
): MutableList<T, M>(model, itemVisualizer, selectionModel, fitContent, scrollCache) {
    public val numColumns: Int = max(1, numColumns)

    public companion object {
        public inline operator fun invoke(
            progression    : IntProgression,
            itemVisualizer : ItemVisualizer<Int, IndexedItem>,
            selectionModel : SelectionModel<Int>? = null,
            fitContent    : Set<Dimension>        = setOf(Width, Height),
            numColumns     : Int                  = 1,
            scrollCache    : Int                  = 0): VerticalMutableList<Int, MutableListModel<Int>> =
            VerticalMutableList(progression.toMutableList(), itemVisualizer, selectionModel, fitContent, numColumns, scrollCache)

        public inline operator fun <reified T> invoke(
            values        : kotlin.collections.List<T>,
            itemVisualizer: ItemVisualizer<T, IndexedItem>,
            selectionModel: SelectionModel<Int>? = null,
            fitContent    : Set<Dimension>       = setOf(Width, Height),
            numColumns    : Int                  = 1,
            scrollCache   : Int                  = 0): VerticalMutableList<T, MutableListModel<T>> =
            VerticalMutableList(mutableListModelOf(*values.toTypedArray()), itemVisualizer, selectionModel, fitContent, numColumns, scrollCache)

        public inline operator fun invoke(
            values        : kotlin.collections.List<View>,
            selectionModel: SelectionModel<Int>? = null,
            fitContent    : Set<Dimension>       = setOf(Width, Height),
            numColumns    : Int                  = 1,
            scrollCache   : Int                  = 0): VerticalMutableList<View, MutableListModel<View>> =
            VerticalMutableList(mutableListModelOf(*values.toTypedArray()), ViewVisualizer, selectionModel, fitContent, numColumns, scrollCache)

        public inline operator fun <T, M: MutableListModel<T>>invoke(
            model         : M,
            itemGenerator : ItemVisualizer<T, IndexedItem>? = null,
            selectionModel: SelectionModel<Int>?            = null,
            fitContent    : Set<Dimension>                  = setOf(Width, Height),
            numColumns    : Int                             = 1,
            scrollCache   : Int                             = 0): VerticalMutableList<T, M> =
            VerticalMutableList(model, itemGenerator, selectionModel, fitContent, numColumns, scrollCache)
    }
}

/**
 * [MutableList] that is intended to display contents horizontally with a configurable number of rows.
 *
 * @param model that holds the data for this List
 * @param itemVisualizer that maps [T] to [View] for each item in the List
 * @param selectionModel that manages the List's selection state
 * @param fitContent determines whether the List scales to fit its rows width and total height
 * @param numRows to organize the items into
 * @param scrollCache determining how many "hidden" rows are rendered above and below the List's view-port. A value of 0 means
 * @property numRows to organize the items into
 */
public class HorizontalMutableList<T, M: MutableListModel<T>>(
    model         : M,
    itemVisualizer: ItemVisualizer<T, IndexedItem>? = null,
    selectionModel: SelectionModel<Int>?            = null,
    fitContent    : Set<Dimension>                  = setOf(Width, Height),
    numRows       : Int                             = 1,
    scrollCache   : Int                             = 0
): MutableList<T, M>(model, itemVisualizer, selectionModel, fitContent, scrollCache) {
    public val numRows: Int = max(1, numRows)

    public companion object {
        public inline operator fun invoke(
            progression    : IntProgression,
            itemVisualizer : ItemVisualizer<Int, IndexedItem>,
            selectionModel : SelectionModel<Int>? = null,
            fitContent    : Set<Dimension>        = setOf(Width, Height),
            numRows        : Int                  = 1,
            scrollCache    : Int                  = 0): HorizontalMutableList<Int, MutableListModel<Int>> =
            HorizontalMutableList(progression.toMutableList(), itemVisualizer, selectionModel, fitContent, numRows, scrollCache)

        public inline operator fun <reified T> invoke(
            values        : kotlin.collections.List<T>,
            itemVisualizer: ItemVisualizer<T, IndexedItem>,
            selectionModel: SelectionModel<Int>? = null,
            fitContent    : Set<Dimension>       = setOf(Width, Height),
            numRows       : Int                  = 1,
            scrollCache   : Int                  = 0): HorizontalMutableList<T, MutableListModel<T>> =
            HorizontalMutableList(mutableListModelOf(*values.toTypedArray()), itemVisualizer, selectionModel, fitContent, numRows, scrollCache)

        public inline operator fun invoke(
            values        : kotlin.collections.List<View>,
            selectionModel: SelectionModel<Int>? = null,
            fitContent    : Set<Dimension>       = setOf(Width, Height),
            numRows       : Int                  = 1,
            scrollCache   : Int                  = 0): HorizontalMutableList<View, MutableListModel<View>> =
            HorizontalMutableList(mutableListModelOf(*values.toTypedArray()), ViewVisualizer, selectionModel, fitContent, numRows, scrollCache)

        public inline operator fun <T, M: MutableListModel<T>>invoke(
            model         : M,
            itemGenerator : ItemVisualizer<T, IndexedItem>? = null,
            selectionModel: SelectionModel<Int>?            = null,
            fitContent    : Set<Dimension>                  = setOf(Width, Height),
            numRows       : Int                             = 1,
            scrollCache   : Int                             = 0): HorizontalMutableList<T, M> =
            HorizontalMutableList(model, itemGenerator, selectionModel, fitContent, numRows, scrollCache)
    }
}