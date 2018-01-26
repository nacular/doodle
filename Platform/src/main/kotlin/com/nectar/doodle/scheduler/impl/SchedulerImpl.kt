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

private open class SimpleTask(protected val value: Int): Task {
    override fun cancel() = window.clearTimeout(value)
}

private class RecurringTask(value: Int): SimpleTask(value) {
    override fun cancel() = window.clearInterval(value)
}

internal class SchedulerImpl: Scheduler {
    override fun after (time : Measure<Time>, job: () -> Unit): Task = SimpleTask   (window.setTimeout(job,  (time  `in` milliseconds).toInt()))
    override fun repeat(every: Measure<Time>, job: () -> Unit): Task = RecurringTask(window.setInterval(job, (every `in` milliseconds).toInt()))
}