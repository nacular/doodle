package com.nectar.doodle.controls

import com.nectar.doodle.accessibility.slider
import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.core.Behavior
import com.nectar.doodle.utils.Orientation
import com.nectar.doodle.utils.Orientation.Horizontal
import com.nectar.doodle.utils.PropertyObservers
import com.nectar.doodle.utils.PropertyObserversImpl
import com.nectar.doodle.utils.size
import kotlin.math.max
import kotlin.math.round

/**
 * Created by Nicholas Eddy on 2/12/18.
 */
open class Slider(model: ConfinedValueModel<Double>, val orientation: Orientation = Horizontal): View(accessibilityRole = slider().apply {
    valueMin = model.limits.start.toInt()
    valueMax = model.limits.endInclusive.toInt()
    valueNow = model.value.toInt()
}) {
    constructor(range: ClosedRange<Double> = 0.0 .. 100.0, value: Double = range.start, orientation: Orientation = Horizontal): this(BasicConfinedValueModel(range, value), orientation)

    var behavior: Behavior<Slider>? = null
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

    @Suppress("PrivatePropertyName")
    private val changed_ by lazy { PropertyObserversImpl<Slider, Double>(this) }

    val changed: PropertyObservers<Slider, Double> = changed_

    private val modelChanged: (ConfinedValueModel<Double>, Double, Double) -> Unit = { _,old,new ->
        (accessibilityRole as? slider)?.let {
            it.valueNow = (((new - range.start) / range.size) * 100).toInt()
        }

        changed_(old, new)
    }

    private val modelLimitsChanged: (ConfinedValueModel<Double>, ClosedRange<Double>, ClosedRange<Double>) -> Unit = { _,old,new ->
        (accessibilityRole as? slider)?.let {
            it.valueMin = range.start.toInt()
            it.valueMax = range.endInclusive.toInt()
        }

    }

    private var snapSize = 0.0

    init {
        model.valueChanged += modelChanged
    }

    override fun render(canvas: Canvas) {
        behavior?.render(this, canvas)
    }

    override fun contains(point: Point) = super.contains(point) && behavior?.contains(this, point) ?: true
}