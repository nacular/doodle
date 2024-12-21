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
import io.nacular.doodle.utils.Orientation
import io.nacular.doodle.utils.Orientation.Horizontal
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.PropertyObserversImpl
import io.nacular.doodle.utils.interpolator
import io.nacular.measured.units.Measure
import io.nacular.measured.units.Units
import kotlin.jvm.JvmName

/**
 * Represents a range selection slider that can be [Horizontal] or [Vertical][io.nacular.doodle.utils.Orientation.Vertical].
 *
 * @constructor
 * @param model containing range and value
 * @param orientation of the control
 */
public open class RangeSlider<T>(
               model       : ConfinedRangeModel<T>,
    public val orientation : Orientation = Horizontal,
               function    : InvertibleFunction = LinearFunction,
               interpolator: Interpolator<T>
): RangeValueSlider<T>(model, interpolator, function) where T: Comparable<T>  {

    @Suppress("PrivatePropertyName")
    private val changed_ = PropertyObserversImpl<RangeSlider<T>, ClosedRange<T>>(this)

    @Suppress("PrivatePropertyName")
    private val limitsChanged_ = PropertyObserversImpl<RangeSlider<T>, ClosedRange<T>>(this)

    /**
     * Notifies of changes to [value].
     */
    public val changed: PropertyObservers<RangeSlider<T>, ClosedRange<T>> = changed_

    /**
     * Notifies of changes to [value].
     */
    public val limitsChanged: PropertyObservers<RangeSlider<T>, ClosedRange<T>> = limitsChanged_

    /**
     * Behavior that controls rendering and other interactions.
     */
    public var behavior: Behavior<RangeSlider<T>>? by behavior()

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
         * @param orientation of the control
         */
        public operator fun invoke(
            limits     : ClosedRange<Int>,
            value      : ClosedRange<Int> = limits.start .. limits.start,
            orientation: Orientation      = Horizontal,
            function   : InvertibleFunction = LinearFunction): RangeSlider<Int> = invoke(BasicConfinedRangeModel(limits, value), orientation, function)

        /**
         * Creates a Slider with the given model.
         *
         * @param model of the bar
         * @param orientation of the control
         */
        public operator fun invoke(
            model      : ConfinedRangeModel<Int>,
            orientation: Orientation      = Horizontal,
            function   : InvertibleFunction = LinearFunction): RangeSlider<Int> = RangeSlider(
            model,
            orientation,
            function,
            IntTypeConverter,
        )

        /**
         * Creates a Slider with a given range and starting value.
         *
         * @param limits of the bar
         * @param value to start with
         * @param orientation of the control
         */
        @JvmName("invokeFloat")
        public operator fun invoke(
            limits     : ClosedRange<Float>,
            value      : ClosedRange<Float> = limits.start .. limits.start,
            orientation: Orientation        = Horizontal,
            function   : InvertibleFunction = LinearFunction): RangeSlider<Float> = invoke(BasicConfinedRangeModel(limits, value), orientation, function)

        /**
         * Creates a Slider with the given model.
         *
         * @param model of the bar
         * @param orientation of the control
         */
        @JvmName("invokeFloat")
        public operator fun invoke(
            model      : ConfinedRangeModel<Float>,
            orientation: Orientation      = Horizontal,
            function   : InvertibleFunction = LinearFunction): RangeSlider<Float> = RangeSlider(
            model,
            orientation,
            function,
            FloatTypeConverter
        )

        /**
         * Creates a Slider with a given range and starting value.
         *
         * @param limits of the bar
         * @param value to start with
         * @param orientation of the control
         */
        @JvmName("invokeDouble")
        public operator fun invoke(
            limits     : ClosedRange<Double>,
            value      : ClosedRange<Double> = limits.start .. limits.start,
            orientation: Orientation         = Horizontal,
            function   : InvertibleFunction = LinearFunction): RangeSlider<Double> = invoke(BasicConfinedRangeModel(limits, value), orientation, function)

        /**
         * Creates a Slider with the given model.
         *
         * @param model of the bar
         * @param orientation of the control
         */
        @JvmName("invokeDouble")
        public operator fun invoke(
            model      : ConfinedRangeModel<Double>,
            orientation: Orientation      = Horizontal,
            function   : InvertibleFunction = LinearFunction): RangeSlider<Double> = RangeSlider(
            model,
            orientation,
            function,
            DoubleTypeConverter
        )

        /**
         * Creates a Slider with a given range and starting value.
         *
         * @param limits of the bar
         * @param value to start with
         * @param orientation of the control
         */
        @JvmName("invokeLong")
        public operator fun invoke(
            limits     : ClosedRange<Long>,
            value      : ClosedRange<Long> = limits.start .. limits.start,
            orientation: Orientation       = Horizontal,
            function   : InvertibleFunction = LinearFunction): RangeSlider<Long> = invoke(BasicConfinedRangeModel(limits, value), orientation, function)

        /**
         * Creates a Slider with the given model.
         *
         * @param model of the bar
         * @param orientation of the control
         */
        @JvmName("invokeLong")
        public operator fun invoke(
            model      : ConfinedRangeModel<Long>,
            orientation: Orientation      = Horizontal,
            function   : InvertibleFunction = LinearFunction): RangeSlider<Long> = RangeSlider(
            model,
            orientation,
            function,
            LongTypeConverter
        )

        /**
         * Creates a Slider with a given range and starting value.
         *
         * @param limits of the bar
         * @param value to start with
         * @param orientation of the control
         */
        @Suppress("UNCHECKED_CAST")
        @JvmName("invokeShort")
        public operator fun invoke(
            limits     : ClosedRange<Short>,
            value      : ClosedRange<Short> = (limits.start .. limits.start) as ClosedRange<Short>,
            orientation: Orientation        = Horizontal,
            function   : InvertibleFunction = LinearFunction): RangeSlider<Short> = invoke(BasicConfinedRangeModel(limits, value), orientation, function)

        /**
         * Creates a Slider with the given model.
         *
         * @param model of the bar
         * @param orientation of the control
         */
        @JvmName("invokeShort")
        public operator fun invoke(
            model      : ConfinedRangeModel<Short>,
            orientation: Orientation      = Horizontal,
            function   : InvertibleFunction = LinearFunction): RangeSlider<Short> = RangeSlider(
            model,
            orientation,
            function,
            ShortTypeConverter
        )

        /**
         * Creates a Slider with a given range and starting value.
         *
         * @param limits of the bar
         * @param value to start with
         * @param orientation of the control
         */
        @Suppress("UNCHECKED_CAST")
        @JvmName("invokeByte")
        public operator fun invoke(
            limits     : ClosedRange<Byte>,
            value      : ClosedRange<Byte> = (limits.start .. limits.start) as ClosedRange<Byte>,
            orientation: Orientation       = Horizontal,
            function   : InvertibleFunction = LinearFunction): RangeSlider<Byte> = invoke(BasicConfinedRangeModel(limits, value), orientation, function)

        /**
         * Creates a Slider with the given model.
         *
         * @param model of the bar
         * @param orientation of the control
         */
        @JvmName("invokeByte")
        public operator fun invoke(
            model      : ConfinedRangeModel<Byte>,
            orientation: Orientation      = Horizontal,
            function   : InvertibleFunction = LinearFunction): RangeSlider<Byte> = RangeSlider(
            model,
            orientation,
            function,
            ByteTypeConverter
        )

        // endregion

        // region ================ Char ========================

        /**
         * Creates a Slider with a given range and starting value.
         *
         * @param limits of the bar
         * @param value to start with
         * @param orientation of the control
         */
        @JvmName("invokeChar")
        public operator fun invoke(
            limits     : ClosedRange<Char>,
            value      : ClosedRange<Char> = limits.start .. limits.start,
            orientation: Orientation       = Horizontal,
            function   : InvertibleFunction = LinearFunction): RangeSlider<Char> = invoke(BasicConfinedRangeModel(limits, value) as ConfinedRangeModel<Char>, orientation, function)

        /**
         * Creates a Slider with the given model.
         *
         * @param model of the bar
         * @param orientation of the control
         */
        @JvmName("invokeChar")
        public operator fun invoke(
            model      : ConfinedRangeModel<Char>,
            orientation: Orientation      = Horizontal,
            function   : InvertibleFunction = LinearFunction): RangeSlider<Char> = RangeSlider(
            model,
            orientation,
            function,
            CharInterpolator
        )

        // endregion

        // region ================ Measure ========================

        /**
         * Creates a Slider with a given range and starting value.
         *
         * @param limits of the bar
         * @param value to start with
         * @param orientation of the control
         */
        @JvmName("invokeMeasure")
        public operator fun <T: Units> invoke(
            limits     : ClosedRange<Measure<T>>,
            value      : ClosedRange<Measure<T>> = limits.start .. limits.start,
            orientation: Orientation             = Horizontal,
            function   : InvertibleFunction = LinearFunction): RangeSlider<Measure<T>> = invoke(BasicConfinedRangeModel(limits, value) as ConfinedRangeModel<Measure<T>>, orientation, function)

        /**
         * Creates a Slider with the given model.
         *
         * @param model of the bar
         * @param orientation of the control
         */
        @JvmName("invokeMeasure")
        public operator fun <T: Units> invoke(
            model      : ConfinedRangeModel<Measure<T>>,
            orientation: Orientation      = Horizontal,
            function   : InvertibleFunction = LinearFunction): RangeSlider<Measure<T>> = RangeSlider(
            model,
            orientation,
            function,
            model.range.start.units.interpolator
        )

        // endregion

        // region ================ T ========================

        /**
         * Creates a Slider with a given range and starting value.
         *
         * @param limits of the bar
         * @param converter to handle numeric translations for [T]
         * @param value to start with
         * @param orientation of the control
         */
        public operator fun <T> invoke(
            limits     : ClosedRange<T>,
            converter  : Interpolator<T>,
            value      : ClosedRange<T>   = limits.start .. limits.start,
            orientation: Orientation      = Horizontal,
            function   : InvertibleFunction = LinearFunction): RangeSlider<T> where T: Comparable<T> = invoke(
            BasicConfinedRangeModel(limits, value),
            converter,
            orientation,
            function
        )

        /**
         * Creates a Slider with the given model.
         *
         * @param model of the bar
         * @param converter to handle numeric translations for [T]
         * @param orientation of the control
         */
        public operator fun <T> invoke(
            model      : ConfinedRangeModel<T>,
            converter  : Interpolator<T>,
            orientation: Orientation      = Horizontal,
            function   : InvertibleFunction = LinearFunction): RangeSlider<T> where T: Comparable<T> = RangeSlider(
            model,
            orientation,
            function,
            converter
        )

        // endregion
    }
}