package com.nectar.doodle.animation

import com.nectar.measured.units.Measure
import com.nectar.measured.units.MeasureRatio
import com.nectar.measured.units.Time

/**
 * Created by Nicholas Eddy on 3/29/18.
 */
class Moment<T>(val position: Measure<T>, val velocity: MeasureRatio<T, Time>)