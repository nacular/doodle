package io.nacular.doodle.animation

import io.nacular.doodle.animation.transition.FixedSpeedLinear
import io.nacular.doodle.animation.transition.FixedTimeLinear
import io.nacular.doodle.animation.transition.NoChange
import io.nacular.doodle.animation.transition.SpeedUpSlowDown
import io.nacular.doodle.animation.transition.Transition
import io.nacular.doodle.utils.Completable
import io.nacular.doodle.utils.Pool
import io.nacular.measured.units.InverseUnits
import io.nacular.measured.units.Measure
import io.nacular.measured.units.Time
import io.nacular.measured.units.Units
import io.nacular.measured.units.UnitsRatio
import io.nacular.measured.units.times
import kotlin.jvm.JvmName

/**
 * Created by Nicholas Eddy on 3/29/18.
 */
public class NoneUnit: Units("")

public val noneUnits: NoneUnit = NoneUnit()

@JvmName("fixedSpeedLinearNumber")
public fun <T: Number> fixedSpeedLinear(speed: Measure<InverseUnits<Time>>): (T, T) -> Transition<NoneUnit> = { _,end -> FixedSpeedLinear(1 * noneUnits * speed, end * noneUnits) }

@JvmName("fixedSpeedLinearUnit")
public fun <T: Units> fixedSpeedLinear(speed: Measure<UnitsRatio<T, Time>>): (Measure<T>, Measure<T>) -> Transition<T> = { _,end -> FixedSpeedLinear(speed, end) }

public fun <T: Number> fixedTimeLinear(time: Measure<Time>): (T, T) -> Transition<NoneUnit> = { _,end -> FixedTimeLinear(time, end * noneUnits) }

public fun <T: Units> fixedTimeLinearM(time: Measure<Time>): (Measure<T>, Measure<T>) -> Transition<T> = { _,end -> FixedTimeLinear(time, end) }

public fun <T: Number> speedUpSlowDown(time: Measure<Time>, accelerationFraction: Float = 0.5f): (T, T) -> Transition<NoneUnit> = { _,end -> SpeedUpSlowDown(time, end * noneUnits, accelerationFraction) }

public fun <T: Units> speedUpSlowDownM(time: Measure<Time>, accelerationFraction: Float = 0.5f): (Measure<T>, Measure<T>) -> Transition<T> = { _,end -> SpeedUpSlowDown(time, end, accelerationFraction) }

public fun <T: Number> noChange (time: Measure<Time>): (T) -> Transition<NoneUnit> = { NoChange(time) }
public fun <T: Units>  noChangeM(time: Measure<Time>): (Measure<T>) -> Transition<T> = { NoChange(time) }


public interface Animation: Completable

public interface Animator {
    public interface Listener {
        public fun changed  (animator: Animator, animations: Set<Animation>) {}
        public fun canceled (animator: Animator, animations: Set<Animation>) {}
        public fun completed(animator: Animator, animations: Set<Animation>) {}
    }

    public interface TransitionBuilder<T: Number> {
        public infix fun then(transition: Transition<NoneUnit>): TransitionBuilder<T>

        public operator fun invoke(block: (T) -> Unit): Animation
    }

    public interface MeasureTransitionBuilder<T: Units> {
        public infix fun then(transition: Transition<T>): MeasureTransitionBuilder<T>

        public operator fun invoke(block: (Measure<T>) -> Unit): Animation
    }

    public interface NumberRangeUsing<T: Number> {
        public infix fun using(transition: (start: T, end: T) -> Transition<NoneUnit>): TransitionBuilder<T>
    }

    public interface NumberUsing<T: Number> {
        public infix fun using(transition: (start: T) -> Transition<NoneUnit>): TransitionBuilder<T>
    }

    public interface MeasureRangeUsing<T: Units> {
        public infix fun using(transition: (start: Measure<T>, end: Measure<T>) -> Transition<T>): MeasureTransitionBuilder<T>
    }

    public interface MeasureUsing<T: Units> {
        public infix fun using(transition: (start: Measure<T>) -> Transition<T>): MeasureTransitionBuilder<T>
    }

    public operator fun <T: Number> invoke(range: Pair<T, T>): NumberRangeUsing<T> = object: NumberRangeUsing<T> {
        override fun using(transition: (start: T, end: T) -> Transition<NoneUnit>) = range using transition
    }

    public operator fun <T: Number> invoke(value: T): NumberUsing<T> = object: NumberUsing<T> {
        override fun using(transition: (start: T) -> Transition<NoneUnit>) = value using transition
    }

    public operator fun <T: Units> invoke(range: Pair<Measure<T>, Measure<T>>): MeasureRangeUsing<T> = object: MeasureRangeUsing<T> {
        override fun using(transition: (start: Measure<T>, end: Measure<T>) -> Transition<T>) = range using transition
    }

    public operator fun <T: Units> invoke(value: Measure<T>): MeasureUsing<T> = object: MeasureUsing<T> {
        override fun using(transition: (start: Measure<T>) -> Transition<T>) = value using transition
    }

    public infix fun <T: Number> Pair<T, T>.using(transition: (start: T, end: T) -> Transition<NoneUnit>): TransitionBuilder<T>
    public infix fun <T: Number> T.using(transition: (start: T) -> Transition<NoneUnit>): TransitionBuilder<T>
    public infix fun <T: Units>  Pair<Measure<T>, Measure<T>>.using(transition: (start: Measure<T>, end: Measure<T>) -> Transition<T>): MeasureTransitionBuilder<T>
    public infix fun <T: Units>  Measure<T>.using(transition: (start: Measure<T>) -> Transition<T>): MeasureTransitionBuilder<T>

    public operator fun invoke(block: Animator.() -> Unit): Completable

    public val listeners: Pool<Listener>
}