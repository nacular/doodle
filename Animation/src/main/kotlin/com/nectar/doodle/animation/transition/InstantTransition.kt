package com.nectar.doodle.animation.transition

import com.nectar.doodle.animation.Moment
import com.nectar.doodle.units.Measure
import com.nectar.doodle.units.Time
import com.nectar.doodle.units.milliseconds

/**
 * Created by Nicholas Eddy on 3/30/18.
 */

/**
 * A transition that instantly jumps to the final value.
 */
class InstantTransition<T>(private val finalValue: Measure<T>): FixedDurationTransition<T>(0.milliseconds) {
    override fun value   (initialState: Moment<T>, timeOffset: Measure<Time>) = endState(initialState)
    override fun endState(initialState: Moment<T>                           ) = Moment(finalValue, initialState.velocity)
}