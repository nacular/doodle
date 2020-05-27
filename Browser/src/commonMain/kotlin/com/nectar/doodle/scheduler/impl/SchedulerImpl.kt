package com.nectar.doodle.scheduler.impl

import com.nectar.doodle.dom.Window
import com.nectar.doodle.scheduler.AnimationScheduler
import com.nectar.doodle.scheduler.Scheduler
import com.nectar.doodle.scheduler.Strand
import com.nectar.doodle.scheduler.Task
import com.nectar.doodle.time.Timer
import com.nectar.measured.units.Measure
import com.nectar.measured.units.Time
import com.nectar.measured.units.Time.Companion.milliseconds
import com.nectar.measured.units.times
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Created by Nicholas Eddy on 10/19/17.
 */

private open class SimpleTask(private val window: Window, timer: Timer, time: Measure<Time>, job: (Measure<Time>) -> Unit): Task {

    private val start = timer.now
    private val value = window.setTimeout({ completed = true; job(timer.now - start) }, (time  `in` milliseconds).toInt())

    override var completed = false

    override fun cancel() {
        window.clearTimeout(value)
        completed = true
    }
}

private open class AnimationTask(private val window: Window, job: (Measure<Time>) -> Unit): Task {

    private val value = window.requestAnimationFrame { time ->
        completed = true
        job(time * milliseconds)
    }

    override var completed = false

    override fun cancel() {
        completed = true
        window.cancelAnimationFrame(value)
    }
}

private class RecurringTask(private val window: Window, timer: Timer, time : Measure<Time>, job: (Measure<Time>) -> Unit): Task {

    private var last = timer.now
    private val value: Int = window.setInterval({ timer.now.let { job(it - last); last = it } },  (time `in` milliseconds).toInt())

    override var completed = false

    override fun cancel() {
        window.clearInterval(value)
        completed = true
    }
}

internal class SchedulerImpl(private val window: Window, private val timer: Timer): Scheduler {
    private var shutdown = false

    override suspend fun delay(time: Measure<Time>) = suspendCoroutine<Unit> { coroutine ->
        after(time) {
            try {
                coroutine.context.ensureActive()
                coroutine.resume(Unit)
            } catch (e: CancellationException) {
                coroutine.resumeWithException(e)
            }
        }
    }

    override suspend fun delayUntil(predicate: (Measure<Time>) -> Boolean) = suspendCoroutine<Unit> { coroutine ->
        try {
            check(predicate, coroutine.context) {
                coroutine.resume(Unit)
            }
        } catch (e: CancellationException) {
            coroutine.resumeWithException(e)
        }
    }

    private fun check(predicate: (Measure<Time>) -> Boolean, coroutineContext: CoroutineContext, complete: () -> Unit) {
        now {
            coroutineContext.ensureActive()
            when {
                predicate(it) -> complete()
                !shutdown     -> check(predicate, coroutineContext, complete)
            }
        }
    }

    override fun after(time: Measure<Time>, job: (Measure<Time>) -> Unit): Task = if (time.amount == 0.0) AnimationTask(window, job) else SimpleTask(window, timer, time, job)
    override fun every(time: Measure<Time>, job: (Measure<Time>) -> Unit): Task = RecurringTask(window, timer, time, job)

    override fun shutdown() {
        shutdown = true
    }
}

internal class AnimationSchedulerImpl(private val window: Window): AnimationScheduler {
    override fun onNextFrame(job: (Measure<Time>) -> Unit): Task = AnimationTask(window, job)
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

    private fun frameExpired(start: Measure<Time>) = (timer.now - start) >= FRAME_DURATION

    private fun processJobs() {
        val start = timer.now

        while (jobs.hasNext()) {
            jobs.next()()

            if (frameExpired(start)) { scheduleJob(); return }
        }
    }

    private companion object {
        private val FRAME_DURATION = 1000 * milliseconds / 60
    }
}

internal class StrandImpl(private val scheduler: AnimationScheduler, private val timer: Timer): Strand {
    override operator fun invoke(jobs: Sequence<() -> Unit>): Task = invoke(jobs.asIterable())
    override operator fun invoke(jobs: Iterable<() -> Unit>): Task = DistributedAnimationTask(scheduler, timer, jobs.iterator())
}