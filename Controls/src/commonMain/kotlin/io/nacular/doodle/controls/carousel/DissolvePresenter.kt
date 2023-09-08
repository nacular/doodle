package io.nacular.doodle.controls.carousel

import io.nacular.doodle.controls.carousel.Carousel.PresentedItem
import io.nacular.doodle.core.View
import io.nacular.doodle.geometry.Vector2D
import io.nacular.doodle.layout.constraints.Bounds
import io.nacular.doodle.layout.constraints.ConstraintDslContext
import io.nacular.doodle.layout.constraints.fill
import io.nacular.doodle.utils.lerp

/**
 * Shows contents of a [Carousel] one by one, and does an opacity fade between them as the frame changes.
 *
 * @param itemConstraints that determine the bounds of each item relative to the Carousel
 */
public class DissolvePresenter<T>(itemConstraints: ConstraintDslContext.(Bounds) -> Unit = fill): ConstraintBasedPresenter<T>(itemConstraints) {
    override fun present(
        carousel         : Carousel<T, *>,
        position         : Position,
        progressToNext   : Float,
        supplementalViews: List<View>,
        items            : (Position) -> PresentedItem?
    ): Presentation {
        val results = mutableListOf<PresentedItem>()

        val currentItem = (items(position) ?: items(position.next!!))!!.apply {
            setBounds(this, carousel.size)

            results += this
        }

        when (val next = position.next?.let(items)) {
            null -> currentItem.opacity = 1f
            else -> next.apply {
                results += this
                setBounds(this, carousel.size)

                currentItem.opacity = lerp(1f, 0f, progressToNext)
                opacity             = lerp(0f, 1f, progressToNext)
            }
        }

        return Presentation(items = results)
    }

    override fun distanceToNext(
        carousel: Carousel<T, *>,
        position: Position,
        offset  : Vector2D,
        items   : (Position) -> PresentedItem?
    ): Distance = when {
        offset.x >= offset.y -> Distance(Vector2D(x = 1), carousel.width )
        else                 -> Distance(Vector2D(y = 1), carousel.height)
    }
}