package com.nectar.doodle.units

/**
 * Created by Nicholas Eddy on 10/19/17.
 */

interface Time

val milliseconds = Unit<Time>(" ms"                          )
val seconds      = Unit<Time>(" s",   1000.0                 )
val minutes      = Unit<Time>(" min", 60 * seconds.multiplier)
val hours        = Unit<Time>(" hr",  60 * minutes.multiplier)

val Int.   milliseconds: Measure<Time> get() = this * com.nectar.doodle.units.milliseconds
val Float. milliseconds: Measure<Time> get() = this * com.nectar.doodle.units.milliseconds
val Long.  milliseconds: Measure<Time> get() = this * com.nectar.doodle.units.milliseconds
val Double.milliseconds: Measure<Time> get() = this * com.nectar.doodle.units.milliseconds

val Int.   seconds     : Measure<Time> get() = this * com.nectar.doodle.units.seconds
val Float. seconds     : Measure<Time> get() = this * com.nectar.doodle.units.seconds
val Long.  seconds     : Measure<Time> get() = this * com.nectar.doodle.units.seconds
val Double.seconds     : Measure<Time> get() = this * com.nectar.doodle.units.seconds

val Int.   minutes     : Measure<Time> get() = this * com.nectar.doodle.units.minutes
val Float. minutes     : Measure<Time> get() = this * com.nectar.doodle.units.minutes
val Long.  minutes     : Measure<Time> get() = this * com.nectar.doodle.units.minutes
val Double.minutes     : Measure<Time> get() = this * com.nectar.doodle.units.minutes

val Int.   hours       : Measure<Time> get() = this * com.nectar.doodle.units.hours
val Float. hours       : Measure<Time> get() = this * com.nectar.doodle.units.hours
val Long.  hours       : Measure<Time> get() = this * com.nectar.doodle.units.hours
val Double.hours       : Measure<Time> get() = this * com.nectar.doodle.units.hours
