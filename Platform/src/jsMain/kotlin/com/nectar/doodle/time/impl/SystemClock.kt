package com.nectar.doodle.time.impl

import com.nectar.doodle.time.Clock
import com.nectar.doodle.time.Timer
import com.nectar.measured.units.Measure
import com.nectar.measured.units.Time
import com.nectar.measured.units.milliseconds
import com.nectar.measured.units.times
import kotlin.browser.window
import kotlin.js.Date

/**
 * Created by Nicholas Eddy on 10/19/17.
 */
class SystemClock: Clock {
    override val epoch: Measure<Time> get() = Date().getTime() * milliseconds
}

class PerformanceTimer: Timer {
    override val now get() = window.performance.now() * milliseconds // TODO: What if performance api not available?
}