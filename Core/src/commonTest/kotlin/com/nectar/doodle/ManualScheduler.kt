package com.nectar.doodle

import com.nectar.doodle.scheduler.Scheduler
import com.nectar.doodle.scheduler.Task
import com.nectar.measured.units.Measure
import com.nectar.measured.units.Time
import com.nectar.measured.units.milliseconds
import com.nectar.measured.units.times

class ManualScheduler: Scheduler {
    private class SimpleTask(override var completed: Boolean = false) : Task {
        override fun cancel() {
            completed = true
        }
    }

    private val tasks = mutableListOf<Pair<SimpleTask, (Measure<Time>) -> Unit>>()

    fun runJobs() = tasks.forEach {
        it.first.completed = true
        it.second(0 * milliseconds)
    }

    override fun after(time: Measure<Time>, job: (Measure<Time>) -> Unit): Task {
        val task = SimpleTask()

        tasks += task to job

        return task
    }

    override fun every(time: Measure<Time>, job: (Measure<Time>) -> Unit): Task {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun delay(time: Measure<Time>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun delayUntil(predicate: (Measure<Time>) -> Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}