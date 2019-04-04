package com.nectar.doodle.controls

import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.theme.Behavior
import com.nectar.doodle.utils.Orientation
import com.nectar.doodle.utils.PropertyObservers
import com.nectar.doodle.utils.PropertyObserversImpl
import com.nectar.doodle.utils.size
import kotlin.math.max
import kotlin.math.round

/**
 * Created by Nicholas Eddy on 2/12/18.
 */
open class Slider(model: ConfinedValueModel<Double>, val orientation: Orientation = Orientation.Horizontal): View() {
    constructor(range: ClosedRange<Double> = 0.0 .. 100.0, value: Double = range.start, orientation: Orientation = Orientation.Horizontal): this(BasicConfinedValueModel(range, value), orientation)

    var renderer: Behavior<Slider>? = null
        set(new) {
            if (field == new) { return }

            field?.uninstall(this)
            field = new?.apply { install(this@Slider) }
        }

    var snapToTicks = false

    var ticks = 0
        set(new) {
            field = max(0, new)

            snapSize = if (field > 0) range.size / field else 0.0
        }

    var model =  model
        set(new) {
            field.valueChanged -= modelChanged

            field = new.also {
                it.valueChanged += modelChanged
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

    @Suppress("PrivatePropertyName")
    private val changed_ by lazy { PropertyObserversImpl<Slider, Double>(this) }

    val changed: PropertyObservers<Slider, Double> = changed_

    private val modelChanged: (ConfinedValueModel<Double>, Double, Double) -> Unit = { _,old,new ->
        changed_(old, new)
    }

    private var snapSize = 0.0

    init {
        model.valueChanged += modelChanged
    }

    override fun render(canvas: Canvas) {
        renderer?.render(this, canvas)
    }

    override fun contains(point: Point) = renderer?.contains(this, point) ?: super.contains(point)
}