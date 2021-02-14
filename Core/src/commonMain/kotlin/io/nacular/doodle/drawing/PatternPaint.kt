package io.nacular.doodle.drawing

import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.Size.Companion.Empty
import io.nacular.doodle.geometry.times
import io.nacular.measured.units.Angle.Companion.degrees
import io.nacular.measured.units.times


/**
 * A [Paint] that repeats the contents of its [Canvas] horizontally and vertically within a shape.
 *
 * @author Nicholas Eddy
 *
 * @property bounds of the Canvas that will be repeated
 * @property transform applied to the fill
 * @property paint operations for the Canvas
 */
public class PatternPaint(public val bounds: Rectangle, public val transform: AffineTransform = Identity, public val paint: Canvas.() -> Unit): Paint() {
    public constructor(size: Size, transform: AffineTransform = Identity, fill: Canvas.() -> Unit): this(Rectangle(size = size), transform, fill)

    public val size: Size get() = bounds.size

    override val visible: Boolean = !bounds.empty

    public companion object {
        public operator fun invoke(bounds: Rectangle, transform: AffineTransform = Identity, fill: Canvas.() -> Unit): PatternPaint =
                PatternPaint(bounds, transform, paint = fill)
    }
}

@Deprecated("Use PatternPaint instead", replaceWith = ReplaceWith("PatternPaint"))
public typealias PatternFill = PatternPaint

/**
 * Creates a [PatternFill] that draws an alternating horizontal striped pattern.
 *
 * @param stripeWidth of the alternating rows
 * @param evenRowColor used to fill the even numbered rows (i.e. 0, 2, 122)
 * @param oddRowColor used to fill the odd numbered rows (i.e. 1, 3, 121)
 */
@Deprecated("Use stripedPaint instead", replaceWith = ReplaceWith("stripedPaint"))
public inline fun stripedFill(stripeWidth: Double, evenRowColor: Color? = null, oddRowColor: Color? = null, transform: AffineTransform = Identity): PatternPaint = stripedPaint(stripeWidth, evenRowColor, oddRowColor, transform)

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
        transform   : AffineTransform = Identity): PatternPaint = PatternPaint(Size(if (evenRowColor.visible || oddRowColor.visible) stripeWidth else 0.0, 2 * stripeWidth), transform) {
    evenRowColor?.let { rect(Rectangle(                 stripeWidth, stripeWidth), ColorPaint(it)) }
    oddRowColor?.let  { rect(Rectangle(0.0, stripeWidth, stripeWidth, stripeWidth), ColorPaint(it)) }
}

/**
 * Creates a [PatternPaint] that draws an alternating horizontal striped pattern.
 *
 * @param rowHeight of the alternating rows
 * @param evenRowColor used to fill the even numbered rows (i.e. 0, 2, 122)
 * @param oddRowColor used to fill the odd numbered rows (i.e. 1, 3, 121)
 */
@Deprecated("Use horizontalStripedPaint instead", replaceWith = ReplaceWith("horizontalStripedPaint"))
public inline fun horizontalStripedFill(rowHeight: Double, evenRowColor: Color? = null, oddRowColor: Color? = null): PatternPaint = horizontalStripedPaint(rowHeight, evenRowColor, oddRowColor)

/**
 * Creates a [PatternPaint] that draws an alternating horizontal striped pattern.
 *
 * @param rowHeight of the alternating rows
 * @param evenRowColor used to fill the even numbered rows (i.e. 0, 2, 122)
 * @param oddRowColor used to fill the odd numbered rows (i.e. 1, 3, 121)
 */
public fun horizontalStripedPaint(rowHeight: Double, evenRowColor: Color? = null, oddRowColor: Color? = null): PatternPaint = stripedFill(
        rowHeight, evenRowColor, oddRowColor
)

/**
 * Creates a [PatternPaint] that draws an alternating vertical striped pattern.
 *
 * @param colWidth of the alternating columns
 * @param evenRowColor used to fill the even numbered rows (i.e. 0, 2, 122)
 * @param oddRowColor used to fill the odd numbered rows (i.e. 1, 3, 121)
 */
@Deprecated("Use verticalStripedPaint instead", replaceWith = ReplaceWith("verticalStripedPaint"))
public inline fun verticalStripedFill(colWidth: Double, evenRowColor: Color? = null, oddRowColor: Color? = null): PatternPaint = verticalStripedPaint(colWidth, evenRowColor, oddRowColor)

/**
 * Creates a [PatternPaint] that draws an alternating vertical striped pattern.
 *
 * @param colWidth of the alternating columns
 * @param evenRowColor used to fill the even numbered rows (i.e. 0, 2, 122)
 * @param oddRowColor used to fill the odd numbered rows (i.e. 1, 3, 121)
 */
public fun verticalStripedPaint(colWidth: Double, evenRowColor: Color? = null, oddRowColor: Color? = null): PatternPaint = stripedFill(
        colWidth, evenRowColor, oddRowColor, Identity.rotate(270 * degrees)
)

/**
 * Creates a [PatternFill] that draws a checkered pattern.
 *
 * @param checkerSize of each rectangle in the checker pattern
 * @param firstColor of the first rectangle, left-to-right
 * @param secondColor of the second rectangle, left-to-right
 */
@Deprecated("Use checkerPaint instead", replaceWith = ReplaceWith("checkerPaint"))
public inline fun checkerFill(checkerSize: Size, firstColor: Color? = null, secondColor: Color? = null): PatternPaint = checkerPaint(checkerSize, firstColor, secondColor)

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