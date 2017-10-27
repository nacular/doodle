package com.nectar.doodle.geometry

import kotlin.math.PI


open class Ellipse(val center: Point, val xRadius: Double, val yRadius: Double): Shape {

    constructor(xRadius: Double, yRadius: Double): this(Point.Origin, xRadius, yRadius)

    init {
        require(xRadius >= 0) { "x-radius must be >= 0" }
        require(yRadius >= 0) { "y-radius must be >= 0" }
    }

    override val boundingRectangle = Rectangle(center.x - xRadius, center.y - yRadius, 2 * xRadius, 2 * yRadius)

    override val area = PI * xRadius * yRadius

    override val empty = xRadius == 0.0 || yRadius == 0.0

    override fun contains(point: Point) = false // TODO: IMPLEMENT

    override fun contains(rectangle: Rectangle) = false // TODO: IMPLEMENT

    override fun intersects(rectangle: Rectangle) = false // TODO: IMPLEMENT

    companion object {
        val Unit = Ellipse(1.0, 1.0)
    }
}
