package com.nectar.doodle.animation.transition

import com.nectar.doodle.animation.Moment
import com.nectar.doodle.units.Measure
import com.nectar.doodle.units.Time
import com.nectar.doodle.units.milliseconds
import kotlin.math.pow

/**
 * Created by Nicholas Eddy on 3/30/18.
 */
class SmoothStop(private val endValue: Double): Transition {
    override fun value(initialState: Moment, timeOffset: Measure<Time>): Moment {
        val acceleration = -initialState.velocity.pow(2.0) / (2 * (endValue - initialState.position))

        if (timeOffset >= duration(initialState, acceleration)) {
            return endState(initialState)
        }

        val time = timeOffset `in` milliseconds

        // TODO: Handle velocity units?
        return Moment(
                initialState.position + initialState.velocity * time + acceleration * time.pow(2.0) / 2,
                initialState.velocity + acceleration * time)
    }

    override fun duration(initialState: Moment) = duration(initialState, -initialState.velocity.pow(2.0) / (2 * (endValue - initialState.position)))

    private fun duration(initialState: Moment, acceleration: Double): Measure<Time> {
        return (-initialState.velocity / acceleration).milliseconds
    }

    override fun endState(initialState: Moment) = Moment(endValue, 0.0)
}