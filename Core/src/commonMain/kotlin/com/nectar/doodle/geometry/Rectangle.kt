package com.nectar.doodle.geometry

import com.nectar.doodle.geometry.Point.Companion.Origin
import com.nectar.doodle.layout.Insets
import kotlin.math.max


class Rectangle(val position: Point = Origin, val size: Size = Size.Empty): ConvexPolygon() {

    constructor(width: Double, height: Double = width): this(Origin, Size(width, height))
    constructor(x: Double = 0.0, y: Double = 0.0, width: Double = 0.0, height: Double = 0.0): this(Point(x, y), Size(width, height))

    constructor(width: Int, height: Int = width): this(Origin, Size(width, height))
    constructor(x: Int = 0, y: Int = 0, width: Int = 0, height: Int = 0): this(Point(x, y), Size(width, height))

    constructor(width: Float, height: Float = width): this(Origin, Size(width, height))
    constructor(x: Float = 0f, y: Float = 0f, width: Float = 0f, height: Float = 0f): this(Point(x, y), Size(width, height))

    val x      get() = position.x
    val y      get() = position.y
    val width  get() = size.width
    val height get() = size.height
    val bottom get() = y + height
    val right  get() = x + width

    override val points by lazy {
        listOf(Point(x, y), Point(x+width, y), Point(x+width, y+height), Point(x, y+height))
    }

    @Suppress("PrivatePropertyName")
    private val hashCode_ by lazy { 31 * position.hashCode() + size.hashCode() }

    override val area              get() = size.area
    override val empty             get() = size.empty
    override val boundingRectangle get() = this

    /**
     * @param rectangle
     * @return a Rectangle representing the intersection of the 2 rectangles
     */
    infix fun intersect(rectangle: Rectangle): Rectangle {
        if (rectangle === this) {
            return this
        }

        val vertical        = y..y+height
        val horizontal      = x..x+width
        val otherVertical   = rectangle.y..rectangle.y + rectangle.height
        val otherHorizontal = rectangle.x..rectangle.x + rectangle.width


        val x1 = when {
            x in otherHorizontal      -> x
            rectangle.x in horizontal -> rectangle.x
            else                      -> 0.0
        }

        val y1 = when {
            y in otherVertical      -> y
            rectangle.y in vertical -> rectangle.y
            else                    -> 0.0
        }

        val x2 = when {
            x + width in otherHorizontal                -> x + width
            rectangle.x + rectangle.width in horizontal -> rectangle.x + rectangle.width
            else                                        -> 0.0
        }

        val y2 = when {
            y + height in otherVertical                -> y + height
            rectangle.y + rectangle.height in vertical -> rectangle.y + rectangle.height
            else                                       -> 0.0
        }

        return Rectangle(x1, y1, x2 - x1, y2 - y1)
    }

    /**
     * @return a Rectangle with the same width/height but positioned at 0,0
     */
    val atOrigin: Rectangle get() = at(0.0, 0.0)

    /**
     * @return a Rectangle with the same width/height but positioned at the given x,y
     */
    fun at(x: Double = this.x, y: Double = this.y) = if (this.x != x || this.y != y) Rectangle(x, y, width, height) else this

    /**
     * @return a Rectangle with the same width/height but positioned at the given point
     */
    infix fun at(position: Point) = at(position.x, position.y)

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

    override operator fun contains(point: Point) = area > 0 && point.x in x..x + width && point.y in y..y + height

    override operator fun contains(rectangle: Rectangle) = contains(rectangle.position) && contains(rectangle.position + Point(rectangle.width, rectangle.height))

    override fun intersects(rectangle: Rectangle) = !(x          >= rectangle.x + rectangle.width  ||
                                                      y          >= rectangle.y + rectangle.height ||
                                                      x + width  <= rectangle.x                    ||
                                                      y + height <= rectangle.y                    ||
                                                      empty                                        ||
                                                      rectangle.empty)

    override fun toString() = "[$x,$y,$width,$height]"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Rectangle) return false

        if (position != other.position) return false
        if (size     != other.size    ) return false

        return true
    }

    override fun hashCode() = hashCode_

    companion object {
        val Empty = Rectangle()
    }
}