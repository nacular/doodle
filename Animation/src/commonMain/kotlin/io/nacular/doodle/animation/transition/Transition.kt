package io.nacular.doodle.animation.transition

import io.nacular.doodle.animation.Moment
import io.nacular.measured.units.Measure
import io.nacular.measured.units.Time
import io.nacular.measured.units.Units

/**
 * Created by Nicholas Eddy on 3/29/18.
 */
interface Transition<T: Units> {
    fun duration(initial: Moment<T>                           ): Measure<Time>
    fun value   (initial: Moment<T>, timeOffset: Measure<Time>): Moment<T>
    fun endState(initial: Moment<T>                           ): Moment<T>
}