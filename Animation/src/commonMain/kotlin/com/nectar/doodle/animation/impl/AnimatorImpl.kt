package com.nectar.doodle.animation.impl

import com.nectar.doodle.animation.Animator
import com.nectar.doodle.animation.InitialPropertyTransition
import com.nectar.doodle.animation.InterpolationStart
import com.nectar.doodle.animation.Listener
import com.nectar.doodle.animation.Listener.ChangeEvent
import com.nectar.doodle.animation.MeasureInterpolationStart
import com.nectar.doodle.animation.Moment
import com.nectar.doodle.animation.NoneUnit
import com.nectar.doodle.animation.PropertyTransitions
import com.nectar.doodle.animation.TransitionBuilder
import com.nectar.doodle.animation.noneUnits
import com.nectar.doodle.animation.transition.Transition
import com.nectar.doodle.scheduler.AnimationScheduler
import com.nectar.doodle.scheduler.Scheduler
import com.nectar.doodle.scheduler.Task
import com.nectar.doodle.time.Timer
import com.nectar.doodle.utils.Cancelable
import com.nectar.measured.units.Measure
import com.nectar.measured.units.Time
import com.nectar.measured.units.div
import com.nectar.measured.units.milliseconds
import com.nectar.measured.units.times


/**
 * Created by Nicholas Eddy on 3/29/18.
 */

private class InitialPropertyTransitionImpl<T: com.nectar.measured.units.Unit>(private val property: InternalProperty<*, T>): InitialPropertyTransition<T> {
    override infix fun using(transition: Transition<T>): PropertyTransitions<T> {
        property.add(transition)

        return PropertyTransitionsImpl(property)
    }
}

private class PropertyTransitionsImpl<T: com.nectar.measured.units.Unit>(private val property: InternalProperty<*, T>): PropertyTransitions<T> {
    override infix fun then(transition: Transition<T>): PropertyTransitions<T> {
        property.add(transition)
        return this
    }
}

private class Result<P, T: com.nectar.measured.units.Unit>(val active: Boolean, property: P, old: Measure<T>, new: Measure<T>): ChangeEvent<P, T>(property, old, new)

private class PropertyDriver<P, T: com.nectar.measured.units.Unit>(private val property: InternalProperty<P, T>) {
    fun drive(totalElapsedTime: Measure<Time>): Result<P, T> {
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

        return Result(activeTransition != null, property.property, oldPosition, momentValue.position)
    }
}

private class TransitionBuilderImpl<T: Number>(
        private val timer             : Timer,
        private val animationScheduler: AnimationScheduler,
        start     : T,
        end       : T,
        transition: (start: T, end: T) -> Transition<NoneUnit>): TransitionBuilder<T> {

    private class CancelableWrapper(var cancelable: Cancelable): Cancelable {
        override fun cancel() { cancelable.cancel() }
    }

    private val property       = InternalProperty("", start * noneUnits).apply { add(transition(start, end)) }
    private val propertyDriver = PropertyDriver(property)
    private var startTime      = 0 * milliseconds
    private lateinit var block: (T) -> Unit
    private lateinit var task: CancelableWrapper


    override fun then(transition: Transition<NoneUnit>) = this.also { property.add(transition) }

    override fun invoke(block: (T) -> Unit): Cancelable {
        startTime = timer.now

        this.block = block

        task = CancelableWrapper(animationScheduler.onNextFrame {
            onAnimate()
        })

        return task
    }

    private fun onAnimate() {
        val totalElapsedTime = timer.now - startTime
        var activeTransition = false

        val result = propertyDriver.drive(totalElapsedTime).also { activeTransition = activeTransition || it.active }

        if (result.new != result.old) {
            block((result.new `in` noneUnits) as T)
        }

        if (activeTransition) {
            task.cancelable = animationScheduler.onNextFrame {
                onAnimate()
            }
        }
    }
}

private class InterpolationStartImpl<T: Number>(private val timer: Timer, private val animationScheduler: AnimationScheduler, private val start: T, private val end: T): InterpolationStart<T> {
    override fun using(transition: (start: T, end: T) -> Transition<NoneUnit>) = TransitionBuilderImpl(timer, animationScheduler, start, end, transition)
}

class AnimatorImpl<P>(
        private val timer             : Timer,
        private val scheduler         : Scheduler,
        private val animationScheduler: AnimationScheduler): Animator<P>
{
    override fun <T: Number> invoke(pair: Pair<T, T>): InterpolationStart<T> = InterpolationStartImpl(timer, animationScheduler, pair.first, pair.second)

    override fun <T: com.nectar.measured.units.Unit> invoke(pair: Pair<Measure<T>, Measure<T>>): MeasureInterpolationStart<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private var task      = null as Task?
    private val drivers   = mutableMapOf<P, PropertyDriver<P, *>>()
    private var startTime = 0 * milliseconds
    private val listeners = mutableSetOf<Listener<P>>()

    override fun <T: com.nectar.measured.units.Unit> invoke(property: P, initialValue: Measure<T>): InitialPropertyTransition<T> {
        val internalProperty = InternalProperty(property, initialValue)

        drivers.getOrPut(property) { PropertyDriver(internalProperty) }

        return InitialPropertyTransitionImpl(internalProperty)
    }

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

    override val running get() = task?.completed == false

    override fun cancel() {
        if (running) {
            task?.cancel()

            listeners.forEach { it.cancelled(this) }
        }
    }

    override fun plusAssign (listener: Listener<P>) = listeners.add   (listener).let { Unit }
    override fun minusAssign(listener: Listener<P>) = listeners.remove(listener).let { Unit }

    private fun onAnimate() {
        val events           = mutableMapOf<P, ChangeEvent<P, *>>()
        val totalElapsedTime = timer.now - startTime
        var activeTransition = false

        drivers.forEach { (property, driver) ->
            val result = driver.drive(totalElapsedTime).also { activeTransition = activeTransition || it.active }

            if (result.new != result.old) {
                events[property] = result
            }
        }

        if (events.isNotEmpty()) {
            listeners.forEach {
                it.changed(this, events)
            }
        }

        if (activeTransition) {
            task = animationScheduler.onNextFrame {
                onAnimate()
            }
        } else {
            listeners.forEach { it.completed(this) }
        }
    }
}

private class KeyFrame {
    var time: Measure<Time> = 0 * milliseconds
}

private class InternalProperty<P, T: com.nectar.measured.units.Unit>(val property: P, initialValue: Measure<T>) {

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