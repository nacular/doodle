package com.zinoti.jaz.time.impl

import com.zinoti.jaz.time.Clock
import com.zinoti.jaz.units.Measure
import com.zinoti.jaz.units.Time
import com.zinoti.jaz.units.milliseconds
import kotlin.js.Date

/**
 * Created by Nicholas Eddy on 10/19/17.
 */
class SystemClock : Clock {
    override val epoch: Measure<Time> get() = Date().getTime().milliseconds
}