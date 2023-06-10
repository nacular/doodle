package io.nacular.doodle.controls.carousel

import io.nacular.doodle.controls.carousel.Carousel.PresentedItem
import io.nacular.doodle.core.View
import io.nacular.doodle.geometry.Vector2D
import io.nacular.doodle.layout.constraints.Bounds
import io.nacular.doodle.layout.constraints.ConstraintDslContext
import io.nacular.doodle.layout.constraints.fill
import io.nacular.doodle.utils.lerp


public class DissolvePresenter<T>(itemConstraints: ConstraintDslContext.(Bounds) -> Unit = fill): ConstraintBasedPresenter<T>(itemConstraints) {
    override fun invoke(
        carousel                 : Carousel<T, *>,
        position                 : Position,
        progressToNext           : Float,
        existingSupplementalViews: List<View>,
        item              : (Position) -> PresentedItem?
    ): Presentation {
        val results = mutableListOf<PresentedItem>()

        val currentItem = (item(position) ?: item(position.next!!))!!.apply {
            setBounds(this) { boundsFromConstraint(this, carousel.size) }

            results += this
        }

        when (val next = position.next?.let(item)) {
            null -> currentItem.opacity = 1f
            else -> next.apply {
                results += this
                setBounds(this) { boundsFromConstraint(this, carousel.size) }

                currentItem.opacity = lerp(1f, 0f, progressToNext)
                opacity             = lerp(0f, 1f, progressToNext)
            }
        }

        return Presentation(items = results)
    }

    override fun pathToNext(
        carousel: Carousel<T, *>,
        position: Position,
        offset  : Vector2D,
        item    : (Position) -> PresentedItem?
    ): NextInfo = when {
        offset.x >= offset.y -> NextInfo(Vector2D(x = 1), carousel.width )
        else                 -> NextInfo(Vector2D(y = 1), carousel.height)
    }
}