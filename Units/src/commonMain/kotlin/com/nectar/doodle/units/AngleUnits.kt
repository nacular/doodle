@file:Suppress("NOTHING_TO_INLINE")

package com.nectar.doodle.units

import kotlin.math.PI

/**
 * Created by Nicholas Eddy on 10/19/17.
 */

interface Angle

val radians = Unit<Angle>(" rad")
val degrees = Unit<Angle>("°", PI / 180)

val Int.   radians: Measure<Angle> get() = this * com.nectar.doodle.units.radians
val Float. radians: Measure<Angle> get() = this * com.nectar.doodle.units.radians
val Long.  radians: Measure<Angle> get() = this * com.nectar.doodle.units.radians
val Double.radians: Measure<Angle> get() = this * com.nectar.doodle.units.radians

val Int.   degrees: Measure<Angle> get() = this * com.nectar.doodle.units.degrees
val Float. degrees: Measure<Angle> get() = this * com.nectar.doodle.units.degrees
val Long.  degrees: Measure<Angle> get() = this * com.nectar.doodle.units.degrees
val Double.degrees: Measure<Angle> get() = this * com.nectar.doodle.units.degrees

inline fun sin  (angle : Measure<Angle>                        ) = kotlin.math.sin  (angle  `in` radians                    )
inline fun cos  (angle : Measure<Angle>                        ) = kotlin.math.cos  (angle  `in` radians                    )
inline fun tan  (angle : Measure<Angle>                        ) = kotlin.math.tan  (angle  `in` radians                    )
inline fun asin (angle : Measure<Angle>                        ) = kotlin.math.asin (angle  `in` radians                    )
inline fun acos (angle : Measure<Angle>                        ) = kotlin.math.acos (angle  `in` radians                    )
inline fun atan (angle : Measure<Angle>                        ) = kotlin.math.atan (angle  `in` radians                    )
inline fun atan2(angle1: Measure<Angle>, angle2: Measure<Angle>) = kotlin.math.atan2(angle1 `in` radians,angle2 `in` radians)
inline fun sinh (angle : Measure<Angle>                        ) = kotlin.math.sinh (angle  `in` radians                    )
inline fun cosh (angle : Measure<Angle>                        ) = kotlin.math.cosh (angle  `in` radians                    )
inline fun tanh (angle : Measure<Angle>                        ) = kotlin.math.tanh (angle  `in` radians                    )
inline fun asinh(angle : Measure<Angle>                        ) = kotlin.math.asinh(angle  `in` radians                    )
inline fun acosh(angle : Measure<Angle>                        ) = kotlin.math.acosh(angle  `in` radians                    )
inline fun atanh(angle : Measure<Angle>                        ) = kotlin.math.atanh(angle  `in` radians                    )