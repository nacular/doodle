package io.nacular.doodle.controls

import io.nacular.doodle.accessibility.RangeRole
import io.nacular.doodle.accessibility.ProgressBarRole
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.core.View
import io.nacular.doodle.core.behavior
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.PropertyObserversImpl
import io.nacular.doodle.utils.size

/**
 * Control that shows progress of some activity. That progress comes via the [model].
 * Changes to the [model] will update the control.
 *
 * @constructor
 * @param model containing progress range and current value
 */
public abstract class ProgressIndicator(model: ConfinedValueModel<Double>, private val role: RangeRole = ProgressBarRole()): View(role) {
    public constructor(range: ClosedRange<Double> = 0.0 .. 100.0, value: Double = range.start): this(BasicConfinedValueModel(range, value))

    private var roleBinding by binding(role.bind(model))

    private val changedHandler: (ConfinedValueModel<Double>, Double, Double) -> Unit = { _,old,new ->
        changed_(old, new)
    }

    private val limitChangedHandler: (ConfinedValueModel<Double>, ClosedRange<Double>, ClosedRange<Double>) -> Unit = { _,old,new ->
        limitsChanged_(old, new)
    }

    init {
        model.valueChanged  += changedHandler
        model.limitsChanged += limitChangedHandler
    }

    /**
     * Defines the control's look and feel
     */
    public var behavior: Behavior<ProgressIndicator>? by behavior()

    /**
     * Underlying model representing the current progress within a range.
     */
    public var model: ConfinedValueModel<Double> = model
        set(new) {
            field.valueChanged  -= changedHandler
            field.limitsChanged -= limitChangedHandler

            field = new.also {
                it.valueChanged  += changedHandler
                it.limitsChanged += limitChangedHandler
                roleBinding = role.bind(it)
            }
        }

    /**
     * Delegates to [behavior]
     */
    override fun render(canvas: Canvas) {
        behavior?.render(this, canvas)
    }

    /**
     * Delegates to [behavior]
     */
    override fun contains(point: Point): Boolean = super.contains(point) && behavior?.contains(this, point) ?: true

    /**
     * Value from 0 to 1 representing the progress from the [model]'s start to end
     */
    public var progress: Double
        get() = (model.value - model.limits.start) / (model.limits.size)
        set(new) {
            model.value = model.limits.start + new * (model.limits.size)
        }

    /**
     * Current progress value derived from [model]
     */
    public var value: Double
        get(   ) = model.value
        set(new) { model.value = new }

    /**
     * Returns the [model]'s range
     */
    public var range: ClosedRange<Double>
        get(   ) = model.limits
        set(new) { model.limits = new }

    override var focusable: Boolean = false

    @Suppress("PrivatePropertyName")
    private val changed_ by lazy { PropertyObserversImpl<ProgressIndicator, Double>(this) }

    @Suppress("PrivatePropertyName")
    private val limitsChanged_ by lazy { PropertyObserversImpl<ProgressIndicator, ClosedRange<Double>>(this) }

    public val changed: PropertyObservers<ProgressIndicator, Double> = changed_
    public val limitsChanged: PropertyObservers<ProgressIndicator, ClosedRange<Double>> = limitsChanged_
}
