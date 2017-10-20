package com.zinoti.jaz.geometry

import kotlin.math.max


class Rectangle private constructor(val position: Point, val size : Size): Shape {

    private constructor(x: Double, y: Double, width: Double, height: Double): this(Point.create(x, y), Size.create(width, height))

    val x      get() = position.x
    val y      get() = position.y
    val width  get() = size.width
    val height get() = size.height

    override val area              = size.area
    override val empty             = size.empty
    override val boundingRectangle = this

    /**
     * @param rectangle
     * @return a Rectangle representing the intersection of the 2 rectangles
     */
    fun intersect(rectangle: Rectangle): Rectangle {
        if (rectangle === this) {
            return this
        }

        val vertical        = y..y+height
        val horizontal      = x..x+width
        val otherVertical   = rectangle.y..rectangle.y + rectangle.height
        val otherHorizontal = rectangle.x..rectangle.x + rectangle.width


        val x1 = when {
            otherHorizontal.contains(x)      -> x
            horizontal.contains(rectangle.x) -> rectangle.x
            else                             -> 0.0
        }

        val y1 = when {
            otherVertical.contains(y)      -> y
            vertical.contains(rectangle.y) -> rectangle.y
            else                           -> 0.0
        }

        val x2 = when {
            otherHorizontal.contains(x + width)                -> x + width
            horizontal.contains(rectangle.x + rectangle.width) -> rectangle.x + rectangle.width
            else                                               -> 0.0
        }

        val y2 = when {
            otherVertical.contains(y + height)                -> y + height
            vertical.contains(rectangle.y + rectangle.height) -> rectangle.y + rectangle.height
            else                                              -> 0.0
        }

        return Rectangle.create(x1, y1, x2 - x1, y2 - y1)
    }

    /**
     * @return a Rectangle with the same width/height but positioned at 0,0
     */
    fun atOrigin(): Rectangle = at(0.0, 0.0)

    /**
     * @return a Rectangle with the same width/height but positioned at the given x,y
     */
    fun at(aX: Double, aY: Double): Rectangle {
        return if (x != aX || y != aY) Rectangle.create(aX, aY, width, height) else this
    }

    /**
     * @param inset amount to resize by
     * @return a Rectangle that has been adjusted as follows [x + i, y + i, w + 2i, h + 2i], where i is the inset
     */
    fun inset(inset: Double) = Rectangle.create(x + inset, y + inset, max(0.0, width  - inset * 2), max(0.0, height - inset * 2))

    override operator fun contains(point: Point) = point.x in x..x + width && point.y in y..y + height

    override operator fun contains(rectangle: Rectangle) = contains(rectangle.position) && contains(rectangle.position + Point.create(rectangle.width, rectangle.height))

    override fun intersects(rectangle: Rectangle): Boolean {
        return !(x > rectangle.x + rectangle.width  ||
                 y > rectangle.y + rectangle.height ||
                 x + width  < rectangle.x ||
                 y + height < rectangle.y)
    }

    override fun toString() = "[$x,$y,$width,$height]"

    companion object {
        /** Creates a Rectangle with 0 width and height at point (0,0). */
        fun create() = EMPTY

        /**
         * Creates a Rectangle instance.
         *
         * @param width  The width
         * @param height The height
         */
        fun create(width: Double, height: Double) = Rectangle(0.0, 0.0, width, height)

        /**
         * Creates a Rectangle instance.
         *
         * @param x      The x
         * @param y      The y
         * @param width  The width
         * @param height The height
         */
        fun create(x: Double, y: Double, width: Double, height: Double) = Rectangle(x, y, width, height)


        val EMPTY = Rectangle(0.0, 0.0, 0.0, 0.0)
    }
}