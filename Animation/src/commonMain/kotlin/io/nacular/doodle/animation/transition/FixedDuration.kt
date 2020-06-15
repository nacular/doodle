package io.nacular.doodle.animation.transition

import io.nacular.doodle.animation.Moment
import io.nacular.measured.units.Measure
import io.nacular.measured.units.Time
import io.nacular.measured.units.Time.Companion.milliseconds
import io.nacular.measured.units.Units
import io.nacular.measured.units.times

/**
 * Created by Nicholas Eddy on 3/30/18.
 */
abstract class FixedDuration<T: Units>(private val duration: Measure<Time>): Transition<T> {
    init {
        (0 * milliseconds).let { require(duration >= it) { "duration must be >= $it" } }
    }

    override fun duration(initial: Moment<T>) = duration
}