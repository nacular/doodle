package com.nectar.doodle.geometry


import com.nectar.doodle.geometry.Point.Companion.Origin

class Circle(center: Point, radius: Double) : Ellipse(center, radius, radius) {

    constructor(radius: Double): this(Point.Origin, radius)

    val radius = xRadius

    fun atOrigin() = when(center) {
        Origin -> this
        else   -> Circle(radius)
    }

    fun inset(aInset: Double) = Circle(center, radius - aInset)

    companion object {
        val Unit = Circle(1.0)
    }
}
