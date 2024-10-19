package io.nacular.doodle.controls.carousel

import io.nacular.doodle.controls.Anchor
import io.nacular.doodle.controls.Anchor.Leading
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

/**
 * Shows contents of a [Carousel] one by one, and "slices" them into ribbons as it transitions between frames.
 *
 * @param numSlices to split the frame into when transitioning
 * @param orientation indicates which direction the items fill travel when transitioning
 * @param itemConstraints that determine the bounds of each item relative to the Carousel
 */
public class SlicerPresenter<T>(
                numSlices      : Int                                   = 5,
    private val orientation    : Orientation                           = Vertical,
    private val anchor         : Anchor                                = Leading,
                itemConstraints: ConstraintDslContext.(Bounds) -> Unit = fill,
): ConstraintBasedPresenter<T>(itemConstraints) {
    private val numSlices = max(1, numSlices)

    override fun present(
        carousel         : Carousel<T, *>,
        position         : Position,
        progressToNext   : Float,
        supplementalViews: List<View>,
        items            : (at: Position) -> PresentedItem?
    ): Presentation {
        val results = mutableListOf<PresentedItem>()

        when (progressToNext) {
            0f -> items(position)?.apply {
                results += this
                setBounds(this, carousel.size)
            }
            1f -> position.next?.let(items)?.apply {
                results += this
                setBounds(this, carousel.size)
            }
            else -> {
                when (val next = position.next) {
                    null -> items(position)?.apply {
                        results += this
                        setBounds(this, carousel.size)
                    }
                    else -> {
                        val sliceWidth  = (if (orientation == Vertical) carousel.width  else carousel.height) / numSlices
                        val sliceLength =  if (orientation == Vertical) carousel.height else carousel.width
                        var bounds      = null as Rectangle?

                        repeat(abs(numSlices)) { slice ->
                            val exponent = when (anchor) {
                                Leading ->              slice + 1
                                else    -> numSlices - (slice - 1)
                            }

                            val offset   = lerp(0.0, -sliceLength, sin(_90 * progressToNext.pow(exponent)).toFloat())

                            val clipRect = when (orientation) {
                                Vertical -> Rectangle(sliceWidth * slice - 1, offset,             sliceWidth + 1, sliceLength   )
                                else     -> Rectangle(offset,                 sliceWidth * slice, sliceLength,    sliceWidth + 1)
                            }

                            items(position)?.apply {
                                results  += this

                                when (bounds) {
                                    null -> setBounds(this, carousel.size) {
                                        bounds = it

                                        it.run {
                                            when (orientation) {
                                                Vertical -> at(y = y + offset)
                                                else     -> at(x = x + offset)
                                            }
                                        }
                                    }
                                    else -> this.bounds = bounds!!.run {
                                        when (orientation) {
                                            Vertical -> at(y = y + offset)
                                            else     -> at(x = x + offset)
                                        }
                                    }
                                }
                                clipPath = PolyClipPath(clipRect)
                            }

                            items(next)?.apply {
                                results  += this
                                when (bounds) {
                                    null -> setBounds(this, carousel.size) {
                                        bounds = it

                                        it.run {
                                            when (orientation) {
                                                Vertical -> at(y = y + sliceLength + offset)
                                                else     -> at(x = x + sliceLength + offset)
                                            }
                                        }
                                    }
                                    else -> this.bounds = bounds!!.run {
                                        when (orientation) {
                                            Vertical -> at(y = y + sliceLength + offset)
                                            else     -> at(x = x + sliceLength + offset)
                                        }
                                    }
                                }
                                clipPath = PolyClipPath(
                                    when (orientation) {
                                        Vertical -> clipRect.at(y = offset + sliceLength)
                                        else     -> clipRect.at(x = offset + sliceLength)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        return Presentation(results)
    }

    override fun distanceToNext(
        carousel: Carousel<T, *>,
        position: Position,
        offset  : Vector2D,
        items   : (Position) -> PresentedItem?
    ): Distance = when (orientation) {
        Horizontal -> Distance(Vector2D(x = 1), carousel.width )
        else       -> Distance(Vector2D(y = 1), carousel.height)
    }
}