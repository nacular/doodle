package io.nacular.doodle.animation.transition

import io.nacular.doodle.animation.Moment
import io.nacular.measured.units.Measure
import io.nacular.measured.units.Time
import io.nacular.measured.units.Units

/**
 * Created by Nicholas Eddy on 3/30/18.
 */
class StepTransition<T: Units>(private val delay: Measure<Time>, remaining: Measure<Time>, private val finalValue: Measure<T>): FixedDuration<T>(delay + remaining) {
    init {
        require(delay.amount >= 0    ) { "delay must be positive"     }
        require(remaining.amount >= 0) { "remaining must be positive" }
    }

    override fun value   (initial: Moment<T>, timeOffset: Measure<Time>) = Moment(if (timeOffset < delay) initial.position else finalValue, initial.velocity)
    override fun endState(initial: Moment<T>                           ) = Moment(finalValue, initial.velocity)
}