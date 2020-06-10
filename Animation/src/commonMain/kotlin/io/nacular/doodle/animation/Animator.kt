package io.nacular.doodle.animation

import io.nacular.doodle.animation.transition.FixedSpeedLinear
import io.nacular.doodle.animation.transition.FixedTimeLinear
import io.nacular.doodle.animation.transition.NoChange
import io.nacular.doodle.animation.transition.SpeedUpSlowDown
import io.nacular.doodle.animation.transition.Transition
import io.nacular.doodle.utils.Completable
import io.nacular.doodle.utils.Pool
import com.nectar.measured.units.InverseUnit
import com.nectar.measured.units.Measure
import com.nectar.measured.units.Time
import com.nectar.measured.units.Unit
import com.nectar.measured.units.UnitRatio
import com.nectar.measured.units.times
import kotlin.jvm.JvmName

/**
 * Created by Nicholas Eddy on 3/29/18.
 */
class NoneUnit: Unit("")

val noneUnits = NoneUnit()

@JvmName("fixedSpeedLinearNumber")
fun <T: Number> fixedSpeedLinear(speed: Measure<InverseUnit<Time>>): (T, T) -> Transition<NoneUnit> = { _,end -> FixedSpeedLinear(1 * noneUnits * speed, end * noneUnits) }

@JvmName("fixedSpeedLinearUnit")
fun <T: Unit> fixedSpeedLinear(speed: Measure<UnitRatio<T, Time>>): (Measure<T>, Measure<T>) -> Transition<T> = { _,end -> FixedSpeedLinear(speed, end) }

fun <T: Number> fixedTimeLinear(time: Measure<Time>): (T, T) -> Transition<NoneUnit> = { _,end -> FixedTimeLinear(time, end * noneUnits) }

fun <T: Unit> fixedTimeLinearM(time: Measure<Time>): (Measure<T>, Measure<T>) -> Transition<T> = { _,end -> FixedTimeLinear(time, end) }

fun <T: Number> speedUpSlowDown(time: Measure<Time>, accelerationFraction: Float = 0.5f): (T, T) -> Transition<NoneUnit> = { _,end -> SpeedUpSlowDown(time, end * noneUnits, accelerationFraction) }

fun <T: Unit> speedUpSlowDownM(time: Measure<Time>, accelerationFraction: Float = 0.5f): (Measure<T>, Measure<T>) -> Transition<T> = { _,end -> SpeedUpSlowDown(time, end, accelerationFraction) }

fun <T: Number> noChange (time: Measure<Time>): (T) -> Transition<NoneUnit> = { NoChange(time) }
fun <T: Unit>   noChangeM(time: Measure<Time>): (Measure<T>) -> Transition<T> = { NoChange(time) }


interface Animation: Completable

interface Animator {
    interface Listener {
        fun changed  (animator: Animator, animations: Set<Animation>) {}
        fun cancelled(animator: Animator, animations: Set<Animation>) {}
        fun completed(animator: Animator, animations: Set<Animation>) {}
    }

    interface TransitionBuilder<T: Number> {
        infix fun then(transition: Transition<NoneUnit>): TransitionBuilder<T>

        operator fun invoke(block: (T) -> kotlin.Unit): Animation
    }

    interface MeasureTransitionBuilder<T: Unit> {
        infix fun then(transition: Transition<T>): MeasureTransitionBuilder<T>

        operator fun invoke(block: (Measure<T>) -> kotlin.Unit): Animation
    }

    interface NumberRangeUsing<T: Number> {
        infix fun using(transition: (start: T, end: T) -> Transition<NoneUnit>): TransitionBuilder<T>
    }

    interface NumberUsing<T: Number> {
        infix fun using(transition: (start: T) -> Transition<NoneUnit>): TransitionBuilder<T>
    }

    interface MeasureRangeUsing<T: Unit> {
        infix fun  using(transition: (start: Measure<T>, end: Measure<T>) -> Transition<T>): MeasureTransitionBuilder<T>
    }

    interface MeasureUsing<T: Unit> {
        infix fun  using(transition: (start: Measure<T>) -> Transition<T>): MeasureTransitionBuilder<T>
    }

    operator fun <T: Number> invoke(range: Pair<T, T>) = object: NumberRangeUsing<T> {
        override fun using(transition: (start: T, end: T) -> Transition<NoneUnit>) = range using transition
    }

    operator fun <T: Number> invoke(value: T) = object: NumberUsing<T> {
        override fun using(transition: (start: T) -> Transition<NoneUnit>) = value using transition
    }

    operator fun <T: Unit> invoke(range: Pair<Measure<T>, Measure<T>>) = object: MeasureRangeUsing<T> {
        override fun using(transition: (start: Measure<T>, end: Measure<T>) -> Transition<T>) = range using transition
    }

    operator fun <T: Unit> invoke(value: Measure<T>) = object: MeasureUsing<T> {
        override fun using(transition: (start: Measure<T>) -> Transition<T>) = value using transition
    }

    infix fun <T: Number> Pair<T, T>.using(transition: (start: T, end: T) -> Transition<NoneUnit>): TransitionBuilder<T>
    infix fun <T: Number> T.using(transition: (start: T) -> Transition<NoneUnit>): TransitionBuilder<T>
    infix fun <T: Unit>   Pair<Measure<T>, Measure<T>>.using(transition: (start: Measure<T>, end: Measure<T>) -> Transition<T>): MeasureTransitionBuilder<T>
    infix fun <T: Unit>   Measure<T>.using(transition: (start: Measure<T>) -> Transition<T>): MeasureTransitionBuilder<T>

    operator fun invoke(block: Animator.() -> kotlin.Unit): Completable

    val listeners: Pool<Listener>
}