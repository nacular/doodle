@file:Suppress("NestedLambdaShadowedImplicitParameter", "ReplaceSingleLineLet")

package io.nacular.doodle.utils

import io.nacular.doodle.core.Internal
import kotlin.jvm.JvmName
import kotlin.math.max


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
 * @param values must be a square 2d array of values
 */
public open class SquareMatrix<T: Number> internal constructor(values: Array<Array<T>>): MatrixImpl<T>(values) {
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
            isIdentity         -> SquareMatrix(values.map { it.map { it.toDouble() }.toTypedArray() }.toTypedArray()) // TODO: This shouldn't require any copy
            determinant == 0.0 -> null
            else               -> {
                val cofactors = values.mapIndexed { r, values ->
                    values.mapIndexed { c, _ ->
                        determinant(r, c) * when {
                            (r + c).isOdd -> -1
                            else          ->  1
                        }
                    }.toTypedArray()
                }.toTypedArray().let {
                    val l: (Int, Int) -> Double = { row, col -> it[row][col] }
                    squareMatrixOf(numRows, l)
                }

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
        val subData = values.filterIndexed { r, _ -> r != row }.map { it.filterIndexed { c, _ -> c != col }.map { it.toDouble() }.toTypedArray() }.toTypedArray()

        return SquareMatrix(subData).determinant
    }
}

/**
 * A matrix used to represent a 2D Affine Transformation. It is of the form
 *
 * ```
 *
 * |m00 m01 m02 m03|
 * |m10 m11 m12 m13|
 * |m20 m21 m22 m23|
 *
 * ```
 *
 * @constructor creates a new instance
 */
internal class AffineMatrix3D @Internal constructor(values: Array<Array<Double>>): SquareMatrix<Double>(values) {
    override operator fun get(row: Int, col: Int): Double = when {
        row < numRows && col < numColumns -> super.get(row, col)
        row == col                        -> 1.0
        else                              -> 0.0
    }

    internal fun get(row: Int, col: Int, is3d: Boolean): Double {
        var nCol = col

        if (is3d && numColumns == 3 && row < 2) {
            nCol = if (col == 2) 3 else if (col == 3) 2 else col
        }

        return this[row, nCol]
    }

    companion object {
        operator fun invoke(
            m00: Double, m01: Double, m02: Double,
            m10: Double, m11: Double, m12: Double): AffineMatrix3D = AffineMatrix3D(
            arrayOf(
            arrayOf(m00, m01, m02),
            arrayOf(m10, m11, m12),
            arrayOf(0.0, 0.0, 1.0))
        )

        operator fun invoke(
            m00: Double, m01: Double, m02: Double, m03: Double,
            m10: Double, m11: Double, m12: Double, m13: Double,
            m20: Double, m21: Double, m22: Double, m23: Double): AffineMatrix3D = AffineMatrix3D(
            arrayOf(
                arrayOf(m00, m01, m02, m03),
                arrayOf(m10, m11, m12, m13),
                arrayOf(m20, m21, m22, m23),
                arrayOf(0.0, 0.0, 0.0, 1.0))
        )
    }
}

/**
 * Creates an NxN [SquareMatrix], where N == [size].
 *
 * @param size of N
 * @param init operation to get each value at [row, col]
 */
@JvmName("squareMatrixOfI") public fun squareMatrixOf(size: Int, init: (row: Int, col: Int) -> Int): SquareMatrix<Int> = SquareMatrix(Array(size) { row -> Array(size) { col -> init(row, col) } })

/**
 * Creates an NxN [SquareMatrix], where N == [size].
 *
 * @param size of N
 * @param init operation to get each value at [row, col]
 */
@JvmName("squareMatrixOfF") public fun squareMatrixOf(size: Int, init: (row: Int, col: Int) -> Float): SquareMatrix<Float> = SquareMatrix(Array(size) { row -> Array(size) { col -> init(row, col) } })

/**
 * Creates an NxN [SquareMatrix], where N == [size].
 *
 * @param size of N
 * @param init operation to get each value at [row, col]
 */
@JvmName("squareMatrixOfD") public fun squareMatrixOf(size: Int, init: (row: Int, col: Int) -> Double): SquareMatrix<Double> = SquareMatrix(Array(size) { row -> Array(size) { col -> init(row, col) } })

/**
 * Creates an NxN [SquareMatrix], where N == [size].
 *
 * @param size of N
 * @param init operation to get each value at [row, col]
 */
@JvmName("squareMatrixOfL") public fun squareMatrixOf(size: Int, init: (row: Int, col: Int) -> Long): SquareMatrix<Long> = SquareMatrix(Array(size) { row -> Array(size) { col -> init(row, col) } })

/**
 * Creates an NxM [Matrix], where N == [rows] and M == [cols].
 *
 * @param rows of the matrix
 * @param cols of the matrix
 * @param init operation to get each value at [row, col]
 */
@JvmName("matrixOfI") public fun matrixOf(rows: Int, cols: Int, init: (row: Int, col: Int) -> Int): Matrix<Int> = when {
    rows != cols -> MatrixImpl(Array(rows) { row -> Array(cols) { col -> init(row, col) } })
    else         -> squareMatrixOf(rows, init)
}

/**
 * Creates an NxM [Matrix], where N == [rows] and M == [cols].
 *
 * @param rows of the matrix
 * @param cols of the matrix
 * @param init operation to get each value at [row, col]
 */
@JvmName("matrixOfD") public fun matrixOf(rows: Int, cols: Int, init: (row: Int, col: Int) -> Double): Matrix<Double> = when {
    rows != cols -> MatrixImpl(Array(rows) { row -> Array(cols) { col -> init(row, col) } })
    else         -> squareMatrixOf(rows, init)
}

/**
 * Creates an NxM [Matrix], where N == [rows] and M == [cols].
 *
 * @param rows of the matrix
 * @param cols of the matrix
 * @param init operation to get each value at [row, col]
 */
@JvmName("matrixOfF") public fun matrixOf(rows: Int, cols: Int, init: (row: Int, col: Int) -> Float): Matrix<Float> = when {
    rows != cols -> MatrixImpl(Array(rows) { row -> Array(cols) { col -> init(row, col) } })
    else         -> squareMatrixOf(rows, init)
}

/**
 * Creates an NxM [Matrix], where N == [rows] and M == [cols].
 *
 * @param rows of the matrix
 * @param cols of the matrix
 * @param init operation to get each value at [row, col]
 */
@JvmName("matrixOfL") public fun matrixOf(rows: Int, cols: Int, init: (row: Int, col: Int) -> Long): Matrix<Long> = when {
    rows != cols -> MatrixImpl(Array(rows) { row -> Array(cols) { col -> init(row, col) } })
    else         -> squareMatrixOf(rows, init)
}

/**
 * Gives the [transposition](https://en.wikipedia.org/wiki/Transpose) of a [SquareMatrix].
 */
public fun <T: Number> SquareMatrix<T>.transpose(): SquareMatrix<T> {
    val values = this.values.copyOf() // shallow copy

    for (row in 0 until numRows) {
        values[row] = this.values[row].copyOf() // need to make copies of each row as well

        for (col in 0 until numColumns) {
            values[row][col] = this[col, row]
        }
    }

    return SquareMatrix(values)
}

/**
 * [Matrix multiplication](https://en.wikipedia.org/wiki/Matrix_multiplication) of two [Matrix]es.
 */
public operator fun Matrix<Double>.times(other: Matrix<Double>): Matrix<Double> {
    require (other.numRows == numColumns) { "matrix column and row counts do not match" }

    val values = Array(numRows) { Array(other.numColumns) { 0.0 } }

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

    val values = Array(numRows) { Array(other.numColumns) { 0.0 } }

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

    val values = Array(numRows) { Array(other.numColumns) { 0.0 } }

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
@JvmName("timesII") public operator fun Int.times(value: SquareMatrix<Int>   ): SquareMatrix<Int>    { val l: (Int   ) -> Int    = { this * it }; return value.map(l) }
@JvmName("timesIF") public operator fun Int.times(value: SquareMatrix<Float> ): SquareMatrix<Float>  { val l: (Float ) -> Float  = { this * it }; return value.map(l) }
@JvmName("timesID") public operator fun Int.times(value: SquareMatrix<Double>): SquareMatrix<Double> { val l: (Double) -> Double = { this * it }; return value.map(l) }
@JvmName("timesIL") public operator fun Int.times(value: SquareMatrix<Long>  ): SquareMatrix<Long>   { val l: (Long  ) -> Long   = { this * it }; return value.map(l) }

@JvmName("timesFI") public operator fun Float.times(value: SquareMatrix<Int>   ): SquareMatrix<Float>  { val l: (Int   ) -> Float  = { this * it }; return value.map(l) }
@JvmName("timesFF") public operator fun Float.times(value: SquareMatrix<Float> ): SquareMatrix<Float>  { val l: (Float ) -> Float  = { this * it }; return value.map(l) }
@JvmName("timesFD") public operator fun Float.times(value: SquareMatrix<Double>): SquareMatrix<Double> { val l: (Double) -> Double = { this * it }; return value.map(l) }
@JvmName("timesFL") public operator fun Float.times(value: SquareMatrix<Long>  ): SquareMatrix<Float>  { val l: (Long  ) -> Float  = { this * it }; return value.map(l) }

@JvmName("timesDI") public operator fun Double.times(value: SquareMatrix<Int>   ): SquareMatrix<Double> { val l: (Int   ) -> Double = { this * it }; return value.map(l) }
@JvmName("timesDF") public operator fun Double.times(value: SquareMatrix<Float> ): SquareMatrix<Double> { val l: (Float ) -> Double = { this * it }; return value.map(l) }
@JvmName("timesDD") public operator fun Double.times(value: SquareMatrix<Double>): SquareMatrix<Double> { val l: (Double) -> Double = { this * it }; return value.map(l) }
@JvmName("timesDL") public operator fun Double.times(value: SquareMatrix<Long>  ): SquareMatrix<Double> { val l: (Long  ) -> Double = { this * it }; return value.map(l) }

@JvmName("timesLI") public operator fun Long.times(value: SquareMatrix<Int>   ): SquareMatrix<Long>   { val l: (Int   ) -> Long   = { this * it }; return value.map(l) }
@JvmName("timesLF") public operator fun Long.times(value: SquareMatrix<Float> ): SquareMatrix<Float>  { val l: (Float ) -> Float  = { this * it }; return value.map(l) }
@JvmName("timesLD") public operator fun Long.times(value: SquareMatrix<Double>): SquareMatrix<Double> { val l: (Double) -> Double = { this * it }; return value.map(l) }
@JvmName("timesLL") public operator fun Long.times(value: SquareMatrix<Long>  ): SquareMatrix<Long>   { val l: (Long  ) -> Long   = { this * it }; return value.map(l) }

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
@JvmName("timesII") public inline operator fun SquareMatrix<Int>.times(value: Int   ): SquareMatrix<Int>    = value * this
@JvmName("timesIF") public inline operator fun SquareMatrix<Int>.times(value: Float ): SquareMatrix<Float>  = value * this
@JvmName("timesID") public inline operator fun SquareMatrix<Int>.times(value: Double): SquareMatrix<Double> = value * this
@JvmName("timesIL") public inline operator fun SquareMatrix<Int>.times(value: Long  ): SquareMatrix<Long>   = value * this

@JvmName("timesFI") public inline operator fun SquareMatrix<Float>.times(value: Int   ): SquareMatrix<Float>  = value * this
@JvmName("timesFF") public inline operator fun SquareMatrix<Float>.times(value: Float ): SquareMatrix<Float>  = value * this
@JvmName("timesFD") public inline operator fun SquareMatrix<Float>.times(value: Double): SquareMatrix<Double> = value * this
@JvmName("timesFL") public inline operator fun SquareMatrix<Float>.times(value: Long  ): SquareMatrix<Float>  = value * this

@JvmName("timesDI") public inline operator fun SquareMatrix<Double>.times(value: Int   ): SquareMatrix<Double> = value * this
@JvmName("timesDF") public inline operator fun SquareMatrix<Double>.times(value: Float ): SquareMatrix<Double> = value * this
@JvmName("timesDD") public inline operator fun SquareMatrix<Double>.times(value: Double): SquareMatrix<Double> = value * this
@JvmName("timesDL") public inline operator fun SquareMatrix<Double>.times(value: Long  ): SquareMatrix<Double> = value * this

@JvmName("timesLI") public inline operator fun SquareMatrix<Long>.times(value: Int   ): SquareMatrix<Long>   = value * this
@JvmName("timesLF") public inline operator fun SquareMatrix<Long>.times(value: Float ): SquareMatrix<Float>  = value * this
@JvmName("timesLD") public inline operator fun SquareMatrix<Long>.times(value: Double): SquareMatrix<Double> = value * this
@JvmName("timesLL") public inline operator fun SquareMatrix<Long>.times(value: Long  ): SquareMatrix<Long>   = value * this

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

    val values = Array(numRows) { Array(other.numColumns) { 0.0 } }

    for (c2 in 0 until other.numColumns) {
        for (r1 in 0 until numRows) {
            val sum = (0 until other.numRows).sumOf { this[r1, it] * other[it, c2] }

            values[r1][c2] = sum
        }
    }

    return SquareMatrix(values)
}

internal operator fun AffineMatrix3D.times(value: Number): AffineMatrix3D = value.toDouble().let {
    when (numColumns) {
        4 -> AffineMatrix3D(
            this[0, 0] * it, this[0, 1] * it, this[0, 2] * it, this[0, 3] * it,
            this[1, 0] * it, this[1, 1] * it, this[1, 2] * it, this[1, 3] * it,
            this[2, 0] * it, this[2, 1] * it, this[2, 2] * it, this[2, 3] * it
        )
        else -> AffineMatrix3D(
            this[0, 0] * it, this[0, 1] * it, this[0, 2] * it,
            this[1, 0] * it, this[1, 1] * it, this[1, 2] * it
        )
    }
}

internal operator fun AffineMatrix3D.div(value: Number): AffineMatrix3D = value.toDouble().let {
    when (numColumns) {
        4 -> AffineMatrix3D(
            this[0, 0] / it, this[0, 1] / it, this[0, 2] / it, this[0, 3] / it,
            this[1, 0] / it, this[1, 1] / it, this[1, 2] / it, this[1, 3] / it,
            this[2, 0] / it, this[2, 1] / it, this[2, 2] / it, this[2, 3] / it
        )
        else -> AffineMatrix3D(
            this[0, 0] / it, this[0, 1] / it, this[0, 2] * it,
            this[1, 0] / it, this[1, 1] / it, this[1, 2] * it
        )
    }
}

/**
 * @see plus
 */
internal operator fun AffineMatrix3D.plus(other: AffineMatrix3D): AffineMatrix3D {
    val is3d = numColumns != other.numColumns

    return AffineMatrix3D(Array(numRows) { row ->
        Array(numColumns) { column ->
            this.get(row, column, is3d) + other.get(row, column, is3d)
        }
    })
}

/**
 * @see minus
 */
internal operator fun AffineMatrix3D.minus(other: AffineMatrix3D): AffineMatrix3D {
    val is3d = numColumns != other.numColumns

    return AffineMatrix3D(Array(numRows) { row ->
        Array(numColumns) { column ->
            this.get(row, column, is3d) - other.get(row, column, is3d)
        }
    })
}

/**
 * @see times
 */
internal operator fun AffineMatrix3D.times(other: AffineMatrix3D): AffineMatrix3D {
    if (other.isIdentity) {
        return this
    }

    if (this.isIdentity) {
        return other
    }

    val is3d       = this.numColumns != other.numColumns
    val numRows    = max(this.numRows,    other.numRows   )
    val numColumns = max(this.numColumns, other.numColumns)

    val values = Array(size = (numRows - 1) * numColumns) { 0.0 }

    var i = 0

    for (r1 in 0 until numRows - 1) {
        for (c2 in 0 until numColumns) {
            values[i++] = (0 until numRows).sumOf { this.get(r1, it, is3d) * other.get(it, c2, is3d) }
        }
    }

    return when (values.size) {
        6 -> AffineMatrix3D(values[0], values[1], values[2],
                            values[3], values[4], values[5])
        else -> AffineMatrix3D(values[0], values[1], values[ 2], values[ 3],
                               values[4], values[5], values[ 6], values[ 7],
                               values[8], values[9], values[10], values[11])
    }
}

/**
 * Creates a new [SquareMatrix] whose members are the [transform] of those in this one.
 *
 * @param transform operation to map elements
 */
@JvmName("mapII") public fun SquareMatrix<Int>.map(transform: (Int) -> Int   ): SquareMatrix<Int>    = SquareMatrix(values.map { it.map { transform(it) }.toTypedArray() }.toTypedArray())
@JvmName("mapID") public fun SquareMatrix<Int>.map(transform: (Int) -> Double): SquareMatrix<Double> = SquareMatrix(values.map { it.map { transform(it) }.toTypedArray() }.toTypedArray())
@JvmName("mapIF") public fun SquareMatrix<Int>.map(transform: (Int) -> Float ): SquareMatrix<Float>  = SquareMatrix(values.map { it.map { transform(it) }.toTypedArray() }.toTypedArray())
@JvmName("mapIL") public fun SquareMatrix<Int>.map(transform: (Int) -> Long  ): SquareMatrix<Long>   = SquareMatrix(values.map { it.map { transform(it) }.toTypedArray() }.toTypedArray())

@JvmName("mapDI") public fun SquareMatrix<Double>.map(transform: (Double) -> Int   ): SquareMatrix<Int>    = SquareMatrix(values.map { it.map { transform(it) }.toTypedArray() }.toTypedArray())
@JvmName("mapDD") public fun SquareMatrix<Double>.map(transform: (Double) -> Double): SquareMatrix<Double> = SquareMatrix(values.map { it.map { transform(it) }.toTypedArray() }.toTypedArray())
@JvmName("mapDF") public fun SquareMatrix<Double>.map(transform: (Double) -> Float ): SquareMatrix<Float>  = SquareMatrix(values.map { it.map { transform(it) }.toTypedArray() }.toTypedArray())
@JvmName("mapDL") public fun SquareMatrix<Double>.map(transform: (Double) -> Long  ): SquareMatrix<Long>   = SquareMatrix(values.map { it.map { transform(it) }.toTypedArray() }.toTypedArray())

@JvmName("mapFI") public fun SquareMatrix<Float>.map(transform: (Float) -> Int   ): SquareMatrix<Int>    = SquareMatrix(values.map { it.map { transform(it) }.toTypedArray() }.toTypedArray())
@JvmName("mapFD") public fun SquareMatrix<Float>.map(transform: (Float) -> Double): SquareMatrix<Double> = SquareMatrix(values.map { it.map { transform(it) }.toTypedArray() }.toTypedArray())
@JvmName("mapFF") public fun SquareMatrix<Float>.map(transform: (Float) -> Float ): SquareMatrix<Float>  = SquareMatrix(values.map { it.map { transform(it) }.toTypedArray() }.toTypedArray())
@JvmName("mapFL") public fun SquareMatrix<Float>.map(transform: (Float) -> Long  ): SquareMatrix<Long>   = SquareMatrix(values.map { it.map { transform(it) }.toTypedArray() }.toTypedArray())

@JvmName("mapLI") public fun SquareMatrix<Long>.map(transform: (Long) -> Int   ): SquareMatrix<Int>    = SquareMatrix(values.map { it.map { transform(it) }.toTypedArray() }.toTypedArray())
@JvmName("mapLD") public fun SquareMatrix<Long>.map(transform: (Long) -> Double): SquareMatrix<Double> = SquareMatrix(values.map { it.map { transform(it) }.toTypedArray() }.toTypedArray())
@JvmName("mapLF") public fun SquareMatrix<Long>.map(transform: (Long) -> Float ): SquareMatrix<Float>  = SquareMatrix(values.map { it.map { transform(it) }.toTypedArray() }.toTypedArray())
@JvmName("mapLL") public fun SquareMatrix<Long>.map(transform: (Long) -> Long  ): SquareMatrix<Long>   = SquareMatrix(values.map { it.map { transform(it) }.toTypedArray() }.toTypedArray())

/**
 * Like [map], but with [col, row] given for each item during tranformation.
 */
@JvmName("mapIndexedI") public fun SquareMatrix<Int   >.mapIndexed(transform: (col: Int, row: Int, Int   ) -> Int   ): SquareMatrix<Int>    = SquareMatrix(values.mapIndexed { row, rows -> rows.mapIndexed { col, value -> transform(col, row, value) }.toTypedArray() }.toTypedArray())
@JvmName("mapIndexedD") public fun SquareMatrix<Double>.mapIndexed(transform: (col: Int, row: Int, Double) -> Double): SquareMatrix<Double> = SquareMatrix(values.mapIndexed { row, rows -> rows.mapIndexed { col, value -> transform(col, row, value) }.toTypedArray() }.toTypedArray())
@JvmName("mapIndexedF") public fun SquareMatrix<Float >.mapIndexed(transform: (col: Int, row: Int, Float ) -> Float ): SquareMatrix<Float>  = SquareMatrix(values.mapIndexed { row, rows -> rows.mapIndexed { col, value -> transform(col, row, value) }.toTypedArray() }.toTypedArray())
@JvmName("mapIndexedL") public fun SquareMatrix<Long  >.mapIndexed(transform: (col: Int, row: Int, Long  ) -> Long  ): SquareMatrix<Long>   = SquareMatrix(values.mapIndexed { row, rows -> rows.mapIndexed { col, value -> transform(col, row, value) }.toTypedArray() }.toTypedArray())

/**
 * An (NxM) [Matrix].
 *
 * @constructor creates a new matrix from numbers
 * @param values must be a square 2d array of values
 */
public open class MatrixImpl<T: Number> internal constructor(internal val values: Array<Array<T>>): Matrix<T> {

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