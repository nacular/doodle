package com.nectar.doodle.scheduler

import com.nectar.doodle.utils.Cancelable
import com.nectar.measured.units.Measure
import com.nectar.measured.units.Time
import com.nectar.measured.units.milliseconds
import com.nectar.measured.units.times

/**
 * Created by Nicholas Eddy on 10/19/17.
 */

interface Task: Cancelable {
    val completed: Boolean
}

interface Scheduler {
    fun now  (                     job: (Measure<Time>) -> Unit): Task = after(0 * milliseconds, job)
    fun after(time: Measure<Time>, job: (Measure<Time>) -> Unit): Task
    fun every(time: Measure<Time>, job: (Measure<Time>) -> Unit): Task

    suspend fun delay     (time     :  Measure<Time>)
    suspend fun delayUntil(predicate: (Measure<Time>) -> Boolean)
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