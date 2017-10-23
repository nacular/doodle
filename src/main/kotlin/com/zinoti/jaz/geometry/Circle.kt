package com.zinoti.jaz.geometry


import com.zinoti.jaz.geometry.Point.Companion.Origin

class Circle private constructor(center: Point, aRadius: Double) : Ellipse(center, aRadius, aRadius) {

    val radius = xRadius

    fun atOrigin() = when(center) {
        Origin -> this
        else   -> Circle.create(radius)
    }

    fun inset(aInset: Double): Circle = Circle.create(center, radius - aInset)

    companion object {
        fun create() = UNIT

        fun create(aRadius: Double) = create(Origin, aRadius)

        fun create(center: Point, radius: Double) = Circle(center, radius)

        val UNIT = create(1.0)
    }
}
