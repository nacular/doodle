package com.nectar.doodle.time.impl

import com.nectar.doodle.time.Clock
import com.nectar.doodle.units.Measure
import com.nectar.doodle.units.Time
import com.nectar.doodle.units.milliseconds
import kotlin.js.Date

/**
 * Created by Nicholas Eddy on 10/19/17.
 */
class SystemClock : Clock {
    override val epoch: Measure<Time> get() = Date().getTime().milliseconds
}