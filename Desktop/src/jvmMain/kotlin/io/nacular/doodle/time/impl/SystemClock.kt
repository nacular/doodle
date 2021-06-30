package io.nacular.doodle.time.impl

import io.nacular.doodle.time.Clock
import io.nacular.measured.units.Measure
import io.nacular.measured.units.Time
import io.nacular.measured.units.Time.Companion.milliseconds
import io.nacular.measured.units.times

/**
 * Created by Nicholas Eddy on 6/25/21.
 */
public class SystemClock(private val delegate: kotlinx.datetime.Clock.System): Clock {
    override val epoch: Measure<Time> get() = delegate.now().toEpochMilliseconds() * milliseconds
}