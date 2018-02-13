package com.nectar.doodle.controls

import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.ranges.size
import com.nectar.doodle.theme.Renderer
import com.nectar.doodle.utils.Orientation
import com.nectar.doodle.utils.Orientation.Horizontal

/**
 * Created by Nicholas Eddy on 2/12/18.
 */
class ProgressBar(model: ConfinedValueModel<Double>, val orientation: Orientation = Horizontal): Gizmo() {
    constructor(range: ClosedRange<Double> = 0.0 .. 100.0, value: Double = range.start, orientation: Orientation = Horizontal): this(BasicConfinedValueModel(range, value), orientation)

    var renderer: Renderer<ProgressBar>? = null

    init {
        model.onChanged += this::onChange
    }

    var model =  model
        set(new) {
            field.onChanged -= this::onChange

            field = new.also {
                it.onChanged += this::onChange
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
        renderer?.render(canvas, this)
    }

    @Suppress("PrivatePropertyName")
    private val onChanged_ = ChangeObserversImpl<ProgressBar>()

    val onChanged: ChangeObservers<ProgressBar> = onChanged_

    private fun onChange(@Suppress("UNUSED_PARAMETER") model: ConfinedValueModel<Double>) {
        onChanged_.set.forEach { it(this) }
    }
}
