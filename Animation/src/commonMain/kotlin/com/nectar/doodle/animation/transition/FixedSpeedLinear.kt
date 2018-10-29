package com.nectar.doodle.animation.transition

import com.nectar.doodle.animation.Moment
import com.nectar.measured.units.Measure
import com.nectar.measured.units.Time
import com.nectar.measured.units.Unit
import com.nectar.measured.units.UnitRatio
import com.nectar.measured.units.abs
import com.nectar.measured.units.div
import com.nectar.measured.units.times


/**
 * Created by Nicholas Eddy on 3/30/18.
 */

class FixedSpeedLinear<T: Unit>(private val speed: Measure<UnitRatio<T, Time>>, private val endValue: Measure<T>): Transition<T> {

    override fun duration(initialState: Moment<T>): Measure<Time> = (endValue - initialState.position) / speed

    override fun value(initialState: Moment<T>, timeOffset: Measure<Time>): Moment<T> {
        val delta = endValue - initialState.position

        return Moment(initialState.position + minOf(abs(delta), speed * timeOffset * if (delta.amount < 0) -1 else 1), initialState.velocity)
    }

    override fun endState(initialState: Moment<T>): Moment<T> {
        val delta = endValue - initialState.position

        return Moment(endValue, speed * if (delta.amount < 0) -1 else 1)
    }
}