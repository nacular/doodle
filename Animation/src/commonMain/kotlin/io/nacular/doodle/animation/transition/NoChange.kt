/**
 * Created by Nicholas Eddy on 3/30/18.
 */
package io.nacular.doodle.animation.transition

import io.nacular.doodle.animation.Moment
import io.nacular.measured.units.Measure
import io.nacular.measured.units.Time
import io.nacular.measured.units.Units


/**
 * Transition that keeps the initial state for a set duration.
 *
 * @author Nicholas Eddy (neddy@zinoti.com)
 */
public class NoChange<T: Units>(duration: Measure<Time>): FixedDuration<T>(duration) {
    override fun value   (initial: Moment<T>, timeOffset: Measure<Time>): Moment<T> = initial
    override fun endState(initial: Moment<T>                           ): Moment<T> = initial
}