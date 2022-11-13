package io.nacular.doodle.controls.table

/**
 * Defines how columns within [Table] and related controls are sized.
 */
public interface ColumnSizePolicy {
    public interface Column {
        public var width         : Double
        public val minWidth      : Double
        public val maxWidth      : Double?
        public val preferredWidth: Double?
    }

    /**
     * Updates the width of each column and return the overall table width needed to
     * properly display the columns based on that width.
     *
     * @param tableWidth of the Table before column widths updated
     * @param columns within the Table
     * @param startIndex of columns that will be sized
     * @return the updated width for the table
     */
    public fun layout(tableWidth: Double, columns: List<Column>, startIndex: Int = 0): Double

    /**
     * Used to set a column's width. Implementations need to set the width(s) of any
     * columns that will be changed to update the table.
     *
     * NOTE: Doing nothing in this method is equivalent to ignoring the resize request.
     *
     * @param tableWidth for the Table
     * @param columns within the Table
     * @param index of the column being changed
     * @param to the width the column is being sized to
     */
    public fun changeColumnWidth(tableWidth: Double, columns: List<Column>, index: Int, to: Double)
}

public object ConstrainedSizePolicy: ColumnSizePolicy {
    override fun layout(tableWidth: Double, columns: List<ColumnSizePolicy.Column>, startIndex: Int): Double {
        var remainingWidth = tableWidth

        // Set initial widths
        columns.forEachIndexed { index, it ->
            if (index >= startIndex) {
                it.width = it.preferredWidth ?: it.minWidth
            }

            remainingWidth -= it.width
        }

        // Can this special knowledge of a dummy last column be avoided?
        columns.drop(startIndex).dropLast(1).filter {
            when {
                remainingWidth > 0 -> it.preferredWidth == null
                else               -> it.preferredWidth != null
            }
        }.run {
            var size = size

            forEach {
                val old = it.width
                it.width += remainingWidth / size--
                remainingWidth -= it.width - old
            }
        }

        columns.lastOrNull()?.let {
            val old = it.width
            it.width += remainingWidth
            remainingWidth -= it.width - old
        }

        return tableWidth - remainingWidth
    }

    override fun changeColumnWidth(tableWidth: Double, columns: List<ColumnSizePolicy.Column>, index: Int, to: Double) {
        columns[index].width = to
    }
}

public object EqualSizePolicy: ColumnSizePolicy {
    override fun layout(tableWidth: Double, columns: List<ColumnSizePolicy.Column>, startIndex: Int): Double {
        val colWidth = tableWidth / (columns.size - 1 - startIndex)

        columns.dropLast(1).forEach {
            it.width = colWidth
        }

        return tableWidth
    }

    override fun changeColumnWidth(tableWidth: Double, columns: List<ColumnSizePolicy.Column>, index: Int, to: Double) {
        columns[index].width = tableWidth / (columns.size - 1)
    }
}

public object ProportionalSizePolicy: ColumnSizePolicy {
    override fun layout(tableWidth: Double, columns: List<ColumnSizePolicy.Column>, startIndex: Int): Double {
        if (tableWidth > 0) {
            val totalColWidth = columns.dropLast(1).sumOf { it.width }

            columns.dropLast(1).forEach {
                it.width = when {
                    totalColWidth > 0 -> (/*it.preferredWidth ?: */it.width) / totalColWidth * tableWidth
                    else              -> tableWidth / (columns.size - 1)
                }
            }
        }

        return tableWidth
    }

    override fun changeColumnWidth(tableWidth: Double, columns: List<ColumnSizePolicy.Column>, index: Int, to: Double) {
        columns[index].width = to
    }
}