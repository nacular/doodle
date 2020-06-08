package io.nacular.doodle.controls

import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.PropertyObserversImpl
import io.nacular.doodle.utils.size

/**
 * Created by Nicholas Eddy on 2/12/18.
 */
open class ProgressIndicator(model: ConfinedValueModel<Double>): View() {
    constructor(range: ClosedRange<Double> = 0.0 .. 100.0, value: Double = range.start): this(BasicConfinedValueModel(range, value))

    var behavior: Behavior<ProgressIndicator>? = null
        set(new) {
            if (field == new) { return }

            field?.uninstall(this)
            field = new?.apply { install(this@ProgressIndicator) }
        }

    private val changedHandler: (ConfinedValueModel<Double>, Double, Double) -> Unit = { _,old,new ->
        changed_(old, new)
    }

    init {
        model.valueChanged += changedHandler
    }

    var model =  model
        set(new) {
            field.valueChanged -= changedHandler

            field = new.also {
                it.valueChanged += changedHandler
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
        behavior?.render(this, canvas)
    }

    override fun contains(point: Point) = super.contains(point) && behavior?.contains(this, point) ?: true

    @Suppress("PrivatePropertyName")
    private val changed_ by lazy { PropertyObserversImpl<ProgressIndicator, Double>(this) }

    val changed: PropertyObservers<ProgressIndicator, Double> = changed_
}
