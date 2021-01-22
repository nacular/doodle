package io.nacular.doodle.animation.transition

import io.nacular.doodle.animation.Moment
import io.nacular.measured.units.Measure
import io.nacular.measured.units.Time
import io.nacular.measured.units.Time.Companion.milliseconds
import io.nacular.measured.units.Units
import io.nacular.measured.units.times

/**
 * Created by Nicholas Eddy on 3/30/18.
 */

/**
 * A transition that instantly jumps to the final value.
 */
public class InstantTransition<T: Units>(private val finalValue: Measure<T>): FixedDuration<T>(0 * milliseconds) {
    override fun value   (initial: Moment<T>, timeOffset: Measure<Time>): Moment<T> = endState(initial)
    override fun endState(initial: Moment<T>                           ): Moment<T> = Moment(finalValue, initial.velocity)
}