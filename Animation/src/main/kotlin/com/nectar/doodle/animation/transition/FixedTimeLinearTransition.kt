package com.nectar.doodle.animation.transition

import com.nectar.doodle.animation.Moment
import com.nectar.doodle.units.Measure
import com.nectar.doodle.units.Time

/**
 * Created by Nicholas Eddy on 3/30/18.
 */
class FixedTimeLinearTransition<T>(duration: Measure<Time>, private val endValue: Measure<T>): FixedDurationTransition<T>(duration) {
    override fun value(initialState: Moment<T>, timeOffset: Measure<Time>): Moment<T> {
        val initialPosition = initialState.position

        return Moment(initialPosition + ((endValue - initialPosition) / duration(initialState) * timeOffset), initialState.velocity)
    }

    override fun endState(initialState: Moment<T>) = Moment(endValue, initialState.velocity)
}