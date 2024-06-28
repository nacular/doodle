package io.nacular.doodle.controls.range

import io.nacular.doodle.controls.BasicConfinedValueModel
import io.nacular.doodle.controls.ByteTypeConverter
import io.nacular.doodle.controls.ConfinedValueModel
import io.nacular.doodle.controls.DoubleTypeConverter
import io.nacular.doodle.controls.FloatTypeConverter
import io.nacular.doodle.controls.IntTypeConverter
import io.nacular.doodle.controls.LongTypeConverter
import io.nacular.doodle.controls.ShortTypeConverter
import io.nacular.doodle.controls.numberTypeConverter
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
import kotlin.reflect.KClass

/**
 * Represents a selection slider that is circular.
 *
 * @constructor
 * @param model containing range and value
 * @param interpolator for mapping numeric range to types
 */
public open class CircularSlider<T>(
    model       : ConfinedValueModel<T>,
    interpolator: Interpolator<T>,
    function    : InvertibleFunction = LinearFunction
): ValueSlider<T>(model, interpolator, function) where T: Comparable<T> {
    @Suppress("PrivatePropertyName")
    private val changed_ by lazy { PropertyObserversImpl<CircularSlider<T>, T>(this) }

    @Suppress("PrivatePropertyName")
    private val limitsChanged_ by lazy { PropertyObserversImpl<CircularSlider<T>, ClosedRange<T>>(this) }

    /**
     * Notifies of changes to [value].
     */
    public val changed: PropertyObservers<CircularSlider<T>, T> = changed_

    /**
     * Notifies of changes to [range].
     */
    public val limitsChanged: PropertyObservers<CircularSlider<T>, ClosedRange<T>> = limitsChanged_

    /**
     * Behavior that controls rendering and other interactions.
     */
    public var behavior: Behavior<CircularSlider<T>>? by behavior()

    override fun render(canvas: Canvas) {
        behavior?.render(this, canvas)
    }

    override fun contains(point: Point): Boolean = super.contains(point) && behavior?.contains(this, point) ?: true

    override fun changed(old: T, new: T) {
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
         * Creates a CircularSlider with a given range and starting value.
         *
         * @param range of the bar
         * @param value to start with
         */
        public operator fun invoke(
            range   : ClosedRange<Int>,
            value   : Int = range.start,
            function: InvertibleFunction = LinearFunction
        ): CircularSlider<Int> = invoke(BasicConfinedValueModel(range, value), function)

        /**
         * Creates a CircularSlider with the given model.
         *
         * @param model of the bar
         */
        public operator fun invoke(model: ConfinedValueModel<Int>, function: InvertibleFunction = LinearFunction): CircularSlider<Int> = CircularSlider(model, IntTypeConverter, function)

        /**
         * Creates a CircularSlider with a given range and starting value.
         *
         * @param range of the bar
         * @param value to start with
         */
        @JvmName("invokeFloat")
        public operator fun invoke(
            range   : ClosedRange<Float>,
            value   : Float = range.start,
            function: InvertibleFunction = LinearFunction
        ): CircularSlider<Float> = invoke(BasicConfinedValueModel(range, value), function)

        /**
         * Creates a CircularSlider with the given model.
         *
         * @param model of the bar
         */
        @JvmName("invokeFloat")
        public operator fun invoke(model: ConfinedValueModel<Float>, function: InvertibleFunction = LinearFunction): CircularSlider<Float> = CircularSlider(model, FloatTypeConverter, function)

        /**
         * Creates a CircularSlider with a given range and starting value.
         *
         * @param range of the bar
         * @param value to start with
         */
        @JvmName("invokeDouble")
        public operator fun invoke(
            range   : ClosedRange<Double>,
            value   : Double = range.start,
            function: InvertibleFunction = LinearFunction
        ): CircularSlider<Double> = invoke(BasicConfinedValueModel(range, value), function)

        /**
         * Creates a CircularSlider with the given model.
         *
         * @param model of the bar
         */
        @JvmName("invokeDouble")
        public operator fun invoke(model: ConfinedValueModel<Double>, function: InvertibleFunction = LinearFunction): CircularSlider<Double> = CircularSlider(model, DoubleTypeConverter, function)

        /**
         * Creates a CircularSlider with a given range and starting value.
         *
         * @param range of the bar
         * @param value to start with
         */
        @JvmName("invokeLong")
        public operator fun invoke(
            range   : ClosedRange<Long>,
            value   : Long = range.start,
            function: InvertibleFunction = LinearFunction
        ): CircularSlider<Long> = invoke(BasicConfinedValueModel(range, value), function)

        /**
         * Creates a CircularSlider with the given model.
         *
         * @param model of the bar
         */
        @JvmName("invokeLong")
        public operator fun invoke(model: ConfinedValueModel<Long>, function: InvertibleFunction = LinearFunction): CircularSlider<Long> = CircularSlider(model, LongTypeConverter, function)

        /**
         * Creates a CircularSlider with a given range and starting value.
         *
         * @param range of the bar
         * @param value to start with
         */
        @JvmName("invokeShort")
        public operator fun invoke(
            range   : ClosedRange<Short>,
            value   : Short = range.start,
            function: InvertibleFunction = LinearFunction
        ): CircularSlider<Short> = invoke(BasicConfinedValueModel(range, value), function)

        /**
         * Creates a CircularSlider with the given model.
         *
         * @param model of the bar
         */
        @JvmName("invokeShort")
        public operator fun invoke(model: ConfinedValueModel<Short>, function: InvertibleFunction = LinearFunction): CircularSlider<Short> = CircularSlider(model, ShortTypeConverter, function)

        /**
         * Creates a CircularSlider with a given range and starting value.
         *
         * @param range of the bar
         * @param value to start with
         */
        @JvmName("invokeByte")
        public operator fun invoke(
            range   : ClosedRange<Byte>,
            value   : Byte = range.start,
            function: InvertibleFunction = LinearFunction
        ): CircularSlider<Byte> = invoke(BasicConfinedValueModel(range, value), function)

        /**
         * Creates a CircularSlider with the given model.
         *
         * @param model of the bar
         */
        @JvmName("invokeByte")
        public operator fun invoke(model: ConfinedValueModel<Byte>, function: InvertibleFunction = LinearFunction): CircularSlider<Byte> = CircularSlider(model, ByteTypeConverter, function)

        // endregion

        // region ================ Char ========================

        /**
         * Creates a CircularSlider with a given range and starting value.
         *
         * @param range of the bar
         * @param value to start with
         */
        public operator fun invoke(
            range   : CharRange,
            value   : Char = range.first,
            function: InvertibleFunction = LinearFunction
        ): CircularSlider<Char> = CircularSlider(
            BasicConfinedValueModel(range, value),
            CharInterpolator,
            function
        )

        /**
         * Creates a CircularSlider with the given model.
         *
         * @param model of the bar
         */
        @JvmName("invokeChar")
        public operator fun invoke(
            model   : ConfinedValueModel<Char>,
            function: InvertibleFunction = LinearFunction
        ): CircularSlider<Char> = CircularSlider(
            model,
            CharInterpolator,
            function
        )

        // endregion

        // region ================ Measure ========================

        /**
         * Creates a CircularSlider with a given range and starting value.
         *
         * @param range of the bar
         * @param value to start with
         */
        public operator fun <T: Units> invoke(
            range   : ClosedRange<Measure<T>>,
            value   : Measure<T> = range.start,
            function: InvertibleFunction = LinearFunction
        ): CircularSlider<Measure<T>> = CircularSlider(
            BasicConfinedValueModel(range, value),
            range.start.units.interpolator,
            function
        )

        /**
         * Creates a CircularSlider with the given model.
         *
         * @param model of the bar
         */
        @JvmName("invokeMeasure")
        public operator fun <T: Units> invoke(
            model: ConfinedValueModel<Measure<T>>,
        ): CircularSlider<Measure<T>> = CircularSlider(
            model,
            model.value.units.interpolator
        )

        // endregion

        // region ================ T ========================

        /**
         * Creates a CircularSlider with a given range and starting value.
         *
         * @param range of the bar
         * @param value to start with
         */
        public operator fun <T> invoke(
            range    : ClosedRange<T>,
            converter: Interpolator<T>,
            value    : T           = range.start,
            function : InvertibleFunction = LinearFunction
        ): CircularSlider<T> where T: Comparable<T> = CircularSlider(
            BasicConfinedValueModel(range, value) as ConfinedValueModel<T>,
            converter,
            function
        )

        /**
         * Creates a CircularSlider with the given model.
         *
         * @param model of the bar
         */
        public operator fun <T> invoke(
            model    : ConfinedValueModel<T>,
            converter: Interpolator<T>,
            function : InvertibleFunction = LinearFunction
        ): CircularSlider<T> where T: Comparable<T> = CircularSlider(
            model,
            converter,
            function
        )

        // endregion

        /**
         * Creates a CircularSlider with the given model.
         *
         * @param model of the bar
         * @param type class type of the slider
         */
        @Suppress("UNUSED_PARAMETER")
        @Deprecated("Use explicit inline versions for numbers instead.")
        public inline operator fun <reified T> invoke(
            model   : ConfinedValueModel<T>,
            type    : KClass<T>,
            function: InvertibleFunction = LinearFunction
        ): CircularSlider<T> where T: Number, T: Comparable<T> = CircularSlider(
            model,
            numberTypeConverter(),
            function
        )
    }
}
