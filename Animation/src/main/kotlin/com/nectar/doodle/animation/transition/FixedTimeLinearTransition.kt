package com.nectar.doodle.animation.transition

import com.nectar.doodle.animation.Moment
import com.nectar.doodle.units.Length
import com.nectar.doodle.units.Measure
import com.nectar.doodle.units.Time

/**
 * Created by Nicholas Eddy on 3/30/18.
 */
class FixedTimeLinearTransition(duration: Measure<Time>, private val endValue: Measure<Length>): FixedDurationTransition(duration) {
    override fun value(initialState: Moment, timeOffset: Measure<Time>): Moment {
        val initialPosition = initialState.position

        return Moment(initialPosition + ((endValue - initialPosition) / duration(initialState) * timeOffset), initialState.velocity)
    }

    override fun endState(initialState: Moment) = Moment(endValue, initialState.velocity)
}