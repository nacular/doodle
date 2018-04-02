package com.nectar.doodle.animation

import com.nectar.doodle.animation.transition.Transition
import com.nectar.doodle.units.Distance
import com.nectar.doodle.units.Measure
import com.nectar.doodle.units.Time
import com.nectar.doodle.units.milliseconds

/**
 * Created by Nicholas Eddy on 3/29/18.
 */

interface Listener {
    class ChangeEvent(val property: AnimatableProperty, val old: Measure<Distance>, val new: Measure<Distance>)

    fun cancelled(animation: Animation) {}
    fun completed(animation: Animation) {}

    fun changed(animation: Animation, properties: Map<AnimatableProperty, ChangeEvent>) {}
}

interface PropertyTransitions {
    infix fun then(transition: Transition): PropertyTransitions
}

interface Animation {
    infix fun of(property: AnimatableProperty): PropertyTransitions

    fun schedule(after: Measure<Time> = 0.milliseconds)
    fun cancel  ()

    operator fun plusAssign (listener: Listener)
    operator fun minusAssign(listener: Listener)
}