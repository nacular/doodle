package com.nectar.doodle.controls

import com.nectar.doodle.utils.Orientation
import com.nectar.doodle.utils.Orientation.Horizontal

/**
 * Created by Nicholas Eddy on 2/12/18.
 */
class ProgressBar(model: ConfinedValueModel<Double>, val orientation: Orientation = Horizontal): ProgressIndicator(model) {
    constructor(range: ClosedRange<Double> = 0.0 .. 100.0, value: Double = range.start, orientation: Orientation = Horizontal): this(BasicConfinedValueModel(range, value), orientation)
}
