package io.nacular.doodle.drawing

import io.nacular.doodle.geometry.Path
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Polygon
import io.nacular.measured.units.Angle
import io.nacular.measured.units.Measure

/**
 * Created by Nicholas Eddy on 10/23/17.
 */
interface Renderer {
    /** Clear contents */
    fun clear()

    // TODO: Make internal
    /** Cleans up commits rendering */
    fun flush()

    /**
     * Draws a line between the given points.
     *
     * @param start point for line
     * @param end point for line
     * @param stroke used for line
     */
    fun line(start: Point, end: Point, stroke: Stroke)

    /**
     * Fills a line path connecting the given points.
     *
     * @param points list of points for the path
     * @param fill to fill with
     * @param fillRule indicating how to fill the path
     */
    fun path(points: List<Point>, fill: Fill, fillRule: FillRule? = null)

    /**
     * Fills a line path connecting the given points.
     *
     * @param path to draw
     * @param fill to fill with
     * @param fillRule indicating how to fill the path
     */
    fun path(path: Path, fill: Fill, fillRule: FillRule? = null)

    /**
     * Draws a line path connecting the given points.
     *
     * @param points list of points for the path
     * @param stroke to outline with
     */
    fun path(points: List<Point>, stroke: Stroke)

    /**
     * Draws a line path connecting the given points.
     *
     * @param path to draw
     * @param stroke to outline with
     */
    fun path(path: Path, stroke: Stroke)

    /**
     * Fills and outlines a line path connecting the given points.
     *
     * @param points list of points for the path
     * @param stroke to outline with
     * @param fill to fill with
     * @param fillRule indicating how to fill the path
     */
    fun path(points: List<Point>, stroke: Stroke, fill: Fill, fillRule: FillRule? = null)

    /**
     * Fills and outlines a line path connecting the given points.
     *
     * @param path to draw
     * @param stroke to use for outlining the path
     * @param fill to fill with
     * @param fillRule indicating how to fill the path
     */
    fun path(path: Path, stroke: Stroke, fill: Fill, fillRule: FillRule? = null)

    /**
     * Fills a polygon.
     *
     * @param polygon to draw
     * @param fill to fill with
     */
    fun poly(polygon: Polygon, fill: Fill)

    /**
     * Fills and outlines a polygon.
     *
     * @param polygon to draw
     * @param stroke to outline with
     * @param fill to fill with
     */
    fun poly(polygon: Polygon, stroke: Stroke, fill: Fill? = null)

    /**
     * Fills an arc centered at the given point and swept by the given angle.
     *
     * @param center point for arc
     * @param sweep of the arc
     * @param fill to fill with
     */
    fun arc(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, fill: Fill)

    /**
     * Fills and outlines an arc centered at the given point and swept by the given angle.
     *
     * @param center point for arc
     * @param sweep of the arc
     * @param stroke to outline with
     * @param fill to fill with
     */
    fun arc(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, stroke: Stroke, fill: Fill? = null)

    /**
     * Fills a width centered at the given point and swept by the given angle.  Wedges are like arcs
     * with their paths closed at the center point.
     *
     * @param center point for arc
     * @param sweep of the arc
     * @param fill to fill with
     */
    fun wedge(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, fill: Fill)

    /**
     * Fills and outlines a width centered at the given point and swept by the given angle.  Wedges are like arcs
     * with their paths closed at the center point.
     *
     * @param center point for arc
     * @param sweep of the arc
     * @param stroke to outline with
     * @param fill to fill with
     */
    fun wedge(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, stroke: Stroke, fill: Fill? = null)

    enum class FillRule {
        NonZero,
        EvenOdd
    }
}