package com.nectar.doodle

import com.nectar.doodle.scheduler.Scheduler
import com.nectar.doodle.scheduler.Task
import com.nectar.measured.units.Measure
import com.nectar.measured.units.Time
import com.nectar.measured.units.milliseconds
import com.nectar.measured.units.times

private class ManualScheduler: Scheduler {
    private class SimpleTask(override var completed: Boolean = false) : Task {
        override fun cancel() {
            completed = true
        }
    }

    val tasks = mutableListOf<Pair<SimpleTask, (Measure<Time>) -> Unit>>()

    fun runJobs() = tasks.forEach {
        it.first.completed = true
        it.second(0 * milliseconds)
    }

    override fun after(time: Measure<Time>, job: (Measure<Time>) -> Unit): Task {
        val task = SimpleTask()

        tasks += task to job

        return task
    }

    override fun repeat(every: Measure<Time>, job: (Measure<Time>) -> Unit): Task {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}