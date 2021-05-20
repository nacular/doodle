package io.nacular.doodle.time.impl

import io.nacular.doodle.time.Timer
import io.nacular.measured.units.Measure
import io.nacular.measured.units.Time
import io.nacular.measured.units.Time.Companion.milliseconds
import io.nacular.measured.units.times
import kotlinx.datetime.Clock

/**
 * Created by Nicholas Eddy on 5/19/21.
 */
internal class TimerImpl(private val clock: Clock): Timer {
    override val now: Measure<Time> get() = clock.now().toEpochMilliseconds() * milliseconds
}