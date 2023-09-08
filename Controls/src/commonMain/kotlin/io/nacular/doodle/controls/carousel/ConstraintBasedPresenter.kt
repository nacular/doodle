package io.nacular.doodle.controls.carousel

import io.nacular.doodle.controls.carousel.Carousel.PresentedItem
import io.nacular.doodle.controls.carousel.CarouselBehavior.Presenter
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.constraints.Bounds
import io.nacular.doodle.layout.constraints.Constrainer
import io.nacular.doodle.layout.constraints.ConstraintDslContext

/**
 * [Presenter] that uses a constraint to determine the selected item's bounds
 * within the [Carousel].
 *
 * @param itemConstraints used to determine the selected item's bounds
 */
public abstract class ConstraintBasedPresenter<T>(
    private val itemConstraints: ConstraintDslContext.(Bounds) -> Unit
): Presenter<T>() {

    /**
     * Sets the bounds of [item] based on the result of [using]. This method
     * tries to handle cases where the item being constrained adjusts its size after
     * a bounds change. This would be the case for an item that maintains an aspect ratio
     * for example. The calculation is run a second time in these cases to allow any
     * constraint to adjust as needed.
     *
     * @param item being presented
     * @param viewPort of the Carousel
     * @param adjust that lets the subclass modify the bounds before it is set
     */
    protected fun setBounds(item: PresentedItem, viewPort: Size, adjust: (Rectangle) -> Rectangle = { it }) {
        val targetBounds = adjust(boundsFromConstraint(item, viewPort))

        item.bounds = targetBounds

        if (item.bounds != targetBounds) {
            item.bounds = adjust(boundsFromConstraint(item, viewPort, forceSetup = true))
        }
    }

    protected fun boundsFromConstraint(
        item       : PresentedItem,
        viewPort   : Size,
        forceSetup : Boolean = false
    ): Rectangle = item.constrainer(
        Rectangle(size = item.size),
        within      = Rectangle(size = viewPort),
        minimumSize = item.minimumSize,
        idealSize   = item.idealSize,
        forceSetup  = forceSetup,
        using       = itemConstraints
    )

    private val PresentedItem.constrainer: Constrainer get() = when (cache) {
        is Constrainer -> (cache as Constrainer)
        else           -> Constrainer().also { cache = it }
    }
}