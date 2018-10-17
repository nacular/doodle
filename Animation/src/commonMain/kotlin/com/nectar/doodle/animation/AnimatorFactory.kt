package com.nectar.doodle.animation

/**
 * Created by Nicholas Eddy on 3/30/18.
 */
interface AnimatorFactory {
    operator fun <P> invoke(): Animator<P>
}