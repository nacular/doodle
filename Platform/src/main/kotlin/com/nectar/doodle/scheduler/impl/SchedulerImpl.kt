package com.nectar.doodle.scheduler.impl

import com.nectar.doodle.scheduler.AnimationScheduler
import com.nectar.doodle.scheduler.Scheduler
import com.nectar.doodle.scheduler.Strand
import com.nectar.doodle.scheduler.Task
import com.nectar.doodle.time.Timer
import com.nectar.measured.units.Measure
import com.nectar.measured.units.Time
import com.nectar.measured.units.milliseconds
import kotlin.browser.window

/**
 * Created by Nicholas Eddy on 10/19/17.
 */

private open class SimpleTask(timer: Timer, time: Measure<Time>, job: (Measure<Time>) -> Unit): Task {

    private val start = timer.now
    private val value = window.setTimeout({ completed = true; job(timer.now - start) }, (time  `in` milliseconds).toInt())

    override var completed = false

    override fun cancel() {
        window.clearTimeout(value)
        completed = true
    }
}

private open class AnimationTask(job: (Measure<Time>) -> Unit): Task {

    private val value = window.requestAnimationFrame { time -> completed = true; job(time.milliseconds) }

    override var completed = false

    override fun cancel() {
        window.cancelAnimationFrame(value)
        completed = true
    }
}

private class RecurringTask(timer: Timer, time : Measure<Time>, job: (Measure<Time>) -> Unit): Task {

    private var last = timer.now
    private val value: Int = window.setInterval({ timer.now.let { job(it - last); last = it } },  (time `in` milliseconds).toInt())

    override var completed = false

    override fun cancel() {
        window.clearInterval(value)
        completed = true
    }
}

internal class SchedulerImpl(private val timer: Timer): Scheduler {
    // TODO: Separate animation scheduler into different interface
    override fun after (time : Measure<Time>, job: (Measure<Time>) -> Unit): Task = if (time.isZero) AnimationTask(job) else SimpleTask(timer, time, job)
    override fun repeat(every: Measure<Time>, job: (Measure<Time>) -> Unit): Task = RecurringTask(timer, every, job)
}

internal class AnimationSchedulerImpl: AnimationScheduler {
    override fun onNextFrame(job: (Measure<Time>) -> Unit): Task = AnimationTask(job)
}


// TODO: Move to a better location
private val frameDuration = 1000.milliseconds / 60

private open class DistributedAnimationTask(private val scheduler: AnimationScheduler, private val timer: Timer, private val jobs: Iterator<() -> Unit>): Task {

    override var completed = false

    private var task: Task? = null

    init {
        processJobs()
    }

    override fun cancel() {
        task?.cancel()

        completed = true
    }

    private fun scheduleJob() {
        if (task == null || task?.completed == true) {
            task = scheduler.onNextFrame { processJobs() }
        }
    }

    private fun frameExpired(start: Measure<Time>) = (timer.now - start) >= frameDuration

    private fun processJobs() {
        val start = timer.now

        while (jobs.hasNext()) {
            jobs.next()()

            if (frameExpired(start)) { scheduleJob(); return }
        }
    }
}

internal class StrandImpl(private val scheduler: AnimationScheduler, private val timer: Timer): Strand {
    override operator fun invoke(jobs: Sequence<() -> Unit>): Task = invoke(jobs.asIterable())
    override operator fun invoke(jobs: Iterable<() -> Unit>): Task = DistributedAnimationTask(scheduler, timer, jobs.iterator())
}