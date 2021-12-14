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

public open class CircularSlider<T>(model: ConfinedValueModel<T>, type: KClass<T>): ValueSlider<T>(model, type) where T: Number, T: Comparable<T> {
    @Suppress("PrivatePropertyName")
    private val changed_ by lazy { PropertyObserversImpl<CircularSlider<T>, T>(this) }

    public val changed: PropertyObservers<CircularSlider<T>, T> = changed_

    public var behavior: Behavior<CircularSlider<T>>? by behavior()

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
        public inline operator fun <reified T> invoke(range: ClosedRange<T>, value: T = range.start): CircularSlider<T> where T: Number, T: Comparable<T> = CircularSlider(model = BasicConfinedValueModel(range, value) as ConfinedValueModel<T>, type = T::class)

        /**
         * Creates a CircularSlider with the model.
         *
         * @param model of the bar
         */
        public inline operator fun <reified T> invoke(model: ConfinedValueModel<T>): CircularSlider<T> where T: Number, T: Comparable<T> = CircularSlider(model, T::class)
    }
}
