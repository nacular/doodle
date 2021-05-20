package io.nacular.doodle.scheduler.impl

import io.nacular.doodle.scheduler.AnimationScheduler
import io.nacular.doodle.scheduler.Scheduler
import io.nacular.doodle.scheduler.Task
import io.nacular.doodle.time.Timer
import io.nacular.measured.units.Measure
import io.nacular.measured.units.Time
import io.nacular.measured.units.Time.Companion.seconds
import io.nacular.measured.units.times
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Created by Nicholas Eddy on 5/18/21.
 */
internal open class SchedulerImpl(private val scope: CoroutineScope, private val timer: Timer): Scheduler {
    private inner class SimpleTask(timer: Timer, time: Measure<Time>, job: (Measure<Time>) -> Unit): Task {
        private val start = timer.now

        private val job = scope.launch {
            kotlinx.coroutines.delay((time `in` Time.milliseconds).toLong())
            completed = true
            job(timer.now - start)
        }

        override var completed = false

        override fun cancel() {
            job.cancel()
            completed = true
        }
    }

    private inner class RecurringTask(timer: Timer, time : Measure<Time>, job: (Measure<Time>) -> Unit): Task {
        private var last = timer.now

        private val job = scope.launch {
            while(true) {
                timer.now.let { job(it - last); last = it }
                kotlinx.coroutines.delay((time `in` Time.milliseconds).toLong())
            }
        }

        override var completed = false

        override fun cancel() {
            job.cancel()
            completed = true
        }
    }

    private var shutdown = false

    override fun after(time: Measure<Time>, job: (Measure<Time>) -> Unit): Task = SimpleTask(timer, time, job)

    override fun every(time: Measure<Time>, job: (Measure<Time>) -> Unit): Task = RecurringTask(timer, time, job)

    override suspend fun delay(time: Measure<Time>) = kotlinx.coroutines.delay((time `in` Time.milliseconds).toLong())

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

    fun shutdown() {
        shutdown = true
    }
}

internal class AnimationSchedulerImpl(scope: CoroutineScope, timer: Timer): AnimationScheduler, SchedulerImpl(scope, timer) {
    override fun onNextFrame(job: (Measure<Time>) -> Unit): Task = after(1 * seconds / 60, job)
}