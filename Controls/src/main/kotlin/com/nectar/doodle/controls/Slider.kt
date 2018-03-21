package com.nectar.doodle.controls

import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.theme.Renderer
import com.nectar.doodle.utils.ChangeObservers
import com.nectar.doodle.utils.ChangeObserversImpl
import com.nectar.doodle.utils.Orientation
import com.nectar.doodle.utils.size
import kotlin.math.max
import kotlin.math.round

/**
 * Created by Nicholas Eddy on 2/12/18.
 */
open class Slider(model: ConfinedValueModel<Double>, val orientation: Orientation = Orientation.Horizontal): Gizmo() {
    constructor(range: ClosedRange<Double> = 0.0 .. 100.0, value: Double = range.start, orientation: Orientation = Orientation.Horizontal): this(BasicConfinedValueModel(range, value), orientation)

    var renderer: Renderer<Slider>? = null

    private val onChange: (ConfinedValueModel<Double>) -> Unit = {
        onChanged_.set.forEach { it(this) }
    }

    init {
        model.onChanged += onChange
    }

    var ticks = 0
        set(new) {
            field = max(0, new)

            snapSize = if (field > 0) range.size / field else 0.0
        }

    var snapToTicks = false

    private var snapSize = 0.0

    var model =  model
        set(new) {
            field.onChanged -= onChange

            field = new.also {
                it.onChanged += onChange
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


    override fun render(canvas: Canvas) {
        renderer?.render(this, canvas)
    }

    override fun contains(point: Point) = renderer?.contains(this, point) ?: super.contains(point)

    @Suppress("PrivatePropertyName")
    private val onChanged_ = ChangeObserversImpl<Slider>()

    val onChanged: ChangeObservers<Slider> = onChanged_
}
