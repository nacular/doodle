package com.nectar.doodle.scheduler.impl

import com.nectar.doodle.scheduler.AnimationScheduler
import com.nectar.doodle.scheduler.Scheduler
import com.nectar.doodle.scheduler.Strand
import com.nectar.doodle.scheduler.Task
import com.nectar.doodle.time.Timer
import com.nectar.measured.units.Measure
import com.nectar.measured.units.Time
import com.nectar.measured.units.milliseconds
import com.nectar.measured.units.times
import kotlin.browser.window
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Created by Nicholas Eddy on 10/19/17.
 */

private open class SimpleTask(timer: Timer, time: Measure<Time>, job: (Measure<Time>) -> Unit): Task {

    private val start = timer.now
    private val value = window.setTimeout({ job(timer.now - start); completed = true }, (time  `in` milliseconds).toInt())

    override var completed = false

    override fun cancel() {
        window.clearTimeout(value)
        completed = true
    }
}

private open class AnimationTask(job: (Measure<Time>) -> Unit): Task {

    private val value = window.requestAnimationFrame { time ->
        job(time * milliseconds)
        completed = true
    }

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
    private var shutdown = false

    override suspend fun delay(time: Measure<Time>) = suspendCoroutine<Unit> { coroutine ->
        after(time) { coroutine.resume(Unit) }
    }

    override suspend fun delayUntil(predicate: (Measure<Time>) -> Boolean) = suspendCoroutine<Unit> { coroutine ->
        check(predicate) {
            coroutine.resume(Unit)
        }
    }

    private fun check(predicate: (Measure<Time>) -> Boolean, complete: () -> Unit) {
        now {
            if (predicate(it)) { complete() }
            else if (!shutdown){
                check(predicate, complete)
            }
        }
    }

    override fun after(time: Measure<Time>, job: (Measure<Time>) -> Unit): Task = if (time.amount == 0.0) AnimationTask(job) else SimpleTask(timer, time, job)
    override fun every(time: Measure<Time>, job: (Measure<Time>) -> Unit): Task = RecurringTask(timer, time, job)

    override fun shutdown() {
        shutdown = true
    }
}

internal class AnimationSchedulerImpl: AnimationScheduler {
    override fun onNextFrame(job: (Measure<Time>) -> Unit): Task = AnimationTask(job)
}

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

    companion object {
        private val frameDuration = 1000 * milliseconds / 60
    }
}

internal class StrandImpl(private val scheduler: AnimationScheduler, private val timer: Timer): Strand {
    override operator fun invoke(jobs: Sequence<() -> Unit>): Task = invoke(jobs.asIterable())
    override operator fun invoke(jobs: Iterable<() -> Unit>): Task = DistributedAnimationTask(scheduler, timer, jobs.iterator())
}