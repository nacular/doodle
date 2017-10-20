package com.zinoti.jaz.geometry

import com.zinoti.jaz.geometry.Point.Companion.ORIGIN
import kotlin.math.PI


open class Ellipse protected constructor(val center: Point, val xRadius: Double, val yRadius: Double): Shape {

    init {
        require(xRadius >= 0) { "x-radius must be >= 0" }
        require(yRadius >= 0) { "y-radius must be >= 0" }
    }

    override val boundingRectangle = Rectangle.create(center.x - xRadius, center.y - yRadius, 2 * xRadius, 2 * yRadius)

    override val area = PI * xRadius * yRadius

    override val empty = xRadius == 0.0 || yRadius == 0.0

    override fun contains(point: Point) = false // TODO: IMPLEMENT

    override fun contains(rectangle: Rectangle) = false // TODO: IMPLEMENT

    override fun intersects(rectangle: Rectangle) = false // TODO: IMPLEMENT

    companion object {
        fun create() = UNIT

        fun create(xRadius: Double, yRadius: Double) = create(ORIGIN, xRadius, yRadius)

        fun create(center: Point, xRadius: Double, yRadius: Double) = Ellipse(center, xRadius, yRadius)

        val UNIT = create(1.0, 1.0)
    }
}
