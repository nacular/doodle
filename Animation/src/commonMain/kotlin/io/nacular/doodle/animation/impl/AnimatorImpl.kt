package io.nacular.doodle.animation.impl

import io.nacular.doodle.animation.Animation
import io.nacular.doodle.animation.Animator
import io.nacular.doodle.animation.Animator.Listener
import io.nacular.doodle.animation.Animator.MeasureTransitionBuilder
import io.nacular.doodle.animation.Animator.TransitionBuilder
import io.nacular.doodle.animation.Moment
import io.nacular.doodle.animation.NoneUnit
import io.nacular.doodle.animation.noneUnits
import io.nacular.doodle.animation.transition.Transition
import io.nacular.doodle.scheduler.AnimationScheduler
import io.nacular.doodle.scheduler.Task
import io.nacular.doodle.time.Timer
import io.nacular.doodle.utils.Completable
import io.nacular.doodle.utils.CompletableImpl
import io.nacular.doodle.utils.CompletableImpl.State.Active
import io.nacular.doodle.utils.CompletableImpl.State.Canceled
import io.nacular.doodle.utils.ObservableSet
import io.nacular.doodle.utils.Pool
import io.nacular.doodle.utils.SetPool
import io.nacular.measured.units.Measure
import io.nacular.measured.units.Time
import io.nacular.measured.units.Time.Companion.milliseconds
import io.nacular.measured.units.Units
import io.nacular.measured.units.div
import io.nacular.measured.units.times

/**
 * Created by Nicholas Eddy on 1/12/20.
 */

public class AnimatorImpl(private val timer: Timer, private val animationScheduler: AnimationScheduler): Animator {

    private class KeyFrame {
        var time = 0 * milliseconds
    }

    private class InternalProperty<T: Units>(initialValue: Measure<T>) {

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

    private class TransitionNode<T: Units>(val transition: Transition<T>, val startTime: KeyFrame, var endTime: KeyFrame) {

        fun shouldStart(elapsedTime: Measure<Time>) = startTime.time <= elapsedTime

        fun calculateEndTime(initialState: Moment<T>) {
            endTime.time = startTime.time + transition.duration(initialState)
        }
    }

    private class Result<T: Units>(val active: Boolean, val old: Measure<T>, val new: Measure<T>)

    private inner class AnimationImpl<T: Units>(
            private val property: InternalProperty<T>, private val block: (Measure<T>) -> Unit): Animation, CompletableImpl() {
        private lateinit var startTime: Measure<Time>

        private var previousPosition = property.value.position

        val isCanceled get() = state == Canceled

        fun run(currentTime: Measure<Time>): Result<T> {
            if (!::startTime.isInitialized) {
                startTime = currentTime
            }

            val totalElapsedTime = currentTime - startTime
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

            return Result(activeTransition != null, previousPosition, momentValue.position).apply {
                if (new != old || totalElapsedTime == 0 * milliseconds) {
                    block(new)
                }

                if (!active) {
                    completed()
                }

                previousPosition = momentValue.position
            }
        }

        override fun cancel() {
            cancel(broadcast = true)
        }

        fun cancel(broadcast: Boolean = true) {
            if (state != Active) {
                return
            }

            super.cancel()

            if (broadcast) {
                (listeners as? SetPool)?.forEach { it.canceled(this@AnimatorImpl, setOf(this)) }
            }
        }
    }

    private inner class TransitionPairBuilderImpl<T: Number>(
            start     : T,
            end       : T,
            transition: (start: T, end: T) -> Transition<NoneUnit>): TransitionBuilder<T> {

        private val property = InternalProperty(start * noneUnits).apply { add(transition(start, end)) }

        override fun then(transition: Transition<NoneUnit>) = this.also { property.add(transition) }

        override fun invoke(block: (T) -> Unit): Animation = AnimationImpl(property) { block(it.amount as T) }.also { animations += it }
    }

    private inner class TransitionBuilderImpl<T: Number>(
            value     : T,
            transition: (value: T) -> Transition<NoneUnit>): TransitionBuilder<T> {

        private val property = InternalProperty(value * noneUnits).apply { add(transition(value)) }

        override fun then(transition: Transition<NoneUnit>) = this.also { property.add(transition) }

        override fun invoke(block: (T) -> Unit): Animation = AnimationImpl(property) { block(it.amount as T) }.also { animations += it }
    }

    private inner class MeasurePairTransitionBuilderImpl<T: Units>(
            start     : Measure<T>,
            end       : Measure<T>,
            transition: (start: Measure<T>, end: Measure<T>) -> Transition<T>): MeasureTransitionBuilder<T> {
        private val property = InternalProperty(start).apply { add(transition(start, end)) }

        override fun then(transition: Transition<T>) = this.also { property.add(transition) }

        override fun invoke(block: (Measure<T>) -> Unit): Animation {
            return AnimationImpl(property) { block(it) }.also { animations += it }
        }
    }

    private inner class MeasureTransitionBuilderImpl<T: Units>(
            value     : Measure<T>,
            transition: (value: Measure<T>) -> Transition<T>): MeasureTransitionBuilder<T> {
        private val property = InternalProperty(value).apply { add(transition(value)) }

        override fun then(transition: Transition<T>) = this.also { property.add(transition) }

        override fun invoke(block: (Measure<T>) -> Unit): Animation {
            return AnimationImpl(property) { block(it) }.also { animations += it }
        }
    }

    private var task       = null as Task?
    private val animations = ObservableSet<AnimationImpl<*>>().apply {
        changed += { _,_,_ ->
            when {
                isNotEmpty() -> if (task?.completed != false) startAnimation()
                else         -> task?.cancel()
            }
        }
    }

    override fun <T: Number> Pair<T, T>.using(transition: (start: T, end: T) -> Transition<NoneUnit>): TransitionBuilder<T> = TransitionPairBuilderImpl(first, second, transition)

    override fun <T: Number> T.using(transition: (value: T) -> Transition<NoneUnit>): TransitionBuilder<T> = TransitionBuilderImpl(this, transition)

    override fun <T: Units> Pair<Measure<T>, Measure<T>>.using(transition: (start: Measure<T>, end: Measure<T>) -> Transition<T>): MeasureTransitionBuilder<T> = MeasurePairTransitionBuilderImpl(first, second, transition)

    override fun <T: Units> Measure<T>.using(transition: (value: Measure<T>) -> Transition<T>): MeasureTransitionBuilder<T> = MeasureTransitionBuilderImpl(this, transition)

    override fun invoke(block: Animator.() -> Unit): Completable {
        val newAnimations = mutableSetOf<AnimationImpl<*>>()

        val listener: (ObservableSet<AnimationImpl<*>>, Set<AnimationImpl<*>>, Set<AnimationImpl<*>>) -> Unit = { _,_,new ->
            newAnimations += new
        }

        animations.changed += listener

        apply(block)

        animations.changed -= listener

        return object: CompletableImpl() {
            private var numCompleted = 0

            init {
                newAnimations.forEach {
                    it.completed += {
                        if (++numCompleted == newAnimations.size) {
                            completed()
                        }
                    }
                }
            }

            override fun cancel() {
                newAnimations.forEach { it.cancel(broadcast = false) }

                (listeners as? SetPool)?.forEach { it.canceled(this@AnimatorImpl, newAnimations) }

                super.cancel()
            }
        }
    }

    override val listeners: Pool<Listener> = SetPool<Listener>()

    private fun startAnimation() {
        task = animationScheduler.onNextFrame {
            onAnimate()
        }
    }

    private fun onAnimate() {
        val changed   = mutableSetOf<Animation>()
        val completed = mutableSetOf<Animation>()

        val iterator = animations.iterator()

        while (iterator.hasNext()) {
            val it = iterator.next()

            when {
                it.isCanceled -> iterator.remove()
                else          -> {
                    val result = it.run(timer.now).also { result ->
                        if (!result.active) {
                            completed += it
                            iterator.remove()
                        }
                    }

                    if (result.new != result.old) {
                        changed += it
                    }
                }
            }
        }

        if (changed.isNotEmpty()) {
            (listeners as? SetPool)?.forEach { it.changed(this, changed) }
        }

        when {
            animations.isNotEmpty() -> task = animationScheduler.onNextFrame {
                onAnimate()
            }
            completed.isNotEmpty()  -> (listeners as? SetPool)?.forEach { it.completed(this, completed) }
        }
    }
}