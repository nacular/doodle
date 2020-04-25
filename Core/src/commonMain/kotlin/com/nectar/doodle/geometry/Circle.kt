package com.nectar.doodle.geometry


import com.nectar.doodle.geometry.Point.Companion.Origin

class Circle(center: Point, radius: Double): Ellipse(center, radius, radius) {

    constructor(radius: Double = 0.0): this(Origin, radius)

    val radius get() = xRadius

    fun at(point: Point) = when(center) {
        point -> this
        else  -> Circle(point, radius)
    }

    fun atOrigin() = at(Origin)

    fun inset(inset: Double) = Circle(center, radius - inset)

    fun withRadius(radius: Double) = Circle(center, radius)

    companion object {
        val Unit  = Circle(1.0)
        val Empty = Circle(   )
    }
}
