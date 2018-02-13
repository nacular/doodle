package com.nectar.doodle.scheduler

import com.nectar.doodle.units.Measure
import com.nectar.doodle.units.Time
import com.nectar.doodle.units.milliseconds

/**
 * Created by Nicholas Eddy on 10/19/17.
 */

interface Task {
    fun cancel()
}

interface Scheduler {
    fun now   (job  : () -> Unit                    ): Task = after(0.milliseconds, job)
    fun after (time : Measure<Time>, job: () -> Unit): Task
    fun repeat(every: Measure<Time>, job: () -> Unit): Task
}