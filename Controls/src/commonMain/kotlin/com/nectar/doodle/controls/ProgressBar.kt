package com.nectar.doodle.controls

import com.nectar.doodle.utils.Orientation
import com.nectar.doodle.utils.Orientation.Horizontal

/**
 * Represents a progress indicator that can be [Horizontal] or [Vertical][com.nectar.doodle.utils.Orientation.Vertical].
 *
 * @constructor
 * @param model containing range and value
 * @param orientation of the control
 */
class ProgressBar(
        model          : ConfinedValueModel<Double> = BasicConfinedValueModel(0.0 .. 100.0),
        val orientation: Orientation                = Horizontal): ProgressIndicator(model)
