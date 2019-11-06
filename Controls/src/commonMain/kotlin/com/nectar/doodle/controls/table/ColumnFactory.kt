package com.nectar.doodle.controls.table

import com.nectar.doodle.controls.IndexedItemVisualizer
import com.nectar.doodle.controls.ItemVisualizer
import com.nectar.doodle.core.View
import com.nectar.doodle.layout.Constraints

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
    operator fun invoke(column: Column<T>, item: T, row: Int, previous: View? = null): View
}

class PassThroughCellVisualizer<T>(private val itemVisualizer: ItemVisualizer<T>): CellVisualizer<T> {
    override fun invoke(column: Column<T>, item: T, row: Int, previous: View?) = itemVisualizer(item, previous)
}

fun <T> passThroughCellVisualizer(itemVisualizer: ItemVisualizer<T>) = object: CellVisualizer<T> {
    override fun invoke(column: Column<T>, item: T, row: Int, previous: View?) = itemVisualizer(item, previous)
}

fun <T> passThroughCellVisualizer(itemVisualizer: IndexedItemVisualizer<T>) = object: CellVisualizer<T> {
    override fun invoke(column: Column<T>, item: T, row: Int, previous: View?) = itemVisualizer(item, row, previous)
}

interface ColumnFactory<T> {
    fun <R> column(
            header        : View?,
            extractor     : T.() -> R,
            cellVisualizer: CellVisualizer<R>,
            builder       : ColumnBuilder.() -> Unit): Column<R>

    fun <R> column(
            header        : View?,
            extractor     : T.() -> R,
            itemVisualizer: ItemVisualizer<R>,
            builder       : ColumnBuilder.() -> Unit) = column(header, extractor, passThroughCellVisualizer(itemVisualizer), builder)

    fun <R> column(
            header        : View?,
            extractor     : T.() -> R,
            itemVisualizer: IndexedItemVisualizer<R>,
            builder       : ColumnBuilder.() -> Unit) = column(header, extractor, passThroughCellVisualizer(itemVisualizer), builder)

    fun column(
            header       : View?,
            cellGenerator: CellVisualizer<Unit>,
            builder      : ColumnBuilder.() -> Unit): Column<Unit> = column(header, {}, cellGenerator, builder)
}

interface MutableColumnFactory<T>: ColumnFactory<T> {
    override fun <R> column(
            header       : View?,
            extractor    : T.() -> R,
            cellVisualizer: CellVisualizer<R>,
            builder      : ColumnBuilder.() -> Unit): Column<R> = column(header, extractor, cellVisualizer, null, builder)

    fun <R> column(
            header       : View?,
            extractor    : T.() -> R,
            cellGenerator: CellVisualizer<R>,
            editor       : ((T) -> T)?,
            builder      : ColumnBuilder.() -> Unit): Column<R>
}