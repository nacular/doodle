package io.nacular.doodle.controls.list

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
               fitContent    : Boolean                         = true,
               numColumns    : Int                             = 1,
               scrollCache   : Int                             = numColumns * 10
): List<T, M>(model, itemVisualizer, selectionModel, fitContent, scrollCache) {
    public val numColumns: Int = max(1, numColumns)

    public companion object {
        public operator fun invoke(
            progression    : IntProgression,
            itemVisualizer : ItemVisualizer<Int, IndexedItem>,
            selectionModel : SelectionModel<Int>? = null,
            fitContent     : Boolean              = true,
            numColumns     : Int                  =  1,
            scrollCache    : Int                  = 10): VerticalList<Int, ListModel<Int>> =
            VerticalList<Int, ListModel<Int>>(IntProgressionModel(progression), itemVisualizer, selectionModel, fitContent, numColumns, scrollCache)

        public operator fun <T> invoke(
            values        : kotlin.collections.List<T>,
            itemVisualizer: ItemVisualizer<T, IndexedItem>,
            selectionModel: SelectionModel<Int>? = null,
            fitContent    : Boolean              = true,
            numColumns    : Int                  =  1,
            scrollCache   : Int                  = 10): VerticalList<T, ListModel<T>> =
            VerticalList<T, ListModel<T>>(SimpleListModel(values), itemVisualizer, selectionModel, fitContent, numColumns, scrollCache)

        public operator fun invoke(
            values        : kotlin.collections.List<View>,
            selectionModel: SelectionModel<Int>? = null,
            fitContent    : Boolean              = true,
            numColumns    : Int                  =  1,
            scrollCache   : Int                  = 10): VerticalList<View, ListModel<View>> =
            VerticalList<View, ListModel<View>>(SimpleListModel(values), ViewVisualizer, selectionModel, fitContent, numColumns, scrollCache)

        public operator fun <T, M: ListModel<T>>invoke(
            model         : M,
            itemGenerator : ItemVisualizer<T, IndexedItem>? = null,
            selectionModel: SelectionModel<Int>?            = null,
            fitContent    : Boolean                         = true,
            numColumns    : Int                             =  1,
            scrollCache   : Int                             = 10): VerticalList<T, M> =
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
    fitContent    : Boolean                         = true,
    numRows       : Int                             = 1,
    scrollCache   : Int                             = numRows * 10
): List<T, M>(model, itemVisualizer, selectionModel, fitContent, scrollCache) {
    public val numRows: Int = max(1, numRows)

    public companion object {
        public operator fun invoke(
            progression    : IntProgression,
            itemVisualizer : ItemVisualizer<Int, IndexedItem>,
            selectionModel : SelectionModel<Int>? = null,
            fitContent     : Boolean              = true,
            numRows        : Int                  =  1,
            scrollCache    : Int                  = 10): HorizontalList<Int, ListModel<Int>> =
            HorizontalList<Int, ListModel<Int>>(IntProgressionModel(progression), itemVisualizer, selectionModel, fitContent, numRows, scrollCache)

        public operator fun <T> invoke(
            values        : kotlin.collections.List<T>,
            itemVisualizer: ItemVisualizer<T, IndexedItem>,
            selectionModel: SelectionModel<Int>? = null,
            fitContent    : Boolean              = true,
            numRows       : Int                  =  1,
            scrollCache   : Int                  = 10): HorizontalList<T, ListModel<T>> =
            HorizontalList<T, ListModel<T>>(SimpleListModel(values), itemVisualizer, selectionModel, fitContent, numRows, scrollCache)

        public operator fun invoke(
            values        : kotlin.collections.List<View>,
            selectionModel: SelectionModel<Int>? = null,
            fitContent    : Boolean              = true,
            numRows       : Int                  =  1,
            scrollCache   : Int                  = 10): HorizontalList<View, ListModel<View>> =
            HorizontalList<View, ListModel<View>>(SimpleListModel(values), ViewVisualizer, selectionModel, fitContent, numRows, scrollCache)

        public operator fun <T, M: ListModel<T>>invoke(
            model         : M,
            itemGenerator : ItemVisualizer<T, IndexedItem>? = null,
            selectionModel: SelectionModel<Int>?            = null,
            fitContent    : Boolean                         = true,
            numRows       : Int                             =  1,
            scrollCache   : Int                             = 10): HorizontalList<T, M> =
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
    fitContent    : Boolean                         = true,
    numColumns    : Int                             = 1,
    scrollCache   : Int                             = numColumns * 10
): MutableList<T, M>(model, itemVisualizer, selectionModel, fitContent, scrollCache) {
    public val numColumns: Int = max(1, numColumns)

    public companion object {
        public inline operator fun invoke(
            progression    : IntProgression,
            itemVisualizer : ItemVisualizer<Int, IndexedItem>,
            selectionModel : SelectionModel<Int>? = null,
            fitContent     : Boolean              = true,
            numColumns     : Int                  =  1,
            scrollCache    : Int                  = 10): VerticalMutableList<Int, MutableListModel<Int>> =
            VerticalMutableList(progression.toMutableList(), itemVisualizer, selectionModel, fitContent, numColumns, scrollCache)

        public inline operator fun <reified T> invoke(
            values        : kotlin.collections.List<T>,
            itemVisualizer: ItemVisualizer<T, IndexedItem>,
            selectionModel: SelectionModel<Int>? = null,
            fitContent    : Boolean              = true,
            numColumns    : Int                  =  1,
            scrollCache   : Int                  = 10): VerticalMutableList<T, MutableListModel<T>> =
            VerticalMutableList(mutableListModelOf(*values.toTypedArray()), itemVisualizer, selectionModel, fitContent, numColumns, scrollCache)

        public inline operator fun invoke(
            values        : kotlin.collections.List<View>,
            selectionModel: SelectionModel<Int>? = null,
            fitContent    : Boolean              = true,
            numColumns    : Int                  =  1,
            scrollCache   : Int                  = 10): VerticalMutableList<View, MutableListModel<View>> =
            VerticalMutableList(mutableListModelOf(*values.toTypedArray()), ViewVisualizer, selectionModel, fitContent, numColumns, scrollCache)

        public inline operator fun <T, M: MutableListModel<T>>invoke(
            model         : M,
            itemGenerator : ItemVisualizer<T, IndexedItem>? = null,
            selectionModel: SelectionModel<Int>?            = null,
            fitContent    : Boolean                         = true,
            numColumns    : Int                             =  1,
            scrollCache   : Int                             = 10): VerticalMutableList<T, M> =
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
    fitContent    : Boolean                         = true,
    numRows       : Int                             = 1,
    scrollCache   : Int                             = numRows * 10
): MutableList<T, M>(model, itemVisualizer, selectionModel, fitContent, scrollCache) {
    public val numRows: Int = max(1, numRows)

    public companion object {
        public inline operator fun invoke(
            progression    : IntProgression,
            itemVisualizer : ItemVisualizer<Int, IndexedItem>,
            selectionModel : SelectionModel<Int>? = null,
            fitContent     : Boolean              = true,
            numRows        : Int                  =  1,
            scrollCache    : Int                  = 10): HorizontalMutableList<Int, MutableListModel<Int>> =
            HorizontalMutableList(progression.toMutableList(), itemVisualizer, selectionModel, fitContent, numRows, scrollCache)

        public inline operator fun <reified T> invoke(
            values        : kotlin.collections.List<T>,
            itemVisualizer: ItemVisualizer<T, IndexedItem>,
            selectionModel: SelectionModel<Int>? = null,
            fitContent    : Boolean              = true,
            numRows       : Int                  =  1,
            scrollCache   : Int                  = 10): HorizontalMutableList<T, MutableListModel<T>> =
            HorizontalMutableList(mutableListModelOf(*values.toTypedArray()), itemVisualizer, selectionModel, fitContent, numRows, scrollCache)

        public inline operator fun invoke(
            values        : kotlin.collections.List<View>,
            selectionModel: SelectionModel<Int>? = null,
            fitContent    : Boolean              = true,
            numRows       : Int                  =  1,
            scrollCache   : Int                  = 10): HorizontalMutableList<View, MutableListModel<View>> =
            HorizontalMutableList(mutableListModelOf(*values.toTypedArray()), ViewVisualizer, selectionModel, fitContent, numRows, scrollCache)

        public inline operator fun <T, M: MutableListModel<T>>invoke(
            model         : M,
            itemGenerator : ItemVisualizer<T, IndexedItem>? = null,
            selectionModel: SelectionModel<Int>?            = null,
            fitContent    : Boolean                         = true,
            numRows       : Int                             =  1,
            scrollCache   : Int                             = 10): HorizontalMutableList<T, M> =
            HorizontalMutableList(model, itemGenerator, selectionModel, fitContent, numRows, scrollCache)
    }
}