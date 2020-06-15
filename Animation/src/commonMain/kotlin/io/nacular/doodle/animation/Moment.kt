package io.nacular.doodle.animation

import io.nacular.measured.units.Measure
import io.nacular.measured.units.Square
import io.nacular.measured.units.Time
import io.nacular.measured.units.Units
import io.nacular.measured.units.UnitsRatio

/**
 * Created by Nicholas Eddy on 3/29/18.
 */
class Moment<T: Units>(val position: Measure<T>, val velocity: Measure<Velocity<T>>)

typealias Velocity<T>     = UnitsRatio<T, Time>
typealias Acceleration<T> = UnitsRatio<T, Square<Time>>
