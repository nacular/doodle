package com.nectar.doodle.units

/**
 * Created by Nicholas Eddy on 4/2/18.
 */
val pixels_second = pixels / seconds

val Int.   pixels_second: MeasureRatio<Length, Time> get() = this * com.nectar.doodle.units.pixels_second
val Float. pixels_second: MeasureRatio<Length, Time> get() = this * com.nectar.doodle.units.pixels_second
val Long.  pixels_second: MeasureRatio<Length, Time> get() = this * com.nectar.doodle.units.pixels_second
val Double.pixels_second: MeasureRatio<Length, Time> get() = this * com.nectar.doodle.units.pixels_second