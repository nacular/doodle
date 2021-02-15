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
public class ProgressBar(
                   model      : ConfinedValueModel<Double> = BasicConfinedValueModel(0.0 .. 100.0),
        public val orientation: Orientation                = Horizontal): ProgressIndicator(model) {
    /**
     * Creates a ProgressBar with a given range and starting value.
     *
     * @param range of the bar
     * @param value to start with
     * @param orientation of the control
     */
    public constructor(range: ClosedRange<Double> = 0.0 .. 100.0, value: Double = range.start, orientation: Orientation = Horizontal): this(BasicConfinedValueModel(range, value), orientation)
}
