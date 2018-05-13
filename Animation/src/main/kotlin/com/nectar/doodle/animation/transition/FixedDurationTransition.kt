package com.nectar.doodle.animation.transition

import com.nectar.doodle.animation.Moment
import com.nectar.doodle.units.Measure
import com.nectar.doodle.units.Time
import com.nectar.doodle.units.milliseconds

/**
 * Created by Nicholas Eddy on 3/30/18.
 */
abstract class FixedDurationTransition<T>(private val duration: Measure<Time>): Transition<T> {
    init {
        require(duration >= 0.milliseconds) { "duration must be >= ${0.milliseconds}" }
    }

    override fun duration(initialState: Moment<T>) = duration
}