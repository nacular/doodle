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
     * @param pen used for line
     */
    fun line(start: Point, end: Point, pen: Pen)

    /**
     * Fills a line path connecting the given points.
     *
     * @param points list of points for the path
     * @param brush to fill with
     * @param fillRule indicating how to fill the path
     */
    fun path(points: List<Point>, brush: Brush, fillRule: FillRule? = null)

    /**
     * Fills a line path connecting the given points.
     *
     * @param path to draw
     * @param brush to fill with
     * @param fillRule indicating how to fill the path
     */
    fun path(path: Path, brush: Brush, fillRule: FillRule? = null)

    /**
     * Draws a line path connecting the given points.
     *
     * @param points list of points for the path
     * @param pen to outline with
     */
    fun path(points: List<Point>, pen: Pen)

    /**
     * Draws a line path connecting the given points.
     *
     * @param path to draw
     * @param pen to outline with
     */
    fun path(path: Path, pen: Pen)

    /**
     * Fills and outlines a line path connecting the given points.
     *
     * @param points list of points for the path
     * @param pen to outline with
     * @param brush to fill with
     * @param fillRule indicating how to fill the path
     */
    fun path(points: List<Point>, pen: Pen, brush: Brush, fillRule: FillRule? = null)

    /**
     * Fills and outlines a line path connecting the given points.
     *
     * @param path to draw
     * @param pen to use for outlining the path
     * @param brush to fill with
     * @param fillRule indicating how to fill the path
     */
    fun path(path: Path, pen: Pen, brush: Brush, fillRule: FillRule? = null)

    /**
     * Fills a polygon.
     *
     * @param polygon to draw
     * @param brush to fill with
     */
    fun poly(polygon: Polygon, brush: Brush)

    /**
     * Fills and outlines a polygon.
     *
     * @param polygon to draw
     * @param pen to outline with
     * @param brush to fill with
     */
    fun poly(polygon: Polygon, pen: Pen, brush: Brush? = null)

    /**
     * Fills an arc centered at the given point and swept by the given angle.
     *
     * @param center point for arc
     * @param sweep of the arc
     * @param brush to fill with
     */
    fun arc(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, brush: Brush)

    /**
     * Fills and outlines an arc centered at the given point and swept by the given angle.
     *
     * @param center point for arc
     * @param sweep of the arc
     * @param pen to outline with
     * @param brush to fill with
     */
    fun arc(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, pen: Pen, brush: Brush? = null)

    /**
     * Fills a width centered at the given point and swept by the given angle.  Wedges are like arcs
     * with their paths closed at the center point.
     *
     * @param center point for arc
     * @param sweep of the arc
     * @param brush to fill with
     */
    fun wedge(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, brush: Brush)

    /**
     * Fills and outlines a width centered at the given point and swept by the given angle.  Wedges are like arcs
     * with their paths closed at the center point.
     *
     * @param center point for arc
     * @param sweep of the arc
     * @param pen to outline with
     * @param brush to fill with
     */
    fun wedge(center: Point, radius: Double, sweep: Measure<Angle>, rotation: Measure<Angle>, pen: Pen, brush: Brush? = null)

    enum class FillRule {
        NonZero,
        EvenOdd
    }
}