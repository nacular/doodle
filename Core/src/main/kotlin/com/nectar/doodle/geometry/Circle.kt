package com.nectar.doodle.geometry


import com.nectar.doodle.geometry.Point.Companion.Origin
import kotlin.math.pow
import kotlin.math.sqrt

class Circle(center: Point, radius: Double): Ellipse(center, radius, radius) {

    constructor(radius: Double): this(Origin, radius)

    val radius get() = xRadius

    override fun contains(point: Point): Boolean {
        val fromCenter = point - center

        return sqrt(fromCenter.x.pow(2) + fromCenter.y.pow(2)) <= radius
    }

    fun at(point: Point) = when(center) {
        point -> this
        else  -> Circle(point, radius)
    }

    fun atOrigin() = at(Origin)

    fun inset(inset: Double) = Circle(center, radius - inset)

    companion object {
        val Unit = Circle(1.0)
    }
}
