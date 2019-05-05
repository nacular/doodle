package com.nectar.doodle.controls.table

interface ColumnSizePolicy<T> {
    interface Column {
        var width         : Double
        val minWidth      : Double
        val maxWidth      : Double?
        val preferredWidth: Double?
    }

    fun layout(table: Table<T, *>, columns: List<Column>, startIndex: Int = 0): Double

    fun widthChanged(table: Table<T, *>, columns: List<Column>, index: Int, to: Double)
}

class ConstrainedSizePolicy<T>: ColumnSizePolicy<T> {
    override fun layout(table: Table<T, *>, columns: List<ColumnSizePolicy.Column>, startIndex: Int): Double {
        var remainingWidth = table.width

        // Set initial widths
        columns.forEachIndexed { index, it ->
            if (index >= startIndex) {
                it.width = it.preferredWidth ?: it.minWidth
            }

            remainingWidth -= it.width
        }

        if (remainingWidth > 0) {
            columns.drop(startIndex).filter { it.preferredWidth == null }.let {
                var size = it.size

                it.forEach {
                    val old = it.width
                    it.width += remainingWidth / size--
                    remainingWidth -= it.width - old
                }
            }
        } else if (remainingWidth < 0) {
            columns.drop(startIndex).filter { it.preferredWidth != null }.let {
                var size = it.size

                it.forEach {
                    val old = it.width
                    it.width += remainingWidth / size--
                    remainingWidth -= it.width - old
                }
            }
        }

        return table.width - remainingWidth
    }

    override fun widthChanged(table: Table<T, *>, columns: List<ColumnSizePolicy.Column>, index: Int, to: Double) {
        var old = columns[index].width

        columns[index].width = to

        var remainingWidth = old - columns[index].width

        var remainingCols = columns.size - 1 - index

        (index + 1 until columns.size).map { columns[it] }.forEach { column ->
            old = column.width

            column.width += remainingWidth / remainingCols--

            remainingWidth -= column.width - old
        }
    }
}