@file:Suppress("NestedLambdaShadowedImplicitParameter", "ReplaceSingleLineLet")

package io.nacular.doodle.utils


/**
 * An NxM [Matrix](https://en.wikipedia.org/wiki/Matrix_(mathematics)).
 */
public interface Matrix<out T: Number> {
    /** The number of rows in the matrix. */
    public val numRows: Int

    /** The number of columns in the matrix. */
    public val numColumns: Int

    /** A value within the Matrix at [row, col]. */
    public operator fun get(row: Int, col: Int): T
}

/**
 * A square (NxN) [Matrix].
 *
 * @constructor creates a new matrix from numbers
 * @param values must be a square 2d list of list of values
 */
public open class SquareMatrix<T: Number> internal constructor(values: List<List<T>>): MatrixImpl<T>(values) {
    /**
     * `true` if this matrix is equal to the [Identity Matrix](https://en.wikipedia.org/wiki/Matrix_(mathematics)#Identity_matrix):
     *
     * ```
     *                                 |1 0 ... 0|
     *                |1 0|            |0 1 ... 0|
     * I1 = |1|, I2 = |0 1|, ..., In = |: : ... :|
     *                                 |0 0 ... 1|
     * ```
     */
    public var isIdentity: Boolean = true
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

    /**
     * The inverse of this matrix if it is [invertible](https://en.wikipedia.org/wiki/Invertible_matrix).
     */
    public val inverse: SquareMatrix<Double>? by lazy {
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

    private val determinant: Double by lazy {
        when (numRows) {
            1    -> this[0,0].toDouble()
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

/**
 * A matrix used to represent a 2D Affine Transformation. It is of the form
 *
 * ```
 *
 * |scaleX shearX translateX|
 * |shearY scaleY translateY|
 * |0      0      1         |
 *
 * ```
 *
 * @constructor creates a new instance
 * @param scaleX component of the matrix
 * @param shearX component of the matrix
 * @param translateX component of the matrix
 * @param shearY component of the matrix
 * @param scaleY component of the matrix
 * @param translateY component of the matrix
 */
public class AffineMatrix3D(
        scaleX    : Double,
        shearX    : Double,
        translateX: Double,
        shearY    : Double,
        scaleY    : Double,
        translateY: Double): SquareMatrix<Double>(listOf(
            listOf(scaleX, shearX, translateX),
            listOf(shearY, scaleY, translateY),
            listOf(   0.0,    0.0,        1.0)))

/**
 * Creates an NxN [SquareMatrix], where N == [size].
 *
 * @param size of N
 * @param init operation to get each value at [row, col]
 */
public fun <T: Number> squareMatrixOf(size: Int, init: (row: Int, col: Int) -> T): SquareMatrix<T> = SquareMatrix(List(size) { row -> List(size) { col -> init(col, row) } })

/**
 * Creates an NxM [Matrix], where N == [rows] and M == [cols].
 *
 * @param rows of the matrix
 * @param cols of the matrix
 * @param init operation to get each value at [row, col]
 */
public fun <T: Number> matrixOf(rows: Int, cols: Int, init: (Int, Int) -> T): Matrix<T> = when {
    rows != cols -> MatrixImpl(List(rows) { row -> List(cols) { col -> init(col, row) } })
    else         -> squareMatrixOf(rows, init)
}

/**
 * Gives the [transposition](https://en.wikipedia.org/wiki/Transpose) of a [SquareMatrix].
 */
public fun <T: Number> SquareMatrix<T>.transpose(): SquareMatrix<T> {
    val values = MutableList(numRows){ MutableList<T?>(numColumns) { null } }

    for (row in 0 until numRows) {
        for (col in 0 until numColumns) {
            values[row][col] = this[col, row]
        }
    }

    return SquareMatrix(values.map { it.mapNotNull { it } })
}

/**
 * [Matrix multiplication](https://en.wikipedia.org/wiki/Matrix_multiplication) of two [Matrix]es.
 */
public operator fun Matrix<Double>.times(other: Matrix<Double>): Matrix<Double> {
    require (other.numRows == numColumns) { "matrix column and row counts do not match" }

    val values = MutableList(numRows) { MutableList(other.numColumns) { 0.0 } }

    for (r1 in 0 until numRows) {
        for (c2 in 0 until other.numColumns) {
            values[r1][c2] = (0 until other.numRows).sumOf { this[r1, it] * other[it, c2] }
        }
    }

    return MatrixImpl(values)
}

/**
 * [Matrix subtraction](https://en.wikipedia.org/wiki/Matrix_addition) of two [Matrix]es.
 */
public operator fun Matrix<Double>.minus(other: Matrix<Double>): Matrix<Double> {
    require (other.numRows == numColumns) { "matrix column and row counts do not match" }

    val values = MutableList(numRows) { MutableList(other.numColumns) { 0.0 } }

    for (row in 0 until numRows) {
        for (col in 0 until numColumns) {
            values[row][col] = this[row, col] - other[row, col]
        }
    }

    return MatrixImpl(values)
}

/**
 * [Matrix addition](https://en.wikipedia.org/wiki/Matrix_addition) of two [Matrix]es.
 */
public operator fun Matrix<Double>.plus(other: Matrix<Double>): Matrix<Double> {
    require (other.numRows == numColumns) { "matrix column and row counts do not match" }

    val values = MutableList(numRows) { MutableList(other.numColumns) { 0.0 } }

    for (row in 0 until numRows) {
        for (col in 0 until numColumns) {
            values[row][col] = this[row, col] + other[row, col]
        }
    }

    return MatrixImpl(values)
}

/**
 * @see times
 */
public operator fun Matrix<Double>.times(other: SquareMatrix<Double>): Matrix<Double> {
    if (other.isIdentity) {
        return this
    }

    return this * (other as Matrix<Double>)
}

/**
 * Left [Scalar multiplication](https://en.wikipedia.org/wiki/Scalar_multiplication) of a [SquareMatrix].
 *
 * ```
 *     |A11 A12 ... A1n|   |λA11 λA12 ... λA1n|
 * λ * |A21 A12 ... A21| = |λA21 λA12 ... λA21|
 *     | :   :  ...  : |   | :     :  ...  :  |
 *     |An1 An2 ... Ann|   |λAn1 λAn2 ... λAnn|
 * ```
 */
public operator fun <T: Number> T.times(value: SquareMatrix<T>): SquareMatrix<Double> = value.map { toDouble() * it.toDouble() }

/**
 * Right [Scalar multiplication](https://en.wikipedia.org/wiki/Scalar_multiplication) of a [SquareMatrix].
 *
 * ```
 * |A11 A12 ... A1n|       |A11λ A12λ ... A1nλ|
 * |A21 A12 ... A21| * λ = |A21λ A12λ ... A21λ|
 * | :   :  ...  : |       | :     :  ...  :  |
 * |An1 An2 ... Ann|       |An1λ An2λ ... Annλ|
 * ```
 */
public operator fun <T: Number> SquareMatrix<T>.times(value: Number): SquareMatrix<Double> = map { it.toDouble() * value.toDouble() }

/**
 * [Matrix multiplication](https://en.wikipedia.org/wiki/Matrix_multiplication) of two [SquareMatrix]es.
 */
public operator fun SquareMatrix<Double>.times(other: SquareMatrix<Double>): SquareMatrix<Double> {
    if (other.isIdentity) {
        return this
    }

    if (this.isIdentity) {
        return other
    }

    val values = MutableList(numRows) { MutableList(other.numColumns) { 0.0 } }

    for (c2 in 0 until other.numColumns) {
        for (r1 in 0 until numRows) {
            val sum = (0 until other.numRows).sumOf { this[r1, it] * other[it, c2] }

            values[r1][c2] = sum
        }
    }

    return SquareMatrix(values)
}

public operator fun AffineMatrix3D.times(value: Number): AffineMatrix3D = value.toDouble().let {
    AffineMatrix3D(
            this[0, 0] * it, this[0, 1] * it, this[0, 2] * it,
            this[1, 0] * it, this[1, 1] * it, this[1, 2] * it)
}

public operator fun AffineMatrix3D.div(value: Number): AffineMatrix3D = value.toDouble().let {
    AffineMatrix3D(
            this[0, 0] / it, this[0, 1] / it, this[0, 2] / it,
            this[1, 0] / it, this[1, 1] / it, this[1, 2] / it)
}

/**
 * @see plus
 */
public operator fun AffineMatrix3D.plus(other: AffineMatrix3D): AffineMatrix3D {
    return AffineMatrix3D(
            this[0, 0] + other[0, 0], this[0, 1] + other[0, 1], this[0, 2] + other[0, 2],
            this[1, 0] + other[1, 0], this[1, 1] + other[1, 1], this[1, 2] + other[1, 2])
}

/**
 * @see minus
 */
public operator fun AffineMatrix3D.minus(other: AffineMatrix3D): AffineMatrix3D {
    return AffineMatrix3D(
            this[0, 0] - other[0, 0], this[0, 1] - other[0, 1], this[0, 2] - other[0, 2],
            this[1, 0] - other[1, 0], this[1, 1] - other[1, 1], this[1, 2] - other[1, 2])
}

/**
 * @see times
 */
public operator fun AffineMatrix3D.times(other: AffineMatrix3D): AffineMatrix3D {
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

    return AffineMatrix3D(values[0], values[1], values[2],
                          values[3], values[4], values[5])
}

/**
 * Creates a new [SquareMatrix] whose members are the [transform] of those in this one.
 *
 * @param transform operation to map elements
 */
public fun <T: Number, R: Number> SquareMatrix<T>.map(transform: (T) -> R): SquareMatrix<R> = SquareMatrix(values.map { it.map { transform(it) } })

/**
 * Like [map], but with [col, row] given for each item during tranformation.
 */
public fun <T: Number, R: Number> SquareMatrix<T>.mapIndexed(transform: (col: Int, row: Int, T) -> R): SquareMatrix<R> = SquareMatrix(values.mapIndexed { row, rows -> rows.mapIndexed { col, value -> transform(col, row, value) } })


/**
 * An (NxM) [Matrix].
 *
 * @constructor creates a new matrix from numbers
 * @param values must be a square 2d list of list of values
 */
public open class MatrixImpl<T: Number> internal constructor(internal val values: List<List<T>>): Matrix<T> {

    final override val numRows   : Int = values.size
    final override val numColumns: Int = if (numRows > 0) values[0].size else 0

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
                if (this[row, col].toDouble() != other[row, col].toDouble()) {
                    return false
                }
            }
        }

        return true
    }

    override fun hashCode(): Int = values.hashCode()
}