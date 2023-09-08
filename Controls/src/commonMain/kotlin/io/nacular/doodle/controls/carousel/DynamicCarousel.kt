package io.nacular.doodle.controls.carousel

import io.nacular.doodle.controls.DynamicListModel
import io.nacular.doodle.controls.ItemVisualizer
import io.nacular.doodle.controls.ModelObserver
import io.nacular.doodle.core.View
import io.nacular.doodle.utils.Pool
import io.nacular.doodle.utils.SetPool
import io.nacular.doodle.utils.diff.Differences

public typealias ItemsObserver<T, M> = (source: DynamicCarousel<T, M>, differences: Differences<T>) -> Unit

/**
 * A [Carousel] that renders and cycles through a potentially mutable list of items of type [T] using a [CarouselBehavior].
 * Items are obtained via the [model] and presented such that a single item is "selected" at any time. Large ("infinite")
 * lists of items are supported efficiently, since Carousel recycles the Views generated to render its items.
 *
 * @param model that holds the data for this Carousel
 * @param itemVisualizer that maps [T] to [View] for each item in the List
 */
public class DynamicCarousel<T, M: DynamicListModel<T>>(
    model         : M,
    itemVisualizer: ItemVisualizer<T, CarouselItem>
): Carousel<T, M>(model, itemVisualizer) {

    private val modelChanged: ModelObserver<T> = { _,diffs ->
        update()

        (itemsChanged as SetPool).forEach { it(this, diffs) }
    }

    /**
     * Notifies of changes to the Carousel
     */
    public val itemsChanged: Pool<ItemsObserver<T, M>> = SetPool()

    init {
        model.changed += modelChanged
    }
}