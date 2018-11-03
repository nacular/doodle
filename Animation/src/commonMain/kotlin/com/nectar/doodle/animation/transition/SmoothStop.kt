package com.nectar.doodle.animation.transition

import com.nectar.doodle.animation.Acceleration
import com.nectar.doodle.animation.Moment
import com.nectar.measured.units.Measure
import com.nectar.measured.units.Time
import com.nectar.measured.units.Unit
import com.nectar.measured.units.div
import com.nectar.measured.units.times

/**
 * Created by Nicholas Eddy on 3/30/18.
 */
class SmoothStop<T: Unit>(private val endValue: Measure<T>): Transition<T> {
    override fun value(initialState: Moment<T>, timeOffset: Measure<Time>): Moment<T> {
        val acceleration: Measure<Acceleration<T>> = -(initialState.velocity * initialState.velocity) / ((endValue - initialState.position) * 2)

//        // If velocity negative, use SmoothStop to go to 0
//        if ((endValue - initialState.position).amount.sign != initialState.velocity.amount.sign) {
//            SmoothStop(endValue: Measure<T>)
//        }

        if (timeOffset >= duration(initialState, acceleration)) {
            return endState(initialState)
        }

        return Moment(
                initialState.position + initialState.velocity * timeOffset + acceleration * timeOffset * timeOffset / 2,
                initialState.velocity + acceleration * timeOffset)
    }

    override fun duration(initialState: Moment<T>) = initialState.let { duration(it, -it.velocity * it.velocity / (2 * (endValue - it.position))) }

    private fun duration(initialState: Moment<T>, acceleration: Measure<Acceleration<T>>): Measure<Time> {
        return -initialState.velocity / acceleration
    }

    override fun endState(initialState: Moment<T>) = Moment(endValue, initialState.velocity * 0)
}