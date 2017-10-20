package com.zinoti.jaz.time

import com.zinoti.jaz.units.Measure
import com.zinoti.jaz.units.Time

/**
 * Created by Nicholas Eddy on 10/19/17.
 */
interface Clock {
    val epoch: Measure<Time>
}