package io.nacular.doodle.animation.transition

import io.nacular.doodle.animation.Moment
import io.nacular.doodle.animation.Velocity
import io.nacular.measured.units.Measure
import io.nacular.measured.units.Time
import io.nacular.measured.units.Units
import io.nacular.measured.units.div
import io.nacular.measured.units.times
import kotlin.math.pow

/**
 * Created by Nicholas Eddy on 10/29/18.
 */
public class Cubic<T: Units>(private val duration: Measure<Time>, private val endValue: Measure<T>, private val endVelocity: Measure<Velocity<T>>): Transition<T> {

    private val deltaTime = duration * 0.1

    override fun value(initial: Moment<T>, timeOffset: Measure<Time>): Moment<T> {

        val t      = timeOffset / duration
        val p0     = initial.position
        val p1     = p0 + deltaTime * initial.velocity
        val p2     = endValue - deltaTime * endVelocity
        val p3     = endValue
        val points = listOf(p0, p1, p2, p3)
        var result = 0 * points[0]
        val n      = points.size - 1

        points.forEachIndexed { i, point ->
            val bin = (n choose i) * (1-t).pow(n-i) * t.pow(i)

            result += bin * point
        }

        return Moment(result, endVelocity) // FIXME: Calculate instantaneous velocity
    }

    override fun duration(initial: Moment<T>): Measure<Time> = duration

    override fun endState(initial: Moment<T>): Moment<T> = Moment(endValue, endVelocity)

    // TODO: Is there a faster way?
    private infix fun Int.choose(value: Int): Int {
        var coefficient = 1

        for (x in this - value + 1..this ) { coefficient *= x }
        for (x in                1..value) { coefficient /= x }

        return coefficient
    }

//    fun fromX(x: Double, x1: T, x2: T, y1: T, y2: T): T {
////        if (mX1 == mY1 && mX2 == mY2) return aX // linear
//        return calcBezier(tForX(x, x1, x2), y1, y2)
//    }
//
//    fun A(a1: T, a2: T) = 1 - 3 * a2 + 3 * a1
//    fun B(a1: T, a2: T) = 3 * a2 - 6 * a1
//    fun C(a1: T       ) = 3 * a1
//
//    // Returns x(t) given t, x1, and x2, or y(t) given t, y1, and y2.
//    fun calcBezier(t: Double, a1: T, a2: T) = ((A(a1, a2)*t + B(a1, a2))*t + C(a1))*t
//
//    // Returns dx/dt given t, x1, and x2, or dy/dt given t, y1, and y2.
//    fun slope(t: Double, a1: T, a2: T) = 3 * A(a1, a2) * t * t + 2 * B(a1, a2) * t + C(a1)
//
//    fun tForX(x: Double, x1: T, x2: T): Double {
//        // Newton raphson iteration
//        var guessT = x
//
//        for (i in 0..4) {
//            val currentSlope = slope(guessT, x1, x2)
//            if (currentSlope == 0.0) return guessT
//            val currentX = calcBezier(guessT, x1, x2) - x
//            guessT -= currentX / currentSlope
//        }
//
//        return guessT
//    }
}