package com.nectar.doodle.scheduler

import com.nectar.doodle.units.Measure
import com.nectar.doodle.units.Time
import com.nectar.doodle.units.milliseconds

/**
 * Created by Nicholas Eddy on 10/19/17.
 */

interface Task {
    val completed: Boolean

    fun cancel()
}

interface Scheduler {
    fun now   (                      job: (Measure<Time>) -> Unit): Task = after(0.milliseconds, job)
    fun after (time : Measure<Time>, job: (Measure<Time>) -> Unit): Task
    fun repeat(every: Measure<Time>, job: (Measure<Time>) -> Unit): Task
}

interface AnimationScheduler {
    fun onNextFrame(job: (Measure<Time>) -> Unit): Task
}

// TODO: Move to a better location
// FIXME: Better name
interface Strand {
    operator fun invoke(jobs: Sequence<() -> Unit>): Task
    operator fun invoke(jobs: Iterable<() -> Unit>): Task
}