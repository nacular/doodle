package com.nectar.doodle.animation

import com.nectar.doodle.animation.transition.FixedSpeedLinear
import com.nectar.doodle.animation.transition.FixedTimeLinear
import com.nectar.doodle.animation.transition.SpeedUpSlowDown
import com.nectar.doodle.animation.transition.Transition
import com.nectar.doodle.utils.Cancelable
import com.nectar.measured.units.InverseUnit
import com.nectar.measured.units.Measure
import com.nectar.measured.units.Time
import com.nectar.measured.units.Unit
import com.nectar.measured.units.UnitRatio
import com.nectar.measured.units.milliseconds
import com.nectar.measured.units.times
import kotlin.jvm.JvmName

/**
 * Created by Nicholas Eddy on 3/29/18.
 */

interface Listener<P> {
    open class ChangeEvent<P, T: Unit>(val property: P, val old: Measure<T>, val new: Measure<T>)

    fun cancelled(animator: Animator<P>) {}
    fun completed(animator: Animator<P>) {}

    fun changed(animator: Animator<P>, properties: Map<P, ChangeEvent<P, *>>) {}
}

interface InitialPropertyTransition<T: Unit> {
    infix fun using(transition: Transition<T>): PropertyTransitions<T>
}

interface PropertyTransitions<T: Unit> {
    infix fun then(transition: Transition<T>): PropertyTransitions<T>
}

class NoneUnit: Unit("none")

val noneUnits = NoneUnit()

@JvmName("fixedSpeedLinearNumber")
fun <T: Number> fixedSpeedLinear(speed: Measure<InverseUnit<Time>>): (T, T) -> Transition<NoneUnit> = { _,end -> FixedSpeedLinear((1 * noneUnits).times(speed),  end * noneUnits) }

@JvmName("fixedSpeedLinearUnit")
fun <T: Unit> fixedSpeedLinear(speed: Measure<UnitRatio<T, Time>>): (Measure<T>, Measure<T>) -> Transition<T> = { _,end -> FixedSpeedLinear(speed, end) }

fun <T: Number> fixedTimeLinear(time: Measure<Time>): (T, T) -> Transition<NoneUnit> = { _,end -> FixedTimeLinear(time, end * noneUnits) }

fun <T: Unit> fixedTimeLinearM(time: Measure<Time>): (Measure<T>, Measure<T>) -> Transition<T> = { _,end -> FixedTimeLinear(time, end) }

fun <T: Number> speedUpSlowDown(time: Measure<Time>, accelerationFraction: Float = 0.5f): (T, T) -> Transition<NoneUnit> = { _,end -> SpeedUpSlowDown(time, end * noneUnits, accelerationFraction) }

fun <T: Unit> speedUpSlowDownM(time: Measure<Time>, accelerationFraction: Float = 0.5f): (Measure<T>, Measure<T>) -> Transition<T> = { _,end -> SpeedUpSlowDown(time, end, accelerationFraction) }

interface TransitionBuilder<T: Number> {
    infix fun then(transition: Transition<NoneUnit>): TransitionBuilder<T>

    operator fun invoke(block: (T) -> kotlin.Unit): Cancelable
}

interface InterpolationStart<T: Number> {
    infix fun using(transition: (start: T, end: T) -> Transition<NoneUnit>): TransitionBuilder<T>
}

interface MeasureTransitionBuilder<T: Unit> {
    infix fun then(transition: Transition<T>): MeasureTransitionBuilder<T>

    operator fun invoke(block: (Measure<T>) -> kotlin.Unit)
}

interface MeasureInterpolationStart<T: Unit> {
    infix fun using(transition: (start: Measure<T>, end: Measure<T>) -> Transition<T>): MeasureTransitionBuilder<T>
}

interface Animator<P> {
    operator fun <T: Unit> invoke(property: P, initialValue: Measure<T>): InitialPropertyTransition<T>

    operator fun <T: Unit> invoke(pair: Pair<Measure<T>, Measure<T>>): MeasureInterpolationStart<T>

    operator fun <T: Number> invoke(pair: Pair<T, T>): InterpolationStart<T>

    val running: Boolean

    fun schedule(after: Measure<Time> = 0 * milliseconds)
    fun cancel  ()

    operator fun plusAssign (listener: Listener<P>)
    operator fun minusAssign(listener: Listener<P>)
}

/*
    val widthAnimation = animation from circle.width * pixels using
                FixedSpeedLinear(100 * (pixels / seconds), circle.width * 2 * pixels) then
                Cubic           (2 * seconds, circle.width * pixels, 0 * (pixels / seconds))
 */