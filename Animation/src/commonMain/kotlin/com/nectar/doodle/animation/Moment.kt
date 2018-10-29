package com.nectar.doodle.animation

import com.nectar.measured.units.Measure
import com.nectar.measured.units.Square
import com.nectar.measured.units.Time
import com.nectar.measured.units.Unit
import com.nectar.measured.units.UnitRatio

/**
 * Created by Nicholas Eddy on 3/29/18.
 */
class Moment<T: Unit>(val position: Measure<T>, val velocity: Measure<UnitRatio<T, Time>>)

typealias Velocity<T>     = UnitRatio<T, Time>
typealias Acceleration<T> = UnitRatio<T, Square<Time>>
