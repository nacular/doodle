package io.nacular.doodle.controls.spinner

public class IntModel(private val progression: IntProgression): CommonSpinnerModel<Int>() {

    override val hasNext    : Boolean get() = value + progression.step <= progression.last
    override val hasPrevious: Boolean get() = value - progression.step >= progression.first

    override fun next    () { if (hasNext    ) { value += progression.step } }
    override fun previous() { if (hasPrevious) { value -= progression.step } }

    override var value: Int = progression.first
        private set(new) {
            if (new == field) { return }

            field = new

            changed_()
        }
}