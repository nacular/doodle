package com.zinoti.jaz.scheduler.impl

import com.zinoti.jaz.scheduler.Scheduler
import com.zinoti.jaz.scheduler.Task
import com.zinoti.jaz.time.Clock
import com.zinoti.jaz.units.Measure
import com.zinoti.jaz.units.Time
import com.zinoti.jaz.units.milliseconds
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

class SchedulerImpl(private val clock: Clock): Scheduler {
    override fun after (time : Measure<Time>, job: () -> Unit): Task = SimpleTask   (window.setTimeout(job,  (time  `in` milliseconds).toInt()))
    override fun repeat(every: Measure<Time>, job: () -> Unit): Task = RecurringTask(window.setInterval(job, (every `in` milliseconds).toInt()))
}