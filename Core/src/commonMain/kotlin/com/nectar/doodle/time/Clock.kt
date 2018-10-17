package com.nectar.doodle.time

import com.nectar.measured.units.Measure
import com.nectar.measured.units.Time

/**
 * Created by Nicholas Eddy on 10/19/17.
 */
interface Clock {
    val epoch: Measure<Time>
}

interface Timer {
    val now: Measure<Time>
}