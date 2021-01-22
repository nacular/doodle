package io.nacular.doodle.animation.transition

import io.nacular.doodle.animation.Moment
import io.nacular.measured.units.Measure
import io.nacular.measured.units.Time
import io.nacular.measured.units.Units
import io.nacular.measured.units.div
import io.nacular.measured.units.times

/**
 * Created by Nicholas Eddy on 3/30/18.
 */
public class FixedTimeLinear<T: Units>(duration: Measure<Time>, private val endValue: Measure<T>): FixedDuration<T>(duration) {
    override fun value(initial: Moment<T>, timeOffset: Measure<Time>): Moment<T> = Moment(initial.position + ((endValue - initial.position) / duration(initial) * timeOffset), initial.velocity)

    override fun endState(initial: Moment<T>): Moment<T> = Moment(endValue, initial.velocity)
}