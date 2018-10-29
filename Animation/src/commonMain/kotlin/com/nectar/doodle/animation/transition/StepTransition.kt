package com.nectar.doodle.animation.transition

import com.nectar.doodle.animation.Moment
import com.nectar.measured.units.Measure
import com.nectar.measured.units.Time
import com.nectar.measured.units.Unit

/**
 * Created by Nicholas Eddy on 3/30/18.
 */
class StepTransition<T: Unit>(private val delay: Measure<Time>, remaining: Measure<Time>, private val finalValue: Measure<T>): FixedDurationTransition<T>(delay + remaining) {
    init {
        require(delay.amount >= 0    ) { "delay must be positive"     }
        require(remaining.amount >= 0) { "remaining must be positive" }
    }

    override fun value   (initialState: Moment<T>, timeOffset: Measure<Time>) = Moment(if (timeOffset < delay) initialState.position else finalValue, initialState.velocity)
    override fun endState(initialState: Moment<T>                           ) = Moment(finalValue, initialState.velocity)
}