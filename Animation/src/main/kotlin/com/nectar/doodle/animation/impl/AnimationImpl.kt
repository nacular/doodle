package com.nectar.doodle.animation.impl

import com.nectar.doodle.animation.AnimatableProperty
import com.nectar.doodle.animation.Animation
import com.nectar.doodle.animation.Listener
import com.nectar.doodle.animation.Moment
import com.nectar.doodle.animation.PropertyTransitions
import com.nectar.doodle.animation.transition.Transition
import com.nectar.doodle.scheduler.AnimationScheduler
import com.nectar.doodle.scheduler.Scheduler
import com.nectar.doodle.scheduler.Task
import com.nectar.doodle.time.Timer
import com.nectar.doodle.units.Measure
import com.nectar.doodle.units.Time
import com.nectar.doodle.units.milliseconds


/**
 * Created by Nicholas Eddy on 3/29/18.
 */

private class PropertyTransitionsImpl(private val property: InternalProperty): PropertyTransitions {
    override infix fun then(transition: Transition): PropertyTransitions {
        property.add(transition)
        return this
    }
}

class AnimationImpl(
        private val timer             : Timer,
        private val scheduler         : Scheduler,
        private val animationScheduler: AnimationScheduler): Animation {

    private var task        = null as Task?
    private var startTime   = 0.milliseconds
    private val transitions = mutableMapOf<AnimatableProperty, InternalProperty>()
    private val listeners   = mutableSetOf<Listener>()

    override infix fun of(property: AnimatableProperty): PropertyTransitions = PropertyTransitionsImpl(transitions.getOrPut(property) { InternalProperty(property) })

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

    override fun plusAssign (listener: Listener) = listeners.add   (listener).let { Unit }
    override fun minusAssign(listener: Listener) = listeners.remove(listener).let { Unit }

    private fun onAnimate() {
        val events           = mutableMapOf<AnimatableProperty, Listener.ChangeEvent>()
        val totalElapsedTime =  timer.now - startTime

        var activeTransition: TransitionNode? = null

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

private class InternalProperty(val property: AnimatableProperty) {

    val transitions      = mutableListOf<TransitionNode>()
    var activeTransition = null as TransitionNode?
        private set

    var value = Moment(property.initialValue, 0.0)

    fun add(transition: Transition) {
        val start = if (transitions.isEmpty()) KeyFrame() else transitions.last().endTime
        val end   = KeyFrame()

        add(transition, start, end)
    }

    fun add(transition: Transition, start: KeyFrame, end: KeyFrame) {
        transitions += TransitionNode(transition, start, end)
    }

    fun nextTransition(initialValue: Moment, elapsedTime: Measure<Time>): TransitionNode? = transitions.firstOrNull()?.let {
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

private class TransitionNode(val transition: Transition, val startTime: KeyFrame, var endTime: KeyFrame) {

    fun shouldStart(elapsedTime: Measure<Time>) = startTime.time <= elapsedTime

    fun calculateEndTime(initialState: Moment) {
        endTime.time = startTime.time + transition.duration(initialState)
    }
}