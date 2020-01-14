package com.nectar.doodle.controls.table

import com.nectar.doodle.controls.IndexedItemVisualizer
import com.nectar.doodle.controls.ItemVisualizer
import com.nectar.doodle.core.View
import com.nectar.doodle.layout.Constraints
import com.nectar.doodle.layout.Insets

interface ColumnBuilder {
    var width          : Double?
    var minWidth       : Double
    var maxWidth       : Double?
    var cellAlignment  : (Constraints.() -> Unit)?
    var headerAlignment: (Constraints.() -> Unit)?
}

internal class ColumnBuilderImpl: ColumnBuilder {
    override var width          : Double?                   = null
    override var minWidth       : Double                    = 0.0
    override var maxWidth       : Double?                   = null
    override var cellAlignment  : (Constraints.() -> Unit)? = null
    override var headerAlignment: (Constraints.() -> Unit)? = null
}

interface CellVisualizer<T> {
    // TODO: Include Table/TreeTable?
    operator fun invoke(column: Column<T>, item: T, row: Int, previous: View? = null, isSelected: () -> Boolean = { false }): View
}

class PassThroughCellVisualizer<T>(private val itemVisualizer: ItemVisualizer<T>): CellVisualizer<T> {
    override fun invoke(column: Column<T>, item: T, row: Int, previous: View?, isSelected: () -> Boolean) = itemVisualizer(item, previous)
}

fun <T> passThroughCellVisualizer(itemVisualizer: ItemVisualizer<T>) = object: CellVisualizer<T> {
    override fun invoke(column: Column<T>, item: T, row: Int, previous: View?, isSelected: () -> Boolean) = itemVisualizer(item, previous)
}

fun <T> passThroughCellVisualizer(itemVisualizer: IndexedItemVisualizer<T>) = object: CellVisualizer<T> {
    override fun invoke(column: Column<T>, item: T, row: Int, previous: View?, isSelected: () -> Boolean) = itemVisualizer(item, row, previous)
}

typealias Extractor<T, R>  = T.() -> R
typealias Sorter<T, S>     = Extractor<T, S> //T.() -> S

interface ColumnFactory<T> {
    fun <R> column(
            header        : View?,
            extractor     : Extractor<T, R>,
            cellVisualizer: CellVisualizer<R>,
            builder       : ColumnBuilder.() -> Unit): Column<R>

    fun <R> column(
            header        : View?,
            extractor     : Extractor<T, R>,
            itemVisualizer: ItemVisualizer<R>,
            builder       : ColumnBuilder.() -> Unit) = column(header, extractor, passThroughCellVisualizer(itemVisualizer), builder)

    fun <R> column(
            header        : View?,
            extractor     : Extractor<T, R>,
            itemVisualizer: IndexedItemVisualizer<R>,
            builder       : ColumnBuilder.() -> Unit) = column(header, extractor, passThroughCellVisualizer(itemVisualizer), builder)

    fun column(
            header       : View?,
            cellGenerator: CellVisualizer<Unit>,
            builder      : ColumnBuilder.() -> Unit): Column<Unit> = column(header, {}, cellGenerator, builder)
}

interface MutableColumnFactory<T> {
    fun <R> column(
            header        : View?,
            extractor     : Extractor<T, R>,
            itemVisualizer: IndexedItemVisualizer<R>,
            builder       : ColumnBuilder.() -> Unit) = column(header, extractor, passThroughCellVisualizer(itemVisualizer), builder)

    fun column(
            header       : View?,
            cellGenerator: CellVisualizer<Unit>,
            builder      : ColumnBuilder.() -> Unit): Column<Unit> = column(header, {}, cellGenerator, builder)


    fun <R, S: Comparable<S>> column(
            header        : View?,
            extractor     : Extractor<T, R>,
            cellVisualizer: CellVisualizer<R>,
            editor        : TableEditor<T>? = null,
            sorter        : Sorter<T, S>? = null,
            builder       : ColumnBuilder.() -> Unit): MutableColumn<T, R>

    fun <R> column(
            header        : View?,
            extractor     : Extractor<T, R>,
            cellVisualizer: CellVisualizer<R>,
            builder       : ColumnBuilder.() -> Unit): MutableColumn<T, R> = column(header, extractor, cellVisualizer, null, null as Sorter<T, Int>?, builder)

    fun <R> column(
            header        : View?,
            extractor     : Extractor<T, R>,
            itemVisualizer: ItemVisualizer<R>,
            builder       : ColumnBuilder.() -> Unit) = column(header, extractor, passThroughCellVisualizer(itemVisualizer), builder)

    fun <R: Comparable<R>> column(
            header        : View?,
            extractor     : Extractor<T, R>,
            cellVisualizer: CellVisualizer<R>,
            editor        : TableEditor<T>? = null,
            sortable      : Boolean = false,
            builder       : ColumnBuilder.() -> Unit): MutableColumn<T, R> = column(header, extractor, cellVisualizer, editor, if (sortable) extractor else null, builder)

    fun <R, S: Comparable<S>> column(
            header        : View?,
            extractor     : Extractor<T, R>,
            itemVisualizer: ItemVisualizer<R>,
            editor        : TableEditor<T>? = null,
            sorter        : Sorter<T, S>? = null,
            builder       : ColumnBuilder.() -> Unit) = column(header, extractor, passThroughCellVisualizer(itemVisualizer), editor, sorter, builder)

    fun <R: Comparable<R>> column(
            header        : View?,
            extractor     : Extractor<T, R>,
            itemVisualizer: ItemVisualizer<R>,
            editor        : TableEditor<T>? = null,
            sortable      : Boolean = false,
            builder       : ColumnBuilder.() -> Unit) = column(header, extractor, passThroughCellVisualizer(itemVisualizer), editor, if (sortable) extractor else null, builder)

    fun <R, S: Comparable<S>> column(
            header        : View?,
            extractor     : Extractor<T, R>,
            itemVisualizer: IndexedItemVisualizer<R>,
            editor        : TableEditor<T>? = null,
            sorter        : Sorter<T, S>? = null,
            builder       : ColumnBuilder.() -> Unit) = column(header, extractor, passThroughCellVisualizer(itemVisualizer), editor, sorter, builder)

    fun <R: Comparable<R>> column(
            header        : View?,
            extractor     : Extractor<T, R>,
            itemVisualizer: IndexedItemVisualizer<R>,
            editor        : TableEditor<T>? = null,
            sortable      : Boolean = false,
            builder       : ColumnBuilder.() -> Unit) = column(header, extractor, passThroughCellVisualizer(itemVisualizer), editor, if (sortable) extractor else null, builder)

    fun <R, S: Comparable<S>> column(
            header        : View?,
            itemVisualizer: IndexedItemVisualizer<Unit>,
            sorter        : Sorter<T, S>? = null,
            builder       : ColumnBuilder.() -> Unit) = column(header, {}, passThroughCellVisualizer(itemVisualizer), null, sorter, builder)
}