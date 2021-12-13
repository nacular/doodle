package io.nacular.doodle.controls.range

import io.nacular.doodle.controls.BasicConfinedValueModel
import io.nacular.doodle.controls.ConfinedValueModel
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.behavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.PropertyObserversImpl
import kotlin.reflect.KClass

public typealias CircularSlider = CircularSlider2<Double>

public open class CircularSlider2<T>(model: ConfinedValueModel<T>, type: KClass<T>): ValueSlider2<T>(model, type) where T: Number, T: Comparable<T> {

    public constructor(range: ClosedRange<Double> = 0.0 .. 100.0, value: Double = range.start): this(BasicConfinedValueModel(range, value) as ConfinedValueModel<T>, Double::class as KClass<T>)

    @Suppress("PrivatePropertyName")
    private val changed_ by lazy { PropertyObserversImpl<CircularSlider2<T>, T>(this) }

    public val changed: PropertyObservers<CircularSlider2<T>, T> = changed_

    public var behavior: Behavior<CircularSlider2<T>>? by behavior()

    override fun render(canvas: Canvas) {
        behavior?.render(this, canvas)
    }

    override fun contains(point: Point): Boolean = super.contains(point) && behavior?.contains(this, point) ?: true

    override fun changed(old: T, new: T) {
        changed_(old, new)
    }

    public companion object {
        /**
         * Creates a CircularSlider with a given range and starting value.
         *
         * @param range of the bar
         * @param value to start with
         */
        public inline operator fun <reified T> invoke(range: ClosedRange<T>, value: T = range.start): CircularSlider2<T> where T: Number, T: Comparable<T> = CircularSlider2(model = BasicConfinedValueModel(range, value) as ConfinedValueModel<T>, type = T::class)

        /**
         * Creates a CircularSlider with the model.
         *
         * @param model of the bar
         */
        public inline operator fun <reified T> invoke(model: ConfinedValueModel<T>): CircularSlider2<T> where T: Number, T: Comparable<T> = CircularSlider2(model, T::class)
    }
}
