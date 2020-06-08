package io.nacular.doodle.controls

import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.PropertyObserversImpl
import io.nacular.doodle.utils.intersect

/**
 * Created by Nicholas Eddy on 2/12/18.
 */

interface ConfinedRangeModel<T: Comparable<T>> {
    var range : ClosedRange<T>
    var limits: ClosedRange<T>

    val atLowerLimit get() = range.start        == limits.start
    val atUpperLimit get() = range.endInclusive == limits.endInclusive

    val rangeChanged : PropertyObservers<ConfinedRangeModel<T>, ClosedRange<T>>
    val limitsChanged: PropertyObservers<ConfinedRangeModel<T>, ClosedRange<T>>
}

interface ConfinedValueModel<T: Comparable<T>> {
    var value : T
    var limits: ClosedRange<T>

    val atLowerLimit get() = value == limits.start
    val atUpperLimit get() = value == limits.endInclusive

    val valueChanged : PropertyObservers<ConfinedValueModel<T>, T>
    val limitsChanged: PropertyObservers<ConfinedValueModel<T>, ClosedRange<T>>
}

@Suppress("PrivatePropertyName")
class BasicConfinedRangeModel<T: Comparable<T>>(limit: ClosedRange<T>, range: ClosedRange<T> = limit): ConfinedRangeModel<T> {
    override var range = range
        set(new) {
            val old = field
            field = minOf(limits.endInclusive, maxOf(new.start, limits.start)) .. maxOf(limits.start, minOf(new.endInclusive, limits.endInclusive))

            if (old != field) {
                rangeChanged_(old, field)
            }
        }

    override var limits = limit
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
class BasicConfinedValueModel<T: Comparable<T>>(limit: ClosedRange<T>, value: T = limit.start): ConfinedValueModel<T> {

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