package com.nectar.doodle.animation.transition

import com.nectar.doodle.animation.Moment
import com.nectar.doodle.units.Measure
import com.nectar.doodle.units.Time

/**
 * Created by Nicholas Eddy on 3/29/18.
 */
interface Transition {
    fun duration(initialState: Moment                           ): Measure<Time>
    fun value   (initialState: Moment, timeOffset: Measure<Time>): Moment
    fun endState(initialState: Moment                           ): Moment
}