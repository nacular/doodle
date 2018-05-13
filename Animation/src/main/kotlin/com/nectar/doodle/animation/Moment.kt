package com.nectar.doodle.animation

import com.nectar.doodle.units.Measure
import com.nectar.doodle.units.MeasureRatio
import com.nectar.doodle.units.Time

/**
 * Created by Nicholas Eddy on 3/29/18.
 */
class Moment<T>(val position: Measure<T>, val velocity: MeasureRatio<T, Time>)