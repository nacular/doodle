package io.nacular.doodle.animation

import io.nacular.doodle.utils.Pausable
import io.nacular.doodle.utils.Pool
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

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
        public fun changed(animator: Animator, animations: Set<Animation<*>>) {}

        /**
         * Notifies that the given [animations] have been canceled.
         *
         * @param animator emitting the event
         * @param animations that were canceled
         */
        public fun canceled(animator: Animator, animations: Set<Animation<*>>) {}

        /**
         * Notifies that the given [animations] completed.
         *
         * @param animator emitting the event
         * @param animations that completed
         */
        public fun completed(animator: Animator, animations: Set<Animation<*>>) {}
    }

    /**
     * Contains data about a numeric animation. This is an intermediate type that is generated
     * in the process of building animations.
     *
     * @see [AnimationBlock]
     */
    public abstract class NumericAnimationInfo<T, V> internal constructor()

    /**
     * Allows block-style animations to be defined and started. These animations are then grouped and
     * managed by a top-level [Animation]. Callers are then able to monitor/cancel the entire group
     * using the returned value.
     *
     * ```
     * val animations = animate {
     *     innerAnimation1 = 0f to 1f using (tween(...)).invoke {
     *     }
     *
     *     innerAnimation2 = start(customAnimation) {
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
         * Initiates an [Animation] from a [NumericAnimationInfo].
         *
         * @param animation info from a [NumericAnimationPlan]
         * @see [NumericAnimationPlan.invoke]
         */
        public infix fun <T, V> Pair<T, T>.using(animation: NumericAnimationInfo<T, V>): Animation<T>

        /**
         * Creates a [NumericAnimationInfo] for a [NumericAnimationPlan].
         *
         * @param onChange notified of changes to the animating value
         */
        public operator fun <T, V> NumericAnimationPlan<T, V>.invoke(onChange: (T) -> Unit): NumericAnimationInfo<T, V>

        /**
         * Starts a custom animation
         *
         * @param animation to start
         * @param onChanged notified of changes to the animating value
         * @return a new animation
         */
        public fun <T> start(animation: AnimationPlan<T>, onChanged: (T) -> Unit): Animation<T>

        /**
         * Starts a new animation block after `this` animation completes. This animation is linked
         * to the current one and will be canceled if the current one is.
         *
         * @param block to execute upon completion of this animation
         * @return a new animation
         */
        public infix fun <T> Animation<T>.then(block: AnimationBlock.() -> Unit): Animation<Any>
    }

    /**
     * Starts the given [animation] and notifies of changes to the underlying value via [onChanged].
     *
     * @param animation to start
     * @param onChanged is called every time the value within [animation] changes
     * @return a job referencing the ongoing animation
     */
    public operator fun <T> invoke(animation: AnimationPlan<T>, onChanged: (T) -> Unit): Animation<T>

    /**
     * Allows block-style animations to be defined and started.
     *
     * @param definitions of which animations to start
     * @see AnimationBlock
     */
    public operator fun invoke(definitions: AnimationBlock.() -> Unit): Animation<Any>

    /**
     * Listeners that are notified of changes to the Animator's running animations
     */
    public val listeners: Pool<Listener>
}

/** A running animation produced by an [Animator] */
public sealed interface Animation<T>: Pausable

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
): Animation<T> = this(animation(range.first, range.second, using), onChanged)

// region ================ Animatable Properties ========================

/**
 * Defines a property that can be animated using the given animation.
 *
 * @param default value to initialize the property with
 * @param using the given animation
 * @param onChanged that notifies when the underlying property changes
 */
public operator fun <V, T, K> Animator.invoke(default: T, using: NumericAnimationPlan<T, K>, onChanged: (old: T, new: T) -> Unit = { _,_ -> }): ReadWriteProperty<V, T> = animatingProperty(
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
    onChanged: (old  : T, new: T) -> Unit = { _,_ -> }
): ReadWriteProperty<V, T> = object: ReadWriteProperty<V, T> {
    private var backingField: T = default
    private var animation: Animation<T>? = null; set(new) {
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