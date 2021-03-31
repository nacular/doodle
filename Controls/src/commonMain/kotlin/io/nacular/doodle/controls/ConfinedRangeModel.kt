package io.nacular.doodle.controls

import io.nacular.doodle.accessibility.RangeRole
import io.nacular.doodle.utils.PropertyObserver
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.PropertyObserversImpl
import io.nacular.doodle.utils.intersect

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
    override var range: ClosedRange<T> = range
        set(new) {
            val old = field
            field = minOf(limits.endInclusive, maxOf(new.start, limits.start)) .. maxOf(limits.start, minOf(new.endInclusive, limits.endInclusive))

            if (old != field) {
                rangeChanged_(old, field)
            }
        }

    override var limits: ClosedRange<T> = limit
        set(new) {
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

public fun RangeRole.bind(model: ConfinedValueModel<Double>): Binding {
    min = model.limits.start
    max = model.limits.endInclusive
    value = model.value

    return object: Binding {
        val limitsChanged: PropertyObserver<ConfinedValueModel<Double>, ClosedRange<Double>> = { _,_,new ->
            min = new.start
            max = new.endInclusive
        }

        val valueChanged: PropertyObserver<ConfinedValueModel<Double>, Double> = { _,_,new ->
            value = new
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