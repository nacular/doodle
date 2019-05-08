package com.nectar.doodle.controls.table

import com.nectar.doodle.controls.ItemGenerator
import com.nectar.doodle.core.View

interface ColumnBuilder<T> {
    fun <R> column(
            header       : View?,
            width        : Double? = null,
            minWidth     : Double = 0.0,
            maxWidth     : Double? = null,
            itemGenerator: ItemGenerator<R>,
            extractor    : T.() -> R): Column<T>
}