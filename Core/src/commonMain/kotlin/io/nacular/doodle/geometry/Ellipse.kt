package io.nacular.doodle.geometry

import io.nacular.doodle.geometry.Point.Companion.Origin
import kotlin.math.PI
import kotlin.math.pow

/**
 * An Ellipse centered at [center], with [xRadius] and [yRadius] as its radii.
 *
 * @constructor creates a new ellipse
 * @property center point of the ellipse
 * @property xRadius or horizontal radius
 * @property yRadius or vertical radius
 */
public open class Ellipse(public val center: Point, public val xRadius: Double, public val yRadius: Double): Shape {

    /**
     * Creates an Ellipse centered at the [Origin].
     *
     * @param xRadius or horizontal radius
     * @param yRadius or vertical radius
     */
    public constructor(xRadius: Double, yRadius: Double): this(Origin, xRadius, yRadius)

    init {
        require(xRadius >= 0) { "x-radius must be >= 0" }
        require(yRadius >= 0) { "y-radius must be >= 0" }
    }

    override val boundingRectangle: Rectangle by lazy { Rectangle(center.x - xRadius, center.y - yRadius, 2 * xRadius, 2 * yRadius) }

    override val area: Double by lazy { PI * xRadius * yRadius }

    override val empty: Boolean get() = area == 0.0

    private val xRadius2 by lazy { xRadius.pow(2) }
    private val yRadius2 by lazy { yRadius.pow(2) }

    override fun contains(point: Point): Boolean = (point - center).run { x.pow(2) / xRadius2 + y.pow(2) / yRadius2 <= 1 }

    override fun contains(rectangle: Rectangle): Boolean = rectangle.points.all { it in this }

    override fun intersects(rectangle: Rectangle): Boolean = TODO("not implemented")

    public companion object {
        /** The **unit** ellipse, centered at the [Origin], with x and y radii equal to 1. */
        public val Unit: Ellipse = Ellipse(1.0, 1.0)

        /** The **empty** ellipse, centered at the [Origin], with x and y radii equal to 0. */
        public val Empty: Ellipse = Ellipse(0.0, 0.0)
    }
}

/**
 * Creates an Ellipse that is inscribed within the rectangle
 *
 * @return the ellipse
 */
public fun Rectangle.inscribedEllipse(): Ellipse = Ellipse(xRadius = width / 2, yRadius = height / 2, center = center)

/**
 * @param insetX to shorten [xRadius][Ellipse.xRadius] by
 * @param insetY to shorten [yRadius][Ellipse.yRadius] by
 * @return an Ellipse with the same attributes as this, but with xRadius and yRadius shortened by [insetX] and [insetY] respectively.
 */
public fun Ellipse.inset(insetX: Double, insetY: Double = insetX): Ellipse = Ellipse(center, xRadius - insetX, yRadius - insetY)
