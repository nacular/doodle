package com.nectar.doodle.animation.transition

import com.nectar.doodle.animation.Moment
import com.nectar.measured.units.Measure
import com.nectar.measured.units.MeasureRatio
import com.nectar.measured.units.Time
import com.nectar.measured.units.abs

/**
 * Created by Nicholas Eddy on 3/30/18.
 */
class FixedSpeedLinear<T>(private val speed: MeasureRatio<T, Time>, private val endValue: Measure<T>): Transition<T> {

    override fun duration(initialState: Moment<T>): Measure<Time> = (endValue - initialState.position) / speed

    override fun value(initialState: Moment<T>, timeOffset: Measure<Time>): Moment<T> {
        val delta    = endValue - initialState.position
        val negative = delta < Measure.zero() //0.pixels

        return Moment(initialState.position + minOf(abs(delta), speed * timeOffset * if (negative) -1 else 1), initialState.velocity)
    }

    override fun endState(initialState: Moment<T>): Moment<T> {
        val delta    = endValue - initialState.position
        val negative = delta < Measure.zero() //0.pixels

        return Moment(endValue, speed * if (negative) -1 else 1)
    }
}