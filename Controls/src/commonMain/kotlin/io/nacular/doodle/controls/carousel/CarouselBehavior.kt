package io.nacular.doodle.controls.carousel

import io.nacular.doodle.controls.carousel.Carousel.ItemMarkers
import io.nacular.doodle.controls.carousel.Carousel.PresentedItem
import io.nacular.doodle.controls.carousel.CarouselBehavior.Presenter
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.View
import io.nacular.doodle.geometry.Vector2D
import io.nacular.doodle.utils.Completable
import io.nacular.doodle.utils.NoOpCompletable
import io.nacular.doodle.utils.NoOpPausable
import io.nacular.doodle.utils.Pausable

/**
 * Controls how [Carousel]s behave and are rendered. It provides the [Presenter] used for Carousels as well as
 * controls how they transition between items.
 */
public interface CarouselBehavior<T>: Behavior<Carousel<T, *>> {
    /**
     * Presenters own the core logic that determines what a [Carousel] shows at any given
     * moment. They control which items from a Carousel's internal data are visible, how
     * they are positioned, sized, transformed, etc. based on the Carousel's current
     * state. Presenters can also add supplemental Views to a Carousel that aid in the
     * presentation they provide.
     */
    public abstract class Presenter<T> {
        /**
         * Provides an abstract current position of the [Carousel] that lets the manager
         * "iterate" back and forward.
         */
        public abstract class Position internal constructor(internal val index: Int): Comparable<Position> {
            /**
             * Provides a new position if one is available before this one.
             *
             * NOTE: The position given might be one seen before if wrapping happens
             */
            public abstract val previous: Position?

            /**
             * Provides a new position if one is available after this one.
             *
             * NOTE: The position given might be one seen before if wrapping happens
             */
            public abstract val next: Position?
        }

        /**
         * Defines the list of data items to display in a [Carousel] as well as a list of supplemental [View]s
         * the Carousel should present.
         */
        public data class Presentation(val items: List<PresentedItem>, val supplementalViews: List<View> = emptyList())

        /**
         * Called by [Carousel] to update the [Presentation] it should display.
         *
         * @param carousel being updated
         * @param position indicating the item currently in the "selected" position
         * @param progressToNext indicates how close (percent `[0-1]`) the carousel is to the next position
         * @param supplementalViews list of supplemental Views within the Carousel (based on previous calls)
         * @param items lambda for creating/recycling [PresentedItem]
         * @return a presentation based on the state provided
         */
        public abstract fun present(
            carousel         : Carousel<T, *>,
            position         : Position,
            progressToNext   : Float,
            supplementalViews: List<View>,
            items            : (at: Position) -> PresentedItem?
        ): Presentation

        public class Distance(public val direction: Vector2D, public val magnitude: Double)

        /**
         * A [Carousel] will call this method when trying to do manual movement. This is a fundamentally different
         * problem than it needs to solve when skipping through items (interpolate between indexes). This requires
         * mapping pixel offsets to indexes. Only a Carousel's [Presenter] knows how items are laid out and can provide
         * any information about "distance" between them. Hence, this API.
         *
         * The magnitude of the result should be in pixel scale, since it will be used to decide the relative index
         * for values passed to [Carousel.moveManually].
         *
         * @param carousel being updated
         * @param position indicating the item currently in the "selected" position
         * @param offset from the item at [position]
         * @param items lambda for creating/recycling [PresentedItem]
         * @returns a [Distance] that describes the direction and distance from the current [position] to the next.
         */
        public abstract fun distanceToNext(
            carousel: Carousel<T, *>,
            position: Position,
            offset  : Vector2D,
            items   : (Position) -> PresentedItem?
        ): Distance

        /**
         * Indicate that the stage is outdated and suggest the [Carousel] invoke [present].
         */
        public fun Carousel<T, *>.update(): Unit = update()
    }

    /**
     * Transitioners control how a [Carousel] transitions between items and how it concludes
     * a manual move.
     */
    public interface Transitioner<T> {
        /**
         * Defines how a [Carousel] should transition between items. The default is to instantaneously jump between items.
         * But this API allows for animating this movement.
         *
         * @param carousel in question
         * @param startItem the carousel transitioning from
         * @param endItem the carousel transitioning to
         * @param update called to indicate progress changes to [carousel]
         */
        public fun transition(
            carousel : Carousel<T, *>,
            startItem: Int,
            endItem  : Int,
            update   : (progress: Float) -> Unit
        ): Pausable = NoOpPausable.also { update(1f) }

        /**
         * Notified whenever [carousel] has started a manual move. This is a good point for Transitioners
         * to start tracking movement state if they wish to provide smooth animation when [moveEnded] is called.
         *
         * @param carousel in question
         * @param position of [carousel] in its local coordinates
         */
        public fun moveStarted(carousel: Carousel<T, *>, position: Vector2D) {}

        /**
         * Notified whenever [carousel] has moves during a manual move (called after [moveStarted]).
         * Transitioners can use this to keep track of things like movement velocity if they wish to provide smooth
         * animation when [moveEnded] is called.
         *
         * @param carousel in question
         * @param position of [carousel] in its local coordinates
         */
        public fun moveUpdated(carousel: Carousel<T, *>, position: Vector2D) {}

        /**
         * Notified whenever [carousel] has ended a manual move. This is where a transition animation would take place
         * to smoothly move to the desired end item.
         *
         * The Transitioner should move [carousel] to an item [markers] when the returned [Completable] completes in
         * order to have a smooth transition. Otherwise, the [carousel] will jump to the closest marker upon
         * completion.
         *
         * The [decelerateWhile] lambda is how the Transitioner updates the [carousel]'s position over time. This
         * lambda will return the current item marker offsets based on the new position. This allows the Transitioner
         * to decide which item position to stop at as it transitions.
         *
         * @param carousel in question
         * @param position of [carousel] in its local coordinates at the time of stop
         * @param markers  of the previous and next items at the time of stop
         * @param decelerateWhile is a lambda that should be called to update [carousel]'s position and track the
         * changing item markers to decide when to stop
         */
        public fun moveEnded(
            carousel       : Carousel<T, *>,
            position       : Vector2D,
            markers        : ItemMarkers,
            decelerateWhile: (position: Vector2D) -> ItemMarkers
        ): Completable = NoOpCompletable.also { decelerateWhile(position) }
    }

    /**
     * The [Presenter] Carousels will use when this behavior is installed.
     */
    public val presenter: Presenter<T>

    /**
     * The [Transitioner] Carousels will use when this behavior is installed.
     */
    public val transitioner: Transitioner<T> get() = object: Transitioner<T> {}
}