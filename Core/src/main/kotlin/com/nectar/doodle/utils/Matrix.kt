package com.nectar.doodle.utils


class Matrix(private val values: Array<DoubleArray> = Array(0) { DoubleArray(0) }) {

    val numRows    = values.size
    val numColumns = if (numRows > 0) values[0].size else 0
    var isIdentity = true
        private set

    init {
        for (i in values.indices) {
            require(values[i].size == numColumns) { "all rows must have the same length" }

            if (isIdentity) {
                val sum = (0 until values[i].size).sumByDouble { values[i][it] }

                isIdentity = sum == 1.0 && values[i][i] == 1.0
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

        return Matrix(values)
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
}
