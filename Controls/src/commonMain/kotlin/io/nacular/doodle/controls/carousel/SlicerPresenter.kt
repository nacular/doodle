package io.nacular.doodle.controls.carousel

import io.nacular.doodle.controls.carousel.Carousel.PresentedItem
import io.nacular.doodle.core.View
import io.nacular.doodle.core.View.PolyClipPath
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Vector2D
import io.nacular.doodle.layout.constraints.Bounds
import io.nacular.doodle.layout.constraints.ConstraintDslContext
import io.nacular.doodle.layout.constraints.fill
import io.nacular.doodle.utils.Orientation
import io.nacular.doodle.utils.Orientation.Horizontal
import io.nacular.doodle.utils.Orientation.Vertical
import io.nacular.doodle.utils._90
import io.nacular.doodle.utils.lerp
import io.nacular.measured.units.Angle.Companion.sin
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow

public class SlicerPresenter<T>(
                numSlices      : Int = 5,
    private val orientation    : Orientation = Vertical,
                itemConstraints: ConstraintDslContext.(Bounds) -> Unit = fill,
): ConstraintBasedPresenter<T>(itemConstraints) {
    private val numSlices = max(1, numSlices)

    override fun invoke(
        carousel                 : Carousel<T, *>,
        position                 : Position,
        progressToNext           : Float,
        existingSupplementalViews: List<View>,
        item                     : (at: Position) -> PresentedItem?
    ): Presentation {
        val results = mutableListOf<PresentedItem>()

        when (progressToNext) {
            0f -> item(position)?.apply {
                results += this
                setBounds(this) { boundsFromConstraint(this, carousel.size) }
            }
            1f -> position.next?.let(item)?.apply {
                results += this
                setBounds(this) { boundsFromConstraint(this, carousel.size) }
            }
            else -> {
                when (val next = position.next) {
                    null -> item(position)?.apply {
                        results += this
                        setBounds(this) { boundsFromConstraint(this, carousel.size) }
                    }
                    else -> {
                        val sliceWidth  = (if (orientation == Vertical) carousel.width else carousel.height) / numSlices
                        val sliceLength =  if (orientation == Vertical) carousel.height else carousel.width

                        repeat(abs(numSlices)) { slice ->
                            val offset   = lerp(0.0, -sliceLength, sin(_90 * progressToNext.pow(numSlices - (slice - 1))).toFloat())
//                            val offset = lerp(0.0, -carousel.height, sin(90 * degrees * progressToNext.pow(slice + 1)).toFloat())

                            val clipRect = when (orientation) {
                                Vertical -> Rectangle(sliceWidth * slice - 1, offset,             sliceWidth + 1, sliceLength   )
                                else     -> Rectangle(offset,                 sliceWidth * slice, sliceLength,    sliceWidth + 1)
                            }

                            item(position)?.apply {
                                results  += this
                                setBounds(this) {
                                    boundsFromConstraint(this, carousel.size).run {
                                        when (orientation) {
                                            Vertical -> at(y = y + offset)
                                            else     -> at(x = x + offset)
                                        }
                                    }
                                }
                                clipPath  = PolyClipPath(clipRect)
                            }

                            item(next)?.apply {
                                results  += this
                                setBounds(this) {
                                    boundsFromConstraint(this, carousel.size).run {
                                        when (orientation) {
                                            Vertical -> at(y = y + sliceLength + offset)
                                            else     -> at(x = x + sliceLength + offset)
                                        }
                                    }
                                }
                                clipPath = PolyClipPath(
                                    if (orientation == Vertical) clipRect.at(y = offset + sliceLength) else clipRect.at(x = offset + sliceLength)
                                )
                            }
                        }
                    }
                }
            }
        }

        return Presentation(results)
    }

    override fun pathToNext(
        carousel: Carousel<T, *>,
        position: Position,
        offset  : Vector2D,
        item    : (Position) -> PresentedItem?
    ): NextInfo = when (orientation) {
        Horizontal -> NextInfo(Vector2D(x = 1), carousel.width )
        else       -> NextInfo(Vector2D(y = 1), carousel.height)
    }
}