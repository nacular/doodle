package io.nacular.doodle.scheduler.impl

import io.nacular.doodle.scheduler.AnimationScheduler
import io.nacular.doodle.scheduler.Scheduler
import io.nacular.doodle.scheduler.Strand
import io.nacular.doodle.scheduler.Task
import io.nacular.doodle.time.Timer
import io.nacular.measured.units.Measure
import io.nacular.measured.units.Time
import io.nacular.measured.units.Time.Companion.milliseconds
import io.nacular.measured.units.times
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Created by Nicholas Eddy on 5/18/21.
 */
internal open class SchedulerImpl(private val scope: CoroutineScope, private val timer: Timer): Scheduler {
    private inner class SimpleTask(timer: Timer, time: Measure<Time>, context: CoroutineContext = EmptyCoroutineContext, job: (Measure<Time>) -> Unit): Task {
        private val start = timer.now

        private val job = scope.launch(context) {
            kotlinx.coroutines.delay((time `in` milliseconds).toLong())
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
                kotlinx.coroutines.delay((time `in` milliseconds).toLong())
            }
        }

        override var completed = false

        override fun cancel() {
            job.cancel()
            completed = true
        }
    }

    private var shutdown = false

    override fun after(time: Measure<Time>, job: (Measure<Time>) -> Unit): Task = SimpleTask(timer, time, job = job)

    override fun every(time: Measure<Time>, job: (Measure<Time>) -> Unit): Task = RecurringTask(timer, time, job)

    override suspend fun delay(time: Measure<Time>) = kotlinx.coroutines.delay((time `in` milliseconds).toLong())

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

    open fun shutdown() {
        shutdown = true
    }
}

internal class DebounceEventQueue constructor(
                    appScope      : CoroutineScope,
                    context       : CoroutineContext,
        private val timer         : Timer,
        private val maxTimeToBlock: Measure<Time> = 4 * milliseconds
) {
    private val queue = Channel<() -> Unit>(Channel.UNLIMITED)

    private var job = appScope.launch(context) {
        var previousTime = timer.now

        for (event in queue) {
            val now = timer.now

            if (now - previousTime >= maxTimeToBlock) {
                previousTime = now
                yield()
            }

            event()
        }
    }

    fun cancel() = job.cancel()

    fun post(event: () -> Unit) {
        queue.trySend(event)
    }
}

internal class AnimationSchedulerImpl(appScope: CoroutineScope, uiDispatcher: CoroutineContext, private val timer: Timer): AnimationScheduler, SchedulerImpl(appScope, timer) {
    private val debounceQueue = DebounceEventQueue(appScope, timer = timer, context = uiDispatcher)

    override fun onNextFrame(job: (Measure<Time>) -> Unit): Task = object: Task {
        init {
            val start = timer.now

            debounceQueue.post {
                if (!completed) {
                    completed = true
                    job(timer.now - start)
                }
            }
        }

        override var completed = false

        override fun cancel() {
            completed = true
        }
    }

    override fun shutdown() {
        super.shutdown()
        debounceQueue.cancel()
    }
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