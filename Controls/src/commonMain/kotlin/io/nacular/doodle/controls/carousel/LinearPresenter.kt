package io.nacular.doodle.controls.carousel

import io.nacular.doodle.controls.carousel.Carousel.PresentedItem
import io.nacular.doodle.core.View
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.Vector2D
import io.nacular.doodle.layout.constraints.Bounds
import io.nacular.doodle.layout.constraints.ConstraintDslContext
import io.nacular.doodle.layout.constraints.fill
import io.nacular.doodle.utils.Orientation
import io.nacular.doodle.utils.Orientation.Horizontal
import io.nacular.doodle.utils.lerp

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
    private val spacing        : Double = 0.0,
                itemConstraints: ConstraintDslContext.(Bounds) -> Unit = fill,
): ConstraintBasedPresenter<T>(itemConstraints) {

    override fun invoke(
        carousel                 : Carousel<T, *>,
        position                 : Position,
        progressToNext           : Float,
        existingSupplementalViews: List<View>,
        item                     : (Position) -> PresentedItem?
    ): Presentation {
        val results = mutableListOf<PresentedItem>()

        var currentPosition = position

        var firstBounds   = null as Rectangle?
        var currentBounds = null as Rectangle?
        var nextItem      = null as PresentedItem?

        // FIXME: Handle under/over shooting better

        do {
            results.add((nextItem ?: (item(currentPosition) ?: (item(currentPosition.next!!.also { currentPosition = it }))))!!.apply {
                bounds = boundsFromConstraint(this, carousel.size)

                bounds = when (val b = currentBounds) {
                    null -> {
                        nextItem = currentPosition.next?.let(item)

                        when (val n = nextItem) {
                            null -> boundsFromConstraint(this, carousel.size)
                            else -> calcBounds(this, n, carousel.size, progressToNext)
                        }.also { firstBounds = it }
                    }
                    else -> {
                        nextItem = null
                        boundsFromConstraint(this, carousel.size).run {
                            when (orientation) {
                                Horizontal -> at(x = b.right  + spacing)
                                else       -> at(y = b.bottom + spacing)
                            }
                        }
                    }
                }

                currentBounds = bounds
            })
        } while (
            currentBounds!!.farEdge + spacing < carousel.size.extent &&
            currentPosition.next?.let {
                currentPosition = it
                if (it == position && currentBounds!!.x == firstBounds!!.x) null else it
            } != null
        )

        currentBounds   = firstBounds
        currentPosition = position

        while (
            currentBounds!!.offset - spacing > 0 &&
            currentPosition.previous?.let {
                currentPosition = it
                if (it == position && currentBounds!!.x == firstBounds!!.x) null else it
            } != null
        ) {
            results.add(item(currentPosition)!!.apply {
                setBounds(this) {
                    boundsFromConstraint(this, carousel.size).run {
                        when (orientation) {
                            Horizontal -> at(x = currentBounds!!.x - width  - spacing)
                            else       -> at(y = currentBounds!!.y - height - spacing)
                        }
                    }
                }

                currentBounds = bounds
            })
        }

        return Presentation(items = results)
    }

    override fun pathToNext(
        carousel: Carousel<T, *>,
        position: Position,
        offset  : Vector2D,
        item    : (Position) -> PresentedItem?
    ): NextInfo {
        val direction = when (orientation) {
            Horizontal -> Vector2D(x = 1)
            else       -> Vector2D(y = 1)
        }

        return when (val current = item(position)) {
            null -> NextInfo(direction, 0.0)
            else -> when (val next = position.next?.let(item)) {
                null -> NextInfo(direction, 0.0)
                else -> {
                    val initial                    = boundsFromConstraint(current, carousel.size)
                    val nextItemBoundsWhenSelected = boundsFromConstraint(next, carousel.size)
                    val nextItemBounds             = when (orientation) {
                        Horizontal -> nextItemBoundsWhenSelected.at(x = initial.right  + spacing)
                        else       -> nextItemBoundsWhenSelected.at(y = initial.bottom + spacing)
                    }

                    when (orientation) {
                        Horizontal -> NextInfo(direction, nextItemBounds.x - nextItemBoundsWhenSelected.x)
                        else       -> NextInfo(direction, nextItemBounds.y - nextItemBoundsWhenSelected.y)
                    }
                }
            }
        }
    }

    private val Size.extent: Double get() = when (orientation) {
        Horizontal -> width
        else       -> height
    }

    private val Rectangle.offset: Double get() = when (orientation) {
        Horizontal -> x
        else       -> y
    }

    private val Rectangle.farEdge: Double get() = when (orientation) {
        Horizontal -> right
        else       -> bottom
    }

    private fun calcBounds(item: PresentedItem, nextItem: PresentedItem, viewPort: Size, progressToNext: Float): Rectangle {
        val initial = boundsFromConstraint(item, viewPort)

        if (progressToNext == 0f) {
            return initial
        }

        val nextItemBoundsWhenSelected = boundsFromConstraint(nextItem, viewPort)
        val nextItemBounds             = when (orientation) {
            Horizontal -> nextItemBoundsWhenSelected.at(x = initial.right  + spacing)
            else       -> nextItemBoundsWhenSelected.at(y = initial.bottom + spacing)
        }

        return when (orientation) {
            Horizontal -> initial.at(x = lerp(initial.x, initial.x - (nextItemBounds.x - nextItemBoundsWhenSelected.x), progressToNext))
            else       -> initial.at(y = lerp(initial.y, initial.y - (nextItemBounds.y - nextItemBoundsWhenSelected.y), progressToNext))
        }
    }
}