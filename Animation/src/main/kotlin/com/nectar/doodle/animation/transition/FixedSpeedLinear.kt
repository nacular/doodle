package com.nectar.doodle.animation.transition

import com.nectar.doodle.animation.Moment
import com.nectar.doodle.units.Length
import com.nectar.doodle.units.Measure
import com.nectar.doodle.units.MeasureRatio
import com.nectar.doodle.units.Time
import com.nectar.doodle.units.abs
import com.nectar.doodle.units.pixels

/**
 * Created by Nicholas Eddy on 3/30/18.
 */
class FixedSpeedLinear(private val speed: MeasureRatio<Length, Time>, private val endValue: Measure<Length>): Transition {

    override fun duration(initialState: Moment): Measure<Time> = (endValue - initialState.position) / speed

    override fun value(initialState: Moment, timeOffset: Measure<Time>): Moment {
        val delta    = endValue - initialState.position
        val negative = delta < 0.pixels

        return Moment(initialState.position + minOf(abs(delta), speed * timeOffset * if (negative) -1 else 1), initialState.velocity)
    }

    override fun endState(initialState: Moment): Moment {
        val delta    = endValue - initialState.position
        val negative = delta < 0.pixels

        return Moment(endValue, speed * if (negative) -1 else 1)
    }
}