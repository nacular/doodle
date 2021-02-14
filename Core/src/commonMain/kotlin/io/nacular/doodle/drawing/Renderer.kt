package io.nacular.doodle.drawing

import io.nacular.doodle.drawing.Renderer.FillRule
import io.nacular.doodle.geometry.Path
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Polygon
import io.nacular.measured.units.Angle
import io.nacular.measured.units.Measure

/**
 * Created by Nicholas Eddy on 10/23/17.
 */
public interface Renderer {
    /** Clear contents */
    public fun clear()

    // TODO: Make internal
    /** Cleans up commits rendering */
    public fun flush()

    /**
     * Draws a line between the given points.
     *
     * @param start point for line
     * @param end point for line
     * @param stroke used for line
     */
    public fun line(start: Point, end: Point, stroke: Stroke)

    /**
     * Fills a line path connecting the given points.
     *
     * @param points list of points for the path
     * @param fill to fill with
     * @param fillRule indicating how to fill the path
     */
    public fun path(points: List<Point>, fill: Paint, fillRule: FillRule? = null)

    /**
     * Fills a line path connecting the given points.
     *
     * @param path to draw
     * @param fill to fill with
     * @param fillRule indicating how to fill the path
     */
    public fun path(path: Path, fill: Paint, fillRule: FillRule? = null)

    /**
     * Draws a line path connecting the given points.
     *
     * @param points list of points for the path
     * @param stroke to outline with
     */
    public fun path(points: List<Point>, stroke: Stroke)

    /**
     * Draws a line path connecting the given points.
     *
     * @param path to draw
     * @param stroke to outline with
     */
    public fun path(path: Path, stroke: Stroke)

    /**
     * Fills and outlines a line path connecting the given points.
     *
     * @param points list of points for the path
     * @param stroke to outline with
     * @param fill to fill with
     * @param fillRule indicating how to fill the path
     */
    public fun path(points: List<Point>, stroke: Stroke, fill: Paint, fillRule: FillRule? = null)

    /**
     * Fills and outlines a line path connecting the given points.
     *
     * @param path to draw
     * @param stroke to use for outlining the path
     * @param fill to fill with
     * @param fillRule indicating how to fill the path
     */
    public fun path(path: Path, stroke: Stroke, fill: Paint, fillRule: FillRule? = null)

    /**
     * Fills a polygon.
     *
     * @param polygon to draw
     * @param fill to fill with
     */
    public fun poly(polygon: Polygon, fill: Paint)

    /**
     * Fills and outlines a polygon.
     *
     * @param polygon to draw
     * @param stroke to outline with
     * @param fill to fill with
     */
    public fun poly(polygon: Polygon, stroke: Stroke, fill: Paint? = null)

    /**
     * Fills an arc centered at the given point and swept by the given angle.
     *
     * @param center point for arc
     * @param sweep of the arc
     * @param fill to fill with
     */
    public fun arc(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, fill: Paint)

    /**
     * Fills and outlines an arc centered at the given point and swept by the given angle.
     *
     * @param center point for arc
     * @param sweep of the arc
     * @param stroke to outline with
     * @param fill to fill with
     */
    public fun arc(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, stroke: Stroke, fill: Paint? = null)

    /**
     * Fills a width centered at the given point and swept by the given angle.  Wedges are like arcs
     * with their paths closed at the center point.
     *
     * @param center point for arc
     * @param sweep of the arc
     * @param fill to fill with
     */
    public fun wedge(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, fill: Paint)

    /**
     * Fills and outlines a width centered at the given point and swept by the given angle.  Wedges are like arcs
     * with their paths closed at the center point.
     *
     * @param center point for arc
     * @param sweep of the arc
     * @param stroke to outline with
     * @param fill to fill with
     */
    public fun wedge(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, stroke: Stroke, fill: Paint? = null)

    public enum class FillRule { NonZero, EvenOdd }
}

/**
 * Fills a line path connecting the given points.
 *
 * @param points list of points for the path
 * @param color to fill with
 * @param fillRule indicating how to fill the path
 */
public inline fun Renderer.path(points: List<Point>, color: Color, fillRule: FillRule? = null): Unit = path(points, ColorPaint(color), fillRule)

/**
 * Fills a line path connecting the given points.
 *
 * @param path to draw
 * @param color to fill with
 * @param fillRule indicating how to fill the path
 */
public inline fun Renderer.path(path: Path, color: Color, fillRule: FillRule? = null): Unit = path(path, ColorPaint(color), fillRule)

/**
 * Fills and outlines a line path connecting the given points.
 *
 * @param points list of points for the path
 * @param stroke to outline with
 * @param color to fill with
 * @param fillRule indicating how to fill the path
 */
public inline fun Renderer.path(points: List<Point>, stroke: Stroke, color: Color, fillRule: FillRule? = null): Unit = path(points, stroke, ColorPaint(color), fillRule)

/**
 * Fills and outlines a line path connecting the given points.
 *
 * @param path to draw
 * @param stroke to use for outlining the path
 * @param color to fill with
 * @param fillRule indicating how to fill the path
 */
public fun Renderer.path(path: Path, stroke: Stroke, color: Color, fillRule: FillRule? = null): Unit = path(path, stroke, ColorPaint(color), fillRule)

/**
 * Fills a polygon.
 *
 * @param polygon to draw
 * @param color to fill with
 */
public inline fun Renderer.poly(polygon: Polygon, color: Color): Unit = poly(polygon, ColorPaint(color))

/**
 * Fills and outlines a polygon.
 *
 * @param polygon to draw
 * @param stroke to outline with
 * @param color to fill with
 */
public inline fun Renderer.poly(polygon: Polygon, stroke: Stroke, color: Color): Unit = poly(polygon, stroke, ColorPaint(color))

/**
 * Fills an arc centered at the given point and swept by the given angle.
 *
 * @param center point for arc
 * @param sweep of the arc
 * @param color to fill with
 */
public inline fun Renderer.arc(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, color: Color): Unit = arc(center, radius, sweep, rotation, ColorPaint(color))

/**
 * Fills and outlines an arc centered at the given point and swept by the given angle.
 *
 * @param center point for arc
 * @param sweep of the arc
 * @param stroke to outline with
 * @param color to fill with
 */
public inline fun Renderer.arc(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, stroke: Stroke, color: Color): Unit = arc(center, radius, sweep, rotation, stroke, ColorPaint(color))

/**
 * Fills a width centered at the given point and swept by the given angle.  Wedges are like arcs
 * with their paths closed at the center point.
 *
 * @param center point for arc
 * @param sweep of the arc
 * @param color to fill with
 */
public inline fun Renderer.wedge(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, color: Color): Unit = wedge(center, radius, sweep, rotation, ColorPaint(color))

/**
 * Fills and outlines a width centered at the given point and swept by the given angle.  Wedges are like arcs
 * with their paths closed at the center point.
 *
 * @param center point for arc
 * @param sweep of the arc
 * @param stroke to outline with
 * @param color to fill with
 */
public inline fun Renderer.wedge(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, stroke: Stroke, color: Color): Unit = wedge(center, radius, sweep, rotation, stroke, ColorPaint(color))