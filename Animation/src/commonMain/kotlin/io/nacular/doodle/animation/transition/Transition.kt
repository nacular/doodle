package io.nacular.doodle.animation.transition

import io.nacular.doodle.animation.Moment
import com.nectar.measured.units.Measure
import com.nectar.measured.units.Time
import com.nectar.measured.units.Unit

/**
 * Created by Nicholas Eddy on 3/29/18.
 */
interface Transition<T: Unit> {
    fun duration(initial: Moment<T>                           ): Measure<Time>
    fun value   (initial: Moment<T>, timeOffset: Measure<Time>): Moment<T>
    fun endState(initial: Moment<T>                           ): Moment<T>
}