package com.zinoti.jaz.scheduler

import com.zinoti.jaz.units.Measure
import com.zinoti.jaz.units.Time

/**
 * Created by Nicholas Eddy on 10/19/17.
 */

interface Task {
    fun cancel()
}

interface Scheduler {
    fun after (time : Measure<Time>, job: () -> Unit): Task
    fun repeat(every: Measure<Time>, job: () -> Unit): Task
}