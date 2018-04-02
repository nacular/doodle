package com.nectar.doodle.units

/**
 * Created by Nicholas Eddy on 3/30/18.
 */

interface Length

val pixels = Unit<Length>(" px")

val Int.   pixels: Measure<Length> get() = this * com.nectar.doodle.units.pixels
val Float. pixels: Measure<Length> get() = this * com.nectar.doodle.units.pixels
val Long.  pixels: Measure<Length> get() = this * com.nectar.doodle.units.pixels
val Double.pixels: Measure<Length> get() = this * com.nectar.doodle.units.pixels