package io.nacular.doodle.controls.range

import io.nacular.doodle.controls.BasicConfinedRangeModel
import io.nacular.doodle.controls.ConfinedRangeModel
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.behavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.PropertyObserversImpl
import kotlin.reflect.KClass

public open class CircularRangeSlider<T>(
    model: ConfinedRangeModel<T>,
    type: KClass<T>
): RangeValueSlider<T>(model, type) where T: Number, T: Comparable<T> {
    @Suppress("PrivatePropertyName")
    private val changed_ by lazy { PropertyObserversImpl<CircularRangeSlider<T>, ClosedRange<T>>(this) }

    @Suppress("PrivatePropertyName")
    private val limitsChanged_ by lazy { PropertyObserversImpl<CircularRangeSlider<T>, ClosedRange<T>>(this) }

    public val changed      : PropertyObservers<CircularRangeSlider<T>, ClosedRange<T>> = changed_
    public val limitsChanged: PropertyObservers<CircularRangeSlider<T>, ClosedRange<T>> = limitsChanged_

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

    public companion object {
        /**
         * Creates a CircularSlider with a given range and starting value.
         *
         * @param range of the bar
         * @param value to start with
         */
        public inline operator fun <reified T> invoke(range: ClosedRange<T>, value: ClosedRange<T> = range.start .. range.start): CircularRangeSlider<T> where T: Number, T: Comparable<T> = CircularRangeSlider(model = BasicConfinedRangeModel(range, value) as ConfinedRangeModel<T>, type = T::class)

        /**
         * Creates a CircularSlider with the model.
         *
         * @param model of the bar
         */
        public inline operator fun <reified T> invoke(model: ConfinedRangeModel<T>): CircularRangeSlider<T> where T: Number, T: Comparable<T> = CircularRangeSlider(model, T::class)
    }
}
