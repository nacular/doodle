package com.nectar.doodle.time.impl

import com.nectar.doodle.dom.Date
import com.nectar.doodle.dom.Performance
import com.nectar.doodle.time.Clock
import com.nectar.doodle.time.Timer
import com.nectar.measured.units.Measure
import com.nectar.measured.units.Time
import com.nectar.measured.units.Time.Companion.milliseconds
import com.nectar.measured.units.times

/**
 * Created by Nicholas Eddy on 10/19/17.
 */
class SystemClock: Clock {
    override val epoch: Measure<Time> get() = Date.now() * milliseconds
}

class PerformanceTimer(private val performance: Performance?): Timer {
    override val now get() = (performance?.now() ?: Date.now()) * milliseconds
}