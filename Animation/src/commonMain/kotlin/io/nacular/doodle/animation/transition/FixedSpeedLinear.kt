package io.nacular.doodle.animation.transition

import io.nacular.doodle.animation.Moment
import io.nacular.doodle.animation.Velocity
import io.nacular.measured.units.Measure
import io.nacular.measured.units.Time
import io.nacular.measured.units.Units
import io.nacular.measured.units.abs
import io.nacular.measured.units.div
import io.nacular.measured.units.times


/**
 * Created by Nicholas Eddy on 3/30/18.
 */

class FixedSpeedLinear<T: Units>(private val speed: Measure<Velocity<T>>, private val endValue: Measure<T>): Transition<T> {

    override fun duration(initial: Moment<T>): Measure<Time> = (endValue - initial.position) / speed

    override fun value(initial: Moment<T>, timeOffset: Measure<Time>): Moment<T> {
        val delta = endValue - initial.position
        val sign  = if (delta.amount < 0) -1 else 1

        return Moment(initial.position + minOf(abs(delta), abs(speed * timeOffset)) * sign, speed * sign)
    }

    override fun endState(initial: Moment<T>) = Moment(endValue, speed * if ((endValue - initial.position).amount < 0) -1 else 1)
}