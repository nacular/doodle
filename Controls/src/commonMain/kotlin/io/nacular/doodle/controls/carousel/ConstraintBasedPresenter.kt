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

    private val constrainer = Constrainer()

    /**
     * Sets the bounds of [item] based on the result of [using]. This method
     * tries to handle cases where the item being constrained adjusts its size after
     * a bounds change. This would be the case for an item that maintains an aspect ratio
     * for example. The calculation is run a second time in these cases to allow any
     * constraint to adjust as needed.
     *
     * @param item being presented
     * @param using the given lambda
     */
    protected fun setBounds(item: PresentedItem, using: PresentedItem.() -> Rectangle) {
        val targetBounds = using(item)

        item.bounds = targetBounds

        if (item.bounds != targetBounds) {
            item.bounds = using(item)
        }
    }

    /**
     * Calculates the bounds for [item] based on [itemConstraints].
     *
     * @param item bounds being calculated for
     * @param viewPort of the Carousel
     */
    protected fun boundsFromConstraint(item: PresentedItem, viewPort: Size): Rectangle = constrainer(
        Rectangle(size = item.size),
        within      = Rectangle(size = viewPort),
        minimumSize = item.minimumSize,
        idealSize   = item.idealSize,
        using       = itemConstraints
    )
}