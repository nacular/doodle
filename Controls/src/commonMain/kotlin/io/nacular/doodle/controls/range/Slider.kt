package io.nacular.doodle.controls.range

import io.nacular.doodle.controls.BasicConfinedValueModel
import io.nacular.doodle.controls.ConfinedValueModel
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.behavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.utils.Orientation
import io.nacular.doodle.utils.Orientation.Horizontal
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.PropertyObserversImpl
import kotlin.reflect.KClass

public typealias Slider = Slider2<Double>

/**
 * Represents a selection slider that can be [Horizontal] or [Vertical][io.nacular.doodle.utils.Orientation.Vertical].
 *
 * @constructor
 * @param model containing range and value
 * @param orientation of the control
 */
public open class Slider2<T>(
        model: ConfinedValueModel<T>,
        public val orientation: Orientation = Horizontal,
        type: KClass<T>): ValueSlider2<T>(model, type) where T: Number, T: Comparable<T>  {

    public constructor(
            range: ClosedRange<Double> = 0.0..100.0,
            value: Double = range.start,
            orientation: Orientation = Horizontal): this(BasicConfinedValueModel(range, value) as ConfinedValueModel<T>, orientation, Double::class as KClass<T>)

    @Suppress("PrivatePropertyName")
    private val changed_ by lazy { PropertyObserversImpl<Slider2<T>, T>(this) }

    public val changed: PropertyObservers<Slider2<T>, T> = changed_

    public var behavior: Behavior<Slider2<T>>? by behavior()

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

    public companion object {
        /**
         * Creates a Slider with a given range and starting value.
         *
         * @param range of the bar
         * @param value to start with
         * @param orientation of the control
         */
        public inline operator fun <reified T> invoke(
                range      : ClosedRange<T>,
                value      : T = range.start,
                orientation: Orientation = Horizontal): Slider2<T> where T: Number, T: Comparable<T> = Slider2(model = BasicConfinedValueModel(range, value) as ConfinedValueModel<T>, orientation, T::class)

        /**
         * Creates a Slider with the given model.
         *
         * @param model of the bar
         * @param orientation of the control
         */
        public inline operator fun <reified T> invoke(
                model      : ConfinedValueModel<T>,
                orientation: Orientation = Horizontal): Slider2<T> where T: Number, T: Comparable<T> = Slider2(model, orientation, T::class)
    }
}