package io.nacular.doodle.animation.transition

import io.nacular.doodle.animation.Moment
import io.nacular.measured.units.Measure
import io.nacular.measured.units.Time
import io.nacular.measured.units.Time.Companion.milliseconds
import io.nacular.measured.units.Units
import io.nacular.measured.units.div
import io.nacular.measured.units.times

/**
 * Created by Nicholas Eddy on 10/29/18.
 */
public class SpeedUpSlowDown<T: Units>(
                    duration            : Measure<Time>,
        private val endValue            : Measure<T>,
        private val accelerationFraction: Float = 0.5f): FixedDuration<T>(duration)
{
    init {
        require(accelerationFraction in 0f..1f) { "accelerationFraction must be in range [0, 1]" }
    }

    override fun value(initial: Moment<T>, timeOffset: Measure<Time>): Moment<T> {
        val duration = duration(initial)

        if( timeOffset >= duration ) { return endState(initial) }

        val time1         = duration * accelerationFraction
        val position1     = initial.position
        val acceleration1 = 2 * (endValue - position1) * accelerationFraction / (time1 * time1)

        if( timeOffset < time1 ) {
            return Moment( position1 + acceleration1 * timeOffset * timeOffset / 2, acceleration1 * timeOffset )
        }

        val position       = position1 + acceleration1 * time1 * time1 / 2
        val velocity       = acceleration1 * time1
        val acceleration2  = -velocity / (duration - time1)
        val timeOffset2    = timeOffset - time1
        val outputVelocity = velocity + acceleration2 * timeOffset2

        return Moment(position + velocity * timeOffset2 + acceleration2 * timeOffset2 * timeOffset2 / 2, outputVelocity )
    }

    override fun endState(initial: Moment<T>): Moment<T> = Moment(endValue, 0 * endValue / (1 * milliseconds))
}