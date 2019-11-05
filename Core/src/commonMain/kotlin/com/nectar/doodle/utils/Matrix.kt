@file:Suppress("NestedLambdaShadowedImplicitParameter", "ReplaceSingleLineLet")

package com.nectar.doodle.utils


interface Matrix<out T: Number> {
    val numRows   : Int
    val numColumns: Int

    operator fun get(row: Int, col: Int): T
}

open class SquareMatrix<T: Number> internal constructor(values: List<List<T>>): MatrixImpl<T>(values) {
    var isIdentity = true
        private set

        init {
        require(numRows == numColumns) { "row and column count must be equal" }

        for (row in values.indices) {
            if (isIdentity) {
                run loop@{
                    values[row].forEachIndexed { index, value ->
                        isIdentity = when {
                            value.toDouble() != 1.0 && index == row -> false
                            value.toDouble() != 0.0 && index != row -> false
                            else                       -> true
                        }

                        if (!isIdentity) { return@loop }
                    }
                }
            }
        }
    }

    val inverse: SquareMatrix<Double>? by lazy {
        when {
            isIdentity         -> this.map { it.toDouble() } // TODO: This shouldn't require any copy
            determinant == 0.0 -> null
            else               -> {
                val cofactors = values.mapIndexed { r, values ->
                    values.mapIndexed { c, _ ->
                        determinant(r, c) * when {
                            (r + c).isOdd -> -1
                            else          ->  1
                        }
                    }
                }.let { squareMatrixOf(numRows) { col, row -> it[row][col] } }

                val transposed = cofactors.transpose()

                (transposed * (1.0 / determinant))
            }
        }
    }

    val determinant: Double by lazy {
        when (numRows) {
            2    -> this[0,0].toDouble() * this[1,1].toDouble() - this[0,1].toDouble() * this[1,0].toDouble()
            else -> {
                var sign   = 1
                var result = 0.0

                for (col in 0 until numColumns) {
                    result += sign * this[0, col].toDouble() * determinant(0, col)
                    sign *= -1
                }

                result
            }
        }
    }

    private fun determinant(row: Int, col: Int): Double {
        val subData = values.filterIndexed { r, _ -> r != row }.map { it.filterIndexed { c, _ -> c != col } }

        return SquareMatrix(subData).determinant
    }
}

class AffineMatrix3D(
        scaleX    : Double,
        shearX    : Double,
        translateX: Double,
        shearY    : Double,
        scaleY    : Double,
        translateY: Double): SquareMatrix<Double>(listOf(
            listOf(scaleX, shearX, translateX),
            listOf(shearY, scaleY, translateY),
            listOf(   0.0,    0.0,        1.0))) {
}

fun <T: Number> squareMatrixOf(size: Int, init: (Int, Int) -> T) = SquareMatrix(List(size) { row -> List(size) { col -> init(col, row) } })

fun <T: Number> matrixOf(rows: Int, cols: Int, init: (Int, Int) -> T): Matrix<T> = when {
    rows != cols -> MatrixImpl(List(rows) { row -> List(cols) { col -> init(col, row) } })
    else         -> squareMatrixOf(rows, init)
}

fun <T: Number> SquareMatrix<T>.transpose(): SquareMatrix<T> {
    val values = MutableList(numRows){ MutableList<T?>(numColumns) { null } }

    for (row in 0 until numRows) {
        for (col in 0 until numColumns) {
            values[row][col] = this[col, row]
        }
    }

    return SquareMatrix(values.map { it.mapNotNull { it } })
}

operator fun <T: Number> SquareMatrix<T>.times(value: Number): SquareMatrix<Double> = map { it.toDouble() * value.toDouble() }

operator fun SquareMatrix<Double>.times(other: SquareMatrix<Double>): SquareMatrix<Double> {
    if (other.isIdentity) {
        return this
    }

    if (this.isIdentity) {
        return other
    }

    val values = MutableList(numRows) { MutableList(other.numColumns) { 0.0 } }

    for (c2 in 0 until other.numColumns) {
        for (r1 in 0 until numRows) {
            val sum = (0 until other.numRows).sumByDouble { this[r1, it] * other[it, c2] }

            values[r1][c2] = sum
        }
    }

    return SquareMatrix(values)
}

operator fun AffineMatrix3D.times(value: Number): AffineMatrix3D = value.toDouble().let {
    AffineMatrix3D(
            this[0, 0] * it, this[0, 1] * it, this[0, 2] * it,
            this[1, 0] * it, this[1, 1] * it, this[1, 2] * it)
}

operator fun AffineMatrix3D.times(other: AffineMatrix3D): AffineMatrix3D {
    if (other.isIdentity) {
        return this
    }

    if (this.isIdentity) {
        return other
    }

    val values = mutableListOf<Double>()//MutableList(numRows - 1) { MutableList(other.numColumns) { 0.0 } }

    for (r1 in 0 until numRows - 1) {
        for (c2 in 0 until other.numColumns) {
            values += (0 until other.numRows).sumByDouble { this[r1, it] * other[it, c2] }
        }
    }

    return AffineMatrix3D(
            values[0], values[1], values[2],
            values[3], values[4], values[5])
}

operator fun Matrix<Double>.times(other: SquareMatrix<Double>): Matrix<Double> {
    if (other.isIdentity) {
        return this
    }

    return this * (other as Matrix<Double>)
}

operator fun Matrix<Double>.times(other: Matrix<Double>): Matrix<Double> {
    require (other.numRows == numColumns) { "matrix column and row counts do not match" }

    val values = MutableList(numRows) { MutableList(other.numColumns) { 0.0 } }

    for (r1 in 0 until numRows) {
        for (c2 in 0 until other.numColumns) {
            values[r1][c2] = (0 until other.numRows).sumByDouble { this[r1, it] * other[it, c2] }
        }
    }

    return MatrixImpl(values)
}

fun <T: Number, R: Number> SquareMatrix<T>.map(transform: (T) -> R): SquareMatrix<R> = values.map { it.map { transform(it) } }.let { SquareMatrix(it) }

fun <T: Number, R: Number> SquareMatrix<T>.mapIndexed(transform: (col: Int, row: Int, T) -> R): SquareMatrix<R> {
    return values.mapIndexed { row, rows -> rows.mapIndexed { col, value -> transform(col, row, value) } }.let { SquareMatrix(it) }
}


open class MatrixImpl<T: Number> internal constructor(internal val values: List<List<T>>): Matrix<T> {

    override val numRows    = values.size
    override val numColumns = if (numRows > 0) values[0].size else 0

    init {
        require(numColumns > 0) { "empty Matrices are invalid" }

        for (row in values.indices) {
            require(values[row].size == numColumns) { "all rows must have the same length" }
        }
    }

    override operator fun get(row: Int, col: Int): T = values[row][col]

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
        if (other !is Matrix<*>) return false

        if (numRows    != other.numRows   ) return false
        if (numColumns != other.numColumns) return false

        for (row in 0 until numRows) {
            for (col in 0 until numColumns) {
                if (this[row, col] != other[row, col]) {
                    return false
                }
            }
        }

        return true
    }

    override fun hashCode() = values.hashCode()
}