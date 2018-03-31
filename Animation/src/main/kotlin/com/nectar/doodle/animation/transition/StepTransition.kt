package com.nectar.doodle.animation.transition

import com.nectar.doodle.animation.Moment
import com.nectar.doodle.units.Measure
import com.nectar.doodle.units.Time
import com.nectar.doodle.units.milliseconds

/**
 * Created by Nicholas Eddy on 3/30/18.
 */
class StepTransition(private val delay: Measure<Time>, remaining: Measure<Time>, private val finalValue: Double): FixedDurationTransition(delay + remaining) {
    init {
        require(delay     >= 0.milliseconds) { "delay must be >= ${0.milliseconds}"     }
        require(remaining >= 0.milliseconds) { "remaining must be >= ${0.milliseconds}" }
    }

    override fun value   (initialState: Moment, timeOffset: Measure<Time>) = Moment(if (timeOffset < delay) initialState.position else finalValue, initialState.velocity)
    override fun endState(initialState: Moment                           ) = Moment(finalValue, initialState.velocity)
}