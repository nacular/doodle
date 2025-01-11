package io.nacular.doodle.controls.table

import io.nacular.doodle.controls.table.ColumnSizePolicy.Column
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 1/9/25.
 */
class ConstrainedSizePolicyTests {
    @Test fun `min width works`() {
        val policy = ConstrainedSizePolicy

        val tableWidth = 28.0 //30.0

        val columns = listOf(
            column(minWidth =  50, width =  50, maxWidth = 150),
            column(minWidth = 100, width = 186                ),
            column(minWidth = 100, width = 100, maxWidth = 150),
            column(minWidth = 100, width = 100, maxWidth = 150),
            column(                                           ), // Last Column
        )
        val startIndex = 0

        expect(350.0) { policy.layout(tableWidth, columns, startIndex) }

        columns.forEach {
            expect(it.width) { it.minWidth }
        }
    }

    private fun column(minWidth: Number = 0.0, width: Number? = null, maxWidth: Number? = null, preferredWidth: Number? = null): Column = object: Column {
        override var width          = width?.toDouble() ?: 0.0; set(value) {
            field = value.toDouble().coerceIn(this.minWidth, this.maxWidth)
        }
        override val minWidth       = minWidth.toDouble()
        override val maxWidth       = maxWidth?.toDouble()
        override val preferredWidth = (preferredWidth ?: width)?.toDouble()
    }
}