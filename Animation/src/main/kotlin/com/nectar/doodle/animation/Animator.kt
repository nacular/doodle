package com.nectar.doodle.animation

import com.nectar.doodle.animation.transition.Transition
import com.nectar.doodle.units.Measure
import com.nectar.doodle.units.Time
import com.nectar.doodle.units.milliseconds

/**
 * Created by Nicholas Eddy on 3/29/18.
 */

interface Listener<P> {
    class ChangeEvent<P, T>(val property: P, val old: Measure<T>, val new: Measure<T>)

    fun cancelled(animator: Animator<P>) {}
    fun completed(animator: Animator<P>) {}

    fun changed(animator: Animator<P>, properties: Map<P, ChangeEvent<P, Any>>) {}
}

interface InitialPropertyTransition<T> {
    infix fun using(transition: Transition<T>): PropertyTransitions<T>
}

interface PropertyTransitions<T> {
    infix fun then(transition: Transition<T>): PropertyTransitions<T>
}

interface Animator<P> {
    operator fun <T> invoke(property: P, initialValue: Measure<T>): InitialPropertyTransition<T>

    fun schedule(after: Measure<Time> = 0.milliseconds)
    fun cancel  ()

    operator fun plusAssign (listener: Listener<P>)
    operator fun minusAssign(listener: Listener<P>)
}