package com.nectar.doodle.animation.units

import com.nectar.doodle.units.Measure
import com.nectar.doodle.units.Measure2
import com.nectar.doodle.units.Time
import com.nectar.doodle.units.Unit
import com.nectar.doodle.units.Unit2
import com.nectar.doodle.units.seconds
import com.nectar.doodle.units.times

/**
 * Created by Nicholas Eddy on 3/30/18.
 */

interface Distance

val meters = Unit<Distance>(" m")

val Int.   meters: Measure<Distance> get() = this * com.nectar.doodle.animation.units.meters
val Float. meters: Measure<Distance> get() = this * com.nectar.doodle.animation.units.meters
val Long.  meters: Measure<Distance> get() = this * com.nectar.doodle.animation.units.meters
val Double.meters: Measure<Distance> get() = this * com.nectar.doodle.animation.units.meters


val meters_second = Unit2(meters, seconds)

val Int.   meters_second: Measure2<Distance, Time> get() = this * com.nectar.doodle.animation.units.meters_second
val Float. meters_second: Measure2<Distance, Time> get() = this * com.nectar.doodle.animation.units.meters_second
val Long.  meters_second: Measure2<Distance, Time> get() = this * com.nectar.doodle.animation.units.meters_second
val Double.meters_second: Measure2<Distance, Time> get() = this * com.nectar.doodle.animation.units.meters_second
