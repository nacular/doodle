package io.nacular.doodle.controls.range

import io.nacular.doodle.controls.BasicConfinedValueModel
import io.nacular.doodle.controls.ByteTypeConverter
import io.nacular.doodle.controls.ConfinedValueModel
import io.nacular.doodle.controls.DoubleTypeConverter
import io.nacular.doodle.controls.FloatTypeConverter
import io.nacular.doodle.controls.IntTypeConverter
import io.nacular.doodle.controls.LongTypeConverter
import io.nacular.doodle.controls.ShortTypeConverter
import io.nacular.doodle.controls.theme.range.SliderBehavior
import io.nacular.doodle.core.behavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
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
 * Represents a selection slider that can be [Horizontal] or [Vertical][io.nacular.doodle.utils.Orientation.Vertical].
 *
 * @constructor
 * @param model containing range and value
 * @param orientation of the control
 * @param function used to map between the slider's input and output.
 * @param interpolator used in mapping between [T] and the slider's domain: [0-1]
 */
public open class Slider<T>(
               model       : ConfinedValueModel<T>,
    public val orientation : Orientation        = Horizontal,
               function    : InvertibleFunction = LinearFunction,
               interpolator: Interpolator<T>
): ValueSlider<T>(model, interpolator, function) where T: Comparable<T>  {

    @Suppress("PrivatePropertyName")
    private val changed_ = PropertyObserversImpl<Slider<T>, T>(this)

    @Suppress("PrivatePropertyName")
    private val limitsChanged_ = PropertyObserversImpl<Slider<T>, ClosedRange<T>>(this)

    /**
     * Notifies of changes to [value].
     */
    public val changed: PropertyObservers<Slider<T>, T> = changed_

    /**
     * Notifies of changes to [range].
     */
    public val limitsChanged: PropertyObservers<Slider<T>, ClosedRange<T>> = limitsChanged_

    /**
     * The bounding [Rectangle] of the handle used to adjust the slider.
     */
    public val handleRectangle: Rectangle get() = behavior?.handleBounds(this) ?: bounds.atOrigin

    /**
     * Behavior that controls rendering and other interactions.
     */
    public var behavior: SliderBehavior<T>? by behavior()

    init {
        role.orientation = orientation
    }

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
         * Creates a Slider with a given range and starting value.
         *
         * @param range of the bar
         * @param value to start with
         * @param orientation of the control
         */
        public operator fun invoke(
            range      : ClosedRange<Int>,
            value      : Int              = range.start,
            orientation: Orientation      = Horizontal,
            function   : InvertibleFunction = LinearFunction
        ): Slider<Int> = invoke(BasicConfinedValueModel(range, value), orientation, function)

        /**
         * Creates a Slider with the given model.
         *
         * @param model of the bar
         * @param orientation of the control
         */
        public operator fun invoke(
            model      : ConfinedValueModel<Int>,
            orientation: Orientation = Horizontal,
            function   : InvertibleFunction = LinearFunction
        ): Slider<Int> = Slider(
            model,
            orientation,
            function,
            IntTypeConverter
        )

        /**
         * Creates a Slider with a given range and starting value.
         *
         * @param range of the bar
         * @param value to start with
         * @param orientation of the control
         */
        @JvmName("invokeFloat")
        public operator fun invoke(
            range      : ClosedRange<Float>,
            value      : Float              = range.start,
            orientation: Orientation        = Horizontal,
            function   : InvertibleFunction = LinearFunction
        ): Slider<Float> = invoke(BasicConfinedValueModel(range, value), orientation, function)

        /**
         * Creates a Slider with the given model.
         *
         * @param model of the bar
         * @param orientation of the control
         */
        @JvmName("invokeFloat")
        public operator fun invoke(
            model      : ConfinedValueModel<Float>,
            orientation: Orientation        = Horizontal,
            function   : InvertibleFunction = LinearFunction
        ): Slider<Float> = Slider(
            model,
            orientation,
            function,
            FloatTypeConverter
        )

        /**
         * Creates a Slider with a given range and starting value.
         *
         * @param range of the bar
         * @param value to start with
         * @param orientation of the control
         */
        @JvmName("invokeDouble")
        public operator fun invoke(
            range      : ClosedRange<Double>,
            value      : Double             = range.start,
            orientation: Orientation        = Horizontal,
            function   : InvertibleFunction = LinearFunction
        ): Slider<Double> = invoke(BasicConfinedValueModel(range, value), orientation, function)

        /**
         * Creates a Slider with the given model.
         *
         * @param model of the bar
         * @param orientation of the control
         */
        @JvmName("invokeDouble")
        public operator fun invoke(
            model      : ConfinedValueModel<Double>,
            orientation: Orientation        = Horizontal,
            function   : InvertibleFunction = LinearFunction
        ): Slider<Double> = Slider(
            model,
            orientation,
            function,
            DoubleTypeConverter
        )

        /**
         * Creates a Slider with a given range and starting value.
         *
         * @param range of the bar
         * @param value to start with
         * @param orientation of the control
         */
        @JvmName("invokeLong")
        public operator fun invoke(
            range      : ClosedRange<Long>,
            value      : Long               = range.start,
            orientation: Orientation        = Horizontal,
            function   : InvertibleFunction = LinearFunction
        ): Slider<Long> = invoke(BasicConfinedValueModel(range, value), orientation, function)

        /**
         * Creates a Slider with the given model.
         *
         * @param model of the bar
         * @param orientation of the control
         */
        @JvmName("invokeLong")
        public operator fun invoke(
            model      : ConfinedValueModel<Long>,
            orientation: Orientation        = Horizontal,
            function   : InvertibleFunction = LinearFunction
        ): Slider<Long> = Slider(
            model,
            orientation,
            function,
            LongTypeConverter
        )

        /**
         * Creates a Slider with a given range and starting value.
         *
         * @param range of the bar
         * @param value to start with
         * @param orientation of the control
         */
        @JvmName("invokeShort")
        public operator fun invoke(
            range      : ClosedRange<Short>,
            value      : Short              = range.start,
            orientation: Orientation        = Horizontal,
            function   : InvertibleFunction = LinearFunction
        ): Slider<Short> = invoke(BasicConfinedValueModel(range, value), orientation, function)

        /**
         * Creates a Slider with the given model.
         *
         * @param model of the bar
         * @param orientation of the control
         */
        @JvmName("invokeShort")
        public operator fun invoke(
            model      : ConfinedValueModel<Short>,
            orientation: Orientation        = Horizontal,
            function   : InvertibleFunction = LinearFunction
        ): Slider<Short> = Slider(
            model,
            orientation,
            function,
            ShortTypeConverter
        )

        /**
         * Creates a Slider with a given range and starting value.
         *
         * @param range of the bar
         * @param value to start with
         * @param orientation of the control
         */
        @JvmName("invokeByte")
        public operator fun invoke(
            range      : ClosedRange<Byte>,
            value      : Byte               = range.start,
            orientation: Orientation        = Horizontal,
            function   : InvertibleFunction = LinearFunction
        ): Slider<Byte> = invoke(BasicConfinedValueModel(range, value), orientation, function)

        /**
         * Creates a Slider with the given model.
         *
         * @param model of the bar
         * @param orientation of the control
         */
        @JvmName("invokeByte")
        public operator fun invoke(
            model      : ConfinedValueModel<Byte>,
            orientation: Orientation        = Horizontal,
            function   : InvertibleFunction = LinearFunction
        ): Slider<Byte> = Slider(
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
         * @param range of the bar
         * @param value to start with
         * @param orientation of the control
         */
        public operator fun invoke(
            range      : CharRange,
            value      : Char               = range.first,
            orientation: Orientation        = Horizontal,
            function   : InvertibleFunction = LinearFunction
        ): Slider<Char> = invoke(BasicConfinedValueModel(range, value), orientation, function)

        /**
         * Creates a Slider with the given model.
         *
         * @param model of the bar
         * @param orientation of the control
         */
        @JvmName("invokeChar")
        public operator fun invoke(
            model      : ConfinedValueModel<Char>,
            orientation: Orientation        = Horizontal,
            function   : InvertibleFunction = LinearFunction
        ): Slider<Char> = Slider(
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
         * @param range of the bar
         * @param value to start with
         * @param orientation of the control
         */
        public operator fun <T: Units> invoke(
            range      : ClosedRange<Measure<T>>,
            value      : Measure<T>         = range.start,
            orientation: Orientation        = Horizontal,
            function   : InvertibleFunction = LinearFunction
        ): Slider<Measure<T>> = invoke(BasicConfinedValueModel(range, value), orientation, function)

        /**
         * Creates a Slider with the given model.
         *
         * @param model of the bar
         * @param orientation of the control
         */
        @JvmName("invokeMeasure")
        public operator fun <T: Units> invoke(
            model      : ConfinedValueModel<Measure<T>>,
            orientation: Orientation        = Horizontal,
            function   : InvertibleFunction = LinearFunction
        ): Slider<Measure<T>> = Slider(
            model,
            orientation,
            function,
            model.value.units.interpolator
        )

        // endregion

        // region ================ T ========================

        /**
         * Creates a Slider with a given range and starting value.
         *
         * @param range of the bar
         * @param value to start with
         * @param orientation of the control
         */
        public operator fun <T> invoke(
            range      : ClosedRange<T>,
            converter  : Interpolator<T>,
            value      : T                  = range.start,
            orientation: Orientation        = Horizontal,
            function   : InvertibleFunction = LinearFunction
        ): Slider<T> where T: Comparable<T> = Slider(
            BasicConfinedValueModel(range, value) as ConfinedValueModel<T>,
            orientation,
            function,
            converter
        )

        /**
         * Creates a Slider with the given model.
         *
         * @param model of the bar
         * @param orientation of the control
         */
        public operator fun <T> invoke(
            model      : ConfinedValueModel<T>,
            converter  : Interpolator<T>,
            orientation: Orientation        = Horizontal,
            function   : InvertibleFunction = LinearFunction
        ): Slider<T> where T: Comparable<T> = Slider(
            model,
            orientation,
            function,
            converter
        )

        // endregion
    }
}