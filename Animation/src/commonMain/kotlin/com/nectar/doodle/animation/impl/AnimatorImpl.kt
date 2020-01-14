package com.nectar.doodle.animation.impl

import com.nectar.doodle.animation.Animation
import com.nectar.doodle.animation.Animator
import com.nectar.doodle.animation.Animator.Listener
import com.nectar.doodle.animation.Animator.MeasureTransitionBuilder
import com.nectar.doodle.animation.Animator.TransitionBuilder
import com.nectar.doodle.animation.Moment
import com.nectar.doodle.animation.NoneUnit
import com.nectar.doodle.animation.noneUnits
import com.nectar.doodle.animation.transition.Transition
import com.nectar.doodle.scheduler.AnimationScheduler
import com.nectar.doodle.scheduler.Task
import com.nectar.doodle.time.Timer
import com.nectar.doodle.utils.Completable
import com.nectar.doodle.utils.CompletableImpl
import com.nectar.doodle.utils.ObservableSet
import com.nectar.doodle.utils.SetPool
import com.nectar.measured.units.Measure
import com.nectar.measured.units.Time
import com.nectar.measured.units.div
import com.nectar.measured.units.milliseconds
import com.nectar.measured.units.times

/**
 * Created by Nicholas Eddy on 1/12/20.
 */

private class KeyFrame {
    var time: Measure<Time> = 0 * milliseconds
}

private class InternalProperty<T: com.nectar.measured.units.Unit>(initialValue: Measure<T>) {

    val transitions      = mutableListOf<TransitionNode<T>>()
    var activeTransition = null as TransitionNode<T>?
        private set

    var value = Moment(initialValue, 0 * initialValue / (1 * milliseconds))

    fun add(transition: Transition<T>) {
        val start = if (transitions.isEmpty()) KeyFrame() else transitions.last().endTime
        val end   = KeyFrame()

        add(transition, start, end)
    }

    fun add(transition: Transition<T>, start: KeyFrame, end: KeyFrame) {
        transitions += TransitionNode(transition, start, end)
    }

    fun nextTransition(initialValue: Moment<T>, elapsedTime: Measure<Time>): TransitionNode<T>? = transitions.firstOrNull()?.let {
        when (it.shouldStart(elapsedTime)) {
            true -> {
                it.calculateEndTime(initialValue)

                transitions      -= it
                activeTransition  = it

                it
            }
            else -> null
        }
    }
}

private class TransitionNode<T: com.nectar.measured.units.Unit>(val transition: Transition<T>, val startTime: KeyFrame, var endTime: KeyFrame) {

    fun shouldStart(elapsedTime: Measure<Time>) = startTime.time <= elapsedTime

    fun calculateEndTime(initialState: Moment<T>) {
        endTime.time = startTime.time + transition.duration(initialState)
    }
}

private class Result<T: com.nectar.measured.units.Unit>(val active: Boolean, val old: Measure<T>, val new: Measure<T>)

class AnimatorImpl(
        private val timer             : Timer,
        private val animationScheduler: AnimationScheduler): Animator {

    private inner class AnimationImpl<T: com.nectar.measured.units.Unit>(
            private val property: InternalProperty<T>, private val block: (Measure<T>) -> Unit): Animation, CompletableImpl() {
        private lateinit var startTime: Measure<Time>

        fun run(currentTime: Measure<Time>): Result<T> {
            if (!::startTime.isInitialized) {
                startTime = currentTime
            }

            val totalElapsedTime = currentTime - startTime
            val oldPosition      = property.value.position
            var momentValue      = property.value
            var activeTransition = property.activeTransition ?: property.nextTransition(momentValue, totalElapsedTime)

            // Skip over out-dated Transitions, making sure to take their end-state into account
            while (activeTransition != null && activeTransition.endTime.time < totalElapsedTime) {
                momentValue      = activeTransition.transition.endState(momentValue)
                activeTransition = property.nextTransition(momentValue, totalElapsedTime)
                property.value   = momentValue
            }

            if (activeTransition != null) {
                momentValue = activeTransition.transition.value(momentValue, totalElapsedTime - activeTransition.startTime.time)
            }

            return Result(activeTransition != null, oldPosition, momentValue.position).also {
                if (it.new != it.old) {
                    block(it.new)
                }
            }
        }

        override fun cancel() {
            super.cancel()

            animations -= this
        }
    }

    private inner class TransitionBuilderImpl<T: Number>(
            start     : T,
            end       : T,
            transition: (start: T, end: T) -> Transition<NoneUnit>): TransitionBuilder<T> {

        private val property = InternalProperty(start * noneUnits).apply { add(transition(start, end)) }

        override fun then(transition: Transition<NoneUnit>) = this.also { property.add(transition) }

        override fun invoke(block: (T) -> Unit): Animation {
            return AnimationImpl(property) { block(it.amount as T) }.also { animations += it }
        }
    }

    private var task       = null as Task?
    private val animations = ObservableSet<AnimationImpl<*>>().apply {
        changed += { _,_,_ ->
            when {
                isNotEmpty() -> startAnimation()
                else         -> task?.cancel()
            }
        }
    }

    override fun <T : Number> Pair<T, T>.using(transition: (start: T, end: T) -> Transition<NoneUnit>): TransitionBuilder<T> = TransitionBuilderImpl(first, second, transition)

    override fun <T : com.nectar.measured.units.Unit> Pair<Measure<T>, Measure<T>>.using(transition: (start: Measure<T>, end: Measure<T>) -> Transition<T>): MeasureTransitionBuilder<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun invoke(block: Animator.() -> Unit): Completable {
        val newAnimations = mutableSetOf<Animation>()

        val listener: (ObservableSet<AnimationImpl<*>>, Set<AnimationImpl<*>>, Set<AnimationImpl<*>>) -> Unit = { _,_,new ->
            newAnimations += new
        }

        animations.changed += listener

        this.apply(block)

        animations.changed -= listener

        return object: CompletableImpl() {
            override fun cancel() {
                newAnimations.forEach { it.cancel() }

                super.cancel()
            }
        }
    }

    override val listeners = SetPool<Listener>()

    private fun startAnimation() {
        task = animationScheduler.onNextFrame {
            onAnimate()
        }
    }

    private fun onAnimate() {
        val changed   = mutableSetOf<Animation>()
        val completed = mutableSetOf<Animation>()

        animations.forEach {
            val result = it.run(timer.now).also { result ->
                if (!result.active) {
                    completed  += it
                    animations -= it
                }
            }

            if (result.new != result.old) {
                changed += it

            }
        }

        if (changed.isNotEmpty()) {
            listeners.forEach {
                it.changed(this, changed)
            }
        }

        if (animations.isNotEmpty()) {
            task = animationScheduler.onNextFrame {
                onAnimate()
            }
        } else {
            listeners.forEach { it.completed(this, completed) }
        }
    }
}