package com.nectar.doodle.animation.transition

import com.nectar.doodle.animation.Moment
import com.nectar.measured.units.Measure
import com.nectar.measured.units.Time
import com.nectar.measured.units.Unit
import com.nectar.measured.units.div
import com.nectar.measured.units.times

/**
 * Created by Nicholas Eddy on 3/30/18.
 */
class FixedTimeLinear<T: Unit>(duration: Measure<Time>, private val endValue: Measure<T>): FixedDuration<T>(duration) {
    override fun value(initialState: Moment<T>, timeOffset: Measure<Time>): Moment<T> {
        val initialPosition = initialState.position

        return Moment(initialPosition + ((endValue - initialPosition) / duration(initialState) * timeOffset), initialState.velocity)
    }

    override fun endState(initialState: Moment<T>) = Moment(endValue, initialState.velocity)
}