package io.nacular.doodle.controls.carousel

import io.nacular.doodle.controls.carousel.Carousel.PresentedItem
import io.nacular.doodle.core.View
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.Vector2D
import io.nacular.doodle.geometry.at
import io.nacular.doodle.geometry.lerp
import io.nacular.doodle.layout.constraints.Bounds
import io.nacular.doodle.layout.constraints.ConstraintDslContext
import io.nacular.doodle.layout.constraints.fill
import io.nacular.doodle.utils.Orientation
import io.nacular.doodle.utils.Orientation.Horizontal

/**
 * [Presenter][CarouselBehavior.Presenter] that shows items in a continuous stream with
 * optional spacing between them. Items are positioned in the [Carousel] using [itemConstraints].
 *
 * @param orientation     that determines direction items will lay out
 * @param spacing         between items
 * @param itemConstraints used to determine item bounds within the Carousel
 */
public class LinearPresenter<T>(
    private val orientation    : Orientation = Horizontal,
    private val spacing        : (Size) -> Double = { 0.0 },
                itemConstraints: ConstraintDslContext.(Bounds) -> Unit = fill,
): ConstraintBasedPresenter<T>(itemConstraints) {

    private fun PresentedItem.center(carouselSize: Size) = boundsFromConstraint(this, carouselSize, forceSetup = true)

    private fun leftOf(bounds: Rectangle, rectangle: Rectangle, carouselSize: Size) = bounds.let {
        when (orientation) {
            Horizontal -> it.at(x = rectangle.x - it.width  - spacing(carouselSize))
            else       -> it.at(y = rectangle.y - it.height - spacing(carouselSize))
        }
    }

    private fun rightOf(bounds: Rectangle, rectangle: Rectangle, carouselSize: Size) = bounds.let {
        when (orientation) {
            Horizontal -> it.at(x = rectangle.right  + spacing(carouselSize))
            else       -> it.at(y = rectangle.bottom + spacing(carouselSize))
        }
    }

    override fun present(
        carousel         : Carousel<T, *>,
        position         : Position,
        progressToNext   : Float,
        supplementalViews: List<View>,
    ): Presentation {
        val spacing         = spacing(carousel.size)
        val results         = mutableListOf<PresentedItem>()
        var currentBounds   = null as Rectangle?
        var selectedBounds  = null as Rectangle?
        var currentPosition = position as Position?

        // FIXME: Handle under/over shooting better

        carousel.item(position)?.let { selected ->
            results += selected

            var nextCenter   = null as Rectangle?
            val centerBounds = selected.center(carousel.size)
            currentPosition  = currentPosition?.next

            val nextItem = carousel.item(currentPosition)?.apply {
                nextCenter    = center(carousel.size)
                bounds        = lerp(rightOf(nextCenter, centerBounds, carousel.size), nextCenter, progressToNext)
                currentBounds = bounds
            }?.also {
                if (it.bounds.x < carousel.width) results += it
            }

            selected.bounds = when (nextItem) {
                null -> centerBounds
                else -> lerp(centerBounds, leftOf(centerBounds, nextCenter!!, carousel.size), progressToNext)
            }

            selectedBounds = selected.bounds
        }

        // Add remaining items
        results += currentPosition?.allNext?.asSequence()
            ?.takeWhile  { currentBounds?.run { right < carousel.width - spacing } ?: false }
            ?.mapNotNull {
                carousel.item(it)?.apply {
                    bounds        = rightOf(center(carousel.size), currentBounds!!, carousel.size)
                    currentBounds = bounds
                }
            }?.toList() ?: emptyList()

        currentBounds = selectedBounds

        // Add subsequent items
        results += position.allPrevious.asSequence()
            .takeWhile  { currentBounds?.run { x > spacing } ?: false }
            .mapNotNull {
                carousel.item(it)?.apply {
                    bounds        = leftOf(center(carousel.size), currentBounds!!, carousel.size)
                    currentBounds = bounds
                }
            }.toList()

        return Presentation(items = results)
    }

    override fun distanceToNext(
        carousel: Carousel<T, *>,
        position: Position,
        offset  : Vector2D,
    ): Distance {
        val spacing = spacing(carousel.size)

        val direction = when (orientation) {
            Horizontal -> Vector2D(x = 1)
            else       -> Vector2D(y = 1)
        }

        return when (val current = carousel.item(position)) {
            null -> Distance(direction, 0.0)
            else -> when (val next = position.next?.let(carousel::item)) {
                null -> Distance(direction, 0.0)
                else -> {
                    val initial                    = boundsFromConstraint(current, carousel.size)
                    val nextItemBoundsWhenSelected = boundsFromConstraint(next,    carousel.size)
                    val nextItemBounds             = when (orientation) {
                        Horizontal -> nextItemBoundsWhenSelected.at(x = initial.right  + spacing)
                        else       -> nextItemBoundsWhenSelected.at(y = initial.bottom + spacing)
                    }

                    when (orientation) {
                        Horizontal -> Distance(direction, nextItemBounds.x - nextItemBoundsWhenSelected.x)
                        else       -> Distance(direction, nextItemBounds.y - nextItemBoundsWhenSelected.y)
                    }
                }
            }
        }
    }

    public companion object {
        /**
         * Create a new [LinearPresenter].
         *
         * @see LinearPresenter
         */
        public operator fun <T> invoke(
            orientation    : Orientation = Horizontal,
            spacing        : Double,
            itemConstraints: ConstraintDslContext.(Bounds) -> Unit = fill
        ): LinearPresenter<T> = LinearPresenter(orientation, { spacing }, itemConstraints)
    }
}