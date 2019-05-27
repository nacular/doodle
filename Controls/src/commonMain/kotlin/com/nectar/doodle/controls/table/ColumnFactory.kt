package com.nectar.doodle.controls.table

import com.nectar.doodle.controls.ItemGenerator
import com.nectar.doodle.core.View
import com.nectar.doodle.layout.Constraints

interface ColumnFactory<T> {
    fun <R> column(
            header        : View?,
            headerPosition: (Constraints.() -> Unit)? = null,
            width         : Double? = null,
            minWidth      : Double = 0.0,
            maxWidth      : Double? = null,
            cellGenerator : ItemGenerator<R>,
            cellPosition  : (Constraints.() -> Unit)? = null,
            extractor     : T.() -> R): Column<R>
}

interface MutableColumnFactory<T>: ColumnFactory<T> {
    override fun <R> column(
            header        : View?,
            headerPosition: (Constraints.() -> Unit)?,
            width         : Double?,
            minWidth      : Double,
            maxWidth      : Double?,
            cellGenerator : ItemGenerator<R>,
            cellPosition  : (Constraints.() -> Unit)?,
            extractor     : T.() -> R): Column<R> = column(header, width, minWidth, maxWidth, cellGenerator, extractor, null)

    fun <R> column(
            header       : View?,
            width        : Double? = null,
            minWidth     : Double = 0.0,
            maxWidth     : Double? = null,
            itemGenerator: ItemGenerator<R>,
            extractor    : T.() -> R,
            editor       : ((T) -> T)?): Column<R>
}