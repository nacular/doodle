package com.nectar.doodle.animation.transition

import com.nectar.doodle.animation.Moment
import com.nectar.measured.units.Measure
import com.nectar.measured.units.Time
import com.nectar.measured.units.Unit
import com.nectar.measured.units.milliseconds
import com.nectar.measured.units.times

/**
 * Created by Nicholas Eddy on 3/30/18.
 */
abstract class FixedDuration<T: Unit>(private val duration: Measure<Time>): Transition<T> {
    init {
        require(duration >= 0 * milliseconds) { "duration must be >= ${0 * milliseconds}" }
    }

    override fun duration(initialState: Moment<T>) = duration
}