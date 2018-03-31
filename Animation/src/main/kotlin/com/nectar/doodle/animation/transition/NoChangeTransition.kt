/**
 * Created by Nicholas Eddy on 3/30/18.
 */
package com.nectar.doodle.animation.transition

import com.nectar.doodle.animation.Moment
import com.nectar.doodle.units.Measure
import com.nectar.doodle.units.Time


/**
 * Transition that keeps the initial state for a set duration.
 * This transition essentially pins the initial state for the
 * specified duration.
 *
 * @author Nicholas Eddy (neddy@zinoti.com)
 */
class NoChangeTransition(duration: Measure<Time>): FixedDurationTransition(duration) {
    override fun value   (initialState: Moment, timeOffset: Measure<Time>) = initialState
    override fun endState(initialState: Moment                           ) = initialState
}