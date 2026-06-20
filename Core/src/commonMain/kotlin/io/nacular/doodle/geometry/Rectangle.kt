package io.nacular.doodle.geometry

import io.nacular.doodle.geometry.Point.Companion.Origin
import io.nacular.doodle.geometry.Rectangle.Companion.Empty
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.utils.lerp
import kotlin.math.max
import kotlin.math.min


/**
 * A rectangular [ConvexPolygon] with 4 sides at right-angles.
 *
 * ```
 *        x
 *(0,0)  ──▶
 *      │
 *   y  ▼
 *          ┌──────────────────────────┐
 *          │                          │
 *          │                          │
 *          │                          │
 *          │                          │ height
 *          │                          │
 *          │                          │
 *          │                          │
 *          └──────────────────────────┘
 *                     width
 *```
 *
 * @author Nicholas Eddy
 * @constructor
 * @property x      The left edge
 * @property y      The top edge
 * @property width  The horizontal length
 * @property height The vertical length
 */
public class Rectangle private constructor(
    public val x     : Double = 0.0,
    public val y     : Double = 0.0,
    public val width : Double = 0.0,
    public val height: Double = 0.0,
): ConvexPolygon() {
    private var position_ = null as Point?

    /** Top-left corner (x,y) */
    public val position: Point get() = position_ ?: Point(x, y).also { position_ = it }

    private var size_ = null as Size?

    /** width-height */
    public val size: Size get() = size_ ?: Size(width, height).also { size_ = it }

    /**
     * Bottom edge
     *
     * ```
     *(0,0)   │
     *        │
     *        │
     *        │   ┌──────────────────────────┐
     *        │   │                          │
     *        │   │                          │
     * bottom │   │                          │
     *        │   │                          │
     *        │   │                          │
     *        │   │                          │
     *        │   │                          │
     *        ▼   └──────────────────────────┘
     *```
     */
    public val bottom: Double get() = y + height

    /**
     * Right edge
     *
     * ```
     *                    right
     *(0,0) ───────────────────────────────▶
     *
     *          ┌──────────────────────────┐
     *          │                          │
     *          │                          │
     *          │                          │
     *          │                          │
     *          │                          │
     *          │                          │
     *          │                          │
     *          └──────────────────────────┘
     *```
     */
    public val right: Double get() = x + width

    /**
     * Point at the center of the rectangle
     *
     * ```
     *                      cx
     *(0,0)  ────────────────▶
     *      │
     *      │    ┌──────────────────────────┐
     *      │    │                          │
     *      │    │                          │
     *      │    │                          │
     *   cy ▼    │             C            │
     *           │                          │
     *           │                          │
     *           │                          │
     *           └──────────────────────────┘
     *```
     */
    public val center: Point get() = Point(x + width / 2, y + height / 2)

    private var origin = null as Rectangle?

    /** Rectangle with the same width/height but positioned at 0,0 */
    public val atOrigin: Rectangle get() = origin ?: at(0.0, 0.0).also { origin = it }

    override val points: List<Point> by lazy {
        listOf(position, Point(x+width, y), Point(x+width, y+height), Point(x, y+height))
    }

    override val area: Double                 get() = size.area
    override val empty: Boolean               get() = size.empty
    override val boundingRectangle: Rectangle get() = this

    @Suppress("PrivatePropertyName")
    private val hashCode_ by lazy { 31 * position.hashCode() + size.hashCode() }

    /**
     * Checks whether the given point is within the boundaries of this Rectangle
     *
     * @return `true` IFF the given point falls within the boundaries of this Rectangle
     */
    override operator fun contains(point: Point): Boolean = area > 0 && point.x in x..right && point.y in y..bottom

    /** @return ```true``` IFF the given rectangle falls within the boundaries of this Polygon */
    override fun contains(rectangle: Rectangle): Boolean = rectangle.position in this && Point(rectangle.right, rectangle.bottom) in this

    /**
     * Checks whether the given rectangle intersects this one
     *
     * @return `true` IFF the given rectangle intersects this Rectangle
     */
    override fun intersects(rectangle: Rectangle): Boolean = !(empty                      ||
                                                               rectangle.empty            ||
                                                               x      >= rectangle.right  ||
                                                               y      >= rectangle.bottom ||
                                                               right  <= rectangle.x      ||
                                                               bottom <= rectangle.y)

    override fun toString(): String = "[$x,$y,$width,$height]"

    override fun equals(other: Any?): Boolean = this === other || (other is Rectangle && equal(other))

    internal fun fastEqual(other: Rectangle): Boolean = this === other || equal(other)

    private fun equal(other: Rectangle): Boolean {
        if (x      != other.x     ) return false
        if (y      != other.y     ) return false
        if (width  != other.width ) return false
        if (height != other.height) return false

        return true
    }

    override fun hashCode(): Int = hashCode_

    public companion object {
        /** The rectangle at the [Origin] with [width] and [height] equal to `0` */
        public val Empty: Rectangle = Rectangle()

        /** @return [Empty]` */
        public operator fun invoke(): Rectangle = Empty

        /** Creates a Rectangle at the [Origin]: `[0, 0, width, height]` */
        public operator fun invoke(width: Int, height: Int = width): Rectangle = invoke(0.0, 0.0, width.toDouble(), height.toDouble())

        /** Creates a Rectangle at the [Origin]: `[0, 0, width, height]` */
        public operator fun invoke(width: Float, height: Float = width): Rectangle = invoke(0.0, 0.0, width.toDouble(), height.toDouble())

        /** Creates a Rectangle at the [Origin]: `[0, 0, width, height]` */
        public operator fun invoke(width: Double, height: Double = width): Rectangle = invoke(0.0, 0.0, width, height)

        /** Creates a Rectangle at the [Origin]: `[0, 0, size.width, size.height]` */
        public operator fun invoke(size: Size): Rectangle = Rectangle(width = size.width, height = size.height)

        /** Creates a Rectangle: `[position.x, position.y, size.width, size.height]` */
        public operator fun invoke(position: Point, size: Size): Rectangle = Rectangle(position.x, position.y, size.width, size.height)

        /** Creates a Rectangle: `[x, y, width, height]` */
        public operator fun invoke(x: Int = 0, y: Int = 0, width: Int = 0, height: Int = 0): Rectangle = invoke(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble())

        /** Creates a Rectangle: `[x, y, width, height]` */
        public operator fun invoke(x: Float = 0f, y: Float = 0f, width: Float = 0f, height: Float = 0f): Rectangle = invoke(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble())

        /** Creates a Rectangle: `[x, y, width, height]` */
        public operator fun invoke(x: Double = 0.0, y: Double = 0.0, width: Double = 0.0, height: Double = 0.0): Rectangle = Rectangle(
            x,
            y,
            max(0.0, width ),
            max(0.0, height)
        )
    }
}

/**
 * Returns a Rectangle that is the intersection of this and the given one.  The result is [Empty] if there is no intersection.
 *
 * @param rectangle to intersect with
 * @return a Rectangle representing the intersection of the 2 rectangles
 */
public infix fun Rectangle.intersect(rectangle: Rectangle): Rectangle {
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

/**
 * Returns a Rectangle that is the union of this and the given one.
 *
 * @param rectangle to union with
 * @return a Rectangle representing the union of the 2 rectangles
 */
public infix fun Rectangle.union(rectangle: Rectangle): Rectangle {
    if (rectangle === this) {
        return this
    }

    val newX = min(x, rectangle.x)
    val newY = min(y, rectangle.y)

    return Rectangle(newX, newY, max(right, rectangle.right) - newX, max(bottom, rectangle.bottom) - newY)
}

/**
 * Returns a rectangle with the same width/height but positioned at the given x,y
 *
 * @return adjusted Rectangle
 */
public fun Rectangle.at(x: Double = this.x, y: Double = this.y): Rectangle = if (this.x != x || this.y != y) Rectangle(
    x,
    y,
    width,
    height
) else this

/**
 * Returns a rectangle with the same width/height but positioned at the given point
 *
 * @return adjusted Rectangle
 */
public infix fun Rectangle.at(position: Point): Rectangle = at(position.x, position.y)

/**
 * Rectangle that has been adjusted as follows `[x + i, y + i, w - 2i, h - 2i]`, where `i` is the inset
 *
 * @param inset amount to resize by
 * @return adjusted Rectangle
 */
public infix fun Rectangle.inset(inset: Double): Rectangle = if (inset == 0.0) this else Rectangle(
    x + inset,
    y + inset,
    max(0.0, width - inset * 2),
    max(0.0, height - inset * 2)
)

/**
 * Rectangle that has been adjusted as follows `[x + left, y + top, w - (left + right), h - (top + bottom)]`.
 *
 * @param top amount to adjust on the top
 * @param left amount to adjust on the left
 * @param right amount to adjust on the right
 * @param bottom amount to adjust on the bottom
 * @return adjusted Rectangle
 */
public fun Rectangle.inset(top: Double = 0.0, left: Double = 0.0, right: Double = 0.0, bottom: Double = 0.0): Rectangle =
    Rectangle(x + left, y + top, max(0.0, width - (left + right)), max(0.0, height - (top + bottom)))

/**
 * Rectangle that has been adjusted as follows `[x + left, y + top, w - (left + right), h - (top + bottom)]`, where
 * the adjustments are from the inset
 *
 * @param inset amount to resize by
 * @return adjusted Rectangle
 */
public infix fun Rectangle.inset(inset: Insets): Rectangle = inset(
    top    = inset.top,
    left   = inset.left,
    right  = inset.right,
    bottom = inset.bottom
)

/**
 * Returns a new Rectangle with the specified [width], [height], and all the other dimensions from this one.
 *
 * @param width the new Rectangle should have
 * @param height the new Rectangle should have
 */
public fun Rectangle.with(width: Double = this.width, height: Double = this.height): Rectangle = when {
    width == this.width && height == this.height -> this
    else                                         -> Rectangle(x, y, width, height)
}

/**
 * Returns a new Rectangle with the specified [size], and all the other dimensions from this one.
 *
 * @param size the new Rectangle should have
 */
public fun Rectangle.with(size: Size): Rectangle = with(size.width, size.height)

/**
 * Returns a new Rectangle with the same size as [this], but with its center moved to [x, y].
 *
 * @param x of the point to center
 * @param y of the point to center
 */
public fun Rectangle.centered(x: Double, y: Double): Rectangle = at(x - width / 2, y - height / 2)

/**
 * Returns a new Rectangle with the same size as [this], but with its center moved to [at].
 *
 * @param at the point to center
 */
public fun Rectangle.centered(at: Point): Rectangle = centered(at.x, at.y)

/**
 * Ratio of Image [width][Rectangle.width]/[height][Rectangle.height].
 */
public val Rectangle.aspectRatio: Double get() = size.aspectRatio

/**
 * Interpolates between 2 [Rectangle]s
 */
public fun lerp(first: Rectangle, second: Rectangle, fraction: Float): Rectangle = Rectangle(
    x = lerp(first.x, second.x, fraction),
    y = lerp(first.y, second.y, fraction),
    width = lerp(first.width, second.width, fraction),
    height = lerp(first.height, second.height, fraction)
)