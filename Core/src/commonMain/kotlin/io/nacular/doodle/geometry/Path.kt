package io.nacular.doodle.geometry

import io.nacular.doodle.geometry.Point.Companion.Origin
import io.nacular.doodle.utils.RotationDirection
import io.nacular.doodle.utils.RotationDirection.Clockwise
import io.nacular.doodle.utils.RotationDirection.CounterClockwise
import io.nacular.doodle.utils.lerp
import io.nacular.measured.units.Angle
import io.nacular.measured.units.Angle.Companion.cos
import io.nacular.measured.units.Angle.Companion.degrees
import io.nacular.measured.units.Angle.Companion.sin
import io.nacular.measured.units.Measure
import io.nacular.measured.units.normalize
import io.nacular.measured.units.sign
import io.nacular.measured.units.times
import kotlin.jvm.JvmInline
import kotlin.math.max
import kotlin.math.min

// region path

/**
 * Represents a path-command string as defined by: https://developer.mozilla.org/en-US/docs/Web/SVG/Attribute/d#Path_commands
 */
@JvmInline
public value class Path internal constructor(public val data: String) {
    public operator fun plus(other: Path): Path = Path(data + other.data)
}

/**
 * Creates a Path from the path data string.
 *
 * @param data conforming to https://developer.mozilla.org/en-US/docs/Web/SVG/Attribute/d#Path_commands
 * @return the path, or `null` if [data] is blank
 */
public fun path(data: String): Path? = if (data.isNotBlank()) Path(data) else null

/**
 * Creates a Path at the given point and a builder to further define it.
 *
 * @param from the starting point of the path
 * @return a builder to continue defining the path
 */
public fun path(from: Point): PathBuilder = PathBuilderImpl(from)

/**
 * Creates a Path at the given x,y and a builder to further define it.
 *
 * @see path
 * @param x the starting x of the path
 * @param y the starting y of the path
 * @return a builder to continue defining the path
 */
public fun path(x: Double, y: Double): PathBuilder = path(Point(x, y))

/**
 * Creates a builder that is initialized with the current path and allows further modifications. The
 * resulting path from that builder is different from the one [then] was called on.
 */
public val Path.extend: PathBuilder get() = PathBuilderImpl(data)

// endregion

// region PathBuilder

/**
 * Provides a way to create [Path]s programmatically.
 */
public sealed interface PathBuilder {
    /**
     * Moves from the current point to this one.
     *
     * @param point to move to
     */
    public infix fun moveTo(point: Point): PathBuilder

    /**
     * Draws a line from the current point to this one.
     *
     * @param point to end at
     */
    public infix fun lineTo(point: Point): PathBuilder

    /**
     * Draws a cubic Bézier curve from the current point to this one.
     *
     * @param point to end at
     * @param firstHandle location of the first control point
     * @param secondHandle location of th second control point
     */
    public fun cubicTo(point: Point, firstHandle: Point, secondHandle: Point): PathBuilder

    /**
     * Draws a quadratic Bézier curve from the current point to this one.
     *
     * @param point to end at
     * @param handle location of the control point
     */
    public fun quadraticTo(point: Point, handle: Point): PathBuilder

    /**
     * Draws an elliptic curve (described [here](https://developer.mozilla.org/en-US/docs/Web/SVG/Tutorial/Paths)) from the current point to this one.
     *
     * @param point to end at
     * @param xRadius of the ellipse
     * @param yRadius of the ellipse
     * @param rotation of the ellipse
     * @param largeArch if the arc should have an arc greater than or less than 180°
     * @param sweep determines if the arc should begin moving at positive angles or negative ones
     */
    public fun arcTo(point: Point, xRadius: Double, yRadius: Double, rotation: Measure<Angle> = 0 * degrees, largeArch: Boolean, sweep: Boolean): PathBuilder

    /**
     * Draws a circular curve (described [here](https://developer.mozilla.org/en-US/docs/Web/SVG/Tutorial/Paths)) from the current point to this one.
     *
     * @param point to end at
     * @param radius of the circle
     * @param rotation of the ellipse
     * @param largeArch if the arc should have an arc greater than or less than 180°
     * @param sweep determines if the arc should begin moving at positive angles or negative ones
     */
    public fun arcTo(point: Point, radius: Double, rotation: Measure<Angle> = 0 * degrees, largeArch: Boolean, sweep: Boolean): PathBuilder = arcTo(
            point, radius, radius, rotation, largeArch, sweep
    )

    /**
     * Adds [path] to the existing path data
     *
     * @param path to be added
     */
    public fun append(path: Path): PathBuilder

    /** Closes the path. */
    public fun close(): Path

    /**
     * Finishes path without closing it.
     */
    public fun finish(): Path
}

/**
 * Moves from the current point to this one.
 *
 * @see PathBuilder.moveTo
 * @param x position move to
 * @param y position move to
 */
public fun PathBuilder.moveTo(x: Double, y: Double): PathBuilder = this.moveTo(Point(x, y))

/**
 * Draws a line from the current point to this one.
 *
 * @see PathBuilder.lineTo
 * @param x position end at
 * @param y position end at
 */
public fun PathBuilder.lineTo(x: Double, y: Double): PathBuilder = this.lineTo(Point(x, y))

// endregion

// region Polygon

/**
 * Converts a [Polygon] to a [Path].
 */
public fun Polygon.toPath(): Path = path(points[0]).apply {
    points.subList(1, points.size).forEach {
        lineTo(it)
    }
}.close()

// endregion

// region Rectangle

/**
 * Converts [Rectangle] with radius to [Path].
 */
public fun Rectangle.toPath(radius: Double): Path = toPath(radius, radius, radius, radius)

/**
 * Converts [Rectangle] with radii to [Path].
 */
@Suppress("LocalVariableName")
public fun Rectangle.toPath(
    topLeftRadius    : Double = 0.0,
    topRightRadius   : Double = 0.0,
    bottomRightRadius: Double = 0.0,
    bottomLeftRadius : Double = 0.0,
): Path {
    val minDimension       = min(width, height) / 2
    val topLeftRadius_     = min(topLeftRadius,     minDimension)
    val topRightRadius_    = min(topRightRadius,    minDimension)
    val bottomRightRadius_ = min(bottomRightRadius, minDimension)
    val bottomLeftRadius_  = min(bottomLeftRadius,  minDimension)

    return path(points[0] + Point(topLeftRadius_, 0.0)).apply {
        lineTo(points[1] - Point(min(minDimension, topRightRadius_), 0.0))

        if (topRightRadius_ > 0) {
            arcTo(points[1] + Point(0.0, topRightRadius_), topRightRadius_, largeArch = false, sweep = true)
        }

        lineTo(points[2] - Point(0.0, bottomRightRadius_))

        if (bottomRightRadius_ > 0) {
            arcTo(points[2] - Point(bottomRightRadius_, 0.0), bottomRightRadius_, largeArch = false, sweep = true)
        }

        lineTo(points[3] + Point(bottomLeftRadius_, 0.0))

        if (bottomLeftRadius_ > 0) {
            arcTo(points[3] - Point(0.0, bottomLeftRadius_), bottomLeftRadius_, largeArch = false, sweep = true)
        }

        lineTo(points[0] + Point(0.0, topLeftRadius_))

        if (topLeftRadius_ > 0) {
            arcTo(points[0] + Point(topLeftRadius_, 0.0), topLeftRadius_, largeArch = false, sweep = true)
        }
    }.close()
}

/**
 * Creates a smooth [Path] resembling a [superellipse](https://en.wikipedia.org/wiki/Superellipse) based on this Rectangle.
 *
 * @param curvature used to determine how smooth the corners are. `0f` means no curvature and `1f` means full curvature,
 * creating an ellipse. A value of `0.5` will create a [squircle](https://en.wikipedia.org/wiki/Squircle).
 *
 * @return a [Path] representing the shape
 */
public fun Rectangle.smooth(curvature: Float): Path = when {
    curvature <= 0 -> toPath()
    curvature >= 1 -> inscribedEllipse().toPath()
    else -> {
        val smoothness = min(1f, curvature)

        val halfWidth  = width  / 2
        val halfHeight = height / 2
        val minWidth   = halfWidth  * (1 - 0.552)
        val minHeight  = halfHeight * (1 - 0.552)

        val xProgress = max(0f, smoothness * 2 - 1)
        val yProgress = min(1f, smoothness * 2    )

        val pVOffset = Point(y = lerp(0.0, halfHeight, yProgress))
        val cVOffset = Point(y = lerp(0.0, minHeight,  xProgress))
        val cHOffset = Point(x = lerp(0.0, minWidth,   xProgress))
        val pHOffset = Point(x = lerp(0.0, halfWidth,  yProgress))

        val cubic: PathBuilder.(Point, Point, Point, Point) -> PathBuilder = { anchor, p, c1, c2 ->
            cubicTo(anchor + p, anchor + c1, anchor + c2)
        }

        path(position + pVOffset).
            cubic (position,                         pHOffset,  cVOffset,  cHOffset).
            lineTo(position + Point(x = width    ) - pHOffset                      ).
            cubic (position + Point(x = width    ),  pVOffset, -cHOffset,  cVOffset).
            lineTo(position + Point(width, height) - pVOffset                      ).
            cubic (position + Point(width, height), -pHOffset, -cVOffset, -cHOffset).
            lineTo(position + Point(y = height   ) + pHOffset                      ).
            cubic (position + Point(y = height   ), -pVOffset,  cHOffset, -cVOffset).
            close()
    }
}

// endregion

// region Ellipse

/**
 * Converts an [Ellipse] to a [Path].
 */
public fun Ellipse.toPath(): Path = Point(center.x, center.y - yRadius).let { topPoint ->
    path(topPoint).
        arcTo(Point(center.x, center.y + yRadius), xRadius = xRadius, yRadius = yRadius, rotation = 0 * degrees, largeArch = true, sweep = true).
        arcTo(topPoint,                            xRadius = xRadius, yRadius = yRadius, rotation = 0 * degrees, largeArch = true, sweep = true).
        close()
}

// endregion

// region Utilities

/**
 * Creates a circle path. The [direction] flag allows multiple circles to be combined
 * to create circular holes. A [ring] can be created by joining an outer and inner circle
 * with opposite directions.
 *
 * @param center of the circle
 * @param radius of the circle
 * @param direction to draw in
 * @return path representing the circle
 */
public fun circle(center: Point, radius: Double, direction: RotationDirection): Path =
    (direction == Clockwise).let { sweep ->
        path(Point(center.x, center.y - radius)).arcTo(
            point     = Point(center.x, center.y + radius),
            xRadius   = radius,
            yRadius   = radius,
            largeArch = true,
            sweep     = sweep
        ).arcTo(Point(center.x, center.y - radius), radius, radius, largeArch = true, sweep = sweep).finish()
    }

/**
 * Creates ring (donut) path.
 *
 * @param center of the torus
 * @param innerRadius of the torus
 * @param outerRadius of the torus
 * @return path representing the ring
 */
public fun ring(center: Point, innerRadius: Double, outerRadius: Double): Path = circle(center, outerRadius, Clockwise) + circle(center, innerRadius, CounterClockwise)

/**
 * Determines how to connect current and end points in a [Path]
 */
public typealias SegmentBuilder = PathBuilder.(current: Point, end: Point) -> Unit

/**
 * Creates a path for a section of a ring (donut) shape. The direction of sweep is
 * controlled by the sign of [end] - [start].
 *
 * @param center of the torus
 * @param innerRadius of the torus
 * @param outerRadius of the torus
 * @param start angle
 * @param end angle
 * @param startCap defining how to cap the starting side of the section
 * @param endCap defining how to cap the ending side of the section
 * @return path representing the ring section
 */
public fun ringSection(
        center     : Point,
        innerRadius: Double,
        outerRadius: Double,
        start      : Measure<Angle>,
        end        : Measure<Angle>,
        startCap   : SegmentBuilder = { _,_  ->            },
        endCap     : SegmentBuilder = { _,it -> lineTo(it) },
): Path {
    val sweep      = (end - start).sign > 0
    val thickness  = outerRadius - innerRadius
    val cosStart   = cos(start)
    val sinStart   = sin(start)
    val cosEnd     = cos(end  )
    val sinEnd     = sin(end  )
    val outerStart = center + Point(outerRadius * cosStart, outerRadius * sinStart)
    val outerEnd   = center + Point(outerRadius * cosEnd,   outerRadius * sinEnd  )
    val innerStart = outerStart - thickness * Point(cosStart, sinStart)
    val innerEnd   = outerEnd   - thickness * Point(cosEnd,   sinEnd  )
    val largeArch  = ((end - start).normalize() `in` degrees) > 180

    return path(outerStart).
        arcTo(outerEnd, outerRadius, outerRadius, largeArch = largeArch, sweep = sweep).apply {
            endCap(this, outerEnd, innerEnd)
        }.
        arcTo(innerStart, innerRadius, innerRadius, largeArch = largeArch, sweep = !sweep).apply {
            startCap(this, innerStart, outerStart)
        }.
        close()
}

/**
 * Creates a path for a semicircle. The direction of sweep is controlled by the sign of [end] - [start].
 *
 * @param center of the circle
 * @param radius of the circle
 * @param start angle
 * @param end angle
 * @return path representing the semicircle
 */
public fun semicircle(
    center: Point,
    radius: Double,
    start : Measure<Angle>,
    end   : Measure<Angle>,
): Path = path(center + Point(radius * cos(start), radius * sin(start))).arcTo(
    point     = center + Point(radius * cos(end  ), radius * sin(end  )),
    xRadius   = radius,
    yRadius   = radius,
    largeArch = ((end - start).normalize() `in` degrees) > 180,
    sweep     = (end - start).sign > 0
).finish()

/**
 * Creates a path based on a square with sides of [length] and smooth rounded corners.
 *
 * @param at this location
 * @param length of the square's side
 * @return a [Path] representing the squircle
 */
public fun squircle(at: Point = Origin, length: Double): Path = Rectangle(at, Size(length)).smooth(0.5f)

// endregion

private class PathBuilderImpl(private var data: String): PathBuilder {
    constructor(start: Point): this("M${start.x},${start.y}")

    override fun moveTo(point: Point) = this.also {
        data += "M${point.x},${point.y}"
    }

    override fun lineTo(point: Point) = this.also {
        data += "L${point.x},${point.y}"
    }

    override fun cubicTo(point: Point, firstHandle: Point, secondHandle: Point) = this.also {
        data += "C${firstHandle.x},${firstHandle.y} ${secondHandle.x},${secondHandle.y} ${point.x},${point.y}"
    }

    override fun quadraticTo(point: Point, handle: Point) = this.also {
        data += "Q${handle.x},${handle.y} ${point.x},${point.y}"
    }

    override fun arcTo(point: Point, xRadius: Double, yRadius: Double, rotation: Measure<Angle>, largeArch: Boolean, sweep: Boolean) = this.also {
        data += "A$xRadius $yRadius ${rotation `in` degrees} ${if (largeArch) 1 else 0} ${if (sweep) 1 else 0} ${point.x},${point.y}"
    }

    override fun append(path: Path): PathBuilder = this.also {
        data += path.data
    }

    override fun close(): Path = Path(data + "Z")

    override fun finish(): Path = Path(data)
}