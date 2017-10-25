package com.zinoti.jaz.drawing

import com.zinoti.jaz.geometry.Point
import com.zinoti.jaz.utils.Matrix
import kotlin.math.cos
import kotlin.math.sin

class AffineTransform private constructor(private val matrix: Matrix) {
    constructor(
            scaleX    : Double = 1.0,
            shearX    : Double = 0.0,
            translateX: Double = 0.0,
            scaleY    : Double = 1.0,
            shearY    : Double = 0.0,
            translateY: Double = 0.0):
            this(Matrix(arrayOf(
                doubleArrayOf(scaleX, shearX, translateX),
                doubleArrayOf(shearY, scaleY, translateY),
                doubleArrayOf(   0.0,    0.0,        1.0))))

    val isIdentity get() = matrix.isIdentity
    val translateX get() = matrix[0, 2]
    val translateY get() = matrix[1, 2]
    val scaleX     get() = matrix[0, 0]
    val scaleY     get() = matrix[1, 1]
    val shearX     get() = matrix[0, 1]
    val shearY     get() = matrix[1, 0]

    fun apply(transform: AffineTransform) = AffineTransform(matrix * transform.matrix)

    fun scale(by: Point) = scale(by.x, by.y)

    fun scale(x: Double, y: Double) = AffineTransform(
            matrix * Matrix(arrayOf(
                    this[  x, 0.0, 0.0],
                    this[0.0,   y, 0.0],
                    this[0.0, 0.0, 1.0])))

    fun translate(by: Point) = translate(by.x, by.y)

    fun translate(x: Double, y: Double) = AffineTransform(
            matrix * Matrix(arrayOf(
                    this[1.0, 0.0, x],
                    this[0.0, 1.0, y],
                    this[0.0, 0.0,         1.0])))

    fun skew(by: Point) = skew(by.x, by.y)

    fun skew(x: Double, y: Double) = AffineTransform(
            matrix * Matrix(arrayOf(
                    this[   1.0, x, 0.0],
                    this[y,    1.0, 0.0],
                    this[   0.0,    0.0, 1.0])))

    fun rotate(angle: Double): AffineTransform {
        val sin = sin(angle)
        val cos = cos(angle)

        return AffineTransform(
                matrix * Matrix(arrayOf(
                        this[cos, -sin, 0.0],
                        this[sin,  cos, 0.0],
                        this[0.0,  0.0, 1.0])))
    }

    fun transform(vararg points: Point): List<Point> {
        return points.map {
            val aPoint = Matrix(arrayOf(doubleArrayOf(it.x), doubleArrayOf(it.y), doubleArrayOf(1.0)))

            val product = matrix * aPoint

            Point.create(product[0, 0], product[1, 0])
        }
    }

    override fun toString() = matrix.toString()

    operator fun get(vararg values: Double): DoubleArray = doubleArrayOf(*values)

    companion object {
        fun create() = Identity

        val Identity = AffineTransform()
    }
}
