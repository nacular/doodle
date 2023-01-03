package io.nacular.doodle.animation

import io.nacular.doodle.animation.Animator.AnimationBlock
import io.nacular.doodle.animation.Animator.Listener
import io.nacular.doodle.animation.Animator.NumericAnimationInfo
import io.nacular.doodle.scheduler.AnimationScheduler
import io.nacular.doodle.scheduler.Task
import io.nacular.doodle.time.Timer
import io.nacular.doodle.utils.CompletableImpl
import io.nacular.doodle.utils.CompletableImpl.State.Active
import io.nacular.doodle.utils.CompletableImpl.State.Canceled
import io.nacular.doodle.utils.ObservableSet
import io.nacular.doodle.utils.Pool
import io.nacular.doodle.utils.SetPool
import io.nacular.doodle.utils.zeroMillis
import io.nacular.measured.units.Measure
import io.nacular.measured.units.Time

/**
 * Animator implementation that uses a [Timer] and [AnimationScheduler].
 *
 * @param timer used to track elapsed time
 * @param animationScheduler that drives the overall ticking of the animator
 */
public class AnimatorImpl(private val timer: Timer, private val animationScheduler: AnimationScheduler): Animator {

    private class Result<T>(val finished: Boolean, val old: T, val new: T)

    private inner class AnimationData<T>(private val animation: AnimationPlan<T>, private val block: (T) -> Unit): Animation<T>, CompletableImpl() {
        private lateinit var startTime: Measure<Time>
        private var previousValue = animation.value(zeroMillis)

        val isCanceled: Boolean get() = state == Canceled

        fun run(currentTime: Measure<Time>): Result<T> {
            if (!::startTime.isInitialized) {
                startTime = currentTime
            }

            val totalElapsedTime = currentTime - startTime
            val currentValue     = animation.value(totalElapsedTime)

            return Result(animation.finished(totalElapsedTime), previousValue, currentValue).apply {
                if (new != old || totalElapsedTime == zeroMillis) {
                    block(new)
                }

                if (finished) {
                    completed()
                }

                previousValue = currentValue
            }
        }

        override fun cancel() {
            cancel(broadcast = true)
        }

        fun cancel(broadcast: Boolean) {
            if (state != Active) {
                return
            }

            super.cancel()

            if (broadcast) {
                (listeners as? SetPool)?.forEach { it.canceled(this@AnimatorImpl, setOf(this)) }
            }
        }
    }

    private inner class GroupAnimation(private val animations: Set<AnimationData<*>>): Animation<Any>, CompletableImpl() {
        private var numCompleted = 0
        private var numCanceled  = 0
        private var initiatingCancel = false

        init {
            animations.forEach {
                it.completed += {
                    if (++numCompleted + numCanceled == animations.size) {
                        completed()
                    }
                }
                it.canceled += {
                    if (!initiatingCancel && ++numCanceled + numCompleted == animations.size) {
                        cancel()
                    }
                }
            }
        }

        override fun cancel() {
            initiatingCancel = true
            animations.forEach { it.cancel() }

            (listeners as? SetPool)?.forEach { it.canceled(this@AnimatorImpl, animations) }

            super.cancel()
            initiatingCancel = false
        }
    }

    private var task       = null as Task?
    private val animations = ObservableSet<AnimationData<*>>().apply {
        changed += { _,_,_ ->
            when {
                isNotEmpty() -> if (task?.completed != false) startAnimation()
                else         -> task?.cancel()
            }
        }
    }

    private var inAnimation = false
    private var concurrentlyModifiedAnimations: ObservableSet<AnimationData<*>>? = null

    override fun <T> invoke(animation: AnimationPlan<T>, onChanged: (T) -> Unit): Animation<T> = AnimationData(animation) {
        onChanged(it)
    }.also {
        addAnimation(it)
    }

    private class AnimationBlockImpl(private val animator: AnimatorImpl): AnimationBlock {
        private data class NumericAnimationInfoImpl<T, V>(val animationPlan: NumericAnimationPlan<T, V>, val block: (T) -> Unit): NumericAnimationInfo<T, V>()

        override operator fun <T, V> NumericAnimationPlan<T, V>.invoke(definitions: (T) -> Unit) = NumericAnimationInfoImpl(this, definitions)

        override infix fun <T, V> Pair<T, T>.using(animation: NumericAnimationInfo<T, V>): Animation<T> = (animation as NumericAnimationInfoImpl).let {
            animator(this, using = it.animationPlan, it.block)
        }

        override fun <T> start(animation: AnimationPlan<T>, onChanged: (T) -> Unit): Animation<T> {
            return animator(animation, onChanged)
        }
    }

    override fun invoke(definitions: AnimationBlock.() -> Unit): Animation<Any> {
        val newAnimations = mutableSetOf<AnimationData<*>>()

        val listener: (ObservableSet<AnimationData<*>>, Set<AnimationData<*>>, Set<AnimationData<*>>) -> Unit = { _,_,new ->
            newAnimations += new
        }

        val animationBlock = AnimationBlockImpl(this)

        animations.changed += listener

        definitions(animationBlock)

        animations.changed -= listener

        return GroupAnimation(newAnimations)
    }

    override val listeners: Pool<Listener> = SetPool()

    private fun startAnimation() {
        task = animationScheduler.onNextFrame {
            onAnimate()
        }
    }

    private fun onAnimate() {
        val changed   = mutableSetOf<Animation<*>>()
        val completed = mutableSetOf<Animation<*>>()

        val iterator = animations.iterator()
        inAnimation = true

        while (iterator.hasNext()) {
            val it = iterator.next()

            when {
                it.isCanceled -> iterator.remove()
                else          -> {
                    val result = it.run(timer.now).also { result ->
                        if (result.finished) {
                            completed += it
                            iterator.remove()
                        }
                    }

                    if (result.new != result.old) {
                        changed += it
                    }
                }
            }
        }

        inAnimation = false
        concurrentlyModifiedAnimations?.let {
            animations.addAll(it)
            it.clear()
            concurrentlyModifiedAnimations = null
        }

        if (changed.isNotEmpty()) {
            (listeners as? SetPool)?.forEach { it.changed(this, changed) }
        }

        if (animations.isNotEmpty()) task = animationScheduler.onNextFrame {
            onAnimate()
        }

        if (completed.isNotEmpty()) {
            (listeners as? SetPool)?.forEach { it.completed(this, completed) }
//            completed.clear()
        }
    }

    private fun addAnimation(animation: AnimationData<*>) {
        when {
            inAnimation -> {
                if (concurrentlyModifiedAnimations == null) {
                    concurrentlyModifiedAnimations = ObservableSet(animations)
                }
                concurrentlyModifiedAnimations?.apply { this += animation }
            }
            else -> animations += animation
        }
    }
}