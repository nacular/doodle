package com.nectar.doodle

import com.nectar.doodle.scheduler.Scheduler
import com.nectar.doodle.scheduler.Task
import com.nectar.doodle.units.Measure
import com.nectar.doodle.units.Time

private class ManualScheduler: Scheduler {
    private class SimpleTask(override var completed: Boolean = false) : Task {
        override fun cancel() {
            completed = true
        }
    }

    val tasks = mutableListOf<Pair<SimpleTask, () -> Unit>>()

    fun runJobs() = tasks.forEach {
        it.first.completed = true
        it.second()
    }

    override fun after(time: Measure<Time>, job: () -> Unit): Task {
        val task = SimpleTask()

        tasks += task to job

        return task
    }

    override fun repeat(every: Measure<Time>, job: () -> Unit): Task {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}