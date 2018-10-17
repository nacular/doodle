/**
 * Created by Nicholas Eddy on 3/30/18.
 */
package com.nectar.doodle.animation.transition

import com.nectar.doodle.animation.Moment
import com.nectar.measured.units.Measure
import com.nectar.measured.units.Time


/**
 * Transition that keeps the initial state for a set duration.
 * This transition essentially pins the initial state for the
 * specified duration.
 *
 * @author Nicholas Eddy (neddy@zinoti.com)
 */
class NoChangeTransition<T>(duration: Measure<Time>): FixedDurationTransition<T>(duration) {
    override fun value   (initialState: Moment<T>, timeOffset: Measure<Time>) = initialState
    override fun endState(initialState: Moment<T>                           ) = initialState
}