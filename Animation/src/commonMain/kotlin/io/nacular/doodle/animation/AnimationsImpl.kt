package io.nacular.doodle.animation

import io.nacular.doodle.animation.RepetitionType.Restart
import io.nacular.doodle.animation.transition.EasingFunction
import io.nacular.doodle.animation.transition.TimedEasing
import io.nacular.doodle.animation.transition.linear
import io.nacular.doodle.utils.isEven
import io.nacular.doodle.utils.zeroMillis
import io.nacular.measured.units.Measure
import io.nacular.measured.units.Time
import io.nacular.measured.units.Time.Companion.milliseconds
import io.nacular.measured.units.div
import io.nacular.measured.units.times
import kotlin.math.min

internal open class NumericAnimationPlanImpl<T>(
    private  val delay    : Measure<Time> = zeroMillis,
    override val converter: AnimationDataConverter<T, Double>,
    private  val animation: TimedEasing,
): FiniteNumericAnimationPlan<T, Double> {
    override fun value(start: Double, end: Double, initialVelocity: Velocity<Double>, elapsedTime: Measure<Time>) = when {
        elapsedTime <= delay -> start
        else                 -> animation.value(start, end, initialVelocity, elapsedTime - delay)
    }

    override fun velocity(start: Double, end: Double, initialVelocity: Velocity<Double>, elapsedTime: Measure<Time>) = when {
        elapsedTime <= delay -> initialVelocity
        else                 -> {
            val v1 = value(start, end, initialVelocity, elapsedTime - 1 * milliseconds)
            val v2 = value(start, end, initialVelocity, elapsedTime)

            Velocity(v2 - v1, 1 * milliseconds)
        }
    }

    override fun duration(start: Double, end: Double, initialVelocity: Velocity<Double>) = delay + animation.duration(start, end, initialVelocity)
}

internal open class MultiNumericAnimationPlanImpl<T>(
    private  val delay    : Measure<Time> = zeroMillis,
    override val converter: MultiDataConverter<T>,
                 easings   : (Int) -> Easing,
): FiniteNumericAnimationPlan<T, Array<Double>> {
    private val timedEasings by lazy { (0 .. converter.size).map { easings(it).run { TimedEasing(duration, easing) } } }

    override fun value(start: Array<Double>, end: Array<Double>, initialVelocity: Velocity<Array<Double>>, elapsedTime: Measure<Time>) = when {
        elapsedTime <= delay -> start
        else                 -> start.mapIndexed { index, value ->
            timedEasings[index].value(value, end[index], Velocity(initialVelocity.change[index], initialVelocity.over), elapsedTime)
        }.toTypedArray()
    }

    override fun velocity(start: Array<Double>, end: Array<Double>, initialVelocity: Velocity<Array<Double>>, elapsedTime: Measure<Time>): Velocity<Array<Double>> = when {
        elapsedTime <= delay -> initialVelocity
        else                 -> {
            val v1 = value(start, end, initialVelocity, elapsedTime - 1 * milliseconds)
            val v2 = value(start, end, initialVelocity, elapsedTime)

            Velocity(v2.zip(v1).map { it.first - it.second }.toTypedArray(), 1 * milliseconds)
        }
    }

    override fun duration(start: Array<Double>, end: Array<Double>, initialVelocity: Velocity<Array<Double>>): Measure<Time> {
        var duration = delay
        start.forEachIndexed { index, value ->
            duration = maxOf(duration, timedEasings[index].duration(value, end[index], Velocity(initialVelocity.change[index], initialVelocity.over)))
        }

        return duration
    }
}

internal class KeyFrameBlockImpl<T>(private val duration: Measure<Time>): KeyFrameBlock<T> {
    class FrameImpl<T>(val value: T, var easing: EasingFunction = linear): KeyFrameBlock.Frame<T> {
        override fun then(easing: EasingFunction) {
            this.easing = easing
        }
    }

    val frames = mutableMapOf<Measure<Time>, FrameImpl<T>>()
    var end: Pair<Measure<Time>, T>? = null

    override infix fun T.at(timeStamp: Measure<Time>) = FrameImpl(this).also {
        frames[timeStamp] = it
        val e = end
        end = when {
            e == null || timeStamp > e.first -> timeStamp to it.value
            else                             -> e
        }
    }

    override infix fun T.at(fraction: Float): KeyFrameBlock.Frame<T> = at(duration * fraction)
}

internal abstract class KeyframeAnimationPlan<T, V>(
    override val converter: AnimationDataConverter<T, V>,
    private  val duration : Measure<Time>,
    private  val delay    : Measure<Time> = zeroMillis,
    block    : KeyFrameBlock<T>.() -> Unit
): FiniteNumericAnimationPlan<T, V> {
    private val keyFrameBlock = KeyFrameBlockImpl<T>(duration)
    private val frames get() = keyFrameBlock.frames

    init {
        block(keyFrameBlock)
    }

    override fun value(start: V, end: V, initialVelocity: Velocity<V>, elapsedTime: Measure<Time>): V {
        when {
            elapsedTime <= delay            -> return start
            elapsedTime >= delay + duration -> return end
        }

        frames[elapsedTime]?.let {
            return converter.serialize(it.value)
        }

        var startTime  = zeroMillis
        var startValue = converter.deserialize(start)
        var endTime    = duration
        var endValue   = converter.deserialize(end)
        var easing     = linear

        frames.forEach { (time, frame) ->
            if (elapsedTime > time && time >= startTime) {
                startTime  = time
                startValue = frame.value
                easing     = frame.easing
            } else if (elapsedTime < time && time <= endTime) {
                endTime    = time
                endValue   = frame.value
            }
        }

        return frameValue(converter.serialize(startValue), converter.serialize(endValue), initialVelocity, easing, endTime - startTime, elapsedTime - startTime)
    }

    override fun velocity(start: V, end: V, initialVelocity: Velocity<V>, elapsedTime: Measure<Time>): Velocity<V> {
        val v1 = value(start, end, initialVelocity, elapsedTime - 1 * milliseconds)
        val v2 = value(start, end, initialVelocity, elapsedTime                        )

        return velocity(v1, v2, 1 * milliseconds)
    }

    override fun duration(start: V, end: V, initialVelocity: Velocity<V>): Measure<Time> = delay + duration

    abstract fun frameValue(start: V, end: V, initialVelocity: Velocity<V>, easing: EasingFunction, duration: Measure<Time>, elapsedTime: Measure<Time>): V

    abstract fun velocity(start: V, end: V, elapsedTime: Measure<Time>): Velocity<V>
}

internal class SingleKeyframeAnimationPlan<T>(
    converter: AnimationDataConverter<T, Double>,
    duration : Measure<Time>,
    delay    : Measure<Time> = zeroMillis,
    block    : KeyFrameBlock<T>.() -> Unit
): KeyframeAnimationPlan<T, Double>(converter, duration, delay, block) {
    override fun frameValue(start: Double, end: Double, initialVelocity: Velocity<Double>, easing: EasingFunction, duration: Measure<Time>, elapsedTime: Measure<Time>): Double {
        return TimedEasing(duration, easing).value(
            start,
            end,
            initialVelocity,
            elapsedTime
        )
    }

    override fun velocity(start: Double, end: Double, elapsedTime: Measure<Time>): Velocity<Double> {
        return Velocity(end - start, elapsedTime)
    }
}

internal class MultiKeyframeAnimationPlan<T>(
    converter: AnimationDataConverter<T, Array<Double>>,
    duration : Measure<Time>,
    delay    : Measure<Time> = zeroMillis,
    block    : KeyFrameBlock<T>.() -> Unit
): KeyframeAnimationPlan<T, Array<Double>>(converter, duration, delay, block) {
    override fun frameValue(start: Array<Double>, end: Array<Double>, initialVelocity: Velocity<Array<Double>>, easing: EasingFunction, duration: Measure<Time>, elapsedTime: Measure<Time>): Array<Double> {
        return start.mapIndexed { index, value ->
            TimedEasing(duration, easing).value(
                value,
                end[index],
                Velocity(initialVelocity.change[index], initialVelocity.over),
                elapsedTime
            )
        }.toTypedArray()
    }

    override fun velocity(start: Array<Double>, end: Array<Double>, elapsedTime: Measure<Time>): Velocity<Array<Double>> {
        return Velocity(end.zip(start).map { it.first - it.second }.toTypedArray(), 1 * milliseconds)
    }
}

internal abstract class RepeatingAnimationPlan<T, V>(
    private   val animation: FiniteNumericAnimationPlan<T, V>,
    private   val type     : RepetitionType = Restart,
    protected val times    : Int
): NumericAnimationPlan<T, V> {
    override val converter get() = animation.converter

    override fun value(start: V, end: V, initialVelocity: Velocity<V>, elapsedTime: Measure<Time>): V = animation.value(
        start,
        end,
        velocityForRepeat   (start, end, initialVelocity, elapsedTime),
        elapsedTimeForRepeat(start, end, initialVelocity, elapsedTime)
    )

    override fun velocity(start: V, end: V, initialVelocity: Velocity<V>, elapsedTime: Measure<Time>): Velocity<V> = animation.velocity(
        start,
        end,
        velocityForRepeat   (start, end, initialVelocity, elapsedTime),
        elapsedTimeForRepeat(start, end, initialVelocity, elapsedTime)
    )

    protected fun iterationDuration(start: V, end: V, initialVelocity: Velocity<V>): Measure<Time> = animation.duration(
        start,
        end,
        initialVelocity
    )

    private fun velocityForRepeat(start: V, end: V, initialVelocity: Velocity<V>, elapsedTime: Measure<Time>): Velocity<V> {
        val duration = iterationDuration(start, end, initialVelocity)

        return when {
            // Start velocity of the 2nd and subsequent iteration will be the velocity at the end of the first iteration, instead of the initial velocity.
            elapsedTime > duration -> velocity(start, end, initialVelocity, duration)
            else                   -> initialVelocity
        }
    }

    private fun elapsedTimeForRepeat(start: V, end: V, initialVelocity: Velocity<V>, elapsedTime: Measure<Time>): Measure<Time> {
        val duration   = iterationDuration(start, end, initialVelocity)
        val numRepeats = min((elapsedTime / duration).toInt(), times)

        return when {
            type == Restart || numRepeats.isEven -> elapsedTime - numRepeats * duration
            else                                 -> ((numRepeats + 1) * duration - elapsedTime)
        }
    }
}

internal abstract class FiniteRepeatingAnimationPlan<T, V>(
    animation: FiniteNumericAnimationPlan<T, V>,
    type     : RepetitionType = Restart,
    times    : Int
): RepeatingAnimationPlan<T, V>(animation, type, times), FiniteNumericAnimationPlan<T, V>