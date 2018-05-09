package com.nectar.doodle.animation

import com.nectar.doodle.animation.transition.Transition
import com.nectar.doodle.units.Length
import com.nectar.doodle.units.Measure
import com.nectar.doodle.units.Time
import com.nectar.doodle.units.milliseconds

/**
 * Created by Nicholas Eddy on 3/29/18.
 */

interface Listener {
    class ChangeEvent(val property: AnimatableProperty, val old: Measure<Length>, val new: Measure<Length>)

    fun cancelled(animator: Animator) {}
    fun completed(animator: Animator) {}

    fun changed(animator: Animator, properties: Map<AnimatableProperty, ChangeEvent>) {}
}

interface InitialPropertyTransition {
    infix fun using(transition: Transition): PropertyTransitions
}

interface PropertyTransitions {
    infix fun then(transition: Transition): PropertyTransitions
}

interface Animator {
    operator fun invoke(property: AnimatableProperty): InitialPropertyTransition

    fun schedule(after: Measure<Time> = 0.milliseconds)
    fun cancel  ()

    operator fun plusAssign (listener: Listener)
    operator fun minusAssign(listener: Listener)
}