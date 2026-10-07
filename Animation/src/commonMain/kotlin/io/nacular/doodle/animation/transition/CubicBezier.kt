package io.nacular.doodle.animation.transition

import kotlin.math.acos
import kotlin.math.cbrt
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/*
 * Port based on https://github.com/gre/bezier-easing
 */

/**
 * Solves x(t) = ((2a * t + 3b) * t + 3c) * t = x for t, with x in (0, 1):
 * u = 1/t is the largest real root of x*u^3 - 3c*u^2 - 3b*u - 2a = 0
 */
private fun tForX(x: Double, a: Double, b: Double, c: Double): Double {
    val j = 1 / max(c, sqrt(x))
    val k = x * j
    val l = k * j
    val s = c * j
    val q = b * l
    val m = s * s + q
    val h = -s * (s * s + 1.5 * q) - a * k * l
    val d = h * h - m * m * m

    val v = when {
        // one real root (Cardano)
        m == 0.0 || d > 1e-12 * h * h -> {
            val u = -cbrt(if (h < 0) h - sqrt(d) else h + sqrt(d))
            (u + m / u).let { if (it.isNaN()) 0.0 else it } // triple root (m = h = 0) gives NaN
        }
        // three real roots, take the largest
        else -> {
            val r = sqrt(m)
            2 * r * cos(acos((-h / (m * r)).coerceIn(-1.0, 1.0)) / 3)
        }
    }

    return min(1.0, k / (v + s))
}

internal fun getCubicBezier(x1: Float, y1: Float, x2: Float, y2: Float): EasingFunction {
    // x(t) = ((2a * t + 3b) * t + 3c) * t, y(t) = ((ay * t + by) * t + cy) * t
    val a  = (3.0 * x1 - 3.0 * x2 + 1) / 2
    val b  = x2 - 2.0 * x1
    val c  = x1.toDouble()
    val ay = 3.0 * y1 - 3.0 * y2 + 1
    val by = 3.0 * (y2 - 2.0 * y1)
    val cy = 3.0 * y1

    return {
        when {
            it <= 0f   -> 0f // values outside (0, 1) saturate to 0 / 1
            it >= 1f   -> 1f
            it.isNaN() -> it
            else       -> tForX(it.toDouble(), a, b, c).let { t -> (((ay * t + by) * t + cy) * t).toFloat() }
        }
    }
}