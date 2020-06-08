package io.nacular.doodle.animation.transition

import io.nacular.doodle.animation.Acceleration
import io.nacular.doodle.animation.Moment
import com.nectar.measured.units.Measure
import com.nectar.measured.units.Time
import com.nectar.measured.units.Unit
import com.nectar.measured.units.div
import com.nectar.measured.units.times

/**
 * Created by Nicholas Eddy on 3/30/18.
 */
class SmoothStop<T: Unit>(private val endValue: Measure<T>): Transition<T> {
    override fun value(initial: Moment<T>, timeOffset: Measure<Time>): Moment<T> {
        val acceleration: Measure<Acceleration<T>> = -(initial.velocity * initial.velocity) / ((endValue - initial.position) * 2)

//        // If velocity negative, use SmoothStop to go to 0
//        if ((endValue - initialState.position).amount.sign != initialState.velocity.amount.sign) {
//            SmoothStop(endValue: Measure<T>)
//        }

        if (timeOffset >= duration(initial, acceleration)) {
            return endState(initial)
        }

        return Moment(
                initial.position + initial.velocity * timeOffset + acceleration * timeOffset * timeOffset / 2,
                initial.velocity + acceleration * timeOffset)
    }

    override fun duration(initial: Moment<T>) = initial.let { duration(it, -it.velocity * it.velocity / (2 * (endValue - it.position))) }

    private fun duration(initialState: Moment<T>, acceleration: Measure<Acceleration<T>>): Measure<Time> {
        return -initialState.velocity / acceleration
    }

    override fun endState(initial: Moment<T>) = Moment(endValue, initial.velocity * 0)
}