package io.nacular.doodle.controls.spinner

public open class LongSpinButtonModel(private val progression: LongProgression, start: Long = progression.first): CommonSpinnerModel<Long>() {

    override val hasNext    : Boolean get() = value + progression.step <= progression.last
    override val hasPrevious: Boolean get() = value - progression.step >= progression.first

    override fun next    () { if (hasNext    ) { value += progression.step } }
    override fun previous() { if (hasPrevious) { value -= progression.step } }

    override var value: Long = start
        protected set(new) {
            if (new == field) { return }

            field = new

            changed_()
        }
}

public class MutableLongSpinButtonModel(progression: LongProgression, start: Long = progression.first): LongSpinButtonModel(progression, start), MutableSpinButtonModel<Long> {
    override var value: Long get() = super.value; set(new) { super.value = new }
}