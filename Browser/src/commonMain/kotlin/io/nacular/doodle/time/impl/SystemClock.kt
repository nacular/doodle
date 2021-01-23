package io.nacular.doodle.time.impl

import io.nacular.doodle.dom.Date
import io.nacular.doodle.dom.Performance
import io.nacular.doodle.time.Clock
import io.nacular.doodle.time.Timer
import io.nacular.measured.units.Measure
import io.nacular.measured.units.Time
import io.nacular.measured.units.Time.Companion.milliseconds
import io.nacular.measured.units.times

/**
 * Created by Nicholas Eddy on 10/19/17.
 */
public class SystemClock: Clock {
    override val epoch: Measure<Time> get() = Date.now() * milliseconds
}

internal class PerformanceTimer(private val performance: Performance?): Timer {
    override val now get() = (performance?.now() ?: Date.now()) * milliseconds
}