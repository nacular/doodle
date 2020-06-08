package io.nacular.doodle.controls

import io.nacular.doodle.utils.Orientation
import io.nacular.doodle.utils.Orientation.Horizontal

/**
 * Represents a progress indicator that can be [Horizontal] or [Vertical][io.nacular.doodle.utils.Orientation.Vertical].
 *
 * @constructor
 * @param model containing range and value
 * @param orientation of the control
 */
class ProgressBar(
        model          : ConfinedValueModel<Double> = BasicConfinedValueModel(0.0 .. 100.0),
        val orientation: Orientation                = Horizontal): ProgressIndicator(model)
