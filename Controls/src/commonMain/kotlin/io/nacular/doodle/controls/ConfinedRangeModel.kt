package io.nacular.doodle.controls

import io.nacular.doodle.accessibility.RangeRole
import io.nacular.doodle.utils.Interpolator
import io.nacular.doodle.utils.PropertyObserver
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.PropertyObserversImpl
import io.nacular.doodle.utils.intersect
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.reflect.KClass

/**
 * Created by Nicholas Eddy on 2/12/18.
 */

public interface ConfinedRangeModel<T: Comparable<T>> {
    public var range : ClosedRange<T>
    public var limits: ClosedRange<T>

    public val atLowerLimit: Boolean get() = range.start        == limits.start
    public val atUpperLimit: Boolean get() = range.endInclusive == limits.endInclusive

    public val rangeChanged : PropertyObservers<ConfinedRangeModel<T>, ClosedRange<T>>
    public val limitsChanged: PropertyObservers<ConfinedRangeModel<T>, ClosedRange<T>>
}

public interface ConfinedValueModel<T: Comparable<T>> {
    public var value : T
    public var limits: ClosedRange<T>

    public val atLowerLimit: Boolean get() = value == limits.start
    public val atUpperLimit: Boolean get() = value == limits.endInclusive

    public val valueChanged : PropertyObservers<ConfinedValueModel<T>, T>
    public val limitsChanged: PropertyObservers<ConfinedValueModel<T>, ClosedRange<T>>
}

@Suppress("PrivatePropertyName")
public class BasicConfinedRangeModel<T: Comparable<T>>(limit: ClosedRange<T>, range: ClosedRange<T> = limit): ConfinedRangeModel<T> {
    override var range: ClosedRange<T> = range; set(new) {
        val old = field

            field = minOf(limits.endInclusive, maxOf(new.start, limits.start)) ..maxOf(limits.start, minOf(new.endInclusive, limits.endInclusive))

        if (old != field) {
            rangeChanged_(old, field)
        }
    }

    override var limits: ClosedRange<T> = limit; set(new) {
        val old = field
        field = new

        if (old != field) {
            (range intersect field).also {
                if (it != range) {
                    range = it

                    // onChanged will be fired
                } else {
                    limitsChanged_(old, new)
                }
            }
        }
    }

    private val rangeChanged_  = PropertyObserversImpl<ConfinedRangeModel<T>, ClosedRange<T>>(this)
    private val limitsChanged_ = PropertyObserversImpl<ConfinedRangeModel<T>, ClosedRange<T>>(this)

    override val rangeChanged : PropertyObservers<ConfinedRangeModel<T>, ClosedRange<T>> = rangeChanged_
    override val limitsChanged: PropertyObservers<ConfinedRangeModel<T>, ClosedRange<T>> = limitsChanged_

    init {
        this.limits = limit
        this.range  = range
    }
}

@Suppress("PrivatePropertyName")
public class BasicConfinedValueModel<T: Comparable<T>>(limit: ClosedRange<T>, value: T = limit.start): ConfinedValueModel<T> {

    private val valueChanged_  = PropertyObserversImpl<ConfinedValueModel<T>, T>             (this)
    private val limitsChanged_ = PropertyObserversImpl<ConfinedValueModel<T>, ClosedRange<T>>(this)

    override val valueChanged : PropertyObservers<ConfinedValueModel<T>, T>              = valueChanged_
    override val limitsChanged: PropertyObservers<ConfinedValueModel<T>, ClosedRange<T>> = limitsChanged_

    private val delegate = BasicConfinedRangeModel(limit, value .. value).also {
        it.rangeChanged  += { _,old,new -> valueChanged_ (old.start, new.start) }
        it.limitsChanged += { _,old,new -> limitsChanged_(old,       new      ) }
    }

    override var value: T
        get(   ) = delegate.range.start
        set(new) { delegate.range = new .. new }
    override var limits: ClosedRange<T>
        get(   ) = delegate.limits
        set(new) { delegate.limits = new }
}

/**
 * Creates a binding between the role and model, keeping the role synchronized with the given model
 */
public fun <T> RangeRole.bind(model: ConfinedValueModel<T>, interpolator: Interpolator<T>, labeler: ((T) -> String)? = null): Binding where T: Comparable<T> {
    val convert = { value: T ->
        interpolator.accessibleNumericValue(model.limits.start, model.limits.endInclusive, value)
    }

    min   = convert(model.limits.start       )
    max   = convert(model.limits.endInclusive)
    value = convert(model.value              )

    return object: Binding {
        val limitsChanged: PropertyObserver<ConfinedValueModel<T>, ClosedRange<T>> = { _,_,new ->
            min = convert(new.start       )
            max = convert(new.endInclusive)
        }

        val valueChanged: PropertyObserver<ConfinedValueModel<T>, T> = { _,_,new ->
            value = convert(new)

            labeler?.let { valueText = it(new) }
        }

        init {
            model.valueChanged  += valueChanged
            model.limitsChanged += limitsChanged
        }

        override fun unbind() {
            model.valueChanged  -= valueChanged
            model.limitsChanged -= limitsChanged
        }
    }
}

internal abstract class NumberTypeConverter<T: Number>: Interpolator<T> {
    override fun accessibleNumericValue(start: T, end: T, value: T): Double = value.toDouble()
}

internal object IntTypeConverter: NumberTypeConverter<Int>() {
    override fun lerp  (start: Int, end: Int, progress: Float) = io.nacular.doodle.utils.lerp(start.toDouble(), end.toDouble(), progress).roundToInt()
    override fun progress(start: Int, end: Int, value   : Int  ) = ((value - start).toDouble() / (end - start)).toFloat()
}

internal object FloatTypeConverter: NumberTypeConverter<Float>() {
    override fun lerp    (start: Float, end: Float, progress: Float) = io.nacular.doodle.utils.lerp(start, end, progress)
    override fun progress(start: Float, end: Float, value   : Float) = ((value - start).toDouble() / (end - start)).toFloat()
}

internal object DoubleTypeConverter: NumberTypeConverter<Double>() {
    override fun lerp    (start: Double, end: Double, progress: Float ) = io.nacular.doodle.utils.lerp(start, end, progress)
    override fun progress(start: Double, end: Double, value   : Double) = ((value - start) / (end - start)).toFloat()
}

internal object LongTypeConverter: NumberTypeConverter<Long>() {
    override fun lerp    (start: Long, end: Long, progress: Float) = io.nacular.doodle.utils.lerp(start.toDouble(), end.toDouble(), progress).roundToLong()
    override fun progress(start: Long, end: Long, value   : Long  ) = ((value - start).toDouble() / (end - start)).toFloat()
}

internal object ShortTypeConverter: NumberTypeConverter<Short>() {
    override fun lerp    (start: Short, end: Short, progress: Float) = io.nacular.doodle.utils.lerp(start.toDouble(), end.toDouble(), progress).roundToInt().toShort()
    override fun progress(start: Short, end: Short, value   : Short  ) = ((value - start).toDouble() / (end - start)).toFloat()
}

internal object ByteTypeConverter: NumberTypeConverter<Byte>() {
    override fun lerp    (start: Byte, end: Byte, progress: Float) = io.nacular.doodle.utils.lerp(start.toDouble(), end.toDouble(), progress).roundToInt().toByte()
    override fun progress(start: Byte, end: Byte, value   : Byte ) = ((value - start).toDouble() / (end - start)).toFloat()
}

internal fun <T: Any> numberTypeConverter(type: KClass<T>): Interpolator<T> = object: Interpolator<T> {
    override fun lerp(start: T, end: T, progress: Float): T = when (type) {
        Int::class    -> io.nacular.doodle.utils.lerp((start as Int   ).toDouble(), (end as Int   ).toDouble(), progress).roundToInt()            as T
        Float::class  -> io.nacular.doodle.utils.lerp((start as Float ).toDouble(), (end as Float ).toDouble(), progress)                         as T
        Double::class -> io.nacular.doodle.utils.lerp((start as Double),            (end as Double),            progress)                         as T
        Long::class   -> io.nacular.doodle.utils.lerp((start as Long  ).toDouble(), (end as Long  ).toDouble(), progress).roundToLong()           as T
        Short::class  -> io.nacular.doodle.utils.lerp((start as Short ).toDouble(), (end as Short ).toDouble(), progress).roundToInt ().toShort() as T
        Byte::class   -> io.nacular.doodle.utils.lerp((start as Byte  ).toDouble(), (end as Byte  ).toDouble(), progress).roundToInt ().toByte () as T
        else -> start
    }

    override fun progress(start: T, end: T, value: T): Float = (((value as Number).toDouble() - (start as Number).toDouble()) / ((end as Number).toDouble() - start.toDouble())).toFloat()
}