package io.nacular.doodle.controls.spinner

import io.nacular.doodle.utils.ObservableList

public open class ListSpinButtonModel<T, out L: List<T>>(protected open val values: L): CommonSpinnerModel<T>() {
    protected var index: Int = 0
        private set(new) {
            if (new == field) { return }

            field = new

            changed_()
        }

    override val hasNext    : Boolean get() = index < values.lastIndex
    override val hasPrevious: Boolean get() = index > 0

    override fun next    () { if (hasNext    ) { ++index } }
    override fun previous() { if (hasPrevious) { --index } }

    override val value: T get() = values[index]
}

public class MutableListSpinButtonModel<T>(values: List<T> = emptyList()): ListSpinButtonModel<T, ObservableList<T>>(ObservableList(values.toMutableList())), MutableSpinButtonModel<T> {
    init {
        super.values.changed += { _,_ ->
            changed_()
        }
    }

    public override val values: ObservableList<T> = super.values

    override var value: T
        get(   ) = super.value
        set(new) {
            if (new == super.value) { return }

            values[index] = new

            changed_()
        }
}