package com.nectar.doodle.animation.transition

import com.nectar.doodle.animation.Acceleration
import com.nectar.doodle.animation.Moment
import com.nectar.measured.units.Measure
import com.nectar.measured.units.Time
import com.nectar.measured.units.Unit
import com.nectar.measured.units.div
import com.nectar.measured.units.times

/**
 * Created by Nicholas Eddy on 3/30/18.
 */
//class SmoothStop<T>(private val endValue: Measure<T>): Transition<T> {
//    override fun value(initialState: Moment<T>, timeOffset: Measure<Time>): Moment<T> {
////        val acceleration = -(initialState.velocity * initialState.velocity) / ((endValue - initialState.position) * 2)
//
////        if (timeOffset >= duration(initialState, acceleration)) {
//            return endState(initialState)
////        }
//
//        // TODO: Handle velocity units?
//        return Moment(
//                initialState.position + initialState.velocity * timeOffset /*+ acceleration * timeOffset.pow(2.0) / 2*/,
//                initialState.velocity /*+ acceleration * timeOffset*/)
//    }
//
//    override fun duration(initialState: Moment<T>) = 0.seconds //duration(initialState, -initialState.velocity.pow(2.0) / ((endValue - initialState.position) * 2))
//
//    private fun duration(initialState: Moment<T>, acceleration: Double): Measure<Time> {
//        return 0.seconds //-initialState.velocity / acceleration
//    }
//
//    override fun endState(initialState: Moment<T>) = Moment(endValue, MeasureRatio.zero())
//}

class SmoothStop<T: Unit>(private val endValue: Measure<T>): Transition<T> {
    override fun value(initialState: Moment<T>, timeOffset: Measure<Time>): Moment<T> {
        val acceleration: Measure<Acceleration<T>> = -(initialState.velocity * initialState.velocity) / ((endValue - initialState.position) * 2)

        if (timeOffset >= duration(initialState, acceleration)) {
            return endState(initialState)
        }

        // TODO: Handle velocity units?
        return Moment(
                initialState.position + initialState.velocity * timeOffset + acceleration * timeOffset * timeOffset / 2,
                initialState.velocity + acceleration * timeOffset)
    }

    override fun duration(initialState: Moment<T>) = duration(initialState, -initialState.velocity * initialState.velocity / ((endValue - initialState.position) * 2))

    private fun duration(initialState: Moment<T>, acceleration: Measure<Acceleration<T>>): Measure<Time> {
        return -initialState.velocity / acceleration
    }

    override fun endState(initialState: Moment<T>) = Moment(endValue, initialState.velocity * 0)
}