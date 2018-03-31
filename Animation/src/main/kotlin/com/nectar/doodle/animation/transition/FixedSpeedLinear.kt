package com.nectar.doodle.animation.transition

import com.nectar.doodle.animation.Moment
import com.nectar.doodle.units.Measure
import com.nectar.doodle.units.Time
import com.nectar.doodle.units.milliseconds
import kotlin.math.abs
import kotlin.math.min

/**
 * Created by Nicholas Eddy on 3/30/18.
 */
class FixedSpeedLinear(private val speed: Double, private val endValue: Double): Transition {

    // FIXME: Specify velocity units?
    override fun duration(initialState: Moment): Measure<Time> = ((endValue - initialState.position) / speed).milliseconds

    override fun value(initialState: Moment, timeOffset: Measure<Time>): Moment {
        val delta    = endValue - initialState.position
        val negative = delta < 0

        // FIXME: Specify velocity units?
        return Moment(initialState.position + min(abs(delta), speed * (timeOffset `in` milliseconds)) * if (negative) -1 else 1, initialState.velocity)
    }

    override fun endState(initialState: Moment): Moment {
        val delta    = endValue - initialState.position
        val negative = delta < 0

        return Moment(endValue, speed * if (negative) -1 else 1)
    }
}