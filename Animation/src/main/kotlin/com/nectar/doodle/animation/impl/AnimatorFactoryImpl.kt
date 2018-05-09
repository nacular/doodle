package com.nectar.doodle.animation.impl

import com.nectar.doodle.animation.AnimatorFactory
import com.nectar.doodle.scheduler.AnimationScheduler
import com.nectar.doodle.scheduler.Scheduler
import com.nectar.doodle.time.Timer

/**
 * Created by Nicholas Eddy on 3/30/18.
 */
class AnimatorFactoryImpl(
        private val timer             : Timer,
        private val scheduler         : Scheduler,
        private val animationScheduler: AnimationScheduler): AnimatorFactory {

    override fun invoke() = AnimatorImpl(timer, scheduler, animationScheduler)
}