package com.zinoti.jaz.geometry


/**
 * Objects implementing this interface represent shapes.
 */

interface Shape {

    /**
     * Returns the Shape's bounding rectangle.
     *
     * @return The bounding rectangle
     */

    val boundingRectangle: Rectangle

    /**
     * Returns the Shape's area
     *
     * @return The Shape's area
     */

    val area: Double

    /**
     * Returns whether the Shape has an area equals to 0.
     *
     * @return true if the Shape's area is zero
     */

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

    fun intersects(rectangle: Rectangle): Boolean
}