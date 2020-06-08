package io.nacular.doodle.geometry

import io.nacular.doodle.geometry.Point.Companion.Origin
import io.nacular.doodle.layout.Insets
import kotlin.math.max


/**
 * A rectangular [ConvexPolygon] with 4 sides at right-angles.
 *
 * @author Nicholas Eddy
 *
 * ```
 *        x
 *(0,0) +-->|
 *      |
 *   y  v
 *      -   +--------------------------+
 *          |                          |
 *          |                          |
 *          |                          |
 *          |                          | height
 *          |                          |
 *          |                          |
 *          |                          |
 *          +--------------------------+
 *                     width
 *```
 *
 * @constructor
 * @property position The top-left corner (x,y)
 * @property size The width-height
 */
class Rectangle(val position: Point = Origin, val size: Size = Size.Empty): ConvexPolygon() {

    /** Creates a Rectangle at the [Origin]: `[0, 0, width, height]` */
    constructor(width: Int, height: Int = width): this(Origin, Size(width, height))

    /** Creates a Rectangle at the [Origin]: `[0, 0, width, height]` */
    constructor(width: Float, height: Float = width): this(Origin, Size(width, height))

    /** Creates a Rectangle at the [Origin]: `[0, 0, width, height]` */
    constructor(width: Double, height: Double = width): this(Origin, Size(width, height))

    /** Creates a Rectangle */
    constructor(x: Int = 0, y: Int = 0, width: Int = 0, height: Int = 0): this(Point(x, y), Size(width, height))

    /** Creates a Rectangle */
    constructor(x: Double = 0.0, y: Double = 0.0, width: Double = 0.0, height: Double = 0.0): this(Point(x, y), Size(width, height))

    /** Creates a Rectangle */
    constructor(x: Float = 0f, y: Float = 0f, width: Float = 0f, height: Float = 0f): this(Point(x, y), Size(width, height))

    /** Left edge */
    val x get() = position.x

    /** Top edge */
    val y get() = position.y

    /** Horizontal extent */
    val width  get() = size.width

    /** Vertical extent */
    val height get() = size.height

    /**
     * Bottom edge
     *
     * ```
     *(0,0)   |
     *        |
     *        |
     *        |   +--------------------------+
     *        |   |                          |
     *        |   |                          |
     * bottom |   |                          |
     *        |   |                          |
     *        |   |                          |
     *        |   |                          |
     *        v   |                          |
     *        -   +--------------------------+
     *```
     */
    val bottom get() = y + height

    /**
     * Right edge
     *
     * ```
     *                    right
     *(0,0) ------------------------------>|
     *
     *          +--------------------------+
     *          |                          |
     *          |                          |
     *          |                          |
     *          |                          |
     *          |                          |
     *          |                          |
     *          |                          |
     *          +--------------------------+
     *```
     */
    val right get() = x + width

    /**
     * Point at the center of the rectangle
     *
     * ```
     *                      cx
     *(0,0) ---------------->|
     *      |
     *      |    +--------------------------+
     *      |    |                          |
     *      |    |                          |
     *      V    |                          |
     *   cy -    |            C             |
     *           |                          |
     *           |                          |
     *           |                          |
     *           +--------------------------+
     *```
     */
    val center get() = position + Point(width / 2, height / 2)

    override val points by lazy {
        listOf(position, Point(x+width, y), Point(x+width, y+height), Point(x, y+height))
    }

    override val area              get() = size.area
    override val empty             get() = size.empty
    override val boundingRectangle get() = this

    @Suppress("PrivatePropertyName")
    private val hashCode_ by lazy { 31 * position.hashCode() + size.hashCode() }

    /**
     * Returns a Rectangle that is the intersection of this and the given one.  The result is [Empty] if there is no intersection.
     *
     * @param rectangle to intersect with
     * @return a Rectangle representing the intersection of the 2 rectangles
     */
    infix fun intersect(rectangle: Rectangle): Rectangle {
        if (rectangle === this) {
            return this
        }

        val vertical        = y..bottom
        val horizontal      = x..right
        val otherVertical   = rectangle.y..rectangle.bottom
        val otherHorizontal = rectangle.x..rectangle.right

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
            right in otherHorizontal      -> right
            rectangle.right in horizontal -> rectangle.right
            else                          -> 0.0
        }

        val y2 = when {
            bottom in otherVertical      -> bottom
            rectangle.bottom in vertical -> rectangle.bottom
            else                         -> 0.0
        }

        return Rectangle(x1, y1, x2 - x1, y2 - y1)
    }

    /** Rectangle with the same width/height but positioned at 0,0 */
    val atOrigin: Rectangle get() = at(0.0, 0.0)

    /**
     * Returns a rectangle with the same width/height but positioned at the given x,y
     *
     * @return adjusted Rectangle
     */
    fun at(x: Double = this.x, y: Double = this.y) = if (this.x != x || this.y != y) Rectangle(x, y, width, height) else this

    /**
     * Returns a rectangle with the same width/height but positioned at the given point
     *
     * @return adjusted Rectangle
     */
    infix fun at(position: Point) = at(position.x, position.y)

    /**
     * Rectangle that has been adjusted as follows `[x + i, y + i, w - 2i, h - 2i]`, where `i` is the inset
     *
     * @param inset amount to resize by
     * @return adjusted Rectangle
     */
    infix fun inset(inset: Double) = if (inset == 0.0) this else Rectangle(x + inset, y + inset, max(0.0, width - inset * 2), max(0.0, height - inset * 2))

    /**
     * Rectangle that has been adjusted as follows `[x + left, y + top, w - (left + right), h - (top + bottom)]`, where
     * the adjustments are from the inset
     *
     * @param inset amount to resize by
     * @return adjusted Rectangle
     */
    infix fun inset(inset: Insets) = Rectangle(x + inset.left, y + inset.top, max(0.0, width  - (inset.left + inset.right)), max(0.0, height - (inset.top + inset.bottom)))

    /**
     * Checks whether the given point is within the boundaries of this Rectangle
     *
     * @return `true` IFF the given point falls within the boundaries of this Rectangle
     */
    override operator fun contains(point: Point) = area > 0 && point.x in x..right && point.y in y..bottom

    /** @return ```true``` IFF the given rectangle falls within the boundaries of this Polygon */
    override fun contains(rectangle: Rectangle) = rectangle.position in this && Point(rectangle.right, rectangle.bottom) in this

    /**
     * Checks whether the given rectangle intersects this one
     *
     * @return `true` IFF the given rectangle intersects this Rectangle
     */
    override fun intersects(rectangle: Rectangle) = !(empty                      ||
                                                      rectangle.empty            ||
                                                      x      >= rectangle.right  ||
                                                      y      >= rectangle.bottom ||
                                                      right  <= rectangle.x      ||
                                                      bottom <= rectangle.y)

    override fun toString() = "[$x,$y,$width,$height]"

    override fun equals(other: Any?): Boolean {
        if (this === other     ) return true
        if (other !is Rectangle) return false

        if (position != other.position) return false
        if (size     != other.size    ) return false

        return true
    }

    override fun hashCode() = hashCode_

    companion object {
        /** The rectangle at the [Origin] with [width] and [height] equal to `0` */
        val Empty = Rectangle()
    }
}