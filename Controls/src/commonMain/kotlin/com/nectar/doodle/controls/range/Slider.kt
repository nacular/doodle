package com.nectar.doodle.controls.range

import com.nectar.doodle.controls.BasicConfinedValueModel
import com.nectar.doodle.controls.ConfinedValueModel
import com.nectar.doodle.core.Behavior
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.utils.Orientation
import com.nectar.doodle.utils.Orientation.Horizontal
import com.nectar.doodle.utils.PropertyObservers
import com.nectar.doodle.utils.PropertyObserversImpl

/**
 * Created by Nicholas Eddy on 2/12/18.
 */
open class Slider(model: ConfinedValueModel<Double>, val orientation: Orientation = Horizontal): ValueSlider(model) {
    constructor(range: ClosedRange<Double> = 0.0 .. 100.0, value: Double = range.start, orientation: Orientation = Horizontal): this(BasicConfinedValueModel(range, value), orientation)

    @Suppress("PrivatePropertyName")
    private val changed_ by lazy { PropertyObserversImpl<Slider, Double>(this) }

    val changed: PropertyObservers<Slider, Double> = changed_

    var behavior: Behavior<Slider>? = null
        set(new) {
            if (field == new) { return }

            field?.uninstall(this)
            field = new?.apply { install(this@Slider) }
        }

    override fun render(canvas: Canvas) {
        behavior?.render(this, canvas)
    }

    override fun contains(point: Point) = super.contains(point) && behavior?.contains(this, point) ?: true

    override fun changed(old: Double, new: Double) {
        changed_(old, new)
    }
}