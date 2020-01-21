package com.nectar.doodle.animation

import com.nectar.doodle.animation.transition.FixedSpeedLinear
import com.nectar.doodle.animation.transition.FixedTimeLinear
import com.nectar.doodle.animation.transition.SpeedUpSlowDown
import com.nectar.doodle.animation.transition.Transition
import com.nectar.doodle.utils.Completable
import com.nectar.doodle.utils.Pool
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

    interface NumberUsing<T: Number> {
        infix fun using(transition: (start: T, end: T) -> Transition<NoneUnit>): TransitionBuilder<T>
    }

    interface MeasureUsing<T: Unit> {
        infix fun  using(transition: (start: Measure<T>, end: Measure<T>) -> Transition<T>): MeasureTransitionBuilder<T>
    }

    operator fun <T: Number> invoke(range: Pair<T, T>) = object: NumberUsing<T> {
        override fun using(transition: (start: T, end: T) -> Transition<NoneUnit>) = range.using(transition)
    }

    operator fun <T: Unit> invoke(range: Pair<Measure<T>, Measure<T>>) = object: MeasureUsing<T> {
        override fun using(transition: (start: Measure<T>, end: Measure<T>) -> Transition<T>) = range.using(transition)
    }

    infix fun <T: Number> Pair<T, T>.using(transition: (start: T, end: T) -> Transition<NoneUnit>): TransitionBuilder<T>
    infix fun <T: Unit>   Pair<Measure<T>, Measure<T>>.using(transition: (start: Measure<T>, end: Measure<T>) -> Transition<T>): MeasureTransitionBuilder<T>

    operator fun invoke(block: Animator.() -> kotlin.Unit): Completable

    val listeners: Pool<Listener>
}