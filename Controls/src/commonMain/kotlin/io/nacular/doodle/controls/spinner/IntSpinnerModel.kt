package io.nacular.doodle.controls.spinner

public open class IntSpinnerModel(private val progression: IntProgression, start: Int = progression.first): CommonSpinnerModel<Int>() {

    override val hasNext    : Boolean get() = value + progression.step <= progression.last
    override val hasPrevious: Boolean get() = value - progression.step >= progression.first

    override fun next    () { if (hasNext    ) { value += progression.step } }
    override fun previous() { if (hasPrevious) { value -= progression.step } }

    override var value: Int = start
        protected set(new) {
            if (new == field || new < progression.first || new > progression.last) { return }

            field = new

            changed_()
        }
}

public class MutableIntSpinnerModel(progression: IntProgression, start: Int = progression.first): IntSpinnerModel(progression, start), MutableSpinnerModel<Int> {
    override var value: Int get() = super.value; set(new) { super.value = new }
}