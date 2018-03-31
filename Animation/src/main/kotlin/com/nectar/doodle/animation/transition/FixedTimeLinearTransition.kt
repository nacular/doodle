package com.nectar.doodle.animation.transition

import com.nectar.doodle.animation.Moment
import com.nectar.doodle.units.Measure
import com.nectar.doodle.units.Time
import com.nectar.doodle.units.milliseconds

/**
 * Created by Nicholas Eddy on 3/30/18.
 */
class FixedTimeLinearTransition(duration: Measure<Time>, private val endValue: Double): FixedDurationTransition(duration) {
    override fun value(initialState: Moment, timeOffset: Measure<Time>): Moment {
        val initialPosition = initialState.position

        // FIXME: Specify velocity units?
        return Moment(initialPosition + ((endValue - initialPosition) / (duration(initialState) `in` milliseconds)) * (timeOffset `in` milliseconds), initialState.velocity)
    }
    override fun endState(initialState: Moment) = Moment(endValue, initialState.velocity)
}