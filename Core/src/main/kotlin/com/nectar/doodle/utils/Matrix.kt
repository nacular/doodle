package com.nectar.doodle.utils


class Matrix(firstRow: DoubleArray, vararg rest: DoubleArray) {

    val values     = arrayOf(firstRow) + rest
    val numRows    = values.size
    val numColumns = if (numRows > 0) values[0].size else 0
    var isIdentity = true
        private set

    init {
        require(numColumns > 0) { "empty Matrixes are invalid" }

        for (row in values.indices) {
            require(values[row].size == numColumns) { "all rows must have the same length" }

            if (isIdentity) {
                values[row].forEachIndexed { index, value ->
                    isIdentity = when {
                        value != 1.0 && index == row -> false
                        value != 0.0 && index != row -> false
                        else                         -> true
                    }

                    if (!isIdentity) { return@forEachIndexed }
                }
            }
        }
    }

    operator fun get(row: Int, column: Int): Double = values[row][column]

    operator fun times(other: Matrix): Matrix {
        require (other.numRows == numColumns) { "matrix column and row counts do not match" }

        if (other.isIdentity) {
            return this
        }

        if (isIdentity) {
            return other
        }

        val values = Array(numRows) { DoubleArray(other.numColumns) }

        for (c2 in 0 until other.numColumns) {
            for (r1 in 0 until numRows) {
                val sum = (0 until other.numRows).sumByDouble { this.values[r1][it] * other.values[it][c2] }

                values[r1][c2] = sum
            }
        }

        return Matrix(values[0], *values.sliceArray(1 until values.size))
    }

    override fun toString(): String {
        val result = StringBuilder()

        for (r in 0 until numRows) {
            result.append((if (r > 0) "\n" else "") + "|")

            for (c in 0 until numColumns) {
                result.append((if (c > 0) " " else "") + values[r][c])
            }

            result.append("|")
        }

        return result.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Matrix) return false

        return values.contentDeepEquals(other.values)
    }

    override fun hashCode() = values.hashCode()
}
