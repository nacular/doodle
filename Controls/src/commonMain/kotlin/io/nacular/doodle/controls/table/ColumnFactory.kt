package io.nacular.doodle.controls.table

import io.nacular.doodle.controls.IndexedItem
import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.core.View
import io.nacular.doodle.layout.Constraints

public interface ColumnBuilder {
    public var width          : Double?
    public var minWidth       : Double
    public var maxWidth       : Double?
    public var cellAlignment  : (Constraints.() -> Unit)?
    public var headerAlignment: (Constraints.() -> Unit)?
}

public interface MutableColumnBuilder<T>: ColumnBuilder {
    public var editor: TableEditor<T>?
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

public interface CellInfo<T>: IndexedItem {
    // TODO: Include Table/TreeTable?
    public val column: Column<T>
}

public typealias CellVisualizer<T> = ItemVisualizer<T, CellInfo<T>>

public typealias Extractor<T, R>  = T.() -> R
public typealias Sorter<T, S>     = Extractor<T, S> //T.() -> S

public interface ColumnFactory<T> {
    public fun <R> column(
            header        : View?,
            extractor     : Extractor<T, R>,
            cellVisualizer: CellVisualizer<R>,
            builder       : ColumnBuilder.() -> Unit): Column<R>

    public fun column(
            header       : View?,
            cellGenerator: CellVisualizer<Unit>,
            builder      : ColumnBuilder.() -> Unit): Column<Unit> = column(header, {}, cellGenerator, builder)
}

public interface MutableColumnFactory<T> {
    public fun column(
            header       : View?,
            cellGenerator: CellVisualizer<Unit>,
            builder      : MutableColumnBuilder<T>.() -> Unit): Column<Unit> = column(header, {}, cellGenerator, builder)


    public fun <R, S: Comparable<S>> column(
            header        : View?,
            extractor     : Extractor<T, R>,
            cellVisualizer: CellVisualizer<R>,
            editor        : TableEditor<T>? = null,
            sorter        : Sorter<T, S>? = null,
            builder       : MutableColumnBuilder<T>.() -> Unit): MutableColumn<T, R>

    public fun <R> column(
            header        : View?,
            extractor     : Extractor<T, R>,
            cellVisualizer: CellVisualizer<R>,
            builder       : MutableColumnBuilder<T>.() -> Unit): MutableColumn<T, R> = column(header, extractor, cellVisualizer, null, null as Sorter<T, Int>?, builder)

    public fun <R: Comparable<R>> column(
            header        : View?,
            extractor     : Extractor<T, R>,
            cellVisualizer: CellVisualizer<R>,
            editor        : TableEditor<T>? = null,
            sortable      : Boolean = false,
            builder       : MutableColumnBuilder<T>.() -> Unit): MutableColumn<T, R> = column(header, extractor, cellVisualizer, editor, if (sortable) extractor else null, builder)
}