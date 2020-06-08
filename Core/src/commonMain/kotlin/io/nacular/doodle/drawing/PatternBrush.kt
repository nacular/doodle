package io.nacular.doodle.drawing

import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.Size.Companion.Empty
import io.nacular.doodle.geometry.times
import com.nectar.measured.units.Angle.Companion.degrees
import com.nectar.measured.units.times


/**
 * A [Brush] that repeats the contents of its [Canvas] horizontally and vertically within a shape.
 *
 * @author Nicholas Eddy
 *
 * @property bounds of the Canvas that will be repeated
 * @property transform applied to the brush
 * @property fill operations for the Canvas
 */
class PatternBrush(val bounds: Rectangle, val transform: AffineTransform = Identity, val fill: Canvas.() -> Unit): Brush() {
    constructor(size: Size, transform: AffineTransform = Identity, fill: Canvas.() -> Unit): this(Rectangle(size = size), transform, fill)

    val size get() = bounds.size

    override val visible = !bounds.empty
}

/**
 * Creates a [PatternBrush] that draws an alternating horizontal striped pattern.
 *
 * @param stripWidth of the alternating rows
 * @param evenRowColor used to fill the even numbered rows (i.e. 0, 2, 122)
 * @param oddRowColor used to fill the odd numbered rows (i.e. 1, 3, 121)
 */
fun stripedBrush(stripWidth  : Double,
                 evenRowColor: Color? = null,
                 oddRowColor : Color? = null,
                 transform   : AffineTransform = Identity) = PatternBrush(Size(if (evenRowColor.visible || oddRowColor.visible) stripWidth else 0.0, 2 * stripWidth), transform) {
    evenRowColor?.let { rect(Rectangle(                 stripWidth, stripWidth), ColorBrush(it)) }
    oddRowColor?.let  { rect(Rectangle(0.0, stripWidth, stripWidth, stripWidth), ColorBrush(it)) }
}


/**
 * Creates a [PatternBrush] that draws an alternating horizontal striped pattern.
 *
 * @param rowHeight of the alternating rows
 * @param evenRowColor used to fill the even numbered rows (i.e. 0, 2, 122)
 * @param oddRowColor used to fill the odd numbered rows (i.e. 1, 3, 121)
 */
fun horizontalStripedBrush(rowHeight: Double, evenRowColor: Color? = null, oddRowColor: Color? = null) = stripedBrush(
        rowHeight, evenRowColor, oddRowColor
)

/**
 * Creates a [PatternBrush] that draws an alternating vertical striped pattern.
 *
 * @param colWidth of the alternating columns
 * @param evenRowColor used to fill the even numbered rows (i.e. 0, 2, 122)
 * @param oddRowColor used to fill the odd numbered rows (i.e. 1, 3, 121)
 */
fun verticalStripedBrush(colWidth: Double, evenRowColor: Color? = null, oddRowColor: Color? = null) = stripedBrush(
        colWidth, evenRowColor, oddRowColor, Identity.rotate(90 * degrees)
)

/**
 * Creates a [PatternBrush] that draws a checkered pattern.
 *
 * @param checkerSize of each rectangle in the checker pattern
 * @param firstColor of the first rectangle, left-to-right
 * @param secondColor of the second rectangle, left-to-right
 */
fun checkerBrush(checkerSize: Size, firstColor: Color? = null, secondColor: Color? = null) = PatternBrush(if (firstColor.visible || secondColor.visible) checkerSize * 2 else Empty) {
    val w  = checkerSize.width
    val h  = checkerSize.height
    val b1 = firstColor?.let  { ColorBrush(it) }
    val b2 = secondColor?.let { ColorBrush(it) }

    b1?.let { rect(Rectangle(0.0, 0.0, w, h), it) }
    b2?.let { rect(Rectangle(0.0,   h, w, h), it) }
    b2?.let { rect(Rectangle(w,   0.0, w, h), it) }
    b1?.let { rect(Rectangle(w,     h, w, h), it) }
}