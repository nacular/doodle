package com.nectar.doodle.geometry

import com.nectar.doodle.geometry.Point.Companion.Origin
import com.nectar.doodle.layout.Insets
import kotlin.math.max


class Rectangle constructor(val position: Point = Origin, val size: Size = Size.Empty): Shape {

    constructor(x: Double = 0.0, y: Double = 0.0, width: Double, height: Double): this(Point(x, y), Size(width, height))

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

        return Rectangle(x1, y1, x2 - x1, y2 - y1)
    }

    /**
     * @return a Rectangle with the same width/height but positioned at 0,0
     */
    fun atOrigin(): Rectangle = at(0.0, 0.0)

    /**
     * @return a Rectangle with the same width/height but positioned at the given x,y
     */
    fun at(x: Double = this.x, y: Double = this.y) = if (this.x != x || this.y != y) Rectangle(x, y, width, height) else this

    /**
     * @return a Rectangle with the same width/height but positioned at the given point
     */
    fun at(position: Point) = at(position.x, position.y)

    /**
     * @param inset amount to resize by
     * @return a Rectangle that has been adjusted as follows [x + i, y + i, w - 2i, h - 2i], where i is the inset
     */
    fun inset(inset: Double) = if (inset == 0.0) this else Rectangle(x + inset, y + inset, max(0.0, width - inset * 2), max(0.0, height - inset * 2))

    /**
     * @param inset amount to resize by
     * @return a Rectangle that has been adjusted as follows [x + left, y + top, w - (left + right), h - (top + bottom)], where
     * the adjustments are from the inset
     */
    fun inset(inset: Insets) = Rectangle(x + inset.left, y + inset.top, max(0.0, width  - (inset.left + inset.right)), max(0.0, height - (inset.top + inset.bottom)))

    override operator fun contains(point: Point) = point.x in x..x + width && point.y in y..y + height

    override operator fun contains(rectangle: Rectangle) = contains(rectangle.position) && contains(rectangle.position + Point(rectangle.width, rectangle.height))

    override fun intersects(rectangle: Rectangle): Boolean {
        return !(x > rectangle.x + rectangle.width  ||
                 y > rectangle.y + rectangle.height ||
                 x + width  < rectangle.x ||
                 y + height < rectangle.y)
    }

    override fun toString() = "[$x,$y,$width,$height]"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Rectangle) return false

        if (position != other.position) return false
        if (size     != other.size    ) return false

        return true
    }

    override fun hashCode(): Int {
        var result = position.hashCode()
        result = 31 * result + size.hashCode()
        return result
    }

    companion object {
        val Empty = Rectangle()
    }
}