package com.nectar.doodle.units

/**
 * Created by Nicholas Eddy on 3/30/18.
 */

interface Distance

val pixels  = Unit<Distance>(" px")

val Int.   pixels: Measure<Distance> get() = this * com.nectar.doodle.units.pixels
val Float. pixels: Measure<Distance> get() = this * com.nectar.doodle.units.pixels
val Long.  pixels: Measure<Distance> get() = this * com.nectar.doodle.units.pixels
val Double.pixels: Measure<Distance> get() = this * com.nectar.doodle.units.pixels

val pixels_second = pixels / seconds

val Int.   pixels_second: MeasureRatio<Distance, Time> get() = this * com.nectar.doodle.units.pixels_second
val Float. pixels_second: MeasureRatio<Distance, Time> get() = this * com.nectar.doodle.units.pixels_second
val Long.  pixels_second: MeasureRatio<Distance, Time> get() = this * com.nectar.doodle.units.pixels_second
val Double.pixels_second: MeasureRatio<Distance, Time> get() = this * com.nectar.doodle.units.pixels_second
