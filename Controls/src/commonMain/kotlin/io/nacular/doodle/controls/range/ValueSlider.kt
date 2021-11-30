package io.nacular.doodle.controls.range

import io.nacular.doodle.accessibility.SliderRole
import io.nacular.doodle.controls.BasicConfinedValueModel
import io.nacular.doodle.controls.ConfinedValueModel
import io.nacular.doodle.controls.bind
import io.nacular.doodle.controls.binding
import io.nacular.doodle.core.View
import kotlin.math.max
import kotlin.math.round
import kotlin.reflect.KClass

@Deprecated("Will be replaced soon with typed version soon.")
public typealias ValueSlider = ValueSlider2<Double>

public abstract class ValueSlider2<T> private constructor(
        model: ConfinedValueModel<T>,
        protected val role: SliderRole = SliderRole()): View(role) where T: Number, T: Comparable<T> {
    public constructor(model: ConfinedValueModel<T>): this(model, SliderRole())
    public constructor(range: ClosedRange<T>, value: T = range.start): this(BasicConfinedValueModel(range, value) as ConfinedValueModel<T>)

    private var roleBinding by binding(role.bind(model))

    public var snapToTicks: Boolean = false

    public var ticks: Int = 0
        set(new) {
            field = max(0, new)

            snapSize = if (field > 0) range.size.toDouble() / field else 0.0
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
            model.value = if (snapToTicks && snapSize > 0) (round(new.toDouble() / snapSize) * snapSize).cast(value::class) else new
        }

    public var range: ClosedRange<T>
        get(   ) = model.limits
        set(new) { model.limits = new }

    protected abstract fun changed(old: T, new: T)

    private val modelChanged: (ConfinedValueModel<T>, T, T) -> Unit = { _,old,new ->
        changed(old, new)
    }

    private var snapSize = 0.0

    init {
        model.valueChanged += modelChanged
    }
}

@Suppress("UNCHECKED_CAST")
internal val <T> ClosedRange<T>.size: T where T: Number, T: Comparable<T> get() = (endInclusive.toDouble() - start.toDouble() + 1) as T

@Suppress("UNCHECKED_CAST")
internal fun <T> Number.cast(type: KClass<*>): T {
    return when (type) {
        Int::class    -> this.toInt   () as T
        Float::class  -> this.toFloat () as T
        Double::class -> this.toDouble() as T
        Long::class   -> this.toLong  () as T
        Char::class   -> this.toChar  () as T
        Short::class  -> this.toShort () as T
        Byte::class   -> this.toByte  () as T
        else          -> this            as T
    }
}