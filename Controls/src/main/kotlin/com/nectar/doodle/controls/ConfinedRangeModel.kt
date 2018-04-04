package com.nectar.doodle.controls

import com.nectar.doodle.utils.ChangeObservers
import com.nectar.doodle.utils.ChangeObserversImpl
import com.nectar.doodle.utils.intersect

/**
 * Created by Nicholas Eddy on 2/12/18.
 */

interface ConfinedRangeModel<T: Comparable<T>> {
    var range : ClosedRange<T>
    var limits: ClosedRange<T>

    val atLowerLimit get() = range.start        == limits.start
    val atUpperLimit get() = range.endInclusive == limits.endInclusive

    val changed: ChangeObservers<ConfinedRangeModel<T>>
}

interface ConfinedValueModel<T: Comparable<T>> {
    var value : T
    var limits: ClosedRange<T>

    val atLowerLimit get() = value == limits.start
    val atUpperLimit get() = value == limits.endInclusive

    val changed: ChangeObservers<ConfinedValueModel<T>>
}

class BasicConfinedRangeModel<T: Comparable<T>>(limit: ClosedRange<T>, range: ClosedRange<T> = limit): ConfinedRangeModel<T> {
    override var range = range
        set(new) {
            val old = field
            field = minOf(limits.endInclusive, maxOf(new.start, limits.start)) .. maxOf(limits.start, minOf(new.endInclusive, limits.endInclusive))

            if (old != field) {
                changed_()
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
                        changed_()
                    }
                }
            }
        }

    @Suppress("PrivatePropertyName")
    private val changed_ = ChangeObserversImpl(this)
    override val changed: ChangeObservers<ConfinedRangeModel<T>> = changed_
}

class BasicConfinedValueModel<T: Comparable<T>>(limit: ClosedRange<T>, value: T = limit.start): ConfinedValueModel<T> {

    @Suppress("PrivatePropertyName")
    private val changed_ = ChangeObserversImpl(this)
    override val changed: ChangeObservers<ConfinedValueModel<T>> = changed_

    private val delegate = BasicConfinedRangeModel(limit, value .. value).also { it.changed += { changed_() } }

    override var value: T
        get(   ) = delegate.range.start
        set(new) { delegate.range = new .. new }
    override var limits: ClosedRange<T>
        get(   ) = delegate.limits
        set(new) { delegate.limits = new }
}