package io.nacular.doodle.scheduler

import io.nacular.doodle.utils.Cancelable
import io.nacular.doodle.utils.zeroMillis
import io.nacular.measured.units.Measure
import io.nacular.measured.units.Time

/**
 * Created by Nicholas Eddy on 10/19/17.
 */
public interface Task: Cancelable {
    public val completed: Boolean
}

public interface Scheduler {
    public fun now  (                     job: (Measure<Time>) -> Unit): Task = after(zeroMillis, job)
    public fun after(time: Measure<Time>, job: (Measure<Time>) -> Unit): Task
    public fun every(time: Measure<Time>, job: (Measure<Time>) -> Unit): Task

    public suspend fun delay     (time     :  Measure<Time>)
    public suspend fun delayUntil(predicate: (Measure<Time>) -> Boolean)
}

public interface AnimationScheduler {
    public fun onNextFrame(job: (Measure<Time>) -> Unit): Task
}

// TODO: Move to a better location
// FIXME: Better name
public interface Strand {
    public operator fun invoke(jobs: Sequence<() -> Unit>): Task
    public operator fun invoke(jobs: Iterable<() -> Unit>): Task
}