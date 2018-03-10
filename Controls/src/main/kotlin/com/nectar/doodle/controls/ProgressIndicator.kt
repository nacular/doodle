package com.nectar.doodle.controls

import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.theme.Renderer
import com.nectar.doodle.utils.size

/**
 * Created by Nicholas Eddy on 2/12/18.
 */
open class ProgressIndicator(model: ConfinedValueModel<Double>): Gizmo() {
    constructor(range: ClosedRange<Double> = 0.0 .. 100.0, value: Double = range.start): this(BasicConfinedValueModel(range, value))

    var renderer: Renderer<ProgressIndicator>? = null

    private val onChange: (ConfinedValueModel<Double>) -> Unit = {
        onChanged_.set.forEach { it(this) }
    }

    init {
        model.onChanged += onChange
    }

    var model =  model
        set(new) {
            field.onChanged -= onChange

            field = new.also {
                it.onChanged += onChange
            }
        }

    var progress: Double
        get() = (model.value - model.limits.start) / (model.limits.size)
        set(new) {
            model.value = model.limits.start + new * (model.limits.size)
        }

    var value: Double
        get(   ) = model.value
        set(new) { model.value = new }

    var range: ClosedRange<Double>
        get(   ) = model.limits
        set(new) { model.limits = new }

    override var focusable = false

    override fun render(canvas: Canvas) {
        renderer?.render(this, canvas)
    }

    override fun contains(point: Point) = renderer?.contains(this, point) ?: super.contains(point)

    @Suppress("PrivatePropertyName")
    private val onChanged_ = ChangeObserversImpl<ProgressIndicator>()

    val onChanged: ChangeObservers<ProgressIndicator> = onChanged_
}
