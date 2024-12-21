package io.nacular.doodle.controls.range

import io.nacular.doodle.controls.BasicConfinedRangeModel
import io.nacular.doodle.controls.ByteTypeConverter
import io.nacular.doodle.controls.ConfinedRangeModel
import io.nacular.doodle.controls.DoubleTypeConverter
import io.nacular.doodle.controls.FloatTypeConverter
import io.nacular.doodle.controls.IntTypeConverter
import io.nacular.doodle.controls.LongTypeConverter
import io.nacular.doodle.controls.ShortTypeConverter
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.behavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.utils.CharInterpolator
import io.nacular.doodle.utils.Interpolator
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.PropertyObserversImpl
import io.nacular.doodle.utils.interpolator
import io.nacular.measured.units.Measure
import io.nacular.measured.units.Units
import kotlin.jvm.JvmName

/**
 * Represents a range selection slider that is circular.
 *
 * @constructor
 * @param model containing range and value
 * @param interpolator used to map T -> Double and back
 */
public open class CircularRangeSlider<T>(
    model       : ConfinedRangeModel<T>,
    interpolator: Interpolator<T>,
    function    : InvertibleFunction = LinearFunction
): RangeValueSlider<T>(model, interpolator, function) where T: Comparable<T> {
    @Suppress("PrivatePropertyName")
    private val changed_ = PropertyObserversImpl<CircularRangeSlider<T>, ClosedRange<T>>(this)

    @Suppress("PrivatePropertyName")
    private val limitsChanged_ = PropertyObserversImpl<CircularRangeSlider<T>, ClosedRange<T>>(this)

    /**
     * Notifies of changes to [value].
     */
    public val changed: PropertyObservers<CircularRangeSlider<T>, ClosedRange<T>> = changed_

    /**
     * Notifies of changes to [value].
     */
    public val limitsChanged: PropertyObservers<CircularRangeSlider<T>, ClosedRange<T>> = limitsChanged_

    /**
     * Behavior that controls rendering and other interactions.
     */
    public var behavior: Behavior<CircularRangeSlider<T>>? by behavior()

    override fun render(canvas: Canvas) {
        behavior?.render(this, canvas)
    }

    override fun contains(point: Point): Boolean = super.contains(point) && behavior?.contains(this, point) ?: true

    override fun changed(old: ClosedRange<T>, new: ClosedRange<T>) {
        changed_(old, new)
    }

    override fun limitsChanged(old: ClosedRange<T>, new: ClosedRange<T>) {
        limitsChanged_(old, new)
    }

    override fun ticksChanged() {
        styleChanged { true }
    }

    public companion object {
        // region ================ Numbers ========================

        /**
         * Creates a Slider with a given range and starting value.
         *
         * @param limits of the bar
         * @param value to start with
         */
        public operator fun invoke(
            limits  : ClosedRange<Int>,
            value   : ClosedRange<Int> = limits.start .. limits.start,
            function: InvertibleFunction = LinearFunction
        ): CircularRangeSlider<Int> = invoke(BasicConfinedRangeModel(limits, value), function)

        /**
         * Creates a Slider with the given model.
         *
         * @param model of the bar
         */
        public operator fun invoke(model: ConfinedRangeModel<Int>, function: InvertibleFunction = LinearFunction): CircularRangeSlider<Int> = CircularRangeSlider(model, IntTypeConverter, function)

        /**
         * Creates a Slider with a given range and starting value.
         *
         * @param limits of the bar
         * @param value to start with
         */
        @JvmName("invokeFloat")
        public operator fun invoke(
            limits  : ClosedRange<Float>,
            value   : ClosedRange<Float> = limits.start .. limits.start,
            function: InvertibleFunction = LinearFunction
        ): CircularRangeSlider<Float> = invoke(BasicConfinedRangeModel(limits, value), function)

        /**
         * Creates a Slider with the given model.
         *
         * @param model of the bar
         */
        @JvmName("invokeFloat")
        public operator fun invoke(model: ConfinedRangeModel<Float>, function: InvertibleFunction = LinearFunction): CircularRangeSlider<Float> = CircularRangeSlider(model, FloatTypeConverter, function)

        /**
         * Creates a Slider with a given range and starting value.
         *
         * @param limits of the bar
         * @param value to start with
         */
        @JvmName("invokeDouble")
        public operator fun invoke(
            limits  : ClosedRange<Double>,
            value   : ClosedRange<Double> = limits.start .. limits.start,
            function: InvertibleFunction = LinearFunction
        ): CircularRangeSlider<Double> = invoke(BasicConfinedRangeModel(limits, value), function)

        /**
         * Creates a Slider with the given model.
         *
         * @param model of the bar
         */
        @JvmName("invokeDouble")
        public operator fun invoke(model: ConfinedRangeModel<Double>, function: InvertibleFunction = LinearFunction): CircularRangeSlider<Double> = CircularRangeSlider(model, DoubleTypeConverter, function)

        /**
         * Creates a Slider with a given range and starting value.
         *
         * @param limits of the bar
         * @param value to start with
         */
        @JvmName("invokeLong")
        public operator fun invoke(
            limits  : ClosedRange<Long>,
            value   : ClosedRange<Long> = limits.start .. limits.start,
            function: InvertibleFunction = LinearFunction
        ): CircularRangeSlider<Long> = invoke(BasicConfinedRangeModel(limits, value), function)

        /**
         * Creates a Slider with the given model.
         *
         * @param model of the bar
         */
        @JvmName("invokeLong")
        public operator fun invoke(model: ConfinedRangeModel<Long>, function: InvertibleFunction = LinearFunction): CircularRangeSlider<Long> = CircularRangeSlider(model, LongTypeConverter, function)

        /**
         * Creates a Slider with a given range and starting value.
         *
         * @param limits of the bar
         * @param value to start with
         */
        @Suppress("UNCHECKED_CAST")
        @JvmName("invokeShort")
        public operator fun invoke(
            limits  : ClosedRange<Short>,
            value   : ClosedRange<Short> = (limits.start .. limits.start) as ClosedRange<Short>,
            function: InvertibleFunction = LinearFunction
        ): CircularRangeSlider<Short> = invoke(BasicConfinedRangeModel(limits, value), function)

        /**
         * Creates a Slider with the given model.
         *
         * @param model of the bar
         */
        @JvmName("invokeShort")
        public operator fun invoke(model: ConfinedRangeModel<Short>, function: InvertibleFunction = LinearFunction): CircularRangeSlider<Short> = CircularRangeSlider(model, ShortTypeConverter, function)

        /**
         * Creates a Slider with a given range and starting value.
         *
         * @param limits of the bar
         * @param value to start with
         */
        @Suppress("UNCHECKED_CAST")
        @JvmName("invokeByte")
        public operator fun invoke(
            limits  : ClosedRange<Byte>,
            value   : ClosedRange<Byte> = (limits.start .. limits.start) as ClosedRange<Byte>,
            function: InvertibleFunction
        ): CircularRangeSlider<Byte> = invoke(BasicConfinedRangeModel(limits, value), function)

        /**
         * Creates a Slider with the given model.
         *
         * @param model of the bar
         */
        @JvmName("invokeByte")
        public operator fun invoke(model: ConfinedRangeModel<Byte>, function: InvertibleFunction = LinearFunction): CircularRangeSlider<Byte> = CircularRangeSlider(model, ByteTypeConverter, function)

        // endregion

        // region ================ Char ========================

        /**
         * Creates a Slider with a given range and starting value.
         *
         * @param limits of the bar
         * @param value to start with
         */
        @JvmName("invokeChar")
        public operator fun invoke(
            limits  : ClosedRange<Char>,
            value   : ClosedRange<Char> = limits.start .. limits.start,
            function: InvertibleFunction = LinearFunction
        ): CircularRangeSlider<Char> = CircularRangeSlider(
            BasicConfinedRangeModel(limits, value) as ConfinedRangeModel<Char>,
            CharInterpolator,
            function
        )

        /**
         * Creates a Slider with the given model.
         *
         * @param model of the bar
         */
        @JvmName("invokeChar")
        public operator fun invoke(model: ConfinedRangeModel<Char>): CircularRangeSlider<Char> = CircularRangeSlider(
            model,
            CharInterpolator
        )

        // endregion

        // region ================ Measure ========================

        /**
         * Creates a Slider with a given range and starting value.
         *
         * @param limits of the bar
         * @param value to start with
         */
        @JvmName("invokeMeasure")
        public operator fun <T: Units> invoke(
            limits  : ClosedRange<Measure<T>>,
            value   : ClosedRange<Measure<T>> = limits.start .. limits.start,
            function: InvertibleFunction = LinearFunction
        ): CircularRangeSlider<Measure<T>> = CircularRangeSlider(
            BasicConfinedRangeModel(limits, value) as ConfinedRangeModel<Measure<T>>,
            value.start.units.interpolator,
            function
        )

        /**
         * Creates a Slider with the given model.
         *
         * @param model of the bar
         */
        @JvmName("invokeMeasure")
        public operator fun <T: Units> invoke(model: ConfinedRangeModel<Measure<T>>): CircularRangeSlider<Measure<T>> = CircularRangeSlider(
            model,
            model.range.start.units.interpolator
        )

        // endregion

        // region ================ T ========================

        /**
         * Creates a Slider with a given range and starting value.
         *
         * @param limits of the bar
         * @param value to start with
         */
        public operator fun <T> invoke(
            limits   : ClosedRange<T>,
            converter: Interpolator<T>,
            value    : ClosedRange<T> = limits.start .. limits.start,
            function : InvertibleFunction = LinearFunction
        ): CircularRangeSlider<T> where T: Comparable<T> = CircularRangeSlider(
            BasicConfinedRangeModel(limits, value) as ConfinedRangeModel<T>,
            converter,
            function
        )

        /**
         * Creates a Slider with the given model.
         *
         * @param model of the bar
         */
        public operator fun <T> invoke(
            model    : ConfinedRangeModel<T>,
            converter: Interpolator<T>,
            function : InvertibleFunction = LinearFunction
        ): CircularRangeSlider<T> where T: Comparable<T> = CircularRangeSlider(
            model,
            converter,
            function
        )

        // endregion
    }
}
