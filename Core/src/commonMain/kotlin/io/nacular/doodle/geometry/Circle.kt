package io.nacular.doodle.geometry


import io.nacular.doodle.geometry.Point.Companion.Origin

/**
 * A circle defined by a center point and radius.
 *
 * @constructor creates a new Circle
 * @param center point of the circle
 * @param radius of the circle
 */
class Circle(center: Point, radius: Double): Ellipse(center, radius, radius) {

    /**
     * Creates a Circle centered at the [Origin].
     *
     * @param radius of the circle
     */
    constructor(radius: Double = 0.0): this(Origin, radius)

    /** The circle's radius */
    val radius get() = xRadius

    override fun toString() = "$center, $radius"

    companion object {
        val Unit  = Circle(1.0)
        val Empty = Circle(   )
    }
}

/**
 * @return a Circle with the same attributes as this, but centered at the [Origin].
 */
fun Circle.atOrigin() = at(Origin)

/**
 * @param point to center the returned circle at
 * @return a Circle with the same attributes as this, but centered at [point].
 */
fun Circle.at(point: Point) = when(center) {
    point -> this
    else  -> Circle(point, radius)
}

/**
 * @param radius of the returned circle
 * @return a Circle with the same attributes as this, but with the given radius.
 */
fun Circle.withRadius(radius: Double) = Circle(center, radius)

/**
 * @param inset to shorten radius by
 * @return a Circle with the same attributes as this, but with the radius shortened by [inset].
 */
fun Circle.inset(inset: Double) = Circle(center, radius - inset)