package io.nacular.doodle.animation

import io.nacular.doodle.core.View
import io.nacular.doodle.utils.Completable
import io.nacular.doodle.utils.Pool
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/** A running animation produced by an [Animator] */
public sealed interface Animation: Completable

/**
 * Manages a set of [AnimationPlan]s and updates them over time.
 */
public sealed interface Animator {
    /**
     * Notified of an Animator's events.
     */
    public interface Listener {
        /**
         * Notifies that the values of the given [animations] have changed.
         *
         * @param animator emitting the event
         * @param animations whose values have changed
         */
        public fun changed(animator: Animator, animations: Set<Animation>) {}

        /**
         * Notifies that the given [animations] have been canceled.
         *
         * @param animator emitting the event
         * @param animations that were canceled
         */
        public fun canceled(animator: Animator, animations: Set<Animation>) {}

        /**
         * Notifies that the given [animations] completed.
         *
         * @param animator emitting the event
         * @param animations that completed
         */
        public fun completed(animator: Animator, animations: Set<Animation>) {}
    }

    public abstract class NumericAnimationInfo<T, V> internal constructor()

    /**
     * Allows block-style animations to be defined and started. These animations are then grouped and
     * managed by a top-level [Completable]. Callers are then able to monitor/cancel the entire group
     * using the returned value.
     *
     * ```
     * val animations = animate {
     *     innerAnimation1 = 0f to 1f using = tween(...).invoke {
     *     }
     *
     *     innerAnimation1 = start(customAnimation) {
     *     }
     *     ...
     * }.apply {
     *     completed += ... // called once when all nested animations are done
     * }
     *
     *
     * animations.cancel() // cancels all animations started in the block
     * ```
     */
    public interface AnimationBlock {
        /**
         * Initiates a [NumericAnimationPlan] using the input from [NumericAnimationPlan.invoke]
         *
         * @param animation created from a [NumericAnimationPlan]
         */
        public infix fun <T, V> Pair<T, T>.using(animation: NumericAnimationInfo<T, V>): Animation

        /**
         * Defines the consumption block for a [NumericAnimationPlan] that is
         */
        public operator fun <T, V> NumericAnimationPlan<T, V>.invoke(definitions: (T) -> Unit): NumericAnimationInfo<T, V>

        /**
         * Starts a custom animation
         *
         * @param animation to start
         * @param onChanged notified of changes to the animating value
         */
        public fun <T> start(animation: AnimationPlan<T>, onChanged: (T) -> Unit): Animation
    }

    /**
     * Starts the given [animation] and notifies of changes to the underlying value via [onChanged].
     *
     * @param animation to start
     * @param onChanged is called every time the value within [animation] changes
     * @return a job referencing the ongoing animation
     */
    public operator fun <T> invoke(animation: AnimationPlan<T>, onChanged: (T) -> Unit): Animation

    /**
     * Allows block-style animations to be defined and started.
     *
     * @param definitions of which animations to start
     * @see AnimationBlock
     */
    public operator fun invoke(definitions: AnimationBlock.() -> Unit): Completable

    /**
     * Listeners that are notified of changes to the Animator's running animations
     */
    public val listeners: Pool<Listener>
}

/**
 * Starts an animation from `range.first` to `range.second`.
 *
 * @param range to animate within
 * @param using this animation
 * @param onChanged notified of changes to the animating value
 */
public operator fun <T, V> Animator.invoke(
    range    : Pair<T, T>,
    using    : NumericAnimationPlan<T, V>,
    onChanged: (T) -> Unit
): Animation = this(animation(range.first, range.second, using), onChanged)

// region ================ Animatable Properties ========================

/**
 * Defines a property that can be animated using the given animation.
 *
 * @param default value to initialize the property with
 * @param using the given animation
 * @param onChanged that notifies when the underlying property changes
 */
public operator fun <V: View, T, K> Animator.invoke(default: T, using: NumericAnimationPlan<T, K>, onChanged: (old: T, new: T) -> Unit = { _,_ -> }): ReadWriteProperty<V, T> = animatingProperty(
    default,
    this,
    { start, end -> animation(start, end, using) },
    onChanged
)

/**
 * Creates a [ReadWriteProperty] that will animate to new values.
 *
 * @param default value for the property to begin with
 * @param animator to use for the animation
 * @param animation to use with the start and end values
 * @param onChanged that notifies when the underlying property changes
 */
public fun <V, T> animatingProperty(
    default  : T,
    animator : Animator,
    animation: (start: T, end: T) -> AnimationPlan<T>,
    onChanged: (old: T, new: T) -> Unit = { _,_ -> }
): ReadWriteProperty<V, T> = object: ReadWriteProperty<V, T> {
    private var backingField: T = default
    private var animation: Animation? = null; set(new) {
        field?.cancel()
        field = new
    }

    override operator fun getValue(thisRef: V, property: KProperty<*>): T = backingField

    override operator fun setValue(thisRef: V, property: KProperty<*>, value: T) {
        val old = backingField

        this.animation = animator(animation(old, value)) {
            if (backingField != it) {
                backingField = it
                onChanged(old, it)
            }
        }
    }
}
// endregion