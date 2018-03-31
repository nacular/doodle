package com.nectar.doodle.animation

import com.nectar.doodle.animation.transition.Transition
import com.nectar.doodle.units.Measure
import com.nectar.doodle.units.Time
import com.nectar.doodle.units.milliseconds

/**
 * Created by Nicholas Eddy on 3/29/18.
 */

interface Listener {
    class ChangeEvent(val property: AnimatableProperty, val old: Double, val new: Double)

    fun cancelled(animation: Animation) {}
    fun completed(animation: Animation) {}

    fun changed(animation: Animation, properties: Map<AnimatableProperty, ChangeEvent>) {}
}

interface Animation {
    fun addTransition(property: AnimatableProperty, transition: Transition)

    fun schedule(delay: Measure<Time> = 0.milliseconds)
    fun cancel  ()

    operator fun plusAssign (listener: Listener)
    operator fun minusAssign(listener: Listener)
}