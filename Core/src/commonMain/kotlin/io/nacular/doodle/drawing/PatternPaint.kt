package io.nacular.doodle.drawing

import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.geometry.Ellipse
import io.nacular.doodle.geometry.Path
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Polygon
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.Size.Companion.Empty
import io.nacular.doodle.geometry.times
import io.nacular.measured.units.Angle
import io.nacular.measured.units.Angle.Companion.degrees
import io.nacular.measured.units.Measure
import io.nacular.measured.units.times

/**
 * A Canvas used within [PatternPaint] that does not support perspective rendering. This is done because support
 * for patterns with perspective is very inconsistent for Web.
 *
 * @see [Canvas]
 */
public interface PatternCanvas: CommonCanvas {
    public fun scale(x: Double, y: Double, block: PatternCanvas.() -> Unit): Unit = when {
        x == 1.0 && y == 1.0 -> block()
        else                 -> transform(Identity.scale(x, y), block)
    }

    public fun scale(around: Point, x: Double = 1.0, y: Double = 1.0, block: PatternCanvas.() -> Unit): Unit = when {
        x == 1.0 && y == 1.0 -> block()
        else                 -> transform(Identity.translate(around).scale(x, y).translate(-around), block)
    }

    public fun rotate(by: Measure<Angle>, block: PatternCanvas.() -> Unit): Unit = transform(Identity.rotate(by), block)

    public fun rotate(around: Point, by: Measure<Angle>, block: PatternCanvas.() -> Unit) {
        transform(Identity.translate(around).rotate(by).translate(-around), block)
    }

    public fun translate(by: Point, block: PatternCanvas.() -> Unit): Unit = when (by) {
        Point.Origin -> block()
        else         -> transform(Identity.translate(by), block)
    }

    public fun flipVertically(block: PatternCanvas.() -> Unit): Unit = scale(1.0, -1.0, block = block)

    public fun flipVertically(around: Double, block: PatternCanvas.() -> Unit): Unit = transform(Identity.translate(y = around ).scale(1.0, -1.0).translate(y = -around), block)

    public fun flipHorizontally(block: PatternCanvas.() -> Unit): Unit = scale(-1.0, 1.0, block = block)

    public fun flipHorizontally(around: Double, block: PatternCanvas.() -> Unit): Unit = transform(Identity.translate(x = around ).scale(-1.0, 1.0  ).translate(x = -around), block)

    public fun transform(transform: AffineTransform, block: PatternCanvas.() -> Unit)

    public fun clip(rectangle: Rectangle, radius: Double = 0.0, block: PatternCanvas.() -> Unit)

    public fun clip(polygon: Polygon, block: PatternCanvas.() -> Unit)

    public fun clip(ellipse: Ellipse, block: PatternCanvas.() -> Unit)

    public fun clip(path: Path, block: PatternCanvas.() -> Unit)

    public fun shadow(shadow: Shadow, block: PatternCanvas.() -> Unit)

    public fun innerShadow(horizontal: Double = 0.0, vertical: Double = 0.0, blurRadius: Double = 1.0, color: Color = Color.Black, block: PatternCanvas.() -> Unit): Unit = shadow(InnerShadow(horizontal, vertical, blurRadius, color), block)

    public fun outerShadow(horizontal: Double = 0.0, vertical: Double = 0.0, blurRadius: Double = 1.0, color: Color = Color.Black, block: PatternCanvas.() -> Unit): Unit = shadow(OuterShadow(horizontal, vertical, blurRadius, color), block)
}

/**
 * A [Paint] that repeats the contents of its [Canvas] horizontally and vertically within a shape.
 *
 * @author Nicholas Eddy
 *
 * @property bounds of the Canvas that will be repeated
 * @property transform applied to the fill
 * @property opacity global opacity of the Canvas that will be repeated
 * @property paint operations for the Canvas
 */
public class PatternPaint(
    public val bounds   : Rectangle,
    public val transform: AffineTransform2D = Identity,
    public val opacity  : Float = 1f,
    public val paint    : PatternCanvas.() -> Unit
): Paint() {
    public constructor(size: Size, transform: AffineTransform2D = Identity, opacity: Float = 1f, fill: PatternCanvas.() -> Unit): this(Rectangle(size = size), transform, opacity, fill)

    public val size: Size get() = bounds.size

    override val visible: Boolean = !bounds.empty

    public companion object {
        public operator fun invoke(bounds: Rectangle, transform: AffineTransform2D = Identity, fill: PatternCanvas.() -> Unit): PatternPaint =
                PatternPaint(bounds, transform, paint = fill)
    }
}

/**
 * Creates a [PatternPaint] that draws an alternating horizontal striped pattern.
 *
 * @param stripeWidth of the alternating rows
 * @param evenRowColor used to fill the even numbered rows (i.e. 0, 2, 122)
 * @param oddRowColor used to fill the odd numbered rows (i.e. 1, 3, 121)
 */
public fun stripedPaint(stripeWidth : Double,
        evenRowColor: Color? = null,
        oddRowColor : Color? = null,
        transform   : AffineTransform2D = Identity): PatternPaint = PatternPaint(Size(if (evenRowColor.visible || oddRowColor.visible) stripeWidth else 0.0, 2 * stripeWidth), transform) {
    evenRowColor?.let { rect(Rectangle(                  stripeWidth, stripeWidth), ColorPaint(it)) }
    oddRowColor?.let  { rect(Rectangle(0.0, stripeWidth, stripeWidth, stripeWidth), ColorPaint(it)) }
}

/**
 * Creates a [PatternPaint] that draws an alternating horizontal striped pattern.
 *
 * @param rowHeight of the alternating rows
 * @param evenRowColor used to fill the even numbered rows (i.e. 0, 2, 122)
 * @param oddRowColor used to fill the odd numbered rows (i.e. 1, 3, 121)
 */
public fun horizontalStripedPaint(rowHeight: Double, evenRowColor: Color? = null, oddRowColor: Color? = null): PatternPaint = stripedPaint(
        rowHeight, evenRowColor, oddRowColor
)

/**
 * Creates a [PatternPaint] that draws an alternating vertical striped pattern.
 *
 * @param colWidth of the alternating columns
 * @param evenRowColor used to fill the even numbered rows (i.e. 0, 2, 122)
 * @param oddRowColor used to fill the odd numbered rows (i.e. 1, 3, 121)
 */
public fun verticalStripedPaint(colWidth: Double, evenRowColor: Color? = null, oddRowColor: Color? = null): PatternPaint = stripedPaint(
        colWidth, evenRowColor, oddRowColor, Identity.rotate(270 * degrees)
)

/**
 * Creates a [PatternPaint] that draws a checkered pattern.
 *
 * @param checkerSize of each rectangle in the checker pattern
 * @param firstColor of the first rectangle, left-to-right
 * @param secondColor of the second rectangle, left-to-right
 */
public fun checkerPaint(checkerSize: Size, firstColor: Color? = null, secondColor: Color? = null): PatternPaint = PatternPaint(if (firstColor.visible || secondColor.visible) checkerSize * 2 else Empty) {
    val w  = checkerSize.width
    val h  = checkerSize.height
    val b1 = firstColor?.let  { ColorPaint(it) }
    val b2 = secondColor?.let { ColorPaint(it) }

    b1?.let { rect(Rectangle(0.0, 0.0, w, h), it) }
    b2?.let { rect(Rectangle(0.0,   h, w, h), it) }
    b2?.let { rect(Rectangle(w,   0.0, w, h), it) }
    b1?.let { rect(Rectangle(w,     h, w, h), it) }
}