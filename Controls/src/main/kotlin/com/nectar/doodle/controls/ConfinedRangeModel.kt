package com.nectar.doodle.controls

import com.nectar.doodle.ranges.intersect

/**
 * Created by Nicholas Eddy on 2/12/18.
 */
typealias ChangeObserver<T> = (source: T) -> Unit

// TODO: Move to Utils
interface ChangeObservers<out T> {
    operator fun plusAssign (observer: ChangeObserver<T>)
    operator fun minusAssign(observer: ChangeObserver<T>)
}

class ChangeObserversImpl<T>: ChangeObservers<T> {
    val set = mutableSetOf<ChangeObserver<T>>()

    override fun plusAssign(observer: ChangeObserver<T>) {
        set += observer
    }

    override fun minusAssign(observer: ChangeObserver<T>) {
        set -= observer
    }
}

interface ConfinedRangeModel<T: Comparable<T>> {
    var range : ClosedRange<T>
    var limits: ClosedRange<T>

    val atLowerLimit get() = range.start        == limits.start
    val atUpperLimit get() = range.endInclusive == limits.endInclusive

    val onChanged: ChangeObservers<ConfinedRangeModel<T>>
}

interface ConfinedValueModel<T: Comparable<T>> {
    var value : T
    var limits: ClosedRange<T>

    val atLowerLimit get() = value == limits.start
    val atUpperLimit get() = value == limits.endInclusive

    val onChanged: ChangeObservers<ConfinedValueModel<T>>
}

class BasicConfinedRangeModel<T: Comparable<T>>(limit: ClosedRange<T>, range: ClosedRange<T> = limit): ConfinedRangeModel<T> {
    override var range = range
        set(new) {
            val old = field
            field = new intersect limits

            if (old != field) {
                onChanged_.set.forEach { it(this) }
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
                        onChanged_.set.forEach { it(this) }
                    }
                }
            }
        }

    @Suppress("PrivatePropertyName")
    private val onChanged_ = ChangeObserversImpl<ConfinedRangeModel<T>>()
    override val onChanged: ChangeObservers<ConfinedRangeModel<T>> = onChanged_
}

class BasicConfinedValueModel<T: Comparable<T>>(limit: ClosedRange<T>, value: T = limit.start): ConfinedValueModel<T> {

    @Suppress("PrivatePropertyName")
    private val onChanged_ = ChangeObserversImpl<ConfinedValueModel<T>>()
    override val onChanged: ChangeObservers<ConfinedValueModel<T>> = onChanged_

    private val delegate = BasicConfinedRangeModel(limit, value .. value).also { it.onChanged += { onChanged_.set.forEach { it(this) } } }

    override var value: T
        get(   ) = delegate.range.start
        set(new) { delegate.range = new .. new }
    override var limits: ClosedRange<T>
        get(   ) = delegate.limits
        set(new) { delegate.limits = new }
}