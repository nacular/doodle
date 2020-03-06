package com.nectar.doodle.animation.transition

import com.nectar.doodle.animation.Moment
import com.nectar.measured.units.Measure
import com.nectar.measured.units.Time
import com.nectar.measured.units.Time.Companion.milliseconds
import com.nectar.measured.units.Unit
import com.nectar.measured.units.times

/**
 * Created by Nicholas Eddy on 3/30/18.
 */
abstract class FixedDuration<T: Unit>(private val duration: Measure<Time>): Transition<T> {
    init {
        (0 * milliseconds).let { require(duration >= it) { "duration must be >= $it" } }
    }

    override fun duration(initial: Moment<T>) = duration
}