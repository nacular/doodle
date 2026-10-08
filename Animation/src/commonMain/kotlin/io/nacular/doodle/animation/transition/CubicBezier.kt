package io.nacular.doodle.animation.transition

import kotlin.math.abs

/*
 * Port based on https://github.com/gre/bezier-easing
 */

/** Coefficients based on Homer's method */
private fun coefficient1(a1: Double, a2: Double) = 1 - 3 * a2 + 3 * a1
private fun coefficient2(a1: Double, a2: Double) =     3 * a2 - 6 * a1
private fun coefficient3(a1: Double           ) =              3 * a1

/** @return dv/t based on Homer's method*/
private fun slope(t: Double, v1: Double, v2: Double) = 3.0 * coefficient1(v1, v2) * t * t + 2.0 * coefficient2(v1, v2) * t + coefficient3(v1)

private fun binarySubdivide(x: Double, a: Double, b: Double, x1: Double, x2: Double): Double {
    var currentAA = a
    var currentAB = b
    var currentX  : Double
    var currentT  : Double
    var i         = 0

    do {
        currentT = currentAA + (currentAB - currentAA) / 2.0
        currentX = calcBezier(currentT, x1, x2) - x
        when {
            currentX > 0.0 -> currentAB = currentT
            else           -> currentAA = currentT
        }
    } while (abs(currentX) > SUBDIVISION_PRECISION && ++i < SUBDIVISION_MAX_ITERATIONS)

    return currentT
}

/** @return t, or -1 when Newton-Raphson has not converged (e.g. on steep curves such as (1, 0, 0, 1) around 0.5) */
private fun newtonRaphsonIterate(x: Double, tGuess: Double, x1: Double, x2: Double): Double {
    var result = tGuess
    var step   = 0.0

    repeat(NEWTON_ITERATIONS) {
        when (val slope = slope(result, x1, x2)) {
            0.0  -> return result
            else -> { step = (calcBezier(result, x1, x2) - x) / slope; result -= step }
        }
    }

    return if (abs(step) <= NEWTON_CONVERGENCE) result else -1.0
}

private fun tForX(x1: Double, x2: Double, x: Double, sampleValues: DoubleArray): Double {
    var intervalStart = 0.0
    var currentSample = 1
    val lastSample    = SPLINE_TABLE_SIZE - 1

    while (currentSample != lastSample && sampleValues[currentSample] <= x) {
        intervalStart += SAMPLE_STEP_SIZE
        ++currentSample
    }
    --currentSample

    // Interpolate to provide an initial guess for t
    val dist   = (x - sampleValues[currentSample]) / (sampleValues[currentSample + 1] - sampleValues[currentSample])
    val tGuess = intervalStart + dist * SAMPLE_STEP_SIZE

    val initialSlope = slope(tGuess, x1, x2)

    return when {
        initialSlope >= NEWTON_MIN_SLOPE -> newtonRaphsonIterate(x, tGuess, x1, x2).takeIf { it >= intervalStart && it <= intervalStart + SAMPLE_STEP_SIZE }
                                            ?: binarySubdivide(x, intervalStart, intervalStart + SAMPLE_STEP_SIZE, x1, x2)
        initialSlope == 0.0              -> tGuess
        else                             -> binarySubdivide(x, intervalStart, intervalStart + SAMPLE_STEP_SIZE, x1, x2)
    }
}

/** @return v(t) based on Homer's method*/
private fun calcBezier(t: Double, v1: Double, v2: Double) = ((coefficient1(v1, v2) * t + coefficient2(v1, v2)) * t + coefficient3(v1)) * t

internal fun getCubicBezier(x1: Float, y1: Float, x2: Float, y2: Float): EasingFunction {
    // Precompute samples table
    // computed in Double: in Float, x(t) is too flat around vertical tangents to find t precisely
    val dx1 = x1.toDouble()
    val dy1 = y1.toDouble()
    val dx2 = x2.toDouble()
    val dy2 = y2.toDouble()
    val sampleValues = DoubleArray(SPLINE_TABLE_SIZE) { calcBezier(it * SAMPLE_STEP_SIZE, dx1, dx2) }

    return {
        when {
            it <= 0f   -> 0f // values outside (0, 1) saturate to 0 / 1
            it >= 1f   -> 1f
            it.isNaN() -> it
            else       -> calcBezier(tForX(dx1, dx2, it.toDouble(), sampleValues), dy1, dy2).toFloat()
        }
    }
}

internal const val NEWTON_MIN_SLOPE           = 0.001
internal const val NEWTON_ITERATIONS          = 4
internal const val SUBDIVISION_PRECISION      = 1e-14
internal const val SUBDIVISION_MAX_ITERATIONS = 60
internal const val NEWTON_CONVERGENCE         = 0.000001
internal const val SPLINE_TABLE_SIZE          = 11
internal const val SAMPLE_STEP_SIZE           = 1.0 / (SPLINE_TABLE_SIZE - 1)