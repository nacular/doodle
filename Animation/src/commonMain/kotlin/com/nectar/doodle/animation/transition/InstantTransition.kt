package com.nectar.doodle.animation.transition

import com.nectar.doodle.animation.Moment
import com.nectar.measured.units.Measure
import com.nectar.measured.units.Time
import com.nectar.measured.units.Time.Companion.milliseconds
import com.nectar.measured.units.Unit
import com.nectar.measured.units.times

/**
 * Created by Nicholas Eddy on 3/30/18.
 */

/**
 * A transition that instantly jumps to the final value.
 */
class InstantTransition<T: Unit>(private val finalValue: Measure<T>): FixedDuration<T>(0 * milliseconds) {
    override fun value   (initial: Moment<T>, timeOffset: Measure<Time>) = endState(initial)
    override fun endState(initial: Moment<T>                           ) = Moment(finalValue, initial.velocity)
}