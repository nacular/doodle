package io.nacular.doodle.animation

import io.nacular.doodle.animation.RepetitionType.Restart
import io.nacular.doodle.animation.transition.EasingFunction
import io.nacular.doodle.animation.transition.TimedEasing
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.utils.zeroMillis
import io.nacular.measured.units.Measure
import io.nacular.measured.units.Time
import io.nacular.measured.units.Time.Companion.milliseconds
import io.nacular.measured.units.Units
import io.nacular.measured.units.times
import kotlin.jvm.JvmName

/**
 * Represents a generic animation of type [T].
 */
public interface AnimationPlan<T> {
    /** Returns the value of the animation after [elapsedTime]. */
    public fun value(elapsedTime: Measure<Time>): T

    /**
     * Returns the velocity of the animation at the [elapsedTime].
     */
    public fun velocity(elapsedTime: Measure<Time>): Velocity<T>

    /**
     * Returns `true` IFF the animation if completed after [elapsedTime]. NOTE: implementations
     * SHOULD NOT return `true` if the animation has not started yet.
     */
    public fun finished(elapsedTime: Measure<Time>): Boolean
}

/**
 * Defines how quickly [T] is changing over a period of time
 *
 * @property change that would occur
 * @property over this given time period
 */
public class Velocity<T>(public val change: T, public val over: Measure<Time>)

/**
 * Low-level animation for values [T] that can be converted to numeric values using a [AnimationDataConverter]. This type is used by higher-level
 * APIs like [animation] and can be created using [tween] for tween animations that leverage [EasingFunction]s.
 */
public interface NumericAnimationPlan<T, V> {
    /** Handles conversions of [T] to and from [V] */
    public val converter: AnimationDataConverter<T, V>

    /**
     * Returns the value of the animation at the [elapsedTime], given the initial conditions.
     *
     * @param start value when the animation began
     * @param end value to animate toward
     * @param initialVelocity when the animation began
     * @param elapsedTime since the animation was created
     */
    public fun value(start: V, end: V, initialVelocity: Velocity<V>, elapsedTime: Measure<Time>): V

    /**
     * Returns the velocity of the animation at the [elapsedTime], given the initial conditions.
     *
     * @param start value when the animation began
     * @param end value to animate toward
     * @param initialVelocity when the animation began
     * @param elapsedTime since the animation was created
     */
    public fun velocity(start: V, end: V, initialVelocity: Velocity<V>, elapsedTime: Measure<Time>): Velocity<V>

    /**
     * Returns `true` IFF the animation if completed after [elapsedTime]. NOTE: implementations
     * SHOULD NOT return `true` if the animation has not started yet.
     *
     * @param start value when the animation began
     * @param end value to animate toward
     * @param initialVelocity when the animation began
     * @param elapsedTime since the animation was created
     */
    public fun finished(start: V, end: V, initialVelocity: Velocity<V>, elapsedTime: Measure<Time>): Boolean
}

/**
 * Represents and animation that runs for a finite amount of time.
 */
public interface FiniteNumericAnimationPlan<T, V>: NumericAnimationPlan<T, V> {
    /**
     * Returns the duration of this animation including any delay.
     *
     * @param start value when the animation began
     * @param end value to animate toward
     * @param initialVelocity when the animation began
     * @return the animation's duration, including any delay
     */
    public fun duration(start: V, end: V, initialVelocity: Velocity<V>): Measure<Time>

    override fun finished(start: V, end: V, initialVelocity: Velocity<V>, elapsedTime: Measure<Time>): Boolean = elapsedTime > duration(start, end, initialVelocity)
}

/**
 * Creates an animation based on [animationPlan] that goes from [start] to [end]. This allows the animation of
 * any [T] that can be animated using a [NumericAnimationPlan].
 *
 * @param start value to begin animating from
 * @param end value to animate to
 * @param animationPlan used to drive the animation
 * @param initialVelocity at the start of the animation
 */
public fun <T, V> animation(
    start          : T,
    end            : T,
    animationPlan  : NumericAnimationPlan<T, V>,
    initialVelocity: Velocity<T>? = null,
): AnimationPlan<T> = object: AnimationPlan<T> {
    private val converter   = animationPlan.converter
    private val startDouble = converter.serialize(start)
    private val endDouble   = converter.serialize(end)
    private val velocity    = (initialVelocity ?: Velocity(converter.zero, 1 * milliseconds)).let { Velocity(converter.serialize(it.change), it.over) }

    override fun value   (elapsedTime: Measure<Time>) = converter.deserialize(animationPlan.value(startDouble, endDouble, velocity, elapsedTime))
    override fun velocity(elapsedTime: Measure<Time>) = animationPlan.velocity(startDouble, endDouble, velocity, elapsedTime).run { Velocity(converter.deserialize(change), over) }
    override fun finished(elapsedTime: Measure<Time>) = animationPlan.finished(startDouble, endDouble, velocity, elapsedTime) //elapsedTime >= animationPlan.duration(startDouble, endDouble, velocity)
}

// region ================ Tween ========================

/**
 * Creates an animation that manipulates values of type [T] that are convertable to [Double].
 *
 * @param converter that maps [T] to and from [Double]
 * @param easing used to animate the numeric representation
 * @param duration of the animation
 * @param delay to apply before the animation begins
 */
public fun <T> tween(
    converter: SingleDataConverter<T>,
    easing   : EasingFunction,
    duration : Measure<Time>,
    delay    : Measure<Time> = zeroMillis,
): FiniteNumericAnimationPlan<T, Double> = NumericAnimationPlanImpl(delay, converter, TimedEasing(duration, easing))

/** @see tween */
public fun tweenInt(
    easing   : EasingFunction,
    duration : Measure<Time>,
    delay    : Measure<Time> = zeroMillis,
): FiniteNumericAnimationPlan<Int, Double> = tween(Int.animationConverter, easing, duration, delay)

/** @see tween */
public fun tweenFloat(
    easing   : EasingFunction,
    duration : Measure<Time>,
    delay    : Measure<Time> = zeroMillis,
): FiniteNumericAnimationPlan<Float, Double> = tween(Float.animationConverter, easing, duration, delay)

/** @see tween */
public fun tweenDouble(
    easing   : EasingFunction,
    duration : Measure<Time>,
    delay    : Measure<Time> = zeroMillis,
): FiniteNumericAnimationPlan<Double, Double> = tween(Double.animationConverter, easing, duration, delay)

/** @see tween */
public fun <T: Units> tween(
    units    : T,
    easing   : EasingFunction,
    duration : Measure<Time>,
    delay    : Measure<Time> = zeroMillis,
): FiniteNumericAnimationPlan<Measure<T>, Double> = tween(units.animationConverter, easing, duration, delay)

/**
 * Defines an [easing] over some [duration].
 *
 * @property easing function
 * @property duration of the easing
 */
public data class Easing(val easing: EasingFunction, val duration : Measure<Time>)

/**
 * Creates an animation that manipulates values of type [T] that are convertable to an array of [Double]. Each dimension of [T]
 * will be animated using a [Easing] returned by the [easings] function.
 *
 * @param converter that maps [T] to and from `Array<Double>`
 * @param easings that map to each dimension of [T]. The [Easing] at index `i` is used to animate the dimension at the same index.
 * @param delay to apply before the animation begins
 */
public fun <T> tween(
    converter: MultiDataConverter<T>,
    easings  : (dimension: Int) -> Easing,
    delay    : Measure<Time> = zeroMillis
): FiniteNumericAnimationPlan<T, Array<Double>> = MultiNumericAnimationPlanImpl(delay, converter, easings)

/** @see tween */
public fun tweenPoint(
    easing  : EasingFunction,
    duration: Measure<Time>,
    delay   : Measure<Time> = zeroMillis,
): FiniteNumericAnimationPlan<Point, Array<Double>> = tween(Point.animationConverter, { Easing(easing, duration) }, delay)

/** @see tween */
public fun tweenPoint(
    easings: (dimension: Int) -> Easing,
    delay  : Measure<Time> = zeroMillis
): FiniteNumericAnimationPlan<Point, Array<Double>> = tween(Point.animationConverter, easings, delay)

/** @see tween */
public fun tweenSize(
    easing  : EasingFunction,
    duration: Measure<Time>,
    delay   : Measure<Time> = zeroMillis,
): FiniteNumericAnimationPlan<Size, Array<Double>> = tween(Size.animationConverter, { Easing(easing, duration) }, delay)

/** @see tween */
public fun tweenSize(
    easings: (dimension: Int) -> Easing,
    delay  : Measure<Time> = zeroMillis
): FiniteNumericAnimationPlan<Size, Array<Double>> = tween(Size.animationConverter, easings, delay)

/** @see tween */
public fun tweenRect(
    easing  : EasingFunction,
    duration: Measure<Time>,
    delay   : Measure<Time> = zeroMillis,
): FiniteNumericAnimationPlan<Rectangle, Array<Double>> = tween(Rectangle.animationConverter, { Easing(easing, duration) }, delay)

/** @see tween */
public fun tweenRect(
    easings: (dimension: Int) -> Easing,
    delay  : Measure<Time> = zeroMillis
): FiniteNumericAnimationPlan<Rectangle, Array<Double>> = tween(Rectangle.animationConverter, easings, delay)

/** @see tween */
public fun tweenColor(
    easing  : EasingFunction,
    duration: Measure<Time>,
    delay   : Measure<Time> = zeroMillis,
): FiniteNumericAnimationPlan<Color, Array<Double>> = tween(Color.animationConverter, { Easing(easing, duration) }, delay)

/** @see tween */
public fun tweenColor(
    easings: (dimension: Int) -> Easing,
    delay  : Measure<Time> = zeroMillis
): FiniteNumericAnimationPlan<Color, Array<Double>> = tween(Color.animationConverter, easings, delay)

// endregion

// region ================ Key Frames ========================

/**
 * Configuration block used when constructing key-frame animations.
 */
public interface KeyFrameBlock<T> {
    /**
     * Defines a single frame within a key-frame animation.
     */
    public sealed interface Frame<T> {
        /**
         * Specifies how to transition to the subsequent frame from this one.
         *
         * @param easing used to transition
         */
        public infix fun then(easing: EasingFunction)
    }

    /**
     * Specifies the frame for the animation at [timeStamp].
     *
     * @param timeStamp for the total animation
     * @return the frame
     */
    public infix fun T.at(timeStamp: Measure<Time>): Frame<T>

    /**
     * Specifies the frame for the animation at [fraction] of the total animation time.
     *
     * @param fraction of the total animation time
     * @return the frame
     */
    public infix fun T.at(fraction: Float): Frame<T>
}

/**
 * Creates an animation that manipulates values of type [T] that are convertable to a [Double].
 *
 * ```
 * keyFrames(converter, duration) {
 *     value1 at duration * 0   then easeInElastic
 *     value2 at duration * 1/3 then linear
 *     value3 at duration * 2/3 then easeOutBack
 * }
 * ```
 *
 * @param converter that maps [T] to and from `Array<Double>`
 * @param duration of the animation
 * @param delay to apply before the animation begins
 * @param frames defining the animation key points
 */
@JvmName("keyFramesSingle")
public fun <T> keyFrames(
    converter: SingleDataConverter<T>,
    duration : Measure<Time>,
    delay    : Measure<Time> = zeroMillis,
    frames   : KeyFrameBlock<T>.() -> Unit
): FiniteNumericAnimationPlan<T, Double> = SingleKeyframeAnimationPlan(converter, duration, delay, frames)

/** @see keyFrames */
public fun keyFramesInt(
    duration : Measure<Time>,
    delay    : Measure<Time> = zeroMillis,
    frames   : KeyFrameBlock<Int>.() -> Unit
): FiniteNumericAnimationPlan<Int, Double> = keyFrames(Int.animationConverter, duration, delay, frames)

/** @see keyFrames */
public fun keyFramesFloat(
    duration : Measure<Time>,
    delay    : Measure<Time> = zeroMillis,
    frames   : KeyFrameBlock<Float>.() -> Unit
): FiniteNumericAnimationPlan<Float, Double> = keyFrames(Float.animationConverter, duration, delay, frames)

/** @see keyFrames */
public fun keyFramesDouble(
    duration : Measure<Time>,
    delay    : Measure<Time> = zeroMillis,
    frames   : KeyFrameBlock<Double>.() -> Unit
): FiniteNumericAnimationPlan<Double, Double> = keyFrames(Double.animationConverter, duration, delay, frames)

/**
 * Creates an animation that manipulates values of type [T] that are convertable to an array of [Double]. Each dimension of [T]
 * will be animated using the definitions within [frames].
 *
 * @param converter that maps [T] to and from `Array<Double>`
 * @param duration of the animation
 * @param delay to apply before the animation begins
 * @param frames defining the animation key points
 * @see keyFrames
 */
@JvmName("keyFramesMulti")
public fun <T> keyFrames(
    converter: MultiDataConverter<T>,
    duration : Measure<Time>,
    delay    : Measure<Time> = zeroMillis,
    frames   : KeyFrameBlock<T>.() -> Unit
): FiniteNumericAnimationPlan<T, Array<Double>> = MultiKeyframeAnimationPlan(converter, duration, delay, frames)

/** @see keyFrames */
public fun keyFramesPoint(
    duration : Measure<Time>,
    delay    : Measure<Time> = zeroMillis,
    frames   : KeyFrameBlock<Point>.() -> Unit
): FiniteNumericAnimationPlan<Point, Array<Double>> = keyFrames(Point.animationConverter, duration, delay, frames)

/** @see keyFrames */
public fun keyFramesSize(
    duration : Measure<Time>,
    delay    : Measure<Time> = zeroMillis,
    frames   : KeyFrameBlock<Size>.() -> Unit
): FiniteNumericAnimationPlan<Size, Array<Double>> = keyFrames(Size.animationConverter, duration, delay, frames)

/** @see keyFrames */
public fun keyFramesRect(
    duration : Measure<Time>,
    delay    : Measure<Time> = zeroMillis,
    frames   : KeyFrameBlock<Rectangle>.() -> Unit
): FiniteNumericAnimationPlan<Rectangle, Array<Double>> = keyFrames(Rectangle.animationConverter, duration, delay, frames)

/** @see keyFrames */
public fun keyFramesColor(
    duration : Measure<Time>,
    delay    : Measure<Time> = zeroMillis,
    frames   : KeyFrameBlock<Color>.() -> Unit
): FiniteNumericAnimationPlan<Color, Array<Double>> = keyFrames(Color.animationConverter, duration, delay, frames)

// endregion

// region ================ Repetition ========================

/**
 * Defines the kind of repetition an animation can have
 */
public enum class RepetitionType {
    /**
     * The animation will start from the beginning when it repeats.
     */
    Restart,

    /**
     * The animation will reverse from its current state when it repeats.
     */
    Reverse
}

/**
 * Repeats the given [animationPlan] for the specified number of [times].
 *
 * @param animationPlan to repeat
 * @param times to repeat
 * @param type of repetition, [Restart] will start over while [Reverse][RepetitionType.Reverse] will reverse
 * @param delay to apply before the animation begins
 */
@JvmName("repeatSingle")
public fun <T> repeat(
    animationPlan: FiniteNumericAnimationPlan<T, Double>,
    times        : Int = 1,
    type         : RepetitionType = Restart,
    delay        : Measure<Time> = zeroMillis
): FiniteNumericAnimationPlan<T, Double> = object: FiniteRepeatingAnimationPlan<T, Double>(animationPlan, type, times, delay) {
    override fun duration(start: Double, end: Double, initialVelocity: Velocity<Double>): Measure<Time> = (times + 1) * iterationDuration(
        start,
        end,
        initialVelocity
    ) + delay
}

/**
 * Repeats the given [animationPlan] for the specified number of [times].
 *
 * @param animationPlan to repeat
 * @param times to repeat
 * @param type of repetition, [Restart] will start over while [Reverse][RepetitionType.Reverse] will reverse
 * @param delay to apply before the animation begins
 */
@JvmName("repeatMulti")
public fun <T> repeat(
    animationPlan: FiniteNumericAnimationPlan<T, Array<Double>>,
    times        : Int            = 1,
    type         : RepetitionType = Restart,
    delay        : Measure<Time> = zeroMillis
): FiniteNumericAnimationPlan<T, Array<Double>> = object: FiniteRepeatingAnimationPlan<T, Array<Double>>(animationPlan, type, times, delay) {
    override fun duration(start: Array<Double>, end: Array<Double>, initialVelocity: Velocity<Array<Double>>): Measure<Time> = (times + 1) * iterationDuration(
        start,
        end,
        initialVelocity
    ) + delay
}

/**
 * Loops the given [animationPlan] indefinitely.
 *
 * @param animationPlan to repeat
 * @param type of repetition, [Restart] will start over while [Reverse][RepetitionType.Reverse] will reverse
 * @param delay to apply before the animation begins
 */
@JvmName("loopSingle")
public fun <T> loop(
    animationPlan: FiniteNumericAnimationPlan<T, Double>,
    type         : RepetitionType = Restart,
    delay        : Measure<Time> = zeroMillis
): NumericAnimationPlan<T, Double> = object: RepeatingAnimationPlan<T, Double>(animationPlan, type, Int.MAX_VALUE, delay) {
    override fun finished(start: Double, end: Double, initialVelocity: Velocity<Double>, elapsedTime: Measure<Time>) = false
}

/**
 * Loops the given [animationPlan] indefinitely.
 *
 * @param animationPlan to repeat
 * @param type of repetition, [Restart] will start over while [Reverse][RepetitionType.Reverse] will reverse
 * @param delay to apply before the animation begins
 */
@JvmName("loopMulti")
public fun <T> loop(
    animationPlan: FiniteNumericAnimationPlan<T, Array<Double>>,
    type         : RepetitionType = Restart,
    delay        : Measure<Time> = zeroMillis
): NumericAnimationPlan<T, Array<Double>> = object: RepeatingAnimationPlan<T, Array<Double>>(animationPlan, type, Int.MAX_VALUE, delay) {
    override fun finished(start: Array<Double>, end: Array<Double>, initialVelocity: Velocity<Array<Double>>, elapsedTime: Measure<Time>) = false
}

// endregion