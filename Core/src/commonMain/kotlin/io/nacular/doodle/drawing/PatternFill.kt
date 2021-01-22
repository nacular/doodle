package io.nacular.doodle.drawing

import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.Size.Companion.Empty
import io.nacular.doodle.geometry.times
import io.nacular.measured.units.Angle.Companion.degrees
import io.nacular.measured.units.times


/**
 * A [Fill] that repeats the contents of its [Canvas] horizontally and vertically within a shape.
 *
 * @author Nicholas Eddy
 *
 * @property bounds of the Canvas that will be repeated
 * @property transform applied to the fill
 * @property fill operations for the Canvas
 */
public class PatternFill(public val bounds: Rectangle, public val transform: AffineTransform = Identity, public val fill: Canvas.() -> Unit): Fill() {
    public constructor(size: Size, transform: AffineTransform = Identity, fill: Canvas.() -> Unit): this(Rectangle(size = size), transform, fill)

    public val size: Size get() = bounds.size

    override val visible: Boolean = !bounds.empty
}

/**
 * Creates a [PatternFill] that draws an alternating horizontal striped pattern.
 *
 * @param stripeWidth of the alternating rows
 * @param evenRowColor used to fill the even numbered rows (i.e. 0, 2, 122)
 * @param oddRowColor used to fill the odd numbered rows (i.e. 1, 3, 121)
 */
public fun stripedFill(stripeWidth : Double,
                 evenRowColor: Color? = null,
                 oddRowColor : Color? = null,
                 transform   : AffineTransform = Identity): PatternFill = PatternFill(Size(if (evenRowColor.visible || oddRowColor.visible) stripeWidth else 0.0, 2 * stripeWidth), transform) {
    evenRowColor?.let { rect(Rectangle(                 stripeWidth, stripeWidth), ColorFill(it)) }
    oddRowColor?.let  { rect(Rectangle(0.0, stripeWidth, stripeWidth, stripeWidth), ColorFill(it)) }
}


/**
 * Creates a [PatternFill] that draws an alternating horizontal striped pattern.
 *
 * @param rowHeight of the alternating rows
 * @param evenRowColor used to fill the even numbered rows (i.e. 0, 2, 122)
 * @param oddRowColor used to fill the odd numbered rows (i.e. 1, 3, 121)
 */
public fun horizontalStripedFill(rowHeight: Double, evenRowColor: Color? = null, oddRowColor: Color? = null): PatternFill = stripedFill(
        rowHeight, evenRowColor, oddRowColor
)

/**
 * Creates a [PatternFill] that draws an alternating vertical striped pattern.
 *
 * @param colWidth of the alternating columns
 * @param evenRowColor used to fill the even numbered rows (i.e. 0, 2, 122)
 * @param oddRowColor used to fill the odd numbered rows (i.e. 1, 3, 121)
 */
public fun verticalStripedFill(colWidth: Double, evenRowColor: Color? = null, oddRowColor: Color? = null): PatternFill = stripedFill(
        colWidth, evenRowColor, oddRowColor, Identity.rotate(270 * degrees)
)

/**
 * Creates a [PatternFill] that draws a checkered pattern.
 *
 * @param checkerSize of each rectangle in the checker pattern
 * @param firstColor of the first rectangle, left-to-right
 * @param secondColor of the second rectangle, left-to-right
 */
public fun checkerFill(checkerSize: Size, firstColor: Color? = null, secondColor: Color? = null): PatternFill = PatternFill(if (firstColor.visible || secondColor.visible) checkerSize * 2 else Empty) {
    val w  = checkerSize.width
    val h  = checkerSize.height
    val b1 = firstColor?.let  { ColorFill(it) }
    val b2 = secondColor?.let { ColorFill(it) }

    b1?.let { rect(Rectangle(0.0, 0.0, w, h), it) }
    b2?.let { rect(Rectangle(0.0,   h, w, h), it) }
    b2?.let { rect(Rectangle(w,   0.0, w, h), it) }
    b1?.let { rect(Rectangle(w,     h, w, h), it) }
}