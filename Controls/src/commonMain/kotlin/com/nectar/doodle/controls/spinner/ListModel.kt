package com.nectar.doodle.controls.spinner

open class ListModel<T, out L: List<T>>(protected val values: L): AbstractModel<T>() {
    protected var index = 0
        private set(new) {
            if (new == field) { return }

            field = new

            changed_()
        }

    override val hasNext     get() = index < values.lastIndex
    override val hasPrevious get() = index > 0

    override fun next    () { if (hasNext    ) { ++index } }
    override fun previous() { if (hasPrevious) { --index } }

    override val value get() = values[index]
}

class MutableListModel<T>(values: MutableList<T>): ListModel<T, MutableList<T>>(values), MutableModel<T> {
    override var value
        get(   ) = super.value
        set(new) {
            if (new == super.value) { return }

            values[index] = new

            changed_()
        }
}