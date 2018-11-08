package com.nectar.doodle.geometry


/**
 * Objects implementing this interface represent shapes.
 */
interface Shape {
    /** @return The bounding rectangle */
    val boundingRectangle: Rectangle

    /** @return The Shape's area */
    val area: Double

    /** @return true if the Shape's area is zero */
    val empty: Boolean

    /**
     * Checks whether the Shape contains a given point.
     *
     * @param  point The point
     * @return true if the point is within the Shape
     */
    operator fun contains(point: Point): Boolean

    /**
     * Checks whether the Shape contains a given rectangle.
     *
     * @param  rectangle The rectangle
     * @return true if the rectangle is within the Shape
     */
    operator fun contains(rectangle: Rectangle): Boolean

    /**
     * Checks whether the Shape intersects a rectangle.
     *
     * @param  rectangle The rectangle
     * @return true if the Shape intersects the rectangle
     */
    infix fun intersects(rectangle: Rectangle): Boolean
}