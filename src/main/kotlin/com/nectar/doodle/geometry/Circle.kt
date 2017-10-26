package com.nectar.doodle.geometry


import com.nectar.doodle.geometry.Point.Companion.Origin
import kotlin.math.pow
import kotlin.math.sqrt

class Circle(center: Point, radius: Double): Ellipse(center, radius, radius) {

    constructor(radius: Double): this(Point.Origin, radius)

    val radius = xRadius

    override fun contains(point: Point): Boolean {
        val fromCenter = point - center

        return sqrt(fromCenter.x.pow(2) + fromCenter.y.pow(2)) <= radius
    }

    fun at(point: Point) = when(center) {
        point -> this
        else  -> Circle(point, radius)
    }

    fun atOrigin() = when(center) {
        Origin -> this
        else   -> Circle(radius)
    }

    fun inset(aInset: Double) = Circle(center, radius - aInset)

    companion object {
        val Unit = Circle(1.0)
    }
}
