package io.nacular.doodle.animation

import io.nacular.measured.units.Measure
import io.nacular.measured.units.Square
import io.nacular.measured.units.Time
import io.nacular.measured.units.Units
import io.nacular.measured.units.UnitsRatio

/**
 * Created by Nicholas Eddy on 3/29/18.
 */
public class Moment<T: Units>(public val position: Measure<T>, public val velocity: Measure<Velocity<T>>)

public typealias Velocity<T>     = UnitsRatio<T, Time>
public typealias Acceleration<T> = UnitsRatio<T, Square<Time>>
