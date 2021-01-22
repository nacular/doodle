package io.nacular.doodle.time

import io.nacular.measured.units.Measure
import io.nacular.measured.units.Time

/**
 * Exposes the current epoch time that is useful for date related time operations.
 * This API is tied to a user's clock, so it should not be used when monotonicity is required.
 * It is also not guaranteed to be as high resolution as needed for precise time operations.
 *
 * Created by Nicholas Eddy on 10/19/17.
 */
public interface Clock {
    /** Current epoch time */
    public val epoch: Measure<Time>
}

/**
 * Provides a precise elapsed time that is useful for relative time measurements
 * that are not affected by the user's clock (i.e. time rollbacks).  The resolution of this
 * time is expected to be higher than [Clock.epoch].
 */
public interface Timer {
    public val now: Measure<Time>
}