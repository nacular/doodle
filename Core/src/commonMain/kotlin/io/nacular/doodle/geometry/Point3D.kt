package io.nacular.doodle.geometry

import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Created by Nicholas Eddy on 2/16/20.
 */
class Point3D(val x: Double = 0.0, val y: Double = 0.0, val z: Double = 0.0) {
    constructor(x: Int   = 0,  y: Int   = 0,  z: Int   = 0 ): this(x.toDouble(), y.toDouble(), z.toDouble())
    constructor(x: Float = 0f, y: Float = 0f, z: Float = 0f): this(x.toDouble(), y.toDouble(), z.toDouble())

    operator fun plus (other: Point3D) = Point3D(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Point3D) = Point3D(x - other.x, y - other.y, z - other.z)

    operator fun times(value: Int   ) = Point3D(x * value, y * value, z * value)
    operator fun div  (value: Int   ) = Point3D(x / value, y / value, z / value)
    operator fun times(value: Float ) = Point3D(x * value, y * value, z * value)
    operator fun div  (value: Float ) = Point3D(x / value, y / value, z / value)
    operator fun times(value: Double) = Point3D(x * value, y * value, z * value)
    operator fun div  (value: Double) = Point3D(x / value, y / value, z / value)

    operator fun unaryMinus() = Point3D(-x, -y, -z)

    infix fun distanceFrom(other: Point3D) = sqrt((x - other.x).pow(2) + (y - other.y).pow(2) + (z - other.z).pow(2))

    override fun hashCode() = hashCode_

    override fun toString() = "[$x,$y,$z]"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Point3D) return false

        return x == other.x && y == other.y && z == other.z
    }

    @Suppress("PrivatePropertyName")
    private val hashCode_ by lazy { arrayOf(x, y, z).contentHashCode() }

    companion object {
        /** Point at 0,0 */
        val Origin = Point3D(0, 0, 0)

        operator fun invoke(point: Point) = Point3D(point.x, point.y)
    }
}

operator fun Point3D.plus (other: Point) = this + Point3D(other)
operator fun Point3D.minus(other: Point) = this - Point3D(other)

val Point3D.twoD get() = Point(x, y)