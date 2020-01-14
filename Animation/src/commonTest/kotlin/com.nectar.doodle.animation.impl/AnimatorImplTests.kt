package com.nectar.doodle.animation.impl

import com.nectar.doodle.animation.Animator
import com.nectar.doodle.animation.Listener
import com.nectar.doodle.animation.NoneUnit
import com.nectar.doodle.animation.noneUnits
import com.nectar.doodle.animation.transition.FixedTimeLinear
import com.nectar.doodle.scheduler.AnimationScheduler
import com.nectar.doodle.scheduler.Scheduler
import com.nectar.doodle.scheduler.Task
import com.nectar.doodle.time.Timer
import com.nectar.measured.units.Measure
import com.nectar.measured.units.Time
import com.nectar.measured.units.milliseconds
import com.nectar.measured.units.times
import kotlin.test.Test

/**
 * Created by Nicholas Eddy on 12/15/19.
 */
private class MonotonicTimer: Timer {
    override var now = 100 * milliseconds
        get() = field.also { field += 1 * milliseconds }
        private set
}

private class TestAnimationScheduler: AnimationScheduler {
    override fun onNextFrame(job: (Measure<Time>) -> Unit): Task = object: Task {
        override val completed = true

        override fun cancel() {}
    }.also {
//        GlobalScope.launch {
            job(1 * milliseconds)
//        }
    }
}

private class ManualScheduler: Scheduler {
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

class AnimatorImplTests {
    @Test fun foo() {
        val timer              = MonotonicTimer()
        val scheduler          = ManualScheduler()
        val animationScheduler = TestAnimationScheduler()

        val animate = AnimatorImpl<String>(timer, scheduler, animationScheduler)

        var progress1 = 0f
        var progress2 = 0f

        animate("a", progress1 * noneUnits).apply {
            this using FixedTimeLinear(280 * milliseconds, 1 * noneUnits)
        }
//        animate("b", progress2 * noneUnits).apply {
//            this using SpeedUpSlowDown(280 * milliseconds, 1 * noneUnits)
//        }
        animate += object: Listener<String> {
            override fun changed(animator: Animator<String>, properties: Map<String, Listener.ChangeEvent<String, *>>) {
                properties.forEach { (property, change) ->
                    println("$property -> ${change.new}")

                    when (property) {
                        "a" -> progress1 = (change.new as Measure<NoneUnit> `in` noneUnits).toFloat()
                        "b" -> progress2 = (change.new as Measure<NoneUnit> `in` noneUnits).toFloat()
                    }
                }
            }
        }
        animate.start()

        scheduler.runJobs()

        animate("a", progress1 * noneUnits).apply {
            this using FixedTimeLinear(280 * milliseconds, 0 * noneUnits)
        }

        animate.start()

        scheduler.runJobs()
    }
}