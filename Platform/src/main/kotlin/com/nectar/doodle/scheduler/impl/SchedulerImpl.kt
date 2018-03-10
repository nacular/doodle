package com.nectar.doodle.scheduler.impl

import com.nectar.doodle.scheduler.Scheduler
import com.nectar.doodle.scheduler.Task
import com.nectar.doodle.units.Measure
import com.nectar.doodle.units.Time
import com.nectar.doodle.units.milliseconds
import kotlin.browser.window

/**
 * Created by Nicholas Eddy on 10/19/17.
 */

private open class SimpleTask(time : Measure<Time>, job: () -> Unit): Task {

    private val value = window.setTimeout({ job(); completed = true }, (time  `in` milliseconds).toInt())

    override var completed = false

    override fun cancel() {
        window.clearTimeout(value)
        completed = true
    }
}

private open class AnimationTask(job: () -> Unit): Task {

    private val value = window.requestAnimationFrame { job(); completed = true }

    override var completed = false

    override fun cancel() {
        window.cancelAnimationFrame(value)
        completed = true
    }
}

private class RecurringTask(time : Measure<Time>, job: () -> Unit): Task {
    private val value: Int = window.setInterval(job,  (time  `in` milliseconds).toInt())

    override var completed = false

    override fun cancel() {
        window.clearInterval(value)
        completed = true
    }
}

internal class SchedulerImpl: Scheduler {
    // TODO: Separate animation scheduler into different interface
    override fun after (time : Measure<Time>, job: () -> Unit): Task = if (time.isZero()) AnimationTask(job) else SimpleTask(time, job)
    override fun repeat(every: Measure<Time>, job: () -> Unit): Task = RecurringTask(every, job)
}