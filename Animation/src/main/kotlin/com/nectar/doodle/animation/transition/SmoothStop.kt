package com.nectar.doodle.animation.transition

import com.nectar.doodle.animation.Moment
import com.nectar.doodle.units.Length
import com.nectar.doodle.units.Measure
import com.nectar.doodle.units.Time
import com.nectar.doodle.units.pixels_second
import com.nectar.doodle.units.seconds

/**
 * Created by Nicholas Eddy on 3/30/18.
 */
class SmoothStop(private val endValue: Measure<Length>): Transition {
    override fun value(initialState: Moment, timeOffset: Measure<Time>): Moment {
//        val acceleration = -(initialState.velocity * initialState.velocity) / ((endValue - initialState.position) * 2)

//        if (timeOffset >= duration(initialState, acceleration)) {
            return endState(initialState)
//        }

        // TODO: Handle velocity units?
        return Moment(
                initialState.position + initialState.velocity * timeOffset /*+ acceleration * timeOffset.pow(2.0) / 2*/,
                initialState.velocity /*+ acceleration * timeOffset*/)
    }

    override fun duration(initialState: Moment) = 0.seconds //duration(initialState, -initialState.velocity.pow(2.0) / ((endValue - initialState.position) * 2))

    private fun duration(initialState: Moment, acceleration: Double): Measure<Time> {
        return 0.seconds //-initialState.velocity / acceleration
    }

    override fun endState(initialState: Moment) = Moment(endValue, 0.pixels_second)
}