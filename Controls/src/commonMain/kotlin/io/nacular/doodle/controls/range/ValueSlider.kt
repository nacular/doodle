package io.nacular.doodle.controls.range

import io.nacular.doodle.accessibility.SliderRole
import io.nacular.doodle.controls.BasicConfinedValueModel
import io.nacular.doodle.controls.ConfinedValueModel
import io.nacular.doodle.controls.bind
import io.nacular.doodle.controls.binding
import io.nacular.doodle.core.View
import io.nacular.doodle.utils.observable
import kotlin.math.max
import kotlin.math.round
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.reflect.KClass

public abstract class ValueSlider<T> internal constructor(
                     model: ConfinedValueModel<T>,
        protected val role: SliderRole = SliderRole(),
        private val type: KClass<T>): View(role) where T: Number, T: Comparable<T> {

    protected constructor(model: ConfinedValueModel<T>, type: KClass<T>): this(model, role = SliderRole(), type)

    public constructor(range: ClosedRange<T>, value: T = range.start, type: KClass<T>): this(BasicConfinedValueModel(range, value) as ConfinedValueModel<T>, type)

    private var roleBinding by binding(role.bind(model))

    public var snapToTicks: Boolean by observable(false) { _,new ->
        if (new) {
            value = value // update value to ensure snapped to the closest tick
        }

        ticksChanged()
    }

    public var ticks: Int = 0
        set(new) {
            field = max(0, new)

            snapSize = if (field > 1) range.size.toDouble() / (field - 1) else null
        }

    public var snapSize: Double? = null
        private set(new) {
            if (new == field) return

            field = new

            if (snapToTicks) {
                value = value // update value to ensure snapped to the closest tick
            }

            ticksChanged()
        }

    public var model: ConfinedValueModel<T> = model
        set(new) {
            field.valueChanged -= modelChanged

            field = new.also {
                it.valueChanged += modelChanged
                roleBinding = role.bind(it)
            }
        }

    public var value: T
        get(   ) = model.value
        set(new) {
            val snapSize_ = snapSize

            model.value = when {
                snapToTicks && snapSize_ != null -> cast((range.start.toDouble() + round((new.toDouble() - range.start.toDouble()) / snapSize_) * snapSize_))
                else                             -> new
            }
        }

    public var range: ClosedRange<T>
        get(   ) = model.limits
        set(new) { model.limits = new }

    internal fun set(to: Double) {
        value = cast(to)
    }

    internal fun adjust(by: Double) {
        value = cast(value.toDouble() + by)
    }

    internal fun set(range: ClosedRange<Double>) {
        model.limits = cast(range.start) .. cast(range.endInclusive)
    }

    protected abstract fun changed      (old: T,              new: T             )
    protected abstract fun limitsChanged(old: ClosedRange<T>, new: ClosedRange<T>)
    protected abstract fun ticksChanged ()

    private val modelChanged: (ConfinedValueModel<T>, T, T) -> Unit = { _,old,new ->
        changed(old, new)
    }

    private val limitsChanged: (ConfinedValueModel<T>, ClosedRange<T>, ClosedRange<T>) -> Unit = { _,old,new ->
        limitsChanged(old, new)
    }

    private fun cast(value: Double): T {
        return when (type) {
            Int::class    -> value.roundToInt           () as T
            Float::class  -> value.toFloat              () as T
            Double::class -> value                         as T
            Long::class   -> value.roundToLong          () as T
            Char::class   -> value.roundToInt().toChar  () as T
            Short::class  -> value.roundToInt().toShort () as T
            Byte::class   -> value.roundToInt().toByte  () as T
            else          -> value                         as T
        }
    }

    init {
        model.valueChanged  += modelChanged
        model.limitsChanged += limitsChanged
    }
}

@Suppress("UNCHECKED_CAST")
internal val <T> ClosedRange<T>.size: T where T: Number, T: Comparable<T> get() = (endInclusive.toDouble() - start.toDouble()) as T