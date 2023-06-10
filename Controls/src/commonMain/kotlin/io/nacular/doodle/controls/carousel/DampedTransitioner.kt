package io.nacular.doodle.controls.carousel

import io.nacular.doodle.controls.carousel.Carousel.ItemMarkers
import io.nacular.doodle.controls.carousel.CarouselBehavior.Transitioner
import io.nacular.doodle.geometry.Vector2D
import io.nacular.doodle.geometry.times
import io.nacular.doodle.scheduler.AnimationScheduler
import io.nacular.doodle.scheduler.Task
import io.nacular.doodle.time.Timer
import io.nacular.doodle.utils.Completable
import io.nacular.doodle.utils.CompletableImpl
import io.nacular.doodle.utils.Pausable
import io.nacular.doodle.utils.autoCanceling
import io.nacular.doodle.utils.observable
import io.nacular.doodle.utils.zeroMillis
import io.nacular.measured.units.InverseUnits
import io.nacular.measured.units.Measure
import io.nacular.measured.units.Square
import io.nacular.measured.units.Time
import io.nacular.measured.units.Time.Companion.milliseconds
import io.nacular.measured.units.Time.Companion.seconds
import io.nacular.measured.units.div
import io.nacular.measured.units.times
import kotlin.math.E
import kotlin.math.abs
import kotlin.math.pow


/**
 * Carousel [Transitioner] that provides smooth, physics-based deceleration at the end of a Carousel's
 * manual movement.
 *
 * @param timer used to track current time
 * @param scheduler used to manage animation
 * @param decelerationTime indicating how quickly stops should decelerate to 0 velocity
 * @param dampLevel indicating how much critical damping should be applied during stopping
 *
 * @see Carousel.completeManualMove
 */
public open class DampedTransitioner<T>(
    private val timer           : Timer,
    private val scheduler       : AnimationScheduler,
    private val decelerationTime: Measure<Time>               =  1 * seconds,
    private val dampLevel       : Measure<InverseUnits<Time>> = 10 / seconds
): Transitioner<T> {

    private inner class AnimationTask(var task: Task): CompletableImpl() {
        public override fun completed() = super.completed()

        override fun cancel() {
            task.cancel()
            super.cancel()
        }
    }

    private class Velocity2D(
        val x: Measure<InverseUnits<Time>> = 0 / milliseconds,
        val y: Measure<InverseUnits<Time>> = 0 / milliseconds
    ) {
        operator fun div       (time : Measure<Time>) = Acceleration2D( x / time,     y / time   )
        operator fun plus      (other: Velocity2D   ) = Velocity2D    ( x + other.x,  y + other.y)
        operator fun times     (time : Measure<Time>) = Vector2D      ( x * time,     y * time   )
        operator fun unaryMinus(                    ) = Velocity2D    (-x,           -y          )
    }

    private class Acceleration2D(
        val x: Measure<InverseUnits<Square<Time>>> = 0 / (milliseconds * milliseconds),
        val y: Measure<InverseUnits<Square<Time>>> = 0 / (milliseconds * milliseconds)
    ) {
        operator fun times(time       : Measure<Time>        ) = Velocity2D(x * time, y * time)
        operator fun times(timeSquared: Measure<Square<Time>>) = Vector2D(x * timeSquared, y * timeSquared)
    }

    private lateinit var endTime              : Measure<Time>
    private          var lastTime             = zeroMillis
    private          var velocity             = Velocity2D()
    private          var endChosen            = false
    private          var animation            : AnimationTask? by autoCanceling()
    private          var lastPosition         = Vector2D()
    private lateinit var acceleration         : Acceleration2D
    private          var predictedEndPosition = Vector2D()
    private          val stopThreshold        = 0.01

    private val constantAccelerationMotion = { initialPosition: Vector2D, time: Measure<Time> ->
        initialPosition + velocity * time + 0.5 * (acceleration * time * time)
    }

    /**
     * https://www.ryanjuckett.com/damped-springs/
     */
    private val criticallyDampedMotion = { initialPosition: Vector2D, totalElapsedTime: Measure<Time> ->
        // x(t) = ((v0 + x0 w) t + x_0) e^(-wt)

        val eW = E.pow(-dampLevel * totalElapsedTime)

        (initialPosition + velocity * totalElapsedTime + initialPosition * (dampLevel * totalElapsedTime)) * eW
    }

    private val endAfterTime = { _: Vector2D, elapsedTime: Measure<Time> -> elapsedTime > endTime }

    private var endAnimation   = endAfterTime
    private var motionFunction = constantAccelerationMotion

    private var markers: ItemMarkers? by observable(null) { _, new ->
        new?.let { checkForEnd(it) }
    }

    override fun moveStarted(carousel: Carousel<T, *>, position: Vector2D) {
        lastTime     = timer.now
        velocity     = Velocity2D()
        endChosen    = false
        lastPosition = position
    }

    override fun moveUpdated(carousel: Carousel<T, *>, position: Vector2D) {
        val currentTime = timer.now
        val deltaTime   = currentTime - lastTime

        if (deltaTime > zeroMillis) {
            velocity     = (position - lastPosition).run { Velocity2D(x / deltaTime, y / deltaTime) }
            lastTime     = currentTime
            lastPosition = position
        }
    }

    override fun moveEnded(
        carousel       : Carousel<T, *>,
        position       : Vector2D,
        markers        : ItemMarkers,
        decelerateWhile: (position: Vector2D) -> ItemMarkers
    ): Completable {
        endTime      = decelerationTime
        acceleration = -velocity / endTime
        lastPosition = position
        this.markers = markers

        return animateStop(position, decelerateWhile)
    }

    private fun checkForEnd(markers: ItemMarkers) {
        if (endChosen) return

        val endTime2 = endTime * endTime

        predictedEndPosition = lastPosition + velocity * endTime + 0.5 * (acceleration * endTime2)

        val distanceBetweenPrevNext = (markers.next - markers.previous).magnitude()
        val distanceAlongDirection  = abs((predictedEndPosition - markers.previous) * (markers.next - markers.previous).normalize())
        var positionAtDamp          = predictedEndPosition
        var timeAtDamp              = zeroMillis
        val updateDampedStartState  = {
            timeAtDamp     = timer.now - lastTime
            positionAtDamp = lastPosition + velocity * timeAtDamp + 0.5 * (acceleration * (timeAtDamp * timeAtDamp))

            velocity += acceleration * timeAtDamp
        }

        if (distanceAlongDirection < distanceBetweenPrevNext / 2) {
            updateDampedStartState()

            motionFunction = { _, time ->
                criticallyDampedMotion(positionAtDamp - markers.previous, time - timeAtDamp).run {
                    Vector2D(markers.previous.x + x, markers.previous.y + y)
                }
            }
            endAnimation = { newPos, _ ->
                abs((newPos - markers.previous) * (markers.next - markers.previous).normalize()) < stopThreshold
            }

            endChosen = true
        } else if (distanceAlongDirection < distanceBetweenPrevNext) {
            updateDampedStartState()

            velocity = -velocity

            motionFunction = { _, time ->
                criticallyDampedMotion(markers.next - positionAtDamp, time - timeAtDamp).run {
                    Vector2D(markers.next.x - x, markers.next.y - y)
                }
            }
            endAnimation = { newPos, _ ->
                abs((markers.next - newPos) * (markers.next - markers.previous).normalize()) < stopThreshold
            }

            endChosen = true
        } else {
            motionFunction = constantAccelerationMotion
            endAnimation = endAfterTime
        }
    }

    private fun animateStop(
        position      : Vector2D,
        decelerateWhile: (position: Vector2D) -> ItemMarkers
    ): Completable = scheduler.onNextFrame {
        val now         = timer.now
        val elapsedTime = now - lastTime
        val newPos      = motionFunction(position, elapsedTime)
        markers         = decelerateWhile(newPos)

        when {
            endAnimation(newPos, elapsedTime) -> {
                animation?.completed()
                endChosen = false
                markers   = null
            }
            else                              -> animateStop(position, decelerateWhile)
        }
    }.let { task ->
        when (val a = animation) {
            null -> AnimationTask(task).also {
                animation     = it
                it.canceled  += { animation = null }
                it.completed += { animation = null }
            }
            else -> a.also { it.task = task }
        }
    }
}

/**
 * Creates a [DampedTransitioner] that calls [transition] when it needs to skip between
 * frames.
 */
public fun <T> dampedTransitioner(
    timer           : Timer,
    scheduler       : AnimationScheduler,
    decelerationTime: Measure<Time>               =  1 * seconds,
    dampLevel       : Measure<InverseUnits<Time>> = 10 / seconds,
    transition      : (
        carousel : Carousel<T, *>,
        startItem: Int,
        endItem  : Int,
        update   : (progress: Float) -> Unit
    ) -> Pausable
): DampedTransitioner<T> = object: DampedTransitioner<T>(timer, scheduler, decelerationTime, dampLevel) {
    override fun transition(
        carousel : Carousel<T, *>,
        startItem: Int,
        endItem  : Int,
        update   : (progress: Float) -> Unit
    ): Pausable = transition(carousel, startItem, endItem, update)
}