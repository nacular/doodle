package com.nectar.doodle.units

import kotlin.math.PI

/**
 * Created by Nicholas Eddy on 10/19/17.
 */

interface Angle

val radians = Unit<Angle>(" rad")
val degrees = Unit<Angle>("°", 180 / PI)

val Int.   radians: Measure<Angle> get() = this * com.nectar.doodle.units.radians
val Float. radians: Measure<Angle> get() = this * com.nectar.doodle.units.radians
val Long.  radians: Measure<Angle> get() = this * com.nectar.doodle.units.radians
val Double.radians: Measure<Angle> get() = this * com.nectar.doodle.units.radians

val Int.   degrees: Measure<Angle> get() = this * com.nectar.doodle.units.degrees
val Float. degrees: Measure<Angle> get() = this * com.nectar.doodle.units.degrees
val Long.  degrees: Measure<Angle> get() = this * com.nectar.doodle.units.degrees
val Double.degrees: Measure<Angle> get() = this * com.nectar.doodle.units.degrees
