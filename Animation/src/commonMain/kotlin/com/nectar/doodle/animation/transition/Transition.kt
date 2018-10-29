package com.nectar.doodle.animation.transition

import com.nectar.doodle.animation.Moment
import com.nectar.measured.units.Measure
import com.nectar.measured.units.Time
import com.nectar.measured.units.Unit

/**
 * Created by Nicholas Eddy on 3/29/18.
 */
interface Transition<T: Unit> {
    fun duration(initialState: Moment<T>                           ): Measure<Time>
    fun value   (initialState: Moment<T>, timeOffset: Measure<Time>): Moment<T>
    fun endState(initialState: Moment<T>                           ): Moment<T>
}