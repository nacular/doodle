package com.nectar.doodle.controls.table

import com.nectar.doodle.controls.ItemGenerator
import com.nectar.doodle.core.View
import com.nectar.doodle.layout.Constraints

interface ColumnBuilder {
    var headerAlignment: (Constraints.() -> Unit)?
    var width         : Double?
    var minWidth      : Double
    var maxWidth      : Double?
    var cellAlignment : (Constraints.() -> Unit)?
}

internal class ColumnBuilderImpl: ColumnBuilder {
    override var headerAlignment: (Constraints.() -> Unit)? = null
    override var width         : Double? = null
    override var minWidth      : Double = 0.0
    override var maxWidth      : Double? = null
    override var cellAlignment : (Constraints.() -> Unit)? = null
}

interface ColumnFactory<T> {
    fun <R> column(
            header       : View?,
            extractor    : T.() -> R,
            cellGenerator: ItemGenerator<R>,
            builder      : ColumnBuilder.() -> Unit): Column<R>
}

interface MutableColumnFactory<T>: ColumnFactory<T> {
    override fun <R> column(
            header       : View?,
            extractor    : T.() -> R,
            cellGenerator: ItemGenerator<R>,
            builder      : ColumnBuilder.() -> Unit): Column<R> = column(header, extractor, cellGenerator, null, builder)

    fun <R> column(
            header       : View?,
            extractor    : T.() -> R,
            cellGenerator: ItemGenerator<R>,
            editor       : ((T) -> T)?,
            builder      : ColumnBuilder.() -> Unit): Column<R>
}