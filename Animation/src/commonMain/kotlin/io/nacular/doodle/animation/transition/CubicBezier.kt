package io.nacular.doodle.animation.transition

import kotlin.math.abs

/*
 * Port based on https://github.com/gre/bezier-easing
 */

/** Coefficients based on Homer's method */
private fun coefficient1(a1: Float, a2: Float) = 1 - 3 * a2 + 3 * a1
private fun coefficient2(a1: Float, a2: Float) =     3 * a2 - 6 * a1
private fun coefficient3(a1: Float           ) =              3 * a1

/** @return dv/t based on Homer's method*/
private fun slope(t: Float, v1: Float, v2: Float) = 3f * coefficient1(v1, v2) * t * t + 2f * coefficient2(v1, v2) * t + coefficient3(v1)

private fun binarySubdivide(x: Float, a: Float, b: Float, x1: Float, x2: Float): Float {
    var currentAA = a
    var currentAB = b
    var currentX  : Float
    var currentT  : Float
    var i         = 0

    do {
        currentT = currentAA + (currentAB - currentAA) / 2f
        currentX = calcBezier(currentT, x1, x2) - x
        when {
            currentX > 0.0 -> currentAB = currentT
            else           -> currentAA = currentT
        }
    } while (abs(currentX) > SUBDIVISION_PRECISION && ++i < SUBDIVISION_MAX_ITERATIONS)

    return currentT
}

private fun newtonRaphsonIterate(x: Float, tGuess: Float, x1: Float, x2: Float): Float {
    var result = tGuess

    repeat(NEWTON_ITERATIONS) {
        when (val slope = slope(result, x1, x2)) {
            0f   -> return result
            else -> result -= (calcBezier(result, x1, x2) - x) / slope
        }
    }

    return result
}

private fun tForX(x1: Float, x2: Float, x: Float, sampleValues: FloatArray): Float {
    var intervalStart = 0f
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
        initialSlope >= NEWTON_MIN_SLOPE -> newtonRaphsonIterate(x, tGuess, x1, x2)
        initialSlope == 0f               -> tGuess
        else                             -> binarySubdivide(x, intervalStart, intervalStart + SAMPLE_STEP_SIZE, x1, x2)
    }
}

/** @return v(t) based on Homer's method*/
private fun calcBezier(t: Float, v1: Float, v2: Float) = ((coefficient1(v1, v2) * t + coefficient2(v1, v2)) * t + coefficient3(v1)) * t

internal fun getCubicBezier(x1: Float, y1: Float, x2: Float, y2: Float): EasingFunction {
    // Precompute samples table
    val sampleValues = FloatArray(SPLINE_TABLE_SIZE) { calcBezier(it * SAMPLE_STEP_SIZE, x1, x2) }

    return {
        when (it) {
            0f, 1f -> it // Because JavaScript number are imprecise, we should guarantee the extremes are right.
            else   -> calcBezier(tForX(x1, x2, it, sampleValues), y1, y2)
        }
    }
}

internal const val NEWTON_MIN_SLOPE           = 0.001
internal const val NEWTON_ITERATIONS          = 4
internal const val SUBDIVISION_PRECISION      = 0.0000001
internal const val SUBDIVISION_MAX_ITERATIONS = 10
internal const val SPLINE_TABLE_SIZE          = 11
internal const val SAMPLE_STEP_SIZE           = 1f / (SPLINE_TABLE_SIZE - 1)