package io.nacular.doodle.geometry


import io.nacular.doodle.geometry.Point.Companion.Origin

/**
 * A circle defined by a center point and radius.
 *
 * @constructor creates a new Circle
 * @param center point of the circle
 * @param radius of the circle
 */
public class Circle(center: Point, radius: Double): Ellipse(center, radius, radius) {

    /**
     * Creates a Circle centered at the [Origin].
     *
     * @param radius of the circle
     */
    public constructor(radius: Double = 0.0): this(Origin, radius)

    /** The circle's radius */
    public val radius: Double get() = xRadius

    override fun toString(): String = "$center, $radius"

    public companion object {
        public val Unit: Circle  = Circle(1.0)
        public val Empty: Circle = Circle(   )
    }
}

/**
 * @return a Circle with the same attributes as this, but centered at the [Origin].
 */
public fun Circle.atOrigin(): Circle = at(Origin)

/**
 * @param point to center the returned circle at
 * @return a Circle with the same attributes as this, but centered at [point].
 */
public fun Circle.at(point: Point): Circle = when(center) {
    point -> this
    else  -> Circle(point, radius)
}

/**
 * @param radius of the returned circle
 * @return a Circle with the same attributes as this, but with the given radius.
 */
public fun Circle.withRadius(radius: Double): Circle = Circle(center, radius)

/**
 * @param inset to shorten radius by
 * @return a Circle with the same attributes as this, but with the radius shortened by [inset].
 */
public fun Circle.inset(inset: Double): Circle = Circle(center, radius - inset)