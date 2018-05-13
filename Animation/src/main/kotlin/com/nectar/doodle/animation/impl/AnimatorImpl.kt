package com.nectar.doodle.animation.impl

import com.nectar.doodle.animation.Animator
import com.nectar.doodle.animation.InitialPropertyTransition
import com.nectar.doodle.animation.Listener
import com.nectar.doodle.animation.Moment
import com.nectar.doodle.animation.PropertyTransitions
import com.nectar.doodle.animation.transition.Transition
import com.nectar.doodle.scheduler.AnimationScheduler
import com.nectar.doodle.scheduler.Scheduler
import com.nectar.doodle.scheduler.Task
import com.nectar.doodle.time.Timer
import com.nectar.doodle.units.Measure
import com.nectar.doodle.units.MeasureRatio
import com.nectar.doodle.units.Time
import com.nectar.doodle.units.milliseconds


/**
 * Created by Nicholas Eddy on 3/29/18.
 */

private class InitialPropertyTransitionImpl<T>(private val property: InternalProperty<*, T>): InitialPropertyTransition<T> {
    override infix fun using(transition: Transition<T>): PropertyTransitions<T> {
        property.add(transition)

        return PropertyTransitionsImpl(property)
    }
}

private class PropertyTransitionsImpl<T>(private val property: InternalProperty<*, T>): PropertyTransitions<T> {
    override infix fun then(transition: Transition<T>): PropertyTransitions<T> {
        property.add(transition)
        return this
    }
}

class AnimatorImpl<P>(
        private val timer             : Timer,
        private val scheduler         : Scheduler,
        private val animationScheduler: AnimationScheduler): Animator<P> {

    private var task        = null as Task?
    private var startTime   = 0.milliseconds
    private val transitions = mutableMapOf<P, InternalProperty<P, Any>>()
    private val listeners   = mutableSetOf<Listener<P>>()

    override fun <T> invoke(property: P, initialValue: Measure<T>): InitialPropertyTransition<T> = InitialPropertyTransitionImpl(transitions.getOrPut(property) { InternalProperty(property, initialValue) as InternalProperty<P, Any> } as InternalProperty<P, T>)

    override fun schedule(after: Measure<Time>) {
        if (task?.completed == false) {
            return
        }

        task = scheduler.after(after) {
            task = animationScheduler.onNextFrame {
                startTime = it
                onAnimate()
            }
        }
    }

    override fun cancel() {
        if (task?.completed == false) {
            task?.cancel()

            listeners.forEach { it.cancelled(this) }
        }
    }

    override fun plusAssign (listener: Listener<P>) = listeners.add   (listener).let { Unit }
    override fun minusAssign(listener: Listener<P>) = listeners.remove(listener).let { Unit }

    private fun onAnimate() {
        val events           = mutableMapOf<P, Listener.ChangeEvent<P, Any>>()
        val totalElapsedTime =  timer.now - startTime

        var activeTransition: TransitionNode<Any>? = null

        for (property in transitions.values) {
            var momentValue  = property.value
            activeTransition = property.activeTransition

            // Read new active Transition
            if (activeTransition == null) {
                activeTransition = property.nextTransition(momentValue, totalElapsedTime)
            }

            // Skip over out-dated Transitions, making sure to take their end-state into account
            while (activeTransition != null && activeTransition.endTime.time < totalElapsedTime) {
                momentValue      = activeTransition.transition.endState(momentValue)
                activeTransition = property.nextTransition(momentValue, totalElapsedTime)
                property.value   = momentValue
            }

            if (activeTransition != null) {
                momentValue = activeTransition.transition.value(momentValue, totalElapsedTime - activeTransition.startTime.time)
            }

            events[property.property] = Listener.ChangeEvent(property.property, property.value.position, momentValue.position)
        }

        if (events.isNotEmpty()) {
            listeners.forEach {
                it.changed(this, events)
            }
        }

        if (activeTransition != null) {
            task = animationScheduler.onNextFrame {
                onAnimate()
            }
        } else {
            listeners.forEach { it.completed(this) }
        }
    }
}

private class KeyFrame {
    var time: Measure<Time> = 0.milliseconds
}

private class InternalProperty<P, T>(val property: P, initialValue: Measure<T>) {

    val transitions      = mutableListOf<TransitionNode<T>>()
    var activeTransition = null as TransitionNode<T>?
        private set

    var value = Moment(initialValue, MeasureRatio.zero())

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

private class TransitionNode<T>(val transition: Transition<T>, val startTime: KeyFrame, var endTime: KeyFrame) {

    fun shouldStart(elapsedTime: Measure<Time>) = startTime.time <= elapsedTime

    fun calculateEndTime(initialState: Moment<T>) {
        endTime.time = startTime.time + transition.duration(initialState)
    }
}