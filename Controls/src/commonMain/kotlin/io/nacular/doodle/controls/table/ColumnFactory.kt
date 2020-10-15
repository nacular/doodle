package io.nacular.doodle.controls.table

import io.nacular.doodle.controls.IndexedIem
import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.core.View
import io.nacular.doodle.layout.Constraints

interface ColumnBuilder {
    var width          : Double?
    var minWidth       : Double
    var maxWidth       : Double?
    var cellAlignment  : (Constraints.() -> Unit)?
    var headerAlignment: (Constraints.() -> Unit)?
}

interface MutableColumnBuilder<T>: ColumnBuilder {
    var editor: TableEditor<T>?
}

internal open class ColumnBuilderImpl: ColumnBuilder {
    override var width          : Double?                   = null
    override var minWidth       : Double                    = 0.0
    override var maxWidth       : Double?                   = null
    override var cellAlignment  : (Constraints.() -> Unit)? = null
    override var headerAlignment: (Constraints.() -> Unit)? = null
}

internal class MutableColumnBuilderImpl<T>: ColumnBuilderImpl(), MutableColumnBuilder<T> {
    override var editor: TableEditor<T>? = null
}

interface CellInfo<T>: IndexedIem {
    // TODO: Include Table/TreeTable?
    val column: Column<T>
}

typealias CellVisualizer<T> = ItemVisualizer<T, CellInfo<T>>

typealias Extractor<T, R>  = T.() -> R
typealias Sorter<T, S>     = Extractor<T, S> //T.() -> S

interface ColumnFactory<T> {
    fun <R> column(
            header        : View?,
            extractor     : Extractor<T, R>,
            cellVisualizer: CellVisualizer<R>,
            builder       : ColumnBuilder.() -> Unit): Column<R>

    fun column(
            header       : View?,
            cellGenerator: CellVisualizer<Unit>,
            builder      : ColumnBuilder.() -> Unit): Column<Unit> = column(header, {}, cellGenerator, builder)
}

interface MutableColumnFactory<T> {
    fun column(
            header       : View?,
            cellGenerator: CellVisualizer<Unit>,
            builder      : MutableColumnBuilder<T>.() -> Unit): Column<Unit> = column(header, {}, cellGenerator, builder)


    fun <R, S: Comparable<S>> column(
            header        : View?,
            extractor     : Extractor<T, R>,
            cellVisualizer: CellVisualizer<R>,
            editor        : TableEditor<T>? = null,
            sorter        : Sorter<T, S>? = null,
            builder       : MutableColumnBuilder<T>.() -> Unit): MutableColumn<T, R>

    fun <R> column(
            header        : View?,
            extractor     : Extractor<T, R>,
            cellVisualizer: CellVisualizer<R>,
            builder       : MutableColumnBuilder<T>.() -> Unit): MutableColumn<T, R> = column(header, extractor, cellVisualizer, null, null as Sorter<T, Int>?, builder)

    fun <R: Comparable<R>> column(
            header        : View?,
            extractor     : Extractor<T, R>,
            cellVisualizer: CellVisualizer<R>,
            editor        : TableEditor<T>? = null,
            sortable      : Boolean = false,
            builder       : MutableColumnBuilder<T>.() -> Unit): MutableColumn<T, R> = column(header, extractor, cellVisualizer, editor, if (sortable) extractor else null, builder)
}