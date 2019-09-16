package com.nectar.doodle.controls.table

interface ColumnSizePolicy {
    interface Column {
        var width         : Double
        val minWidth      : Double
        val maxWidth      : Double?
        val preferredWidth: Double?
    }

    fun layout(width: Double, columns: List<Column>, startIndex: Int = 0): Double

    fun widthChanged(width: Double, columns: List<Column>, index: Int, to: Double)
}

class ConstrainedSizePolicy: ColumnSizePolicy {
    override fun layout(width: Double, columns: List<ColumnSizePolicy.Column>, startIndex: Int): Double {
        var remainingWidth = width

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

        return width - remainingWidth
    }

    override fun widthChanged(width: Double, columns: List<ColumnSizePolicy.Column>, index: Int, to: Double) {
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