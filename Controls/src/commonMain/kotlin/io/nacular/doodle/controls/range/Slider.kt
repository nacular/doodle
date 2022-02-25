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

/**
 * Represents a selection slider that can be [Horizontal] or [Vertical][io.nacular.doodle.utils.Orientation.Vertical].
 *
 * @constructor
 * @param model containing range and value
 * @param orientation of the control
 * @param type class type of the slider
 */
public open class Slider<T>(
                   model      : ConfinedValueModel<T>,
        public val orientation: Orientation = Horizontal,
                   type       : KClass<T>): ValueSlider<T>(model, type) where T: Number, T: Comparable<T>  {

    @Suppress("PrivatePropertyName")
    private val changed_ by lazy { PropertyObserversImpl<Slider<T>, T>(this) }

    @Suppress("PrivatePropertyName")
    private val limitsChanged_ by lazy { PropertyObserversImpl<Slider<T>, ClosedRange<T>>(this) }

    public val changed      : PropertyObservers<Slider<T>, T> = changed_
    public val limitsChanged: PropertyObservers<Slider<T>, ClosedRange<T>> = limitsChanged_

    public var behavior: Behavior<Slider<T>>? by behavior()

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
                orientation: Orientation = Horizontal): Slider<T> where T: Number, T: Comparable<T> = Slider(model = BasicConfinedValueModel(range, value) as ConfinedValueModel<T>, orientation, T::class)

        /**
         * Creates a Slider with the given model.
         *
         * @param model of the bar
         * @param orientation of the control
         */
        public inline operator fun <reified T> invoke(
                model      : ConfinedValueModel<T>,
                orientation: Orientation = Horizontal): Slider<T> where T: Number, T: Comparable<T> = Slider(model, orientation, T::class)
    }
}