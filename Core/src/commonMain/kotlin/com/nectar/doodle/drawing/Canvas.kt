package com.nectar.doodle.drawing

import com.nectar.doodle.drawing.AffineTransform.Companion.Identity
import com.nectar.doodle.drawing.Color.Companion.Black
import com.nectar.doodle.geometry.Circle
import com.nectar.doodle.geometry.Ellipse
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Point.Companion.Origin
import com.nectar.doodle.geometry.Polygon
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.geometry.div
import com.nectar.doodle.image.Image
import com.nectar.doodle.text.StyledText
import com.nectar.measured.units.Angle
import com.nectar.measured.units.Measure


sealed class Shadow(val horizontal: Double, val vertical: Double, val blurRadius: Double, val color: Color)
class InnerShadow(horizontal: Double = 0.0, vertical: Double = 0.0, blurRadius: Double = 0.0, color: Color = Black): Shadow(horizontal, vertical, blurRadius, color)
class OuterShadow(horizontal: Double = 0.0, vertical: Double = 0.0, blurRadius: Double = 0.0, color: Color = Black): Shadow(horizontal, vertical, blurRadius, color)

/**
 * All rendering operations are done using Canvases. A canvas represents an "infinite", 2-D surface that can be
 * "drawn" onto. Despite this, there is a [size] associated with each Canvas that indicates its clipping region.
 * The framework manages Canvases and makes them available to [View][com.nectar.doodle.core.View]s during
 * [View.render][com.nectar.doodle.core.View.render].
 *
 * Canvases are intended to be efficient and reuse previous content when they are re-rendered.
 *
 * @author Nicholas Eddy
 */
interface Canvas: Renderer {
    /**
     * The size to which the Canvas will clip to by default.  A View's Canvas won't clip if
     * [View.clipCanvasToBounds][com.nectar.doodle.core.View.clipCanvasToBounds] == `false`
     */
    var size: Size

    /**
     * Linearly scales the operations within [block].
     *
     * ```kotlin
     * canvas.scale(x = 2.0, y = 2.0)  {
     *  rect(Rectangle(10, 10)) // Draws a rectangle that is scaled to 20 x 20
     * }
     * ```
     *
     * @param x amount to scale horizontally
     * @param y amount to scale vertically
     * @param block being transformed
     */
    fun scale(x: Double = 1.0, y: Double = 1.0, block: Canvas.() -> Unit) = when {
        x == 1.0 && y == 1.0 -> block()
        else                 -> transform(Identity.scale(x, y), block)
    }

    /**
     * Linearly scales the operations within [block] around the given point.
     *
     * ```kotlin
     * canvas.scale(around = Point(5, 5), x = 2.0, y = 2.0)  {
     *  rect(Rectangle(10, 10)) // Draws rectangle: -5, -5, 20, 20
     * }
     * ```
     *
     * @param around this point
     * @param x amount to scale horizontally
     * @param y amount to scale vertically
     * @param block being transformed
     */
    fun scale(around: Point, x: Double = 1.0, y: Double = 1.0, block: Canvas.() -> Unit) = when {
        x == 1.0 && y == 1.0 -> block()
        else                 -> {
            val point = around - (size / 2.0).run { Point(width, height) }

            transform(Identity.translate(point).scale(x, y).translate(-point), block)
        }
    }

    /**
     * Rotates the operations within [block] around 0,0 by the given angle.
     *
     * @param by this angle
     * @param block being transformed
     */
    fun rotate(by: Measure<Angle>, block: Canvas.() -> Unit) = transform(Identity.rotate(by), block)

    /**
     * Rotates the operations within [block] around the given point by the given angle.
     *
     * @param around this point
     * @param by this angle
     * @param block being transformed
     */
    fun rotate(around: Point, by: Measure<Angle>, block: Canvas.() -> Unit) {
        transform(Identity.translate(around).rotate(by).translate(-around), block)
    }

    /**
     * Translates the operations within [block] by the given x, y.
     *
     * @param by this amount
     * @param block being transformed
     */
    fun translate(by: Point, block: Canvas.() -> Unit) = when (by) {
        Origin -> block()
        else   -> transform(Identity.translate(by), block)
    }

    /**
     * Flips the operations within [block] vertically around the center of the Canvas.
     *
     * @param block being transformed
     */
    fun flipVertically(block: Canvas.() -> Unit) = scale(1.0, -1.0, block = block)

    /**
     * Flips the operations within [block] vertically around the given y-coordinate.
     *
     * @param around this y coordinate
     * @param block being transformed
     */
    fun flipVertically(around: Double, block: Canvas.() -> Unit) = transform(Identity.
            translate(Point(0.0, around)).
            scale(1.0, -1.0).
            translate(Point(0.0, -around)),
            block)

    /**
     * Flips the operations within [block] horizontally around the center of the Canvas.
     *
     * @param block being transformed
     */
    fun flipHorizontally(block: Canvas.() -> Unit) = scale(-1.0, 1.0, block = block)

    /**
     * Flips the operations within [block] horizontally around the given x-coordinate.
     *
     * @param around this x coordinate
     * @param block being transformed
     */
    fun flipHorizontally(around: Double, block: Canvas.() -> Unit) = transform(Identity.
            translate(Point(around, 0.0)).
            scale(-1.0, 1.0).
            translate(Point(-around, 0.0)),
            block)

    /**
     * Transforms the operations within [block].
     *
     * @param transform to use
     * @param block being transformed
     */
    fun transform(transform: AffineTransform, block: Canvas.() -> Unit)

    /**
     * Fills a rectangle.
     *
     * @param rectangle to draw
     * @param brush to fill with
     */
    fun rect(rectangle: Rectangle, brush: Brush)

    /**
     * Fills and outlines a rectangle.
     *
     * @param rectangle to draw
     * @param pen to outline with
     * @param brush to fill with
     */
    fun rect(rectangle: Rectangle, pen: Pen, brush: Brush? = null)

    /**
     * Fills a rounded rectangle.
     *
     * @param rectangle to draw
     * @param radius for corners
     * @param brush to fill with
     */
    fun rect(rectangle: Rectangle, radius: Double, brush: Brush)

    /**
     * Fills and outlines a rounded rectangle.
     *
     * @param rectangle to draw
     * @param radius for corners
     * @param pen to outline with
     * @param brush to fill with
     */
    fun rect(rectangle: Rectangle, radius: Double, pen: Pen, brush: Brush? = null)

    /**
     * Fills a circle.
     *
     * @param circle to draw
     * @param brush to fill with
     */
    fun circle(circle: Circle, brush: Brush)

    /**
     * Fills and outlines a circle.
     *
     * @param circle to draw
     * @param pen to outline with
     * @param brush to fill with
     */
    fun circle(circle: Circle, pen: Pen, brush: Brush? = null)

    /**
     * Fills an ellipse.
     *
     * @param ellipse to draw
     * @param brush to fill with
     */
    fun ellipse(ellipse: Ellipse, brush: Brush)

    /**
     * Fills and outlines an ellipse.
     *
     * @param ellipse to draw
     * @param pen to outline with
     * @param brush to fill with
     */
    fun ellipse(ellipse: Ellipse, pen: Pen, brush: Brush? = null)

    /**
     * Draws unwrapped plain text in the default [Font].
     *
     * @param text to draw
     * @param at this point
     * @param brush to fill with
     */
    fun text(text: String, at: Point, brush: Brush) = text(text, null, at, brush)

    /**
     * Draws unwrapped plain text.
     *
     * @param text to draw
     * @param font to use
     * @param at this point
     * @param brush to fill with
     */
    fun text(text: String, font: Font?, at: Point, brush: Brush)

    /**
     * Draws styled text.
     *
     * @param text to draw
     * @param at this point
     */
    fun text(text: StyledText, at: Point)

    /**
     * Draws wrapped plain text.
     *
     * @param text to draw
     * @param font to use
     * @param at this point
     * @param leftMargin where text wraps
     * @param rightMargin where text wraps
     * @param brush to fill with
     */
    fun wrapped(
            text       : String,
            font       : Font? = null,
            at         : Point,
            leftMargin : Double,
            rightMargin: Double,
            brush      : Brush)

    /**
     * Draws wrapped styled text.
     *
     * @param text to draw
     * @param at this point
     * @param leftMargin where text wraps
     * @param rightMargin where text wraps
     */
    fun wrapped(
            text       : StyledText,
            at         : Point,
            leftMargin : Double,
            rightMargin: Double)

    /**
     * Draws an image.
     *
     * @param image to draw
     * @param destination rectangle on the Canvas to draw image
     * @param opacity of the drawn image
     * @param radius of image corners if rounding
     * @param source rectangle within the image to draw into destination
     */
    fun image(image: Image, destination: Rectangle = Rectangle(size = image.size), opacity: Float = 1f, radius: Double = 0.0, source: Rectangle = Rectangle(size = image.size))

    /**
     * Clips the operations within [block] within the given rectangle.
     *
     * @param rectangle to clip within
     * @param block to be clipped
     */
    fun clip(rectangle: Rectangle, radius: Double = 0.0, block: Canvas.() -> Unit)

    /**
     * Clips the operations within [block] within the given polygon.
     *
     * @param polygon to clip within
     * @param block to be clipped
     */
    fun clip(polygon: Polygon, block: Canvas.() -> Unit)

    /**
     * Adds a shadow to the operations within [block].
     *
     * @param shadow to add
     * @param block with operations
     */
    fun shadow(shadow: Shadow, block: Canvas.() -> Unit)

    /**
     * Adds an inner shadow to the operations within [block].
     *
     * @param horizontal offset of the shadow
     * @param vertical offset of the shadow
     * @param blurRadius of the shadow
     * @param color of the shadow
     * @param block shadow applied to
     */
    fun innerShadow(horizontal: Double = 0.0, vertical: Double = 0.0, blurRadius: Double = 1.0, color: Color = Black, block: Canvas.() -> Unit) = shadow(InnerShadow(horizontal, vertical, blurRadius, color), block)

    /**
     * Adds an outer shadow to the operations within [block].
     *
     * @param horizontal offset of the shadow
     * @param vertical offset of the shadow
     * @param blurRadius of the shadow
     * @param color of the shadow
     * @param block shadow applied to
     */
    fun outerShadow(horizontal: Double = 0.0, vertical: Double = 0.0, blurRadius: Double = 1.0, color: Color = Black, block: Canvas.() -> Unit) = shadow(OuterShadow(horizontal, vertical, blurRadius, color), block)
}
