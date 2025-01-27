package io.nacular.doodle.controls.spinbutton

public open class IntSpinButtonModel(private val progression: IntProgression, start: Int = progression.first): CommonSpinButtonModel<Int>() {

    override val hasNext    : Boolean get() = value + progression.step <= progression.last
    override val hasPrevious: Boolean get() = value - progression.step >= progression.first

    override fun next    () { if (hasNext    ) { value += progression.step } }
    override fun previous() { if (hasPrevious) { value -= progression.step } }

    override var value: Int = start.coerceIn(progression.first, progression.last); protected set(new) {
        if (new == field || new < progression.first || new > progression.last) { return }

        field = new

        changed_()
    }
}

public class MutableIntSpinButtonModel(progression: IntProgression, start: Int = progression.first): IntSpinButtonModel(progression, start), MutableSpinButtonModel<Int> {
    override var value: Int get() = super.value; set(new) { super.value = new }
}