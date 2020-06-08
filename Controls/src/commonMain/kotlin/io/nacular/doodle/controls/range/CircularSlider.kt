package io.nacular.doodle.controls.range

import io.nacular.doodle.controls.BasicConfinedValueModel
import io.nacular.doodle.controls.ConfinedValueModel
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.PropertyObserversImpl

open class CircularSlider(model: ConfinedValueModel<Double>): ValueSlider(model) {
    constructor(range: ClosedRange<Double> = 0.0 .. 100.0, value: Double = range.start): this(BasicConfinedValueModel(range, value))

    @Suppress("PrivatePropertyName")
    private val changed_ by lazy { PropertyObserversImpl<CircularSlider, Double>(this) }

    val changed: PropertyObservers<CircularSlider, Double> = changed_

    var behavior: Behavior<CircularSlider>? = null
        set(new) {
            if (field == new) { return }

            field?.uninstall(this)
            field = new?.apply { install(this@CircularSlider) }
        }

    override fun render(canvas: Canvas) {
        behavior?.render(this, canvas)
    }

    override fun contains(point: Point) = super.contains(point) && behavior?.contains(this, point) ?: true

    override fun changed(old: Double, new: Double) {
        changed_(old, new)
    }
}
