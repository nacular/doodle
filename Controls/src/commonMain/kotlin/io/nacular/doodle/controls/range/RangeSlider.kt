package io.nacular.doodle.controls.range

import io.nacular.doodle.controls.BasicConfinedRangeModel
import io.nacular.doodle.controls.ConfinedRangeModel
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.behavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.utils.Orientation
import io.nacular.doodle.utils.Orientation.Horizontal
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.PropertyObserversImpl
import kotlin.reflect.KClass

/**
 * Represents a range selection slider that can be [Horizontal] or [Vertical][io.nacular.doodle.utils.Orientation.Vertical].
 *
 * @constructor
 * @param model containing range and value
 * @param orientation of the control
 */
public open class RangeSlider<T>(
                   model      : ConfinedRangeModel<T>,
        public val orientation: Orientation = Horizontal,
                   type       : KClass<T>): RangeValueSlider<T>(model, type) where T: Number, T: Comparable<T>  {

    @Suppress("PrivatePropertyName")
    private val changed_ by lazy { PropertyObserversImpl<RangeSlider<T>, ClosedRange<T>>(this) }

    @Suppress("PrivatePropertyName")
    private val limitsChanged_ by lazy { PropertyObserversImpl<RangeSlider<T>, ClosedRange<T>>(this) }

    public val changed: PropertyObservers<RangeSlider<T>, ClosedRange<T>> = changed_

    public val limitsChanged: PropertyObservers<RangeSlider<T>, ClosedRange<T>> = limitsChanged_

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
        /**
         * Creates a Slider with a given range and starting value.
         *
         * @param limits of the bar
         * @param value to start with
         * @param orientation of the control
         */
        public inline operator fun <reified T> invoke(
                limits     : ClosedRange<T>,
                value      : ClosedRange<T> = limits.start .. limits.start,
                orientation: Orientation = Horizontal): RangeSlider<T> where T: Number, T: Comparable<T> = RangeSlider(model = BasicConfinedRangeModel(limits, value) as ConfinedRangeModel<T>, orientation, T::class)

        /**
         * Creates a Slider with the given model.
         *
         * @param model of the bar
         * @param orientation of the control
         */
        public inline operator fun <reified T> invoke(
                model      : ConfinedRangeModel<T>,
                orientation: Orientation = Horizontal): RangeSlider<T> where T: Number, T: Comparable<T> = RangeSlider(model, orientation, T::class)
    }
}