/**
 * Created by Nicholas Eddy on 3/30/18.
 */
package com.nectar.doodle.animation.transition

import com.nectar.doodle.animation.Moment
import com.nectar.measured.units.Measure
import com.nectar.measured.units.Time
import com.nectar.measured.units.Unit


/**
 * Transition that keeps the initial state for a set duration.
 *
 * @author Nicholas Eddy (neddy@zinoti.com)
 */
class NoChange<T: Unit>(duration: Measure<Time>): FixedDuration<T>(duration) {
    override fun value   (initial: Moment<T>, timeOffset: Measure<Time>) = initial
    override fun endState(initial: Moment<T>                           ) = initial
}