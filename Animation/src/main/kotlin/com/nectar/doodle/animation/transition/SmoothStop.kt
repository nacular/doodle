package com.nectar.doodle.animation.transition

import com.nectar.doodle.animation.Moment
import com.nectar.doodle.units.Measure
import com.nectar.doodle.units.MeasureRatio
import com.nectar.doodle.units.Time
import com.nectar.doodle.units.seconds

/**
 * Created by Nicholas Eddy on 3/30/18.
 */
class SmoothStop<T>(private val endValue: Measure<T>): Transition<T> {
    override fun value(initialState: Moment<T>, timeOffset: Measure<Time>): Moment<T> {
//        val acceleration = -(initialState.velocity * initialState.velocity) / ((endValue - initialState.position) * 2)

//        if (timeOffset >= duration(initialState, acceleration)) {
            return endState(initialState)
//        }

        // TODO: Handle velocity units?
        return Moment(
                initialState.position + initialState.velocity * timeOffset /*+ acceleration * timeOffset.pow(2.0) / 2*/,
                initialState.velocity /*+ acceleration * timeOffset*/)
    }

    override fun duration(initialState: Moment<T>) = 0.seconds //duration(initialState, -initialState.velocity.pow(2.0) / ((endValue - initialState.position) * 2))

    private fun duration(initialState: Moment<T>, acceleration: Double): Measure<Time> {
        return 0.seconds //-initialState.velocity / acceleration
    }

    override fun endState(initialState: Moment<T>) = Moment<T>(endValue, MeasureRatio.zero())
}