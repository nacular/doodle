package io.nacular.doodle.geometry

import io.nacular.doodle.geometry.Point.Companion.Origin
import kotlin.math.PI
import kotlin.math.pow


open class Ellipse(val center: Point, val xRadius: Double, val yRadius: Double): Shape {

    constructor(xRadius: Double, yRadius: Double): this(Origin, xRadius, yRadius)

    init {
        require(xRadius >= 0) { "x-radius must be >= 0" }
        require(yRadius >= 0) { "y-radius must be >= 0" }
    }

    override val boundingRectangle by lazy { Rectangle(center.x - xRadius, center.y - yRadius, 2 * xRadius, 2 * yRadius) }

    override val area by lazy { PI * xRadius * yRadius }

    override val empty get() = area == 0.0

    private val xRadius2 by lazy { xRadius.pow(2) }
    private val yRadius2 by lazy { yRadius.pow(2) }

    override fun contains(point: Point) = (point - center).run { x.pow(2) / xRadius2 + y.pow(2) / yRadius2 <= 1 }

    override fun contains(rectangle: Rectangle) = rectangle.position in this && Point(rectangle.right, rectangle.bottom) in this

    override fun intersects(rectangle: Rectangle): Boolean = TODO("not implemented")

    companion object {
        val Unit  = Ellipse(1.0, 1.0)
        val Empty = Ellipse(0.0, 0.0)
    }
}
