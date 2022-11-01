package io.nacular.doodle.controls.table

import io.nacular.doodle.controls.IndexedItem
import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.core.View
import io.nacular.doodle.layout.constraints.Bounds
import io.nacular.doodle.layout.constraints.ConstraintDslContext
import io.nacular.doodle.utils.Extractor

public interface ColumnBuilder {
    public var width          : Double?
    public var minWidth       : Double
    public var maxWidth       : Double?
    public var cellAlignment  : (ConstraintDslContext.(Bounds) -> Unit)?
    public var headerAlignment: (ConstraintDslContext.(Bounds) -> Unit)?
    public var footerAlignment: (ConstraintDslContext.(Bounds) -> Unit)?
}

public interface MutableColumnBuilder<T>: ColumnBuilder {
    public var editor: TableEditor<T>?
}

internal open class ColumnBuilderImpl: ColumnBuilder {
    override var width          : Double?                   = null
    override var minWidth       : Double                    = 0.0
    override var maxWidth       : Double?                   = null
    override var cellAlignment  : (ConstraintDslContext.(Bounds) -> Unit)? = null
    override var headerAlignment: (ConstraintDslContext.(Bounds) -> Unit)? = null
    override var footerAlignment: (ConstraintDslContext.(Bounds) -> Unit)? = null
}

internal class MutableColumnBuilderImpl<T>: ColumnBuilderImpl(), MutableColumnBuilder<T> {
    override var editor: TableEditor<T>? = null
}

public interface CellInfo<T, R>: IndexedItem {
    // TODO: Include Table/TreeTable?
    public val column: Column<R>
    public val item  : T
}

public typealias CellVisualizer<Item, Field> = ItemVisualizer<Field, CellInfo<Item, Field>>

public typealias Sorter<T, S>     = Extractor<T, S> //T.() -> S

public interface ColumnFactory<T> {
    public fun <R> column(
        header        : View?,
        extractor     : Extractor<T, R>,
        cellVisualizer: CellVisualizer<T, R>,
        footer        : View? = null,
        builder       : ColumnBuilder.() -> Unit): Column<R>

    public fun column(
        header       : View?,
        cellGenerator: CellVisualizer<T, Unit>,
        footer       : View? = null,
        builder      : ColumnBuilder.() -> Unit): Column<Unit> = column(header = header, footer = footer, extractor = {}, cellVisualizer = cellGenerator, builder = builder)
}

public interface MutableColumnFactory<T> {
    public fun column(
        header       : View?,
        cellGenerator: CellVisualizer<T, Unit>,
        footer       : View? = null,
        builder      : MutableColumnBuilder<T>.() -> Unit): Column<Unit> = column(header = header, footer = footer, extractor = {}, cellVisualizer = cellGenerator, builder = builder)


    public fun <R, C: Comparable<C>> column(
        header        : View?,
        extractor     : Extractor<T, R>,
        cellVisualizer: CellVisualizer<T, R>,
        editor        : TableEditor<T>? = null,
        sorter        : Sorter<T, C>?   = null,
        footer        : View?           = null,
        builder       : MutableColumnBuilder<T>.() -> Unit): MutableColumn<T, R>

    public fun <R> column(
        header        : View?,
        extractor     : Extractor<T, R>,
        cellVisualizer: CellVisualizer<T, R>,
        footer        : View? = null,
        builder       : MutableColumnBuilder<T>.() -> Unit): MutableColumn<T, R> = column(header = header, footer = footer, extractor = extractor, cellVisualizer = cellVisualizer, editor = null, sorter = null as Sorter<T, Int>?, builder = builder)

    public fun <R: Comparable<R>> column(
        header        : View?,
        extractor     : Extractor<T, R>,
        cellVisualizer: CellVisualizer<T, R>,
        editor        : TableEditor<T>? = null,
        sortable      : Boolean         = false,
        footer        : View?           = null,
        builder       : MutableColumnBuilder<T>.() -> Unit): MutableColumn<T, R> = column(header = header, footer = footer, extractor = extractor, cellVisualizer = cellVisualizer, editor = editor, sorter = if (sortable) extractor else null, builder = builder)
}