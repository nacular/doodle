package com.nectar.doodle.animation

import com.nectar.doodle.units.Length
import com.nectar.doodle.units.Measure
import com.nectar.doodle.units.MeasureRatio
import com.nectar.doodle.units.Time

/**
 * Created by Nicholas Eddy on 3/29/18.
 */
class Moment(val position: Measure<Length>, val velocity: MeasureRatio<Length, Time>)