package com.nectar.doodle.drawing

import com.nectar.doodle.geometry.Point
import com.nectar.doodle.units.Angle
import com.nectar.doodle.units.Measure
import com.nectar.doodle.units.radians
import com.nectar.doodle.utils.Matrix
import kotlin.math.cos
import kotlin.math.sin

class AffineTransform private constructor(private val matrix: Matrix) {

    /**
     * Creates a transform with the given properties
     *
     * @param scaleX     how much to scale the x direction
     * @param shearX     how much to shear the x direction
     * @param translateX how much to translate in the x direction
     * @param scaleY     how much to scale the y direction
     * @param shearY     how much to shear the y direction
     * @param translateY how much to translate in the y direction
     */
    constructor(
            scaleX    : Double = 1.0,
            shearX    : Double = 0.0,
            translateX: Double = 0.0,
            scaleY    : Double = 1.0,
            shearY    : Double = 0.0,
            translateY: Double = 0.0):
            this(Matrix(
                doubleArrayOf(scaleX, shearX, translateX),
                doubleArrayOf(shearY, scaleY, translateY),
                doubleArrayOf(   0.0,    0.0,        1.0)))

    val isIdentity       = matrix.isIdentity
    val translateX get() = matrix[0, 2]
    val translateY get() = matrix[1, 2]
    val scaleX     get() = matrix[0, 0]
    val scaleY     get() = matrix[1, 1]
    val shearX     get() = matrix[0, 1]
    val shearY     get() = matrix[1, 0]

    operator fun times(other: AffineTransform) = AffineTransform(matrix * other.matrix)

    fun scale(by: Point) = scale(by.x, by.y)

    fun scale(x: Double, y: Double) = AffineTransform(
            matrix * Matrix(this[  x, 0.0, 0.0],
                            this[0.0,   y, 0.0],
                            this[0.0, 0.0, 1.0]))

    fun translate(by: Point) = translate(by.x, by.y)

    fun translate(x: Double, y: Double) = AffineTransform(
            matrix * Matrix(this[1.0, 0.0,   x],
                            this[0.0, 1.0,   y],
                            this[0.0, 0.0, 1.0]))

    fun skew(by: Point) = skew(by.x, by.y)

    fun skew(x: Double, y: Double) = AffineTransform(
            matrix * Matrix(this[1.0,   x, 0.0],
                            this[  y, 1.0, 0.0],
                            this[0.0, 0.0, 1.0]))

    fun rotate(angle: Measure<Angle>): AffineTransform {
        val radians = angle.`in`(radians)
        val sin     = sin(radians)
        val cos     = cos(radians)

        return AffineTransform(
                matrix * Matrix(this[cos, -sin, 0.0],
                                this[sin,  cos, 0.0],
                                this[0.0,  0.0, 1.0]))
    }

    /**
     * Transforms the given set of points.
     *
     * @param points that will be transformed
     * @return a list of points transformed by this object
     */
    operator fun invoke(vararg points: Point): List<Point> {
        return points.map {
            val point = Matrix(doubleArrayOf(it.x), doubleArrayOf(it.y), doubleArrayOf(1.0))

            val product = matrix * point

            Point(product[0, 0], product[1, 0])
        }
    }

//    operator fun invoke(rectangle: Rectangle): Rectangle {
//        val points = rectangle.run { invoke(position, position + Point(width, height)) }
//
//        return Rectangle(points[0], (points[1] - points[0]).run { Size(x, y) })
//    }

    override fun toString() = matrix.toString()

    private operator fun get(vararg values: Double): DoubleArray = doubleArrayOf(*values)

    companion object {
        val Identity = AffineTransform()
    }
}
