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
 */
public class Velocity<T>(public val change: T, public val over: Measure<Time>)

/**
 * Low-level animation for values [T] that can be converted to numeric values using a [AnimationDataConverter]. This type is used by higher-level
 * APIs like [animation] and can be created using [tween] for tween animations that leverage [EasingFunction]s.
 */
public sealed interface NumericAnimationPlan<T, V> {
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
     * Returns the duration of this animation including any delay.
     *
     * @param start value when the animation began
     * @param end value to animate toward
     * @param initialVelocity when the animation began
     * @return the animation's duration, including any delay
     */
    public fun duration(start: V, end: V, initialVelocity: Velocity<V>): Measure<Time>
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
    override fun finished(elapsedTime: Measure<Time>) = elapsedTime >= animationPlan.duration(startDouble, endDouble, velocity)
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
    converter: AnimationDataConverter<T, Double>,
    easing   : EasingFunction,
    duration : Measure<Time>,
    delay    : Measure<Time> = zeroMillis,
): NumericAnimationPlan<T, Double> = NumericAnimationPlanImpl(delay, converter, TimedEasing(duration, easing))

/** @see tween */
public fun tweenInt(
    easing   : EasingFunction,
    duration : Measure<Time>,
    delay    : Measure<Time> = zeroMillis,
): NumericAnimationPlan<Int, Double> = tween(Int.animationConverter, easing, duration, delay)

/** @see tween */
public fun tweenFloat(
    easing   : EasingFunction,
    duration : Measure<Time>,
    delay    : Measure<Time> = zeroMillis,
): NumericAnimationPlan<Float, Double> = tween(Float.animationConverter, easing, duration, delay)

/** @see tween */
public fun tweenDouble(
    easing   : EasingFunction,
    duration : Measure<Time>,
    delay    : Measure<Time> = zeroMillis,
): NumericAnimationPlan<Double, Double> = tween(Double.animationConverter, easing, duration, delay)

/** @see tween */
public fun <T: Units> tween(
    units    : T,
    easing   : EasingFunction,
    duration : Measure<Time>,
    delay    : Measure<Time> = zeroMillis,
): NumericAnimationPlan<Measure<T>, Double> = tween(units.animationConverter, easing, duration, delay)

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
): NumericAnimationPlan<T, Array<Double>> = MultiNumericAnimationPlanImpl(delay, converter, easings)

/** @see tween */
public fun tweenPoint(
    easing  : EasingFunction,
    duration: Measure<Time>,
    delay   : Measure<Time> = zeroMillis,
): NumericAnimationPlan<Point, Array<Double>> = tween(Point.animationConverter, { Easing(easing, duration) }, delay)

/** @see tween */
public fun tweenPoint(
    easings: (dimension: Int) -> Easing,
    delay  : Measure<Time> = zeroMillis
): NumericAnimationPlan<Point, Array<Double>> = tween(Point.animationConverter, easings, delay)

/** @see tween */
public fun tweenSize(
    easing  : EasingFunction,
    duration: Measure<Time>,
    delay   : Measure<Time> = zeroMillis,
): NumericAnimationPlan<Size, Array<Double>> = tween(Size.animationConverter, { Easing(easing, duration) }, delay)

/** @see tween */
public fun tweenSize(
    easings: (dimension: Int) -> Easing,
    delay  : Measure<Time> = zeroMillis
): NumericAnimationPlan<Size, Array<Double>> = tween(Size.animationConverter, easings, delay)

/** @see tween */
public fun tweenRect(
    easing  : EasingFunction,
    duration: Measure<Time>,
    delay   : Measure<Time> = zeroMillis,
): NumericAnimationPlan<Rectangle, Array<Double>> = tween(Rectangle.animationConverter, { Easing(easing, duration) }, delay)

/** @see tween */
public fun tweenRect(
    easings: (dimension: Int) -> Easing,
    delay  : Measure<Time> = zeroMillis
): NumericAnimationPlan<Rectangle, Array<Double>> = tween(Rectangle.animationConverter, easings, delay)

/** @see tween */
public fun tweenColor(
    easing  : EasingFunction,
    duration: Measure<Time>,
    delay   : Measure<Time> = zeroMillis,
): NumericAnimationPlan<Color, Array<Double>> = tween(Color.animationConverter, { Easing(easing, duration) }, delay)

/** @see tween */
public fun tweenColor(
    easings: (dimension: Int) -> Easing,
    delay  : Measure<Time> = zeroMillis
): NumericAnimationPlan<Color, Array<Double>> = tween(Color.animationConverter, easings, delay)

// endregion

// region ================ Key Frames ========================

public interface KeyFrameBlock<T> {
    public sealed interface Frame<T> {
        public infix fun then(easing: EasingFunction)
    }

    public infix fun T.at(timeStamp: Measure<Time>): Frame<T>

    public infix fun T.at(fraction: Float): Frame<T>
}

@JvmName("keyFramesSingle")
public fun <T> keyFrames(
    converter: AnimationDataConverter<T, Double>,
    duration : Measure<Time>,
    delay    : Measure<Time> = zeroMillis,
    frames   : KeyFrameBlock<T>.() -> Unit
): NumericAnimationPlan<T, Double> = SingleKeyframeAnimationPlan(converter, duration, delay, frames)

public fun keyFramesInt(
    duration : Measure<Time>,
    delay    : Measure<Time> = zeroMillis,
    frames   : KeyFrameBlock<Int>.() -> Unit
): NumericAnimationPlan<Int, Double> = keyFrames(Int.animationConverter, duration, delay, frames)

public fun keyFramesFloat(
    duration : Measure<Time>,
    delay    : Measure<Time> = zeroMillis,
    frames   : KeyFrameBlock<Float>.() -> Unit
): NumericAnimationPlan<Float, Double> = keyFrames(Float.animationConverter, duration, delay, frames)

public fun keyFramesDouble(
    duration : Measure<Time>,
    delay    : Measure<Time> = zeroMillis,
    frames   : KeyFrameBlock<Double>.() -> Unit
): NumericAnimationPlan<Double, Double> = keyFrames(Double.animationConverter, duration, delay, frames)

@JvmName("keyFramesMulti")
public fun <T> keyFrames(
    converter: AnimationDataConverter<T, Array<Double>>,
    duration : Measure<Time>,
    delay    : Measure<Time> = zeroMillis,
    frames   : KeyFrameBlock<T>.() -> Unit
): NumericAnimationPlan<T, Array<Double>> = MultiKeyframeAnimationPlan(converter, duration, delay, frames)

public fun keyFramesPoint(
    duration : Measure<Time>,
    delay    : Measure<Time> = zeroMillis,
    frames   : KeyFrameBlock<Point>.() -> Unit
): NumericAnimationPlan<Point, Array<Double>> = keyFrames(Point.animationConverter, duration, delay, frames)

public fun keyFramesSize(
    duration : Measure<Time>,
    delay    : Measure<Time> = zeroMillis,
    frames   : KeyFrameBlock<Size>.() -> Unit
): NumericAnimationPlan<Size, Array<Double>> = keyFrames(Size.animationConverter, duration, delay, frames)

public fun keyFramesRect(
    duration : Measure<Time>,
    delay    : Measure<Time> = zeroMillis,
    frames   : KeyFrameBlock<Rectangle>.() -> Unit
): NumericAnimationPlan<Rectangle, Array<Double>> = keyFrames(Rectangle.animationConverter, duration, delay, frames)

public fun keyFramesColor(
    duration : Measure<Time>,
    delay    : Measure<Time> = zeroMillis,
    frames   : KeyFrameBlock<Color>.() -> Unit
): NumericAnimationPlan<Color, Array<Double>> = keyFrames(Color.animationConverter, duration, delay, frames)

// endregion

// region ================ Repetition ========================

public enum class RepetitionType { Restart, Reverse }

@JvmName("repeatSingle")
public fun <T> repeat(
    animationPlan: NumericAnimationPlan<T, Double>,
    times        : Int = 1,
    type         : RepetitionType = Restart
): NumericAnimationPlan<T, Double> = object: RepeatingAnimationPlan<T, Double>(animationPlan, type) {
    override fun duration(start: Double, end: Double, initialVelocity: Velocity<Double>): Measure<Time> = (times + 1) * iterationDuration(
        start,
        end,
        initialVelocity
    )
}

@JvmName("repeatMulti")
public fun <T> repeat(
    animationPlan: NumericAnimationPlan<T, Array<Double>>,
    times        : Int            = 1,
    type         : RepetitionType = Restart
): NumericAnimationPlan<T, Array<Double>> = object: RepeatingAnimationPlan<T, Array<Double>>(animationPlan, type) {
    override fun duration(start: Array<Double>, end: Array<Double>, initialVelocity: Velocity<Array<Double>>): Measure<Time> = (times + 1) * iterationDuration(
        start,
        end,
        initialVelocity
    )
}

@JvmName("loopSingle")
public fun <T> loop(
    animationPlan: NumericAnimationPlan<T, Double>,
    type         : RepetitionType = Restart
): NumericAnimationPlan<T, Double> = object: RepeatingAnimationPlan<T, Double>(animationPlan, type) {
    override fun duration(start: Double, end: Double, initialVelocity: Velocity<Double>): Measure<Time> = Double.MAX_VALUE * milliseconds
}

@JvmName("loopMulti")
public fun <T> loop(
    animationPlan: NumericAnimationPlan<T, Array<Double>>,
    type         : RepetitionType = Restart
): NumericAnimationPlan<T, Array<Double>> = object: RepeatingAnimationPlan<T, Array<Double>>(animationPlan, type) {
    override fun duration(start: Array<Double>, end: Array<Double>, initialVelocity: Velocity<Array<Double>>): Measure<Time> = Double.MAX_VALUE * milliseconds
}

// endregion