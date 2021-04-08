package io.nacular.doodle.controls.range

import io.nacular.doodle.accessibility.slider
import io.nacular.doodle.controls.BasicConfinedValueModel
import io.nacular.doodle.controls.ConfinedValueModel
import io.nacular.doodle.controls.bind
import io.nacular.doodle.controls.binding
import io.nacular.doodle.core.View
import io.nacular.doodle.utils.size
import kotlin.math.max
import kotlin.math.round

public abstract class ValueSlider private constructor(model: ConfinedValueModel<Double>, protected val role: slider = slider()): View(role) {
    public constructor(model: ConfinedValueModel<Double>): this(model, slider())
    public constructor(range: ClosedRange<Double> = 0.0 .. 100.0, value: Double = range.start): this(BasicConfinedValueModel(range, value))

    private var roleBinding by binding(role.bind(model))

    public var snapToTicks: Boolean = false

    public var ticks: Int = 0
        set(new) {
            field = max(0, new)

            snapSize = if (field > 0) range.size / field else 0.0
        }

    public var model: ConfinedValueModel<Double> = model
        set(new) {
            field.valueChanged -= modelChanged

            field = new.also {
                it.valueChanged += modelChanged
                roleBinding = role.bind(it)
            }
        }

    public var value: Double
        get(   ) = model.value
        set(new) {
            model.value = if (snapToTicks && snapSize > 0) round(new/snapSize) * snapSize else new
        }

    public var range: ClosedRange<Double>
        get(   ) = model.limits
        set(new) { model.limits = new }


    protected abstract fun changed(old: Double, new: Double)

    private val modelChanged: (ConfinedValueModel<Double>, Double, Double) -> Unit = { _,old,new ->
        changed(old, new)
    }

    private var snapSize = 0.0

    init {
        model.valueChanged += modelChanged
    }
}