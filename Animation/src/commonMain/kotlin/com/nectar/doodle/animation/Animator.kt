package com.nectar.doodle.animation

import com.nectar.doodle.animation.transition.Transition
import com.nectar.measured.units.Measure
import com.nectar.measured.units.Time
import com.nectar.measured.units.Unit
import com.nectar.measured.units.milliseconds
import com.nectar.measured.units.times

/**
 * Created by Nicholas Eddy on 3/29/18.
 */

interface Listener<P> {
    open class ChangeEvent<P, T: Unit>(val property: P, val old: Measure<T>, val new: Measure<T>)

    fun cancelled(animator: Animator<P>) {}
    fun completed(animator: Animator<P>) {}

    fun changed(animator: Animator<P>, properties: Map<P, ChangeEvent<P, *>>) {}
}

interface InitialPropertyTransition<T: Unit> {
    infix fun using(transition: Transition<T>): PropertyTransitions<T>
}

interface PropertyTransitions<T: Unit> {
    infix fun then(transition: Transition<T>): PropertyTransitions<T>
}

interface Animator<P> {
    operator fun <T: Unit> invoke(property: P, initialValue: Measure<T>): InitialPropertyTransition<T>

    val running: Boolean

    fun schedule(after: Measure<Time> = 0 * milliseconds)
    fun cancel  ()

    operator fun plusAssign (listener: Listener<P>)
    operator fun minusAssign(listener: Listener<P>)
}

/*
    val widthAnimation = animation from circle.width * pixels using
                FixedSpeedLinear(100 * (pixels / seconds), circle.width * 2 * pixels) then
                Cubic           (2 * seconds, circle.width * pixels, 0 * (pixels / seconds))
 */