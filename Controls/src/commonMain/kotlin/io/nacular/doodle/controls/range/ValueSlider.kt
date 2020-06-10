package io.nacular.doodle.controls.range

import io.nacular.doodle.accessibility.slider
import io.nacular.doodle.controls.BasicConfinedValueModel
import io.nacular.doodle.controls.ConfinedValueModel
import io.nacular.doodle.core.View
import io.nacular.doodle.utils.size
import kotlin.math.max
import kotlin.math.round

abstract class ValueSlider(model: ConfinedValueModel<Double>): View(accessibilityRole = slider().apply {
    valueMin = model.limits.start.toInt()
    valueMax = model.limits.endInclusive.toInt()
    valueNow = model.value.toInt()
}) {
    constructor(range: ClosedRange<Double> = 0.0 .. 100.0, value: Double = range.start): this(BasicConfinedValueModel(range, value))

    var snapToTicks = false

    var ticks = 0
        set(new) {
            field = max(0, new)

            snapSize = if (field > 0) range.size / field else 0.0
        }

    var model =  model
        set(new) {
            field.valueChanged  -= modelChanged
            field.limitsChanged -= modelLimitsChanged

            field = new.also {
                it.valueChanged  += modelChanged
                it.limitsChanged += modelLimitsChanged
            }
        }

    var value: Double
        get(   ) = model.value
        set(new) {
            model.value = if (snapToTicks && snapSize > 0) round(new/snapSize) * snapSize else new
        }

    var range: ClosedRange<Double>
        get(   ) = model.limits
        set(new) { model.limits = new }


    protected abstract fun changed(old: Double, new: Double)

    private val modelChanged: (ConfinedValueModel<Double>, Double, Double) -> Unit = { _,old,new ->
        (accessibilityRole as? slider)?.let {
            it.valueNow = (((new - range.start) / range.size) * 100).toInt()
        }

        changed(old, new)
    }

    private val modelLimitsChanged: (ConfinedValueModel<Double>, ClosedRange<Double>, ClosedRange<Double>) -> Unit = { _,_,_ ->
        (accessibilityRole as? slider)?.let {
            it.valueMin = range.start.toInt()
            it.valueMax = range.endInclusive.toInt()
        }
    }

    private var snapSize = 0.0

    init {
        model.valueChanged += modelChanged
    }
}