package io.nacular.doodle.animation

import io.nacular.doodle.animation.transition.FixedSpeedLinear
import io.nacular.doodle.animation.transition.FixedTimeLinear
import io.nacular.doodle.animation.transition.NoChange
import io.nacular.doodle.animation.transition.SpeedUpSlowDown
import io.nacular.doodle.animation.transition.Transition
import io.nacular.doodle.utils.Completable
import io.nacular.doodle.utils.Pool
import io.nacular.measured.units.InverseUnits
import io.nacular.measured.units.Measure
import io.nacular.measured.units.Time
import io.nacular.measured.units.Units
import io.nacular.measured.units.UnitsRatio
import io.nacular.measured.units.times
import kotlin.jvm.JvmName

/**
 * Created by Nicholas Eddy on 3/29/18.
 */
class NoneUnit: Units("")

val noneUnits = NoneUnit()

@JvmName("fixedSpeedLinearNumber")
fun <T: Number> fixedSpeedLinear(speed: Measure<InverseUnits<Time>>): (T, T) -> Transition<NoneUnit> = { _,end -> FixedSpeedLinear(1 * noneUnits * speed, end * noneUnits) }

@JvmName("fixedSpeedLinearUnit")
fun <T: Units> fixedSpeedLinear(speed: Measure<UnitsRatio<T, Time>>): (Measure<T>, Measure<T>) -> Transition<T> = { _,end -> FixedSpeedLinear(speed, end) }

fun <T: Number> fixedTimeLinear(time: Measure<Time>): (T, T) -> Transition<NoneUnit> = { _,end -> FixedTimeLinear(time, end * noneUnits) }

fun <T: Units> fixedTimeLinearM(time: Measure<Time>): (Measure<T>, Measure<T>) -> Transition<T> = { _,end -> FixedTimeLinear(time, end) }

fun <T: Number> speedUpSlowDown(time: Measure<Time>, accelerationFraction: Float = 0.5f): (T, T) -> Transition<NoneUnit> = { _,end -> SpeedUpSlowDown(time, end * noneUnits, accelerationFraction) }

fun <T: Units> speedUpSlowDownM(time: Measure<Time>, accelerationFraction: Float = 0.5f): (Measure<T>, Measure<T>) -> Transition<T> = { _,end -> SpeedUpSlowDown(time, end, accelerationFraction) }

fun <T: Number> noChange (time: Measure<Time>): (T) -> Transition<NoneUnit> = { NoChange(time) }
fun <T: Units>  noChangeM(time: Measure<Time>): (Measure<T>) -> Transition<T> = { NoChange(time) }


interface Animation: Completable

interface Animator {
    interface Listener {
        fun changed  (animator: Animator, animations: Set<Animation>) {}
        fun cancelled(animator: Animator, animations: Set<Animation>) {}
        fun completed(animator: Animator, animations: Set<Animation>) {}
    }

    interface TransitionBuilder<T: Number> {
        infix fun then(transition: Transition<NoneUnit>): TransitionBuilder<T>

        operator fun invoke(block: (T) -> Unit): Animation
    }

    interface MeasureTransitionBuilder<T: Units> {
        infix fun then(transition: Transition<T>): MeasureTransitionBuilder<T>

        operator fun invoke(block: (Measure<T>) -> Unit): Animation
    }

    interface NumberRangeUsing<T: Number> {
        infix fun using(transition: (start: T, end: T) -> Transition<NoneUnit>): TransitionBuilder<T>
    }

    interface NumberUsing<T: Number> {
        infix fun using(transition: (start: T) -> Transition<NoneUnit>): TransitionBuilder<T>
    }

    interface MeasureRangeUsing<T: Units> {
        infix fun using(transition: (start: Measure<T>, end: Measure<T>) -> Transition<T>): MeasureTransitionBuilder<T>
    }

    interface MeasureUsing<T: Units> {
        infix fun using(transition: (start: Measure<T>) -> Transition<T>): MeasureTransitionBuilder<T>
    }

    operator fun <T: Number> invoke(range: Pair<T, T>) = object: NumberRangeUsing<T> {
        override fun using(transition: (start: T, end: T) -> Transition<NoneUnit>) = range using transition
    }

    operator fun <T: Number> invoke(value: T) = object: NumberUsing<T> {
        override fun using(transition: (start: T) -> Transition<NoneUnit>) = value using transition
    }

    operator fun <T: Units> invoke(range: Pair<Measure<T>, Measure<T>>) = object: MeasureRangeUsing<T> {
        override fun using(transition: (start: Measure<T>, end: Measure<T>) -> Transition<T>) = range using transition
    }

    operator fun <T: Units> invoke(value: Measure<T>) = object: MeasureUsing<T> {
        override fun using(transition: (start: Measure<T>) -> Transition<T>) = value using transition
    }

    infix fun <T: Number> Pair<T, T>.using(transition: (start: T, end: T) -> Transition<NoneUnit>): TransitionBuilder<T>
    infix fun <T: Number> T.using(transition: (start: T) -> Transition<NoneUnit>): TransitionBuilder<T>
    infix fun <T: Units>  Pair<Measure<T>, Measure<T>>.using(transition: (start: Measure<T>, end: Measure<T>) -> Transition<T>): MeasureTransitionBuilder<T>
    infix fun <T: Units>  Measure<T>.using(transition: (start: Measure<T>) -> Transition<T>): MeasureTransitionBuilder<T>

    operator fun invoke(block: Animator.() -> Unit): Completable

    val listeners: Pool<Listener>
}