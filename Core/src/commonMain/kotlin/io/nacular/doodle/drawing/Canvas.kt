package io.nacular.doodle.drawing

import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.geometry.Circle
import io.nacular.doodle.geometry.Ellipse
import io.nacular.doodle.geometry.Path
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Point.Companion.Origin
import io.nacular.doodle.geometry.Polygon
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.div
import io.nacular.doodle.image.Image
import io.nacular.doodle.text.StyledText
import io.nacular.measured.units.Angle
import io.nacular.measured.units.Measure


public sealed class Shadow(public val horizontal: Double, public val vertical: Double, public val blurRadius: Double, public val color: Color)
public class InnerShadow(horizontal: Double = 0.0, vertical: Double = 0.0, blurRadius: Double = 0.0, color: Color = Black): Shadow(horizontal, vertical, blurRadius, color)
public class OuterShadow(horizontal: Double = 0.0, vertical: Double = 0.0, blurRadius: Double = 0.0, color: Color = Black): Shadow(horizontal, vertical, blurRadius, color)

/**
 * All rendering operations are done using Canvases. A canvas represents an "infinite", 2-D surface that can be
 * "drawn" onto. Despite this, there is a [size] associated with each Canvas that indicates its clipping region.
 * The framework manages Canvases and makes them available to [View][io.nacular.doodle.core.View]s during
 * [View.render][io.nacular.doodle.core.View.render].
 *
 * Canvases are intended to be efficient and reuse previous content when they are re-rendered.
 *
 * @author Nicholas Eddy
 */
public interface Canvas: Renderer {
    /**
     * The size to which the Canvas will clip to by default.  A View's Canvas won't clip if
     * [View.clipCanvasToBounds][io.nacular.doodle.core.View.clipCanvasToBounds] == `false`
     */
    public var size: Size

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
    public fun scale(x: Double = 1.0, y: Double = 1.0, block: Canvas.() -> Unit): Unit = when {
        x == 1.0 && y == 1.0 -> block()
        else                 -> transform(Identity.scale(x, y), block)
    }

    /**
     * Linearly scales the operations within [block] around the given point.
     *
     * ```kotlin
     * canvas.scale(around = Point(5, 5), x = 2.0, y = 2.0)  {
     *    rect(Rectangle(10, 10)) // Draws rectangle: -5, -5, 20, 20
     * }
     * ```
     *
     * @param around this point
     * @param x amount to scale horizontally
     * @param y amount to scale vertically
     * @param block being transformed
     */
    public fun scale(around: Point, x: Double = 1.0, y: Double = 1.0, block: Canvas.() -> Unit): Unit = when {
        x == 1.0 && y == 1.0 -> block()
        else                 -> transform(Identity.translate(around).scale(x, y).translate(-around), block)
    }

    /**
     * Rotates the operations within [block] around 0,0 by the given angle.
     *
     * @param by this angle
     * @param block being transformed
     */
    public fun rotate(by: Measure<Angle>, block: Canvas.() -> Unit): Unit = transform(Identity.rotate(by), block)

    /**
     * Rotates the operations within [block] around the given point by the given angle.
     *
     * @param around this point
     * @param by this angle
     * @param block being transformed
     */
    public fun rotate(around: Point, by: Measure<Angle>, block: Canvas.() -> Unit) {
        transform(Identity.translate(around).rotate(by).translate(-around), block)
    }

    /**
     * Translates the operations within [block] by the given x, y.
     *
     * @param by this amount
     * @param block being transformed
     */
    public fun translate(by: Point, block: Canvas.() -> Unit): Unit = when (by) {
        Origin -> block()
        else   -> transform(Identity.translate(by), block)
    }

    /**
     * Flips the operations within [block] vertically around the center of the Canvas.
     *
     * @param block being transformed
     */
    public fun flipVertically(block: Canvas.() -> Unit): Unit = scale(1.0, -1.0, block = block)

    /**
     * Flips the operations within [block] vertically around the given y-coordinate.
     *
     * @param around this y coordinate
     * @param block being transformed
     */
    public fun flipVertically(around: Double, block: Canvas.() -> Unit): Unit = transform(Identity.
            translate(y = around ).
            scale    (1.0, -1.0  ).
            translate(y = -around),
            block)

    /**
     * Flips the operations within [block] horizontally around the center of the Canvas.
     *
     * @param block being transformed
     */
    public fun flipHorizontally(block: Canvas.() -> Unit): Unit = scale(-1.0, 1.0, block = block)

    /**
     * Flips the operations within [block] horizontally around the given x-coordinate.
     *
     * @param around this x coordinate
     * @param block being transformed
     */
    public fun flipHorizontally(around: Double, block: Canvas.() -> Unit): Unit = transform(Identity.
            translate(x = around ).
            scale    (-1.0, 1.0  ).
            translate(x = -around),
            block)

    /**
     * Transforms the operations within [block].
     *
     * @param transform to use
     * @param block being transformed
     */
    public fun transform(transform: AffineTransform, block: Canvas.() -> Unit)

    /**
     * Fills a rectangle.
     *
     * @param rectangle to draw
     * @param fill to fill with
     */
    public fun rect(rectangle: Rectangle, fill: Paint)

    /**
     * Fills and outlines a rectangle.
     *
     * @param rectangle to draw
     * @param stroke to outline with
     * @param fill to fill with
     */
    public fun rect(rectangle: Rectangle, stroke: Stroke, fill: Paint? = null)

    /**
     * Fills a rounded rectangle.
     *
     * @param rectangle to draw
     * @param radius for corners
     * @param fill to fill with
     */
    public fun rect(rectangle: Rectangle, radius: Double, fill: Paint)

    /**
     * Fills and outlines a rounded rectangle.
     *
     * @param rectangle to draw
     * @param radius for corners
     * @param stroke to outline with
     * @param fill to fill with
     */
    public fun rect(rectangle: Rectangle, radius: Double, stroke: Stroke, fill: Paint? = null)

    /**
     * Fills a circle.
     *
     * @param circle to draw
     * @param fill to fill with
     */
    public fun circle(circle: Circle, fill: Paint)

    /**
     * Fills and outlines a circle.
     *
     * @param circle to draw
     * @param stroke to outline with
     * @param fill to fill with
     */
    public fun circle(circle: Circle, stroke: Stroke, fill: Paint? = null)

    /**
     * Fills an ellipse.
     *
     * @param ellipse to draw
     * @param fill to fill with
     */
    public fun ellipse(ellipse: Ellipse, fill: Paint)

    /**
     * Fills and outlines an ellipse.
     *
     * @param ellipse to draw
     * @param stroke to outline with
     * @param fill to fill with
     */
    public fun ellipse(ellipse: Ellipse, stroke: Stroke, fill: Paint? = null)

    /**
     * Draws unwrapped plain text in the default [Font].
     *
     * @param text to draw
     * @param at this point
     * @param fill to fill with
     */
    public fun text(text: String, at: Point = Origin, fill: Paint): Unit = text(text, null, at, fill)

    /**
     * Draws unwrapped plain text.
     *
     * @param text to draw
     * @param font to use
     * @param at this point
     * @param fill to fill with
     */
    public fun text(text: String, font: Font?, at: Point, fill: Paint)

    /**
     * Draws styled text.
     *
     * @param text to draw
     * @param at this point
     */
    public fun text(text: StyledText, at: Point = Origin)

    /**
     * Draws wrapped plain text.
     *
     * @param text to draw
     * @param font to use
     * @param at this point
     * @param leftMargin where text wraps
     * @param rightMargin where text wraps
     * @param fill to fill with
     */
    public fun wrapped(text: String, font: Font? = null, at: Point = Origin, leftMargin: Double, rightMargin: Double, fill: Paint)

    /**
     * Draws wrapped styled text.
     *
     * @param text to draw
     * @param at this point
     * @param leftMargin where text wraps
     * @param rightMargin where text wraps
     */
    public fun wrapped(text: StyledText, at: Point = Origin, leftMargin: Double, rightMargin: Double)

    /**
     * Draws an image.
     *
     * @param image to draw
     * @param destination rectangle on the Canvas to draw image
     * @param opacity of the drawn image
     * @param radius of image corners if rounding
     * @param source rectangle within the image to draw into destination
     */
    public fun image(image: Image, destination: Rectangle = Rectangle(size = image.size), opacity: Float = 1f, radius: Double = 0.0, source: Rectangle = Rectangle(size = image.size))

    /**
     * Draws an image.
     *
     * @param image to draw
     * @param at this point on the Canvas
     * @param opacity of the drawn image
     * @param radius of image corners if rounding
     * @param source rectangle within the image to draw into destination
     */
    public fun image(image: Image, at: Point, opacity: Float = 1f, radius: Double = 0.0, source: Rectangle = Rectangle(size = image.size)): Unit = image(image, Rectangle(position = at, size = image.size), opacity, radius, source)

    /**
     * Clips the operations within [block] within the given rectangle.
     *
     * @param rectangle to clip within
     * @param block to be clipped
     */
    public fun clip(rectangle: Rectangle, radius: Double = 0.0, block: Canvas.() -> Unit)

    /**
     * Clips the operations within [block] within the given polygon.
     *
     * @param polygon to clip within
     * @param block to be clipped
     */
    public fun clip(polygon: Polygon, block: Canvas.() -> Unit)

    /**
     * Clips the operations within [block] within the given ellipse.
     *
     * @param ellipse to clip within
     * @param block to be clipped
     */
    public fun clip(ellipse: Ellipse, block: Canvas.() -> Unit)

    /**
     * Clips the operations within [block] within the given path.
     *
     * @param path to clip within
     * @param block to be clipped
     */
    public fun clip(path: Path, block: Canvas.() -> Unit)

    /**
     * Adds a shadow to the operations within [block].
     *
     * @param shadow to add
     * @param block with operations
     */
    public fun shadow(shadow: Shadow, block: Canvas.() -> Unit)

    /**
     * Adds an inner shadow to the operations within [block].
     *
     * @param horizontal offset of the shadow
     * @param vertical offset of the shadow
     * @param blurRadius of the shadow
     * @param color of the shadow
     * @param block shadow applied to
     */
    public fun innerShadow(horizontal: Double = 0.0, vertical: Double = 0.0, blurRadius: Double = 1.0, color: Color = Black, block: Canvas.() -> Unit): Unit = shadow(InnerShadow(horizontal, vertical, blurRadius, color), block)

    /**
     * Adds an outer shadow to the operations within [block].
     *
     * @param horizontal offset of the shadow
     * @param vertical offset of the shadow
     * @param blurRadius of the shadow
     * @param color of the shadow
     * @param block shadow applied to
     */
    public fun outerShadow(horizontal: Double = 0.0, vertical: Double = 0.0, blurRadius: Double = 1.0, color: Color = Black, block: Canvas.() -> Unit): Unit = shadow(OuterShadow(horizontal, vertical, blurRadius, color), block)
}

/**
 * The width to which the Canvas will clip by default.
 * @see Canvas.size
 */
public inline val Canvas.width: Double get() = size.width

/**
 * The height to which the Canvas will clip by default.
 * @see Canvas.size
 */
public inline val Canvas.height: Double get() = size.height

/**
 * Fills a rectangle.
 *
 * @param rectangle to draw
 * @param color to fill with
 */
public inline fun Canvas.rect(rectangle: Rectangle, color: Color): Unit = rect(rectangle, color.paint)

/**
 * Fills and outlines a rectangle.
 *
 * @param rectangle to draw
 * @param stroke to outline with
 * @param color to fill with
 */
public inline fun Canvas.rect(rectangle: Rectangle, stroke: Stroke, color: Color): Unit = rect(rectangle, stroke, color.paint)

/**
 * Fills a rounded rectangle.
 *
 * @param rectangle to draw
 * @param radius for corners
 * @param color to fill with
 */
public inline fun Canvas.rect(rectangle: Rectangle, radius: Double, color: Color): Unit = rect(rectangle, radius, color.paint)

/**
 * Fills and outlines a rounded rectangle.
 *
 * @param rectangle to draw
 * @param radius for corners
 * @param stroke to outline with
 * @param color to fill with
 */
public inline fun Canvas.rect(rectangle: Rectangle, radius: Double, stroke: Stroke, color: Color): Unit = rect(rectangle, radius, stroke, color.paint)

/**
 * Fills a circle.
 *
 * @param circle to draw
 * @param color to fill with
 */
public inline fun Canvas.circle(circle: Circle, color: Color): Unit = circle(circle, color.paint)

/**
 * Fills and outlines a circle.
 *
 * @param circle to draw
 * @param stroke to outline with
 * @param color to fill with
 */
public inline fun Canvas.circle(circle: Circle, stroke: Stroke, color: Color): Unit = circle(circle, stroke, color.paint)

/**
 * Fills an ellipse.
 *
 * @param ellipse to draw
 * @param color to fill with
 */
public inline fun Canvas.ellipse(ellipse: Ellipse, color: Color): Unit = ellipse(ellipse, color.paint)

/**
 * Fills and outlines an ellipse.
 *
 * @param ellipse to draw
 * @param stroke to outline with
 * @param color to fill with
 */
public inline fun Canvas.ellipse(ellipse: Ellipse, stroke: Stroke, color: Color): Unit = ellipse(ellipse, stroke, color.paint)

/**
 * Draws unwrapped plain text in the default [Font].
 *
 * @param text to draw
 * @param at this point
 * @param color to fill with
 */
public inline fun Canvas.text(text: String, at: Point = Origin, color: Color): Unit = text(text, at, color.paint)

/**
 * Draws unwrapped plain text.
 *
 * @param text to draw
 * @param font to use
 * @param at this point
 * @param color to fill with
 */
public inline fun Canvas.text(text: String, font: Font?, at: Point = Origin, color: Color): Unit = text(text, font, at, color.paint)

/**
 * Draws wrapped plain text.
 *
 * @param text to draw
 * @param font to use
 * @param at this point
 * @param leftMargin where text wraps
 * @param rightMargin where text wraps
 * @param color to fill with
 */
public inline fun Canvas.wrapped(text: String, font: Font? = null, at: Point, leftMargin: Double, rightMargin: Double, color: Color): Unit = wrapped(text, font, at, leftMargin, rightMargin, color.paint)